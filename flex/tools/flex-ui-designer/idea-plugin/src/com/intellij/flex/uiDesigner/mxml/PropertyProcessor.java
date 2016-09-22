package com.intellij.flex.uiDesigner.mxml;

import com.google.common.base.CharMatcher;
import com.intellij.flex.uiDesigner.EmbedSwfManager;
import com.intellij.flex.uiDesigner.InjectionUtil;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.xml.*;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.containers.ObjectIntHashMap;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.flex.uiDesigner.mxml.MxmlWriter.LOG;
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

  private final ObjectIntHashMap<String> classFactoryMap = new ObjectIntHashMap<>();

  PropertyProcessor(InjectedASWriter injectedASWriter, BaseWriter writer, MxmlWriter mxmlWriter) {
    this.injectedASWriter = injectedASWriter;
    this.writer = writer;
    this.mxmlWriter = mxmlWriter;
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

  private ValueWriter processPercentable(XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor)
    throws InvalidPropertyException {
    String value = valueProvider.getTrimmed();
    final boolean hasPercent;
    if (value.isEmpty() || ((hasPercent = value.endsWith("%")) && value.length() == 1)) {
      throw new InvalidPropertyException(valueProvider.getElement(), "invalid.numeric.value");
    }

    if (hasPercent) {
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
                             @NotNull MxmlObjectReferenceProvider objectReferenceProvider) throws InvalidPropertyException {
    if (descriptor.isPredefined()) {
      LOG.error("unknown language element " + descriptor.getName());
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
        // skip binding and event AS-423
        if (!typeName.equals(FlexAnnotationNames.BINDABLE) && !typeName.equals(FlexAnnotationNames.EVENT)) {
          LOG.error("unsupported element: " + element.getText());
        }
        return null;
      }
    }
    else if (type.equals(JSCommonTypeNames.FUNCTION_CLASS_NAME)) {
      if (name.equals("itemRendererFunction")) {
        // AS-135
        if (MxmlUtil.isPropertyOfSparkDataGroup(descriptor)) {
          name = "itemRenderer";
          return new ValueWriter() {
            @Override
            public PropertyKind write(AnnotationBackedDescriptor descriptor,
                                      XmlElementValueProvider valueProvider,
                                      PrimitiveAmfOutputStream out,
                                      BaseWriter writer,
                                      boolean isStyle,
                                      Context parentContext) {
              writeNonProjectClassFactory(MxmlUtil.UNKNOWN_ITEM_RENDERER_CLASS_NAME);
              return PRIMITIVE;
            }
          };
        }
      }
      // skip functions, IDEA-74041
    }
    else if (typeName.equals(FlexAnnotationNames.EVENT)) {
      // skip event handlers
      return null;
    }

    ValueWriter valueWriter = processInjected(valueProvider, descriptor, isStyle, objectReferenceProvider);
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
    ClassBackedElementDescriptor classBackedElementDescriptor = (ClassBackedElementDescriptor)parent.getDescriptor();
    LOG.assertTrue(classBackedElementDescriptor != null);
    AnnotationBackedDescriptor defaultPropertyDescriptor = classBackedElementDescriptor.getDefaultPropertyDescriptor();
    LOG.assertTrue(defaultPropertyDescriptor != null);
    return processInjected(valueProvider, defaultPropertyDescriptor, defaultPropertyDescriptor.isStyle(), context);
  }

  public ValueWriter processInjected(XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor, boolean isStyle, @NotNull MxmlObjectReferenceProvider mxmlObjectReferenceProvider)
    throws InvalidPropertyException {
    ValueWriter valueWriter = injectedASWriter.processProperty(valueProvider, descriptor.getName(), descriptor.getType(), isStyle, mxmlObjectReferenceProvider);
    if (valueWriter instanceof ClassValueWriter && isSkinClass(descriptor)) {
      SkinProjectClassValueWriter skinProjectClassValueWriter = getSkinProjectClassValueWriter(
        getProjectComponentFactoryId(((ClassValueWriter)valueWriter).getJsClass()));
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
    return InjectionUtil.getProjectComponentFactoryId(jsClass, mxmlWriter.projectComponentReferenceCounter);
  }

  boolean processFxModel(XmlTag tag) {
    final MxmlObjectReference objectReference = processFxDeclarationId(tag, null, false);
    if (objectReference == null) {
      return false;
    }

    writer.referableHeader(objectReference.id);

    final XmlTag[] subTags = tag.getSubTags();
    if (subTags.length == 1) {
      final ModelObjectReferenceProvider modelObjectReference = new ModelObjectReferenceProvider(writer);
      modelObjectReference.reference = objectReference;
      processFxModelTagChildren(subTags[0], modelObjectReference);
    }
    else {
      // as object without any properties
      writer.objectHeader("mx.utils.ObjectProxy");
      writer.endObject();

      if (subTags.length > 1) {
        LOG.warn("Skip model, only one root tag is allowed: " + tag.getText());
      }
    }

    return true;
  }

  @Nullable
  private MxmlObjectReference processFxDeclarationId(XmlTag tag, @Nullable String classNameTrick, boolean allowNullableIdOrClassName) {
    String id = getAttributeValue(tag, "id");
    if (id == null) {
      if (classNameTrick == null) {
        if (!allowNullableIdOrClassName) {
          LOG.warn("Skip fx:model/fx:component, id is not specified or empty: " + tag.getText());
        }
        return null;
      }
    }

    // parentContext for fx:Model/fx:Component always null, because located inside fx:Declarations (i.e. parentContext always is top level)
    // state specific is not allowed for fx:Model/fx:Component (flex compiler doesn't support it)
    final MxmlObjectReference objectReference = new MxmlObjectReference(writer.allocateAbsoluteStaticObjectId());
    if (id != null) {
      injectedASWriter.putMxmlObjectReference(id, objectReference);
    }
    if (classNameTrick != null) {
      injectedASWriter.putMxmlObjectReference('$' + classNameTrick, objectReference);
    }
    return objectReference;
  }

  static String getAttributeValue(XmlTag tag, String attributeName) {
    final XmlAttribute attribute = tag.getAttribute(attributeName);
    return attribute == null ? null : StringUtil.nullize(attribute.getDisplayValue());
  }

  boolean processFxComponent(XmlTag tag, boolean allowNullableIdOrClassName) {
    String className = getAttributeValue(tag, "className");
    if (className != null && writeFxComponentReferenceIfProcessed(className)) {
      return true;
    }

    final MxmlObjectReference objectReference = processFxDeclarationId(tag, className, allowNullableIdOrClassName);
    if (objectReference == null && !allowNullableIdOrClassName) {
      return false;
    }

    final int objectReferenceId = objectReference == null ? writer.allocateAbsoluteStaticObjectId() : objectReference.id;
    final int sizePosition = writer.componentFactory(objectReferenceId);
    final XmlTag[] subTags = tag.getSubTags();
    final int objectTableSize;
    final PrimitiveAmfOutputStream out = writer.getOut();
    if (subTags.length == 1) {
      out.write(Amf3Types.OBJECT);

      Context context = writer.createInnerComponentContext(objectReference, objectReferenceId);
      mxmlWriter.processPropertyTagValue(null, tag, context, COMPLEX);
      objectTableSize = context.getScope().referenceCounter;
    }
    else {
      // as object without any properties
      writer.objectHeader("Object");
      writer.endObject();

      if (subTags.length > 1) {
        LOG.warn("Skip fx:component, only one root tag is allowed: " + tag.getText());
      }

      objectTableSize = 0;
    }

    out.putShort(out.size() - (sizePosition + 2 /* short size */), sizePosition);
    out.putShort(objectTableSize, sizePosition + 2);

    return true;
  }

  private static class ModelObjectReferenceProvider implements MxmlObjectReferenceProvider {
    private MxmlObjectReference reference;
    private final BaseWriter writer;

    public ModelObjectReferenceProvider(BaseWriter writer) {
      this.writer = writer;
    }

    @Override
    public MxmlObjectReference getMxmlObjectReference() {
      if (reference == null) {
        reference = new MxmlObjectReference(writer.allocateAbsoluteStaticObjectId());

        writer.property("2");
        writer.getOut().writeUInt29(reference.id);
      }

      return reference;
    }
  }

  // tagLocalName is null, if parent is map item (i.e. not as property value, but as array item)
  private int writeFxModelTagIfContainsXmlText(XmlTag parent, @Nullable String tagLocalName,
                                               ModelObjectReferenceProvider objectReferenceProvider) {
    for (XmlTagChild child : parent.getValue().getChildren()) {
      // ignore any subtags if XmlText presents, according to flex compiler behavior
      if (child instanceof XmlText && !MxmlUtil.containsOnlyWhitespace(child)) {
        final ValueWriter valueWriter;
        try {
          valueWriter = injectedASWriter.processProperty(child, tagLocalName, null, false, objectReferenceProvider);
        }
        catch (InvalidPropertyException e) {
          // we don't need any out rollback - nothing is written yet
          mxmlWriter.problemsHolder.add(e);
          return -1;
        }

        if (valueWriter != InjectedASWriter.IGNORE) {
          if (valueWriter != null) {
            throw new IllegalStateException("What?");
          }
          else {
            if (tagLocalName != null) {
              writer.property(tagLocalName);
            }
            writeUntypedPrimitiveValue(writer.getOut(), ((XmlText)child).getValue());
          }
        }

        // ignore any attributes
        return 1;
      }
      else if (child instanceof XmlTag) {
        return 0;
      }
    }

    return 0;
  }

  private boolean processFxModelTagChildren(final XmlTag parent, ModelObjectReferenceProvider parentObjectReferenceProvider) {
    writer.objectHeader("mx.utils.ObjectProxy");

    final XmlTag[] parentSubTags = parent.getSubTags();
    for (XmlTag tag : parentSubTags) {
      final String tagLocalName = tag.getLocalName();
      final XmlTag[] subTags = parent.findSubTags(tagLocalName, tag.getNamespace());
      if (subTags.length > 1) {
        writer.property(tagLocalName);
        final int lengthPosition = writer.arrayHeader();
        int length = 0;
        ModelObjectReferenceProvider subObjectReferenceProvider = null;
        for (XmlTag subTag : subTags) {
          final int result = writeFxModelTagIfContainsXmlText(subTag, null, parentObjectReferenceProvider);
          if (result == 1) {
            length++;
          }
          else if (result == 0) {
            if (subObjectReferenceProvider == null) {
              subObjectReferenceProvider = new ModelObjectReferenceProvider(writer);
            }

            if (processFxModelTagChildren(subTag, subObjectReferenceProvider)) {
              length++;
            }
            subObjectReferenceProvider.reference = null;
          }
        }

        writer.getOut().putShort(length, lengthPosition);
      }
      else if (writeFxModelTagIfContainsXmlText(tag, tagLocalName, parentObjectReferenceProvider) == 0) {
        writer.property(tagLocalName);
        processFxModelTagChildren(tag, new ModelObjectReferenceProvider(writer));
      }
    }

    for (final XmlAttribute attribute : parent.getAttributes()) {
      mxmlWriter.writeSimpleAttributeBackedProperty(attribute, new AnyXmlAttributeDescriptorWrapper(attribute.getDescriptor()),
                                                    parentObjectReferenceProvider);
    }

    writer.endObject();
    return true;
  }

  boolean writeTagIfFxOrFxg(XmlTag tag, XmlElementDescriptor descriptor, @Nullable Context parentContext, boolean allowIncludeInExcludeFrom, PrimitiveAmfOutputStream out) throws InvalidPropertyException {
    String type = descriptor.getQualifiedName();
    // AS-110
    if (!JavaScriptSupportLoader.MXML_URI3.equals(tag.getNamespace())) {
      return writeIfFxg(tag, descriptor, parentContext, allowIncludeInExcludeFrom, out);
    }
    else if (type.equals(JSCommonTypeNames.OBJECT_CLASS_NAME) || type.equals(AsCommonTypeNames.DATE)) {
      return false;
    }

    if (JSCommonTypeNames.ARRAY_CLASS_NAME.equals(type)) {
      // see valArr in EmbedSwfAndImageFromCss
      out.write(AmfExtendedTypes.MXML_ARRAY);
      mxmlWriter.processTagChildren(tag, mxmlWriter.processIdAttributeOfFxTag(tag, parentContext, allowIncludeInExcludeFrom), parentContext,
                                    false, ARRAY);
      return true;
    }
    else if (CodeContext.AS3_VEC_VECTOR_QUALIFIED_NAME.equals(type)) {
      return mxmlWriter.processMxmlVector(tag, parentContext, allowIncludeInExcludeFrom);
    }

    final boolean isXml;
    if (type.equals(JSCommonTypeNames.XML_LIST_CLASS_NAME)) {
      out.write(AmfExtendedTypes.XML_LIST);
      isXml = true;
    }
    else if (type.equals(JSCommonTypeNames.XML_CLASS_NAME)) {
      out.write(AmfExtendedTypes.XML);
      isXml = true;
    }
    else {
      out.write(AmfExtendedTypes.REFERABLE);
      isXml = false;
    }

    mxmlWriter.processIdAttributeOfFxTag(tag, parentContext, allowIncludeInExcludeFrom);
    if (isXml) {
      out.writeAmfUtf(tag.getValue().getText());
    }
    else {
      boolean result = writeIfPrimitive(mxmlWriter.valueProviderFactory.create(tag), type, out, null, false, true);
      LOG.assertTrue(result);
    }

    return true;
  }

  private boolean writeIfFxg(XmlTag tag, XmlElementDescriptor descriptor, Context parentContext, boolean allowIncludeInExcludeFrom, PrimitiveAmfOutputStream out) {
    if (descriptor instanceof ClassBackedElementDescriptor) {
      PsiElement declaration = descriptor.getDeclaration();
      PsiFile psiFile = declaration.getContainingFile();
      VirtualFile virtualFile = psiFile.getVirtualFile();
      MxmlWriter.LOG.assertTrue(virtualFile != null);
      if (JavaScriptSupportLoader.isFxgFile(virtualFile)) {
        out.write(AmfExtendedTypes.REFERABLE);
        mxmlWriter.processIdAttributeOfFxTag(tag, parentContext, allowIncludeInExcludeFrom);
        out.write(AmfExtendedTypes.SWF);
        out.writeUInt29(EmbedSwfManager.getInstance().add(virtualFile, EmbedSwfManager.FXG_MARKER, writer.getAssetCounter()));
        return true;
      }
    }
    return false;
  }

  boolean writeIfPrimitive(XmlElementValueProvider valueProvider, String type, PrimitiveAmfOutputStream out, @Nullable AnnotationBackedDescriptor descriptor,
                           boolean isStyle, boolean emptyNumericAs0) throws InvalidPropertyException {
    if (writer.writeIfApplicable(valueProvider, type, out, descriptor, isStyle, emptyNumericAs0)) {
      return true;
    }
    else if (type.equals(AsCommonTypeNames.CLASS)) {
      processClass(valueProvider);
      return true;
    }

    return false;
  }

  // see ClassProperty test
  private void processClass(XmlElementValueProvider valueProvider) throws InvalidPropertyException {
    JSClass jsClass = valueProvider.getJsClass();
    // IDEA-73537, cannot use only valueProvider.getJsClass()
    if (jsClass == null) {
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

      final Module module = ModuleUtilCore.findModuleForPsiElement(valueProvider.getElement());
      if (module != null) {
        jsClass = (JSClass)ActionScriptClassResolver
          .findClassByQNameStatic(trimmed, module.getModuleWithDependenciesAndLibrariesScope(false));
      }

      if (jsClass == null) {
        throw new InvalidPropertyException(exceptionElement, "unresolved.class", trimmed);
      }
    }

    if (InjectionUtil.isProjectComponent(jsClass)) {
      if (ActionScriptClassResolver.isParentClass(jsClass, "spark.components.View")) {
        int projectComponentFactoryId = getProjectComponentFactoryId(jsClass);
        assert projectComponentFactoryId != -1;
        writer.projectClassReference(projectComponentFactoryId);
      }
      else {
        throw new InvalidPropertyException(valueProvider.getElement(), "class.reference.support.only.skin.class.or.view", jsClass.getQualifiedName());
      }
    }
    else {
      writer.classReference(jsClass.getQualifiedName());
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public PropertyKind write(AnnotationBackedDescriptor descriptor, XmlElementValueProvider valueProvider, PrimitiveAmfOutputStream out,
                            BaseWriter writer, boolean isStyle, @Nullable Context parentContext) throws InvalidPropertyException {
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

    if (writeIfPrimitive(valueProvider, type, out, descriptor, isStyle, false)) {
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
            if (!mxmlWriter.processMxmlVector(contentTag, parentContext, false)) {
              throw new InvalidPropertyException(contentTag, "invalid.vector.value");
            }
            
            return isStyle ? PRIMITIVE_STYLE : PRIMITIVE;
          }
        }
      }

      writer.vectorHeader(descriptor.getArrayType());
      return VECTOR;
    }
    else if (type.equals(JSCommonTypeNames.OBJECT_CLASS_NAME) || type.equals(JSCommonTypeNames.ANY_TYPE)) {
      final PropertyKind propertyKind = writeUntypedPropertyValue(out, valueProvider, descriptor, isStyle, parentContext);
      if (propertyKind != null) {
        return propertyKind;
      }
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
                writer.stringReference(XmlElementValueProvider.EMPTY);
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

  private PropertyKind writeUntypedPropertyValue(PrimitiveAmfOutputStream out, XmlElementValueProvider valueProvider,
                                                 AnnotationBackedDescriptor descriptor, boolean isStyle, @Nullable Context parentContext)
    throws InvalidPropertyException {
    if (descriptor.isRichTextContent()) {
      writer.writeString(valueProvider, descriptor);
      return null;
    }

    if (valueProvider instanceof XmlAttributeValueProvider && isInlineArray(valueProvider.getTrimmed())) {
      writeInlineArray(valueProvider);
      return null;
    }

    final CharSequence charSequence;
    // IDEA-73099, IDEA-73960
    if (valueProvider instanceof XmlTagValueProvider) {
      final XmlTag tag = ((XmlTagValueProvider)valueProvider).getTag();
      final XmlTagChild[] children = tag.getValue().getChildren();
      int subTagsLength = 0;
      XmlText xmlText = null;
      for (XmlTagChild child : children) {
        if (child instanceof XmlTag) {
          subTagsLength++;
          if (xmlText != null || subTagsLength > 1) {
            break;
          }
        }
        else if (child instanceof XmlText && !MxmlUtil.containsOnlyWhitespace(child)) {
          assert xmlText == null;
          xmlText = (XmlText)child;
        }
      }

      if (subTagsLength == 0) {
        if (xmlText == null) {
          writer.stringReference(XmlElementValueProvider.EMPTY);
          return null;
        }
        else {
          charSequence = XmlTextValueProvider.getSubstituted(xmlText);
        }
      }
      else if (subTagsLength == 1 && xmlText == null) {
        final XmlTag childTag = tag.getSubTags()[0];
        final XmlElementDescriptor childTagDescriptor = childTag.getDescriptor();
        LOG.assertTrue(childTagDescriptor != null);
        if (writeTagIfFxOrFxg(childTag, childTagDescriptor, parentContext, false, out)) {
          return null;
        }
        else {
          out.write(Amf3Types.OBJECT);
          return isStyle ? COMPLEX_STYLE : COMPLEX;
        }
      }
      else {
        out.write(Amf3Types.ARRAY);
        return ARRAY;
      }
    }
    else {
      charSequence = writer.writeIfEmpty(valueProvider);
    }

    if (charSequence != null) {
      writeUntypedPrimitiveValue(out, charSequence);
    }

    return null;
  }

  private void writeUntypedPrimitiveValue(PrimitiveAmfOutputStream out, CharSequence charSequence) {
    final String s = CharMatcher.WHITESPACE.trimFrom(charSequence);
    if (s.equals("true")) {
      out.write(Amf3Types.TRUE);
      return;
    }
    if (s.equals("false")) {
      out.write(Amf3Types.FALSE);
      return;
    }

    //noinspection UnusedCatchParameter
    try {
      out.writeAmfInt(Integer.parseInt(s));
    }
    catch (NumberFormatException e) {
      try {
        out.writeAmfDouble(Double.parseDouble(s));
      }
      catch (NumberFormatException ignored) {
        // write untrimmed string
        writer.string(charSequence);
      }
    }
  }

  private void writeClassFactory(XmlElementValueProvider valueProvider) throws InvalidPropertyException {
    if (valueProvider instanceof XmlTagValueProvider) {
      XmlTag tag = ((XmlTagValueProvider)valueProvider).getTag();
      XmlTag[] subTags = tag.getSubTags();
      if (subTags.length > 0) {
        processFxComponent(subTags[0], true);
        return;
      }
    }

    String className = valueProvider.getTrimmed();
    if (writeFxComponentReferenceIfProcessed(className) || writeReferenceIfReferenced(className)) {
      return;
    }

    final JSClass jsClass = valueProvider.getJsClass();
    if (jsClass == null) {
      throw new InvalidPropertyException(valueProvider.getElement(), "unresolved.class", valueProvider.getTrimmed());
    }

    final Trinity<Integer, String, Condition<AnnotationBackedDescriptor>> effectiveClassInfo;
    final PsiElement parent = jsClass.getNavigationElement().getParent();
    if (parent instanceof XmlTag && MxmlUtil.isComponentLanguageTag((XmlTag)parent)) {
      // if referenced by inner class name, but inner fx component is not yet processed
      if (parent.getContainingFile().equals(valueProvider.getElement().getContainingFile())) {
        processFxComponent((XmlTag)parent, false);
        return;
      }
      else {
        effectiveClassInfo = new Trinity<>(-1, "mx.core.UIComponent", null);
      }
    }
    else {
      effectiveClassInfo =
        MxmlUtil.computeEffectiveClass(valueProvider.getElement(), jsClass, mxmlWriter.projectComponentReferenceCounter, false);
    }

    if (effectiveClassInfo.first == -1) {
      if (effectiveClassInfo.second != null) {
        if (effectiveClassInfo.second.equals("mx.core.UIComponent")) {
          PsiMetaData psiMetaData = valueProvider.getPsiMetaData();
          if (psiMetaData != null &&
              psiMetaData.getName().equals("itemRenderer") &&
              MxmlUtil.isPropertyOfSparkDataGroup((AnnotationBackedDescriptor)psiMetaData)) {
            className = MxmlUtil.UNKNOWN_ITEM_RENDERER_CLASS_NAME;
          }
          else {
            className = MxmlUtil.UNKNOWN_COMPONENT_CLASS_NAME;
          }
        }
        else {
          className = effectiveClassInfo.second;
        }
      }

      writeNonProjectUnreferencedClassFactory(className);
    }
    else {
      writer.documentFactoryReference(effectiveClassInfo.first);
    }
  }

  private boolean writeFxComponentReferenceIfProcessed(String className) {
    MxmlObjectReference reference = injectedASWriter.getNullableValueReference('$' + className);
    if (reference != null) {
      writer.objectReference(reference.id);
      return true;
    }

    return false;
  }

  private void writeNonProjectUnreferencedClassFactory(String className) {
    int reference = writer.allocateAbsoluteStaticObjectId();
    classFactoryMap.put(className, reference);
    writer.referableHeader(reference).newInstance(FlexCommonTypeNames.CLASS_FACTORY, 1, false).classReference(className);
  }

  private void writeNonProjectClassFactory(String className) {
    if (!writeReferenceIfReferenced(className)) {
      writeNonProjectUnreferencedClassFactory(className);
    }
  }

  private boolean writeReferenceIfReferenced(String className) {
    int reference = classFactoryMap.get(className);
    if (reference != -1) {
      writer.objectReference(reference);
      return true;
    }

    return false;
  }
}