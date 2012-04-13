package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.ByteArrayOutputStreamEx;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.mxml.MxmlUtil;
import com.intellij.flex.uiDesigner.mxml.PrimitiveWriter;
import com.intellij.flex.uiDesigner.mxml.XmlAttributeValueProvider;
import com.intellij.flex.uiDesigner.mxml.XmlElementValueProvider;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.Nullable;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;

final class IncrementalDocumentSynchronizer implements Runnable {
  private final PsiTreeChangeEvent event;

  public IncrementalDocumentSynchronizer(PsiTreeChangeEvent event) {
    this.event = event;
  }

  @Nullable
  private XmlElementValueProvider findSupportedTarget() {
    if (DesignerApplicationManager.getInstance().isApplicationClosed()) {
      return null;
    }

    PsiElement element = event.getParent();
    // if we change attribute value via line marker, so, event.getParent() will be XmlAttribute instead of XmlAttributeValue
    while (!(element instanceof XmlAttribute)) {
      element = element.getParent();
      if (element instanceof XmlTag || element instanceof PsiFile || element == null) {
        return null;
      }
    }

    XmlAttribute attribute = (XmlAttribute)element;
    if (JavaScriptSupportLoader.MXML_URI3.equals(attribute.getNamespace())) {
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

  @Override
  public void run() {
    DesignerApplicationManager designerManager = DesignerApplicationManager.getInstance();
    if (designerManager.isInitialRendering()) {
      return;
    }

    XmlFile psiFile = (XmlFile)event.getFile();
    assert psiFile != null;
    DocumentInfo info = DocumentFactoryManager.getInstance().getInfo(psiFile);
    if (!incrementalSync(info)) {
      designerManager.runWhenRendered(psiFile, new AsyncResult.Handler<DocumentInfo>() {
        @Override
        public void run(DocumentInfo documentInfo) {
          notifyUpdated(documentInfo);
        }
      });
    }
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
    catch (InvalidPropertyException e) {
      return true;
    }
    catch (NumberFormatException e) {
      return true;
    }
    finally {
      if (needRollbackStringWriter) {
        stringWriter.rollback();
      }
    }

    Client.getInstance().updatePropertyOrStyle(info.getId(), componentId, new Consumer<AmfOutputStream>() {
      @Override
      public void consume(AmfOutputStream stream) {
        stringWriter.writeTo(stream);
        stream.write(descriptor.isStyle());
        dataOut.writeTo(stream);
      }
    }).doWhenDone(new Runnable() {
      @Override
      public void run() {
        Document document = FileDocumentManager.getInstance().getCachedDocument(info.getElement());
        if (document != null) {
          info.documentModificationStamp = document.getModificationStamp();
          notifyUpdated(info);
        }
      }
    });

    return true;
  }

  private static void notifyUpdated(DocumentInfo finalInfo) {
    ApplicationManager.getApplication().getMessageBus().syncPublisher(DesignerApplicationManager.MESSAGE_TOPIC)
      .documentIncrementallyUpdated(finalInfo);
  }
}