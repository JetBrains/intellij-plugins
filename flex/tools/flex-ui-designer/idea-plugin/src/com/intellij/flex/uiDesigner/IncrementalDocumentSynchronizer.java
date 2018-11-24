package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.ByteArrayOutputStreamEx;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.mxml.MxmlUtil;
import com.intellij.flex.uiDesigner.mxml.PrimitiveWriter;
import com.intellij.flex.uiDesigner.mxml.XmlAttributeValueProvider;
import com.intellij.flex.uiDesigner.mxml.XmlElementValueProvider;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ui.update.Update;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.Nullable;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;

final class IncrementalDocumentSynchronizer extends Update {
  private final PsiTreeChangeEvent event;
  private boolean isSkippedXml;
  private boolean isStyleDataChanged;

  public IncrementalDocumentSynchronizer(PsiTreeChangeEvent event) {
    super("FlashUIDesigner.incrementalUpdate");
    this.event = event;
  }

  @Override
  public boolean canEat(Update update) {
    if (!(update instanceof IncrementalDocumentSynchronizer)) {
      return false;
    }

    PsiTreeChangeEvent otherEvent = ((IncrementalDocumentSynchronizer)update).event;
    if (event.getFile() != otherEvent.getFile()) {
      return false;
    }

    // todo we don't support incremental update for CSS
    if (event.getFile() instanceof StylesheetFile) {
      return true;
    }

    return event.getParent() == otherEvent.getParent() &&
           event.getElement() == otherEvent.getElement();
  }

  @Override
  public void run() {
    DesignerApplicationManager designerManager = DesignerApplicationManager.getInstance();
    if (designerManager.isInitialRendering() || designerManager.isApplicationClosed()) {
      return;
    }

    // PsiTreeChangeEvent dispatched only for root psi file, i.e not for injected
    // (so, if CSS is injected, we get psi event about mxml file, but not about injected css file)
    // WELL, IT IS NOT TRUE!!! YESTERDAY IT ALWAYS WAS XmlFile, but TODAY IT IS CssFile :)
    final XmlFile xmlFile;
    if (event.getFile() instanceof XmlFile) {
      xmlFile = (XmlFile)event.getFile();
    }
    else {
      assert event.getFile() instanceof StylesheetFile;
      styleChanged();
      return;
    }

    DocumentInfo info = DocumentFactoryManager.getInstance().getNullableInfo(xmlFile);
    if (info != null && !incrementalSync(info)) {
      if (isStyleDataChanged) {
        styleChanged();
      }
      else if (!isSkippedXml) {
        initialRender(designerManager, xmlFile);
      }
    }
  }

  private void styleChanged() {
    // BE AWARE!!! INJECTION BEHAVIOR IS NOT PREDICTABLE, file may be injected.
    //noinspection ConstantConditions
    VirtualFile file = event.getFile().getViewProvider().getVirtualFile();
    if (file instanceof VirtualFileWindow) {
      file = ((VirtualFileWindow)file).getDelegate();
    }

    DesignerApplicationManager.getInstance().renderDocumentsAndCheckLocalStyleModification(
      new Document[]{FileDocumentManager.getInstance().getCachedDocument(file)}, true, false);
  }

  @Nullable
  private XmlElementValueProvider findSupportedTarget() {
    PsiElement element = event.getParent();
    // if we change attribute value via line marker, so, event.getParent() will be XmlAttribute instead of XmlAttributeValue
    while (!(element instanceof XmlAttribute)) {
      element = element.getParent();
      if (element instanceof XmlTag) {
        XmlTag tag = (XmlTag)element;
        XmlElementDescriptor descriptor = tag.getDescriptor();
        if (descriptor instanceof ClassBackedElementDescriptor) {
          ClassBackedElementDescriptor classBackedElementDescriptor = (ClassBackedElementDescriptor)descriptor;
          if (classBackedElementDescriptor.isPredefined()) {
            isStyleDataChanged = descriptor.getQualifiedName().equals(FlexPredefinedTagNames.STYLE);
            isSkippedXml = isStyleDataChanged ||
                           (!MxmlUtil.isObjectLanguageTag(tag) &&
                            !descriptor.getQualifiedName().equals(FlexPredefinedTagNames.DECLARATIONS));
          }
        }
        return null;
      }
      else if (element instanceof PsiFile || element == null) {
        return null;
      }
    }

    XmlAttribute attribute = (XmlAttribute)element;
    if (JavaScriptSupportLoader.MXML_URI3.equals(attribute.getNamespace()) || attribute.getValueElement() == null) {
      return null;
    }

    XmlAttributeDescriptor xmlDescriptor = attribute.getDescriptor();
    if (!(xmlDescriptor instanceof AnnotationBackedDescriptor)) {
      return null;
    }

    AnnotationBackedDescriptor descriptor = (AnnotationBackedDescriptor)xmlDescriptor;
    if (descriptor.isPredefined() || MxmlUtil.isIdLanguageAttribute(attribute, descriptor)) {
      return null;
    }

    // todo incremental sync for state-specific attributes
    PsiReference[] references = attribute.getReferences();
    if (references.length > 1) {
      for (int i = references.length - 1; i > -1; i--) {
        PsiReference psiReference = references[i];
        if (psiReference instanceof FlexReferenceContributor.StateReference) {
          return null;
        }
      }
    }
    else {
      String prefix = attribute.getName() + '.';
      for (XmlAttribute anotherAttribute : attribute.getParent().getAttributes()) {
        if (anotherAttribute != attribute && anotherAttribute.getName().startsWith(prefix)) {
          return null;
        }
      }
    }

    XmlAttributeValueProvider valueProvider = new XmlAttributeValueProvider(attribute);
    // skip binding
    PsiLanguageInjectionHost injectedHost = valueProvider.getInjectedHost();
    if (injectedHost != null && InjectedLanguageUtil.hasInjections(injectedHost)) {
      return null;
    }

    return valueProvider;
  }

  public static void initialRender(DesignerApplicationManager designerManager, XmlFile xmlFile) {
    designerManager.renderIfNeed(xmlFile, null);
  }

  private boolean incrementalSync(final DocumentInfo info) {
    final XmlElementValueProvider valueProvider = findSupportedTarget();
    if (valueProvider == null) {
      return false;
    }

    XmlTag tag = (XmlTag)valueProvider.getElement().getParent();
    if (!(tag.getDescriptor() instanceof ClassBackedElementDescriptor)) {
      return false;
    }

    int componentId = info.rangeMarkerIndexOf(tag);
    if (componentId == -1) {
      return false;
    }

    final AnnotationBackedDescriptor descriptor = (AnnotationBackedDescriptor)valueProvider.getPsiMetaData();
    assert descriptor != null;
    final String typeName = descriptor.getTypeName();
    final String type = descriptor.getType();
    if (type == null) {
      return !typeName.equals(FlexAnnotationNames.EFFECT);
    }
    else if (type.equals(JSCommonTypeNames.FUNCTION_CLASS_NAME) || typeName.equals(FlexAnnotationNames.EVENT)) {
      return true;
    }

    final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter();
    //noinspection IOResourceOpenedButNotSafelyClosed
    final PrimitiveAmfOutputStream dataOut = new PrimitiveAmfOutputStream(new ByteArrayOutputStreamEx(16));
    PrimitiveWriter writer = new PrimitiveWriter(dataOut, stringWriter);
    boolean needRollbackStringWriter = true;
    try {
      if (descriptor.isAllowsPercentage()) {
        String value = valueProvider.getTrimmed();
        final boolean hasPercent;
        if (value.isEmpty() || ((hasPercent = value.endsWith("%")) && value.length() == 1)) {
          return true;
        }

        final String name;
        if (hasPercent) {
          name = descriptor.getPercentProxy();
          value = value.substring(0, value.length() - 1);
        }
        else {
          name = descriptor.getName();
        }

        stringWriter.write(name, dataOut);
        dataOut.writeAmfDouble(value);
      }
      else {
        stringWriter.write(descriptor.getName(), dataOut);
        if (!writer.writeIfApplicable(valueProvider, dataOut, descriptor)) {
          needRollbackStringWriter = false;
          stringWriter.rollback();
          return false;
        }
      }

      needRollbackStringWriter = false;
    }
    catch (InvalidPropertyException ignored) {
      return true;
    }
    catch (NumberFormatException ignored) {
      return true;
    }
    finally {
      if (needRollbackStringWriter) {
        stringWriter.rollback();
      }
    }

    Client.getInstance().updatePropertyOrStyle(info.getId(), componentId, stream -> {
      stringWriter.writeTo(stream);
      stream.write(descriptor.isStyle());
      dataOut.writeTo(stream);
    }).doWhenDone(() -> DesignerApplicationManager.createDocumentRenderedNotificationDoneHandler(true).consume(info));

    return true;
  }

}