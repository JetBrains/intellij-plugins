package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InjectionUtil;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.ObjectIntHashMap;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.css.FlexCssPropertyDescriptor;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.flex.uiDesigner.mxml.PropertyProcessor.PropertyKind.*;

class PropertyProcessor implements ValueWriter {
  enum PropertyKind {
    ARRAY, VECTOR, COMPLEX, COMPLEX_STYLE, PRIMITIVE, PRIMITIVE_STYLE, IGNORE;

    boolean isComplex() {
      return ordinal() < PRIMITIVE.ordinal();
    }

    boolean isList() {
      return ordinal() < COMPLEX.ordinal();
    }
  }

  private final InjectedASWriter injectedASWriter;
  private final MxmlWriter mxmlWriter;
  private final BaseWriter writer;

  private String name;
  private boolean isEffect;
  private boolean isStyle;

  private final ObjectIntHashMap<String> classFactoryMap = new ObjectIntHashMap<String>();
  private final List<XmlFile> unregisteredDocumentFactories = new ArrayList<XmlFile>();

  PropertyProcessor(InjectedASWriter injectedASWriter, BaseWriter writer, MxmlWriter mxmlWriter) {
    this.injectedASWriter = injectedASWriter;
    this.writer = writer;
    this.mxmlWriter = mxmlWriter;
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

  @Nullable
  public ValueWriter process(XmlElement element, XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor,
                             Context context) throws InvalidPropertyException {
    if (descriptor.isPredefined()) {
      MxmlWriter.LOG.error("unknown language element " + descriptor.getName());
      return null;
    }

    name = descriptor.getName();

    isStyle = descriptor.isStyle();
    isEffect = false;
    final @Nullable String type = descriptor.getType();
    final String typeName = descriptor.getTypeName();
    if (type == null) {
      if (typeName.equals(FlexAnnotationNames.EFFECT)) {
        isStyle = true;
        isEffect = true;
      }
      else {
        if (!typeName.equals(FlexAnnotationNames.BINDABLE)) { // skip binding
          MxmlWriter.LOG.error("unsupported element: " + element.getText());
        }
        return null;
      }
    }
    else if (typeName.equals(FlexAnnotationNames.EVENT) /* skip event handlers */) {
      return null;
    }

    ValueWriter valueWriter = processInjected(valueProvider, descriptor, isStyle, context);
    if (valueWriter != null) {
      return valueWriter == InjectedASWriter.IGNORE ? null : valueWriter;
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

    return this;
  }

  public ValueWriter processXmlTextAsDefaultPropertyWithComplexType(XmlElementValueProvider valueProvider, XmlTag parent, Context context)
    throws InvalidPropertyException {
    @SuppressWarnings("ConstantConditions")
    final AnnotationBackedDescriptor defaultPropertyDescriptor = ((ClassBackedElementDescriptor)parent.getDescriptor())
      .getDefaultPropertyDescriptor();
    assert defaultPropertyDescriptor != null;

    return processInjected(valueProvider, defaultPropertyDescriptor, defaultPropertyDescriptor.isStyle(), context);
  }

  public ValueWriter processInjected(XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor, boolean isStyle, Context context)
    throws InvalidPropertyException {
    ValueWriter valueWriter = injectedASWriter.processProperty(valueProvider, descriptor.getName(), descriptor.getType(), isStyle, context);
    if (valueWriter instanceof ClassValueWriter && isSkinClass(descriptor)) {
      SkinProjectClassValueWriter skinProjectClassValueWriter = getSkinProjectClassValueWriter(
        getProjectComponentFactoryId(((ClassValueWriter)valueWriter).getJsClas()));
      if (skinProjectClassValueWriter != null) {
        return skinProjectClassValueWriter;
      }
    }

    return valueWriter;
  }

  private boolean isSkinClass(AnnotationBackedDescriptor descriptor) {
    return isStyle && name.equals("skinClass") && AsCommonTypeNames.CLASS.equals(descriptor.getType());
  }

  @Nullable
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
    JSClass jsClass = valueProvider.getJsClass();
    return jsClass != null ? getProjectComponentFactoryId(jsClass) : -1;
  }

  private int getProjectComponentFactoryId(JSClass jsClass) throws InvalidPropertyException {
    return InjectionUtil.getProjectComponentFactoryId(jsClass, unregisteredDocumentFactories);
  }

  public void reset() {
    classFactoryMap.clear();
    if (unregisteredDocumentFactories != null && !unregisteredDocumentFactories.isEmpty()) {
      unregisteredDocumentFactories.clear();
    }
    isEffect = false;
  }

  boolean writeIfPrimitive(XmlElementValueProvider valueProvider, String type, PrimitiveAmfOutputStream out,
                           BaseWriter writer, @Nullable AnnotationBackedDescriptor descriptor) throws InvalidPropertyException {
    if (type.equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
      writeString(valueProvider, descriptor);
    }
    else if (type.equals(JSCommonTypeNames.NUMBER_CLASS_NAME)) {
      final String trimmed = valueProvider.getTrimmed();
      if (StringUtil.isEmpty(trimmed)) {
        throw new InvalidPropertyException(valueProvider.getElement(), "invalid.number.value");
      }
      out.writeAmfDouble(trimmed);
    }
    else if (type.equals(JSCommonTypeNames.BOOLEAN_CLASS_NAME)) {
      out.writeAmfBoolean(valueProvider.getTrimmed());
    }
    else if (type.equals(JSCommonTypeNames.INT_TYPE_NAME) || type.equals(JSCommonTypeNames.UINT_TYPE_NAME)) {
      final String trimmed = valueProvider.getTrimmed();
      if (trimmed.isEmpty()) {
        throw new InvalidPropertyException(valueProvider.getElement(), "invalid.integer.value");
      }

      if (descriptor != null && FlexCssPropertyDescriptor.COLOR_FORMAT.equals(descriptor.getFormat())) {
        writer.writeColor(valueProvider.getElement(), trimmed, isStyle);
      }
      else {
        out.writeAmfInt(trimmed);
      }
    }
    else if (type.equals(AsCommonTypeNames.CLASS)) {
      processClass(valueProvider);
    }
    else {
      return false;
    }

    return true;
  }

  // see ClassProperty test
  private boolean processClass(XmlElementValueProvider valueProvider) throws InvalidPropertyException {
    // IDEA-73537, cannot use valueProvider.getJsClass()
    String trimmed = valueProvider.getTrimmed();
    XmlElement exceptionElement = valueProvider.getElement();
    if (trimmed.isEmpty() && valueProvider.getElement() instanceof XmlTag) {
      // case 1, fx:Class
      final XmlTag propertyTag = (XmlTag)valueProvider.getElement();
      final XmlTag[] propertyTagSubTags = propertyTag.getSubTags();
      if (propertyTagSubTags.length == 1) {
        final XmlTag contentTag = propertyTagSubTags[0];
        exceptionElement = contentTag;
        final XmlElementDescriptor contentTagDescriptor = contentTag.getDescriptor();
        if (contentTagDescriptor instanceof ClassBackedElementDescriptor &&
            AsCommonTypeNames.CLASS.equals(contentTagDescriptor.getQualifiedName())) {
          trimmed = contentTag.getValue().getTrimmedText();
        }
      }
    }

    if (trimmed.isEmpty()) {
      throw new InvalidPropertyException(exceptionElement, "invalid.class.value");
    }
    else {
      final Module module = ModuleUtil.findModuleForPsiElement(valueProvider.getElement());
      if (module != null &&
          JSResolveUtil.findClassByQName(trimmed, module.getModuleWithDependenciesAndLibrariesScope(false)) != null) {
        writer.writeClass(trimmed);
        return true;
      }

      throw new InvalidPropertyException(exceptionElement, "error.unresolved.class", trimmed);
    }
  }

  @Override
  public PropertyKind write(AnnotationBackedDescriptor descriptor, XmlElementValueProvider valueProvider, PrimitiveAmfOutputStream out,
                            BaseWriter writer, boolean isStyle) throws InvalidPropertyException {
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
    else if (isEffect()) {
      out.write(Amf3Types.OBJECT);
      return COMPLEX;
    }

    if (writeIfPrimitive(valueProvider, type, out, writer, descriptor)) {

    }
    else if (type.equals(JSCommonTypeNames.ARRAY_CLASS_NAME)) {
      if (!descriptor.isRichTextContent() && valueProvider instanceof XmlAttributeValueProvider &&
          isInlineArray(valueProvider.getTrimmed())) {
        writeInlineArray(valueProvider);
      }
      else {
        out.write(Amf3Types.ARRAY);
        return ARRAY;
      }
    }
    else if (descriptor.getArrayType() != null) {
      // test for mxml vector
      // Why we don't it for array?
      //<mx:selectedIndices>
      //        <fx:Array>
      //          <fx:int>1</fx:int>
      //          <fx:int>2</fx:int>
      //        </fx:Array>y
      //      </mx:selectedIndices>
      //
      // will be compiled fine as [[1, 2], "y\n   "]
      //
      // but vector
      //<fx:Vector type="int" fixed="true">
      //          <fx:int>1</fx:int>
      //          <fx:int>2</fx:int>
      //        </fx:Vector>y
      // will not be compiled
      // [untitled] In initializer for 'selectedIndices', type __AS3__.vec.Vector.<int> is not assignable to target __AS3__.vec.Vector.<int> element type int.
      // [untitled] In initializer for 'selectedIndices', type Object is not assignable to target __AS3__.vec.Vector.<int> element type int.
      // so, we need check only for mxml vector

      if (valueProvider.getElement() instanceof XmlTag) {
        final XmlTag propertyTag = (XmlTag)valueProvider.getElement();
        final XmlTag[] subTags = propertyTag.getSubTags();
        if (subTags.length == 1) {
          final XmlTag contentTag = subTags[0];
          final XmlElementDescriptor contentTagDescriptor = contentTag.getDescriptor();
          if (contentTagDescriptor instanceof ClassBackedElementDescriptor &&
              CodeContext.AS3_VEC_VECTOR_QUALIFIED_NAME.equals(contentTagDescriptor.getQualifiedName())) {
            if (!mxmlWriter.processMxmlVector(contentTag, null)) {
              throw new InvalidPropertyException(contentTag, "invalid.vector.value");
            }
            
            return isStyle ? PRIMITIVE_STYLE : PRIMITIVE;
          }
        }
      }

      writer.writeVectorHeader(descriptor.getArrayType());
      return VECTOR;
    }
    else if (type.equals(JSCommonTypeNames.OBJECT_CLASS_NAME) || type.equals(JSCommonTypeNames.ANY_TYPE)) {
      writeUntypedPropertyValue(valueProvider, descriptor);
    }
    else if (type.equals(FlexCommonTypeNames.IFACTORY)) {
      writeClassFactory(valueProvider);
    }
    else {
      out.write(Amf3Types.OBJECT);
      return isStyle ? COMPLEX_STYLE : COMPLEX;
    }

    return isStyle ? PRIMITIVE_STYLE : PRIMITIVE;
  }

  // IDEA-64721, IDEA-72814, see flex2.compiler.mxml.lang.TextParser
  private static boolean isInlineArray(String text) {
    return text.length() >= 2 && text.charAt(0) == '[' && text.charAt(text.length() - 1) == ']';
  }

  private void writeString(XmlElementValueProvider valueProvider, @Nullable AnnotationBackedDescriptor descriptor) {
    if (descriptor != null && descriptor.isEnumerated()) {
      writer.writeStringReference(valueProvider.getTrimmed());
    }
    else {
      CharSequence v = writeIfEmpty(valueProvider);
      if (v != null) {
        writer.writeString(v);
      }
    }
  }

  @Nullable
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

  private void writeInlineArray(XmlElementValueProvider valueProvider) {
    final PrimitiveAmfOutputStream out = writer.getOut();
    out.write(Amf3Types.ARRAY);
    final int lengthPosition = out.allocateShort();
    int validChildrenCount = 0;
    final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    final String value = valueProvider.getTrimmed();
    try {
      char quoteChar = '\'';
      boolean inQuotes = false;
      for (int index = 1, length = value.length(); index < length; index++) {
        char c = value.charAt(index);
        switch (c) {
          case '[':
            if (inQuotes) {
              builder.append(c);
            }
            break;
          case '"':
          case '\'':
            if (inQuotes) {
              if (quoteChar == c) {
                inQuotes = false;
              }
              else {
                builder.append(c);
              }
            }
            else {
              inQuotes = true;
              quoteChar = c;
            }
            break;
          case ',':
          case ']':
            if (inQuotes) {
              builder.append(c);
            }
            else {
              int beginIndex = 0;
              int endIndex = builder.length();
              while (beginIndex < endIndex && builder.charAt(beginIndex) <= ' ') {
                beginIndex++;
              }
              while (beginIndex < endIndex && builder.charAt(endIndex - 1) <= ' ') {
                endIndex--;
              }

              if (endIndex == 0) {
                writer.writeStringReference(XmlElementValueProvider.EMPTY);
              }
              else {
                out.write(Amf3Types.STRING);
                out.writeAmfUtf(builder, false, beginIndex, endIndex);
              }

              validChildrenCount++;
              builder.setLength(0);
            }
            break;
          default:
            builder.append(c);
        }
      }
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }

    out.putShort(validChildrenCount, lengthPosition);
  }

  private void writeUntypedPropertyValue(XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor) {
    if (descriptor.isRichTextContent()) {
      writeString(valueProvider, descriptor);
      return;
    }

    // todo support not only attribute, but tag too
    if (valueProvider instanceof XmlAttributeValueProvider && isInlineArray(valueProvider.getTrimmed())) {
      writeInlineArray(valueProvider);
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
        throw new InvalidPropertyException(tag, "error.inline.component.are.not.supported");
      }
    }

    String className = valueProvider.getTrimmed();
    int reference = classFactoryMap.get(className);
    if (reference != -1) {
      writer.writeObjectReference(reference);
      return;
    }

    JSClass jsClass = valueProvider.getJsClass();
    if (jsClass == null) {
      throw new InvalidPropertyException(valueProvider.getElement(), "error.unresolved.class", valueProvider.getTrimmed());
    }

    final int projectComponentFactoryId = getProjectComponentFactoryId(jsClass);
    if (projectComponentFactoryId == -1) {
      reference = writer.getRootScope().referenceCounter++;
      classFactoryMap.put(className, reference);

      writer.writeConstructorHeader(FlexCommonTypeNames.CLASS_FACTORY, reference);
      writer.writeClass(className);
    }
    else {
      writer.writeDocumentFactoryReference(projectComponentFactoryId);
    }
  }
}