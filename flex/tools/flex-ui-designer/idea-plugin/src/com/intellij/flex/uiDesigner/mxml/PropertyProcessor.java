package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.DocumentFactoryManager;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.ObjectIntHashMap;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.css.FlexCssPropertyDescriptor;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class PropertyProcessor {
  private static final Logger LOG = Logger.getInstance(PropertyProcessor.class.getName());

  static final int ARRAY = -1;
  private static final int COMPLEX = 0;
  private static final int COMPLEX_STYLE = 1;
  static final int PRIMITIVE = 2;
  static final int PRIMITIVE_STYLE = 3;
  static final int IGNORE = 4;

  private final InjectedASWriter injectedASWriter;
  private final BaseWriter writer;

  private String name;
  private boolean isEffect;
  private boolean isStyle;

  private final ObjectIntHashMap<String> classFactoryMap = new ObjectIntHashMap<String>();
  private final List<XmlFile> unregisteredDocumentFactories = new ArrayList<XmlFile>();

  PropertyProcessor(InjectedASWriter injectedASWriter, BaseWriter writer) {
    this.injectedASWriter = injectedASWriter;
    this.writer = writer;
  }

  public List<XmlFile> getUnregisteredDocumentFactories() {
    return unregisteredDocumentFactories;
  }

  public String getName() {
    return name;
  }

  public boolean isStyle() {
    return isStyle;
  }

  public boolean isEffect() {
    return isEffect;
  }

  private ValueWriter processPercentable(XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor) {
    String value = valueProvider.getTrimmed();
    if (value.endsWith("%")) {
      name = descriptor.getPercentProxy();
      value = value.substring(0, value.length() - 1);
    }
    else {
      name = descriptor.getName();
    }

    return new PercentableValueWriter(value);
  }

  public ValueWriter process(XmlElement element, XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor, 
                             Context context) throws InvalidPropertyException {
    if (descriptor.isPredefined()) {
      LOG.error("unknown language element " + descriptor.getName());
      return null;
    }

    name = descriptor.getName();

    isStyle = descriptor.isStyle();
    final @Nullable String type = descriptor.getType();
    final String typeName = descriptor.getTypeName();
    if (type == null) {
      if (typeName.equals(FlexAnnotationNames.EFFECT)) {
        isStyle = true;
        isEffect = true;
      }
      else {
        if (!typeName.equals(FlexAnnotationNames.BINDABLE)) { // skip binding
          LOG.error("unsupported element: " + element.getText());
        }
        return null;
      }
    }
    else if (typeName.equals(FlexAnnotationNames.EVENT) /* skip event handlers */) {
      return null;
    }

    ValueWriter valueWriter = injectedASWriter.processProperty(valueProvider, name, type, isStyle, context);
    if (valueWriter == InjectedASWriter.IGNORE) {
      return null;
    }
    else if (valueWriter != null) {
      if (valueWriter instanceof ClassValueWriter && isSkinClass(descriptor)) {
        SkinProjectClassValueWriter skinProjectClassValueWriter = getSkinProjectClassValueWriter(getSkinProjectClassDocumentFactoryId(
          ((ClassValueWriter)valueWriter).getJsClas(), valueProvider));
        if (skinProjectClassValueWriter != null) {
          return skinProjectClassValueWriter;
        }
      }
      
      return valueWriter;
    }
    else if (descriptor.isAllowsPercentage()) {
      return processPercentable(valueProvider, descriptor);
    }
    else if (isSkinClass(descriptor)) {
      valueWriter = getSkinProjectClassValueWriter(getSkinProjectClassDocumentFactoryId(valueProvider));
      if (valueWriter != null) {
        return valueWriter;
      }
    }

    if (AsCommonTypeNames.CLASS.equals(type)) {
      return new ClassStringValueWriter(valueProvider.getTrimmed());
    }
    else {
      return new ValueWriterImpl(valueProvider, descriptor);
    }
  }

  private boolean isSkinClass(AnnotationBackedDescriptor descriptor) {
    return isStyle && name.equals("skinClass") && AsCommonTypeNames.CLASS.equals(descriptor.getType());
  }
  
  private SkinProjectClassValueWriter getSkinProjectClassValueWriter(int skinProjectClassDocumentFactoryId) {
    if (skinProjectClassDocumentFactoryId != -1) {
      name = "skinFactory";
      return new SkinProjectClassValueWriter(skinProjectClassDocumentFactoryId);
    }
    else {
      return null;
    }
  }

  private int getSkinProjectClassDocumentFactoryId(XmlElementValueProvider valueProvider) throws InvalidPropertyException {
    JSClass jsClass = getJsClass(valueProvider);
    return jsClass != null ? getSkinProjectClassDocumentFactoryId(jsClass, valueProvider) : -1;
  }

  @Nullable
  private static JSClass getJsClass(XmlElementValueProvider valueProvider) {
    XmlElement injectedHost = valueProvider.getInjectedHost();
    if (injectedHost != null) {
      PsiReference[] references = injectedHost.getReferences();
      if (references.length > 0) {
        PsiElement element = references[references.length - 1].resolve();
        if (element instanceof JSClass) {
          return (JSClass)element;
        }
      }
    }

    return null;
  }
  
  private int getSkinProjectClassDocumentFactoryId(JSClass jsClass, XmlElementValueProvider valueProvider) throws InvalidPropertyException {
    PsiFile psiFile = jsClass.getContainingFile();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    assert virtualFile != null;
    boolean inSourceContent = ProjectRootManager.getInstance(psiFile.getProject()).getFileIndex().isInSourceContent(virtualFile);
    if (psiFile instanceof XmlFile) {
      if (inSourceContent) {
        return DocumentFactoryManager.getInstance(psiFile.getProject()).getId(virtualFile, (XmlFile)psiFile, unregisteredDocumentFactories);
      }
    }
    else if (inSourceContent) {
      throw new InvalidPropertyException("error.support.only.mxml.based.component", valueProvider.getTrimmed());
    }
    
    return -1;
  }

  public void reset() {
    classFactoryMap.clear();
    if (unregisteredDocumentFactories != null && !unregisteredDocumentFactories.isEmpty()) {
      unregisteredDocumentFactories.clear();
    }
    isEffect = false;
  }

  private class ValueWriterImpl implements ValueWriter {
    private final XmlElementValueProvider valueProvider;
    private final AnnotationBackedDescriptor descriptor;

    public ValueWriterImpl(XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor) {
      this.valueProvider = valueProvider;
      this.descriptor = descriptor;
    }

    @Override
    public int write(PrimitiveAmfOutputStream out, BaseWriter writer, boolean isStyle) throws InvalidPropertyException {
      final String type = descriptor.getType();
      if (isStyle) {
        int flags = 0;
        if (isEffect()) {
          flags |= StyleFlags.EFFECT;
          out.write(flags);
          out.write(Amf3Types.OBJECT);
          return COMPLEX_STYLE;
        }
        else {
          out.write(flags);
        }
      }

      if (type.equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
        writeString(valueProvider, descriptor);
      }
      else if (type.equals(JSCommonTypeNames.NUMBER_CLASS_NAME)) {
        out.writeAmfDouble(valueProvider.getTrimmed());
      }
      else if (type.equals(JSCommonTypeNames.BOOLEAN_CLASS_NAME)) {
        out.writeAmfBoolean(valueProvider.getTrimmed());
      }
      else if (type.equals(AsCommonTypeNames.INT) || type.equals(AsCommonTypeNames.UINT)) {
        String format = descriptor.getFormat();
        if (format != null && format.equals(FlexCssPropertyDescriptor.COLOR_FORMAT)) {
          writer.writeColor(valueProvider.getTrimmed(), isStyle);
        }
        else {
          out.writeAmfInt(valueProvider.getTrimmed());
        }
      }
      else if (type.equals(JSCommonTypeNames.ARRAY_CLASS_NAME)) {
        out.write(Amf3Types.ARRAY);
        return ARRAY;
      }
      else if (type.equals(JSCommonTypeNames.OBJECT_CLASS_NAME) || type.equals(JSCommonTypeNames.ANY_TYPE)) {
        writeUntypedPropertyValue(valueProvider, descriptor);
      }
      else if (type.equals(FlexClassNames.IFACTORY)) {
        writeClassFactory(valueProvider);
      }
      else {
        out.write(Amf3Types.OBJECT);
        return isStyle ? COMPLEX_STYLE : COMPLEX;
      }

      return isStyle ? PRIMITIVE_STYLE : PRIMITIVE;
    }

    private void writeString(XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor) {
      if (descriptor.isEnumerated()) {
        writer.writeStringReference(valueProvider.getTrimmed());
      }
      else {
        CharSequence v = writeIfEmpty(valueProvider);
        if (v != null) {
          writer.writeString(v);
        }
      }
    }

    private CharSequence writeIfEmpty(XmlElementValueProvider valueProvider) {
      CharSequence v = valueProvider.getSubstituted();
      if (v == XmlElementValueProvider.EMPTY) {
        writer.writeStringReference(XmlElementValueProvider.EMPTY);
        return null;
      }
      else {
        return v;
      }
    }

    private void writeUntypedPropertyValue(XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor) {
      if (descriptor.isRichTextContent()) {
        writeString(valueProvider, descriptor);
        return;
      }

      final CharSequence charSequence = writeIfEmpty(valueProvider);
      if (charSequence == null) {
        return;
      }

      final String value = charSequence.toString();
      try {
        writer.getOut().writeAmfInt(Integer.parseInt(value));
      }
      catch (NumberFormatException e) {
        try {
          writer.getOut().writeAmfDouble(Double.parseDouble(value));
        }
        catch (NumberFormatException ignored) {
          writer.writeString(charSequence);
        }
      }
    }

    private void writeClassFactory(XmlElementValueProvider valueProvider) throws InvalidPropertyException {
      if (valueProvider instanceof XmlTagValueProvider) {
        XmlTag tag = ((XmlTagValueProvider)valueProvider).getTag();
        XmlTag[] subTags = tag.getSubTags();
        if (subTags.length > 0) {
          throw new InvalidPropertyException("error.inner.component.are.not.supported");
        }
      }

      JSClass jsClass = getJsClass(valueProvider);
      if (jsClass == null) {
        throw new InvalidPropertyException("error.unresolved.class", valueProvider.getTrimmed());
      }
      
      String className = jsClass.getQualifiedName();
      int reference = classFactoryMap.get(className);
      if (reference == -1) {
        reference = writer.getRootScope().referenceCounter++;
        classFactoryMap.put(className, reference);

        writer.writeConstructorHeader(FlexClassNames.CLASS_FACTORY, reference);
        writer.writeClass(className);
      }
      else {
        writer.writeObjectReference(reference);
      }
    }
  }
}