package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.ByteRange;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.*;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.IOException;
import java.util.List;

import static com.intellij.flex.uiDesigner.mxml.PropertyProcessor.PropertyKind;

public class MxmlWriter {
  static final Logger LOG = Logger.getInstance(MxmlWriter.class.getName());

  private final PrimitiveAmfOutputStream out;

  private final BaseWriter writer;
  private StateWriter stateWriter;
  private final InjectedASWriter injectedASWriter;

  private XmlTextValueProvider xmlTextValueProvider;
  private XmlTagValueProvider xmlTagValueProvider;
  private final XmlAttributeValueProvider xmlAttributeValueProvider = new XmlAttributeValueProvider();

  private boolean hasStates;
  private final PropertyProcessor propertyProcessor;
  private boolean requireCallResetAfterMessage;

  private ProblemsHolder problemsHolder;

  public MxmlWriter(PrimitiveAmfOutputStream out) {
    this.out = out;
    writer = new BaseWriter(out);
    injectedASWriter = new InjectedASWriter(writer);
    propertyProcessor = new PropertyProcessor(injectedASWriter, writer, this);
  }

  public XmlFile[] write(@NotNull final XmlFile psiFile, @NotNull final ProblemsHolder problemsHolder,
                         @NotNull final RequiredAssetsInfo requiredAssetsInfo) throws IOException {
    try {
      this.problemsHolder = problemsHolder;
      problemsHolder.setCurrentFile(psiFile.getVirtualFile());
      injectedASWriter.setProblemsHolder(problemsHolder);
      writer.requiredAssetsInfo = requiredAssetsInfo;
      requireCallResetAfterMessage = true;

      writer.beginMessage();

      AccessToken token = ReadAction.start();
      try {
        XmlTag rootTag = psiFile.getRootTag();
        assert rootTag != null;
        ClassBackedElementDescriptor rootTagDescriptor = (ClassBackedElementDescriptor)rootTag.getDescriptor();
        assert rootTagDescriptor != null;
        writer.writeObjectHeader(rootTagDescriptor.getQualifiedName());
        processElements(rootTag, null, false, -1, out.size() - 2);
      }
      finally {
        token.finish();
      }

      writer.endObject();

      if (stateWriter != null) {
        stateWriter.write();
        hasStates = false;
      }
      else {
        out.write(0);
      }

      injectedASWriter.write();
      writer.endMessage();
      List<XmlFile> unregisteredDocumentReferences = propertyProcessor.getUnregisteredDocumentFactories();
      return unregisteredDocumentReferences.isEmpty()
             ? null
             : unregisteredDocumentReferences.toArray(new XmlFile[unregisteredDocumentReferences.size()]);
    }
    finally {
      problemsHolder.setCurrentFile(null);
      this.problemsHolder = null;
      writer.requiredAssetsInfo = null;
      injectedASWriter.setProblemsHolder(null);
      requireCallResetAfterMessage = false;
      resetAfterMessage();
    }
  }

  public void reset() {
    if (requireCallResetAfterMessage) {
      resetAfterMessage();
    }

    writer.reset();
    if (stateWriter != null) {
      stateWriter.reset();
    }

    injectedASWriter.reset();
  }

  private void resetAfterMessage() {
    xmlAttributeValueProvider.setAttribute(null);
    xmlTextValueProvider = null;
    xmlTagValueProvider = null;

    writer.resetAfterMessage();
    propertyProcessor.reset();
  }

  private XmlElementValueProvider createValueProvider(XmlText xmlText) {
    if (xmlTextValueProvider == null) {
      xmlTextValueProvider = new XmlTextValueProvider();
    }

    xmlTextValueProvider.setXmlText(xmlText);
    return xmlTextValueProvider;
  }

  private XmlElementValueProvider createValueProvider(XmlTag tag) {
    if (xmlTagValueProvider == null) {
      xmlTagValueProvider = new XmlTagValueProvider();
    }

    xmlTagValueProvider.setTag(tag);
    return xmlTagValueProvider;
  }

  private XmlElementValueProvider createValueProvider(XmlAttribute attribute) {
    xmlAttributeValueProvider.setAttribute(attribute);
    return xmlAttributeValueProvider;
  }

  private boolean processElements(final XmlTag parent, final @Nullable Context parentContext, final boolean allowIncludeInExludeFrom,
                               int dataPosition, final int referencePosition) {
    boolean cssDeclarationSourceDefined = false;
    boolean staticChild = true;

    Context context = null;
    ByteRange dataRange = null;
    // if state specific property before includeIn, state override data range wil be added before object data range, so,
    // we keep current index and insert at the specified position
    final int dataRangeIndex = writer.getBlockOut().getNextMarkerIndex();

    out.writeUInt29(writer.P_FUD_POSITION);
    out.writeUInt29(parent.getTextOffset());

    for (final XmlAttribute attribute : parent.getAttributes()) {
      XmlAttributeDescriptor attributeDescriptor = attribute.getDescriptor();
      final AnnotationBackedDescriptor descriptor;
      if (attributeDescriptor instanceof AnnotationBackedDescriptor) {
        descriptor = (AnnotationBackedDescriptor)attributeDescriptor;
        // id and includeIn/excludeFrom only as attribute, not as tag
        if (descriptor.isPredefined()) {
          if (descriptor.hasIdType()) {
            injectedASWriter.processObjectWithExplicitId(attribute.getValue(), context);
          }
          else if (allowIncludeInExludeFrom) {
            String name = descriptor.getName();
            boolean excludeFrom = false;
            if (name.equals(FlexStateElementNames.INCLUDE_IN) || (excludeFrom = name.equals(FlexStateElementNames.EXCLUDE_FROM))) {
              if (context == null) {
                context = writer.createDynamicObjectStateContext();
              }

              // must be before stateWriter.includeIn — start object data range before state data range
              dataRange = writer.getBlockOut().startRange(dataPosition, dataRangeIndex);
              ((DynamicObjectContext)context).setDataRange(dataRange);

              stateWriter.includeInOrExcludeFrom(attribute.getValueElement(), parentContext, (DynamicObjectContext)context, excludeFrom);
              staticChild = false;
            }
            else if (name.equals(FlexStateElementNames.ITEM_CREATION_POLICY)) {
              if (attribute.getValue().charAt(0) == 'i') {
                if (context == null) {
                  context = writer.createDynamicObjectStateContext();
                }

                ((DynamicObjectContext)context).setImmediateCreation(true);
              }
            }
            else if (name.equals(FlexStateElementNames.ITEM_DESTRUCTION_POLICY)) {
              if (attribute.getValue().charAt(0) == 'a') {
                stateWriter.applyItemAutoDestruction(context, parentContext);
              }
            }
          }
        }
        else if (descriptor.hasIdType() && MxmlUtil.isIdLanguageIdAttribute(attribute)) {
          String explicitId = attribute.getValue();
          writer.writeIdProperty(explicitId);
          injectedASWriter.processObjectWithExplicitId(explicitId, context);
        }
        else if (descriptor.getTypeName().equals(FlexAnnotationNames.EVENT) ||
                 descriptor.getTypeName().equals(FlexAnnotationNames.BINDABLE)) {
          // skip
        }
        else if (hasStates && stateWriter.checkStateSpecificPropertyValue(this, propertyProcessor, attribute,
                                                                          createValueProvider(attribute),
                                                                          descriptor, context, parentContext)) {
          // skip
        }
        else {
          cssDeclarationSourceDefined = writeProperty(attribute, descriptor, context, cssDeclarationSourceDefined);
        }
      }
      else if (attributeDescriptor instanceof AnyXmlAttributeDescriptor) {
        writeProperty(attribute, new AnyXmlAttributeDescriptorWrapper((AnyXmlAttributeDescriptor)attributeDescriptor), context, false);
      }
    }

    if (hasStates) {
      if (context == null) {
        context = stateWriter.createContextForStaticBackSiblingAndFinalizeStateSpecificAttributes(allowIncludeInExludeFrom,
                                                                                                  referencePosition, parentContext,
                                                                                                  injectedASWriter);
      }
    }
    else {
      assert context == null;
      context = writer.createStaticContext(parentContext, referencePosition);
    }

    writer.resetPreallocatedId();

    processTagChildren(parent, context, parentContext, true, null, cssDeclarationSourceDefined);
    // initializeReference must be after process all elements — after sub tag also, due to <RadioButton id="visa" label="Visa" 
    // width="150"><group>{cardtype} !!id (for binding target, RadioButton id="visa") allocation here!!</group></RadioButton>
    if (dataPosition != -1) {
      writer.endObject();
      if (dataRange != null) {
        writer.getBlockOut().endRange(dataRange);
      }
    }

    return staticChild;
  }

  private boolean writeProperty(XmlAttribute attribute, AnnotationBackedDescriptor descriptor, Context context,
                                boolean cssDeclarationSourceDefined) {
    int beforePosition = out.size();
    final PropertyKind propertyKind = writeProperty(attribute, createValueProvider(attribute), descriptor, cssDeclarationSourceDefined, context);
    if (propertyKind != PropertyKind.IGNORE) {
      if (propertyProcessor.isStyle()) {
        cssDeclarationSourceDefined = true;
      }
      if (propertyKind.isComplex()) {
        writer.getBlockOut().setPosition(beforePosition);
        addProblem(attribute, "error.unknown.attribute.value.type", descriptor.getType());
      }
    }
    return cssDeclarationSourceDefined;
  }

  void processTagChildren(final XmlTag parent, final @Nullable Context context, final @Nullable Context parentContext,
                          boolean propertiesExpected, @Nullable PropertyKind listKind, boolean cssRulesetDefined) {
    int lengthPosition = listKind == null ? 0 : out.allocateShort();
    int explicitContentOccured = -1;
    int validAndStaticChildrenCount = 0;
    final XmlTagChild[] children = parent.getValue().getChildren();
    for (XmlTagChild child : children) {
      if (child instanceof XmlTag) {
        XmlTag tag = (XmlTag)child;
        XmlElementDescriptor descriptor = tag.getDescriptor();
        assert descriptor != null;
        if (descriptor instanceof ClassBackedElementDescriptor) {
          if (explicitContentOccured == 1) {
            LOG.warn("Default content already processed, skip " + child);
            continue;
          }

          ClassBackedElementDescriptor classBackedDescriptor = (ClassBackedElementDescriptor)descriptor;
          if (classBackedDescriptor.isPredefined()) {
            if (descriptor.getQualifiedName().equals(FlexPredefinedTagNames.DECLARATIONS)) {
              injectedASWriter.readDeclarations(this, tag);
            }

            continue;
          }
          else if (MxmlUtil.isAbstract(classBackedDescriptor)) {
            addProblem(child, "error.abstract.class", classBackedDescriptor.getQualifiedName());
            continue;
          }

          if (propertiesExpected && explicitContentOccured == -1) {
            explicitContentOccured = 0;
            final PropertyKind defaultPropertyKind = processDefaultProperty(parent, createValueProvider(tag), classBackedDescriptor, children.length, context, cssRulesetDefined);
            if (defaultPropertyKind == null) {
              continue;
            }
            else if (defaultPropertyKind.isList()) {
              lengthPosition = out.allocateShort();
              listKind = defaultPropertyKind;
            }
            else if (defaultPropertyKind == PropertyKind.PRIMITIVE) {
              validAndStaticChildrenCount++;
              continue;
            }
          }

          if (processClassBackedSubTag(tag, classBackedDescriptor, context, listKind != null)) {
            validAndStaticChildrenCount++;
          }
        }
        else if (propertiesExpected && descriptor instanceof AnnotationBackedDescriptor) {
          AnnotationBackedDescriptor annotationBackedDescriptor = (AnnotationBackedDescriptor)descriptor;
          // explicit content after contiguous child elements serving as the default property value
          // skip invalid, contiguous child elements already processed and explicit content (i.e. AnnotationBackedDescriptor, property tag) was occured
          if (explicitContentOccured == 0) {
            explicitContentOccured = 1;
            endList(listKind, validAndStaticChildrenCount, lengthPosition);
          }

          if (tag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI4) && tag.getLocalName().equals(FlexStateElementNames.STATES)) {
            if (tag.getSubTags().length != 0) {
              hasStates = true;
              assert parentContext == null;
              if (stateWriter == null) {
                stateWriter = new StateWriter(writer);
              }
              stateWriter.readDeclaration(tag);
            }
          }
          else if (hasStates && stateWriter.checkStateSpecificPropertyValue(this, propertyProcessor, tag, createValueProvider(tag),
                                                                            annotationBackedDescriptor, context, parentContext)) {
            // skip
          }
          else {
            final PropertyKind propertyKind = writeProperty(tag, createValueProvider(tag), annotationBackedDescriptor, cssRulesetDefined,
                                              context);
            if (propertyKind != PropertyKind.IGNORE) {
              if (propertyProcessor.isStyle()) {
                cssRulesetDefined = true;
              }

              if (propertyKind.isComplex()) {
                assert context != null;
                processPropertyTagValue(tag, context, propertyKind);
              }
            }
          }
        }
      }
      else if (child instanceof XmlText && !MxmlUtil.containsOnlyWhitespace(child)) {
        if (explicitContentOccured == 1) {
          LOG.warn("Default content already processed, skip " + child);
          continue;
        }

        if (propertiesExpected && explicitContentOccured == -1) {
          explicitContentOccured = 0;
          final XmlElementValueProvider valueProvider = createValueProvider((XmlText)child);
          final PropertyKind defaultPropertyKind = processDefaultProperty(parent, valueProvider, null, children.length, context, cssRulesetDefined);
          if (defaultPropertyKind == null) {
            continue;
          }
          else if (defaultPropertyKind.isList()) {
            lengthPosition = out.allocateShort();
            listKind = defaultPropertyKind;
          }
          else if (defaultPropertyKind == PropertyKind.PRIMITIVE) {
            validAndStaticChildrenCount++;
            continue;
          }
          else {
            final ValueWriter valueWriter;
            try {
              valueWriter = propertyProcessor.processXmlTextAsDefaultPropertyWithComplexType(valueProvider, parent, context);
            }
            catch (InvalidPropertyException e) {
              // we don't need any out rollback — nothing is written yet
              problemsHolder.add(e);
              continue;
            }

            if (valueWriter == null) {
              throw new IllegalArgumentException("unexpected default property kind " + defaultPropertyKind);
            }
            else if (valueWriter == InjectedASWriter.IGNORE) {
              continue;
            }
            else {

            }
          }
        }
        else if (listKind == PropertyKind.VECTOR) {
          LOG.warn("skip " + child + " due to IDEA-73478");
          // IDEA-73478, XmlText allowed only for fx:Array, but not for fx:Vector (even with type String)
          break;
        }

        writer.writeString(((XmlText)child).getValue());
        validAndStaticChildrenCount++;
      }
    }

    endList(listKind, validAndStaticChildrenCount, lengthPosition);
  }

  private void endList(@Nullable PropertyKind listKind, int validChildrenCount, int lengthPosition) {
    if (listKind != null) {
      assert validChildrenCount < 65535;
      out.putShort(validChildrenCount, lengthPosition);
    }
  }

  // process tag value, opposite to processTagChildren expects only ClassBackedSubTag or XmlText (attributes already processed or isn't expected)
  void processPropertyTagValue(final XmlTag parent, final @Nullable Context parentContext, final PropertyKind propertyKind) {
    processTagChildren(parent, parentContext, null, false, propertyKind.isList() ? propertyKind : null, false);
  }

  private boolean processClassBackedSubTag(XmlTag tag, ClassBackedElementDescriptor descriptor, @Nullable Context parentContext,
                                        boolean isListItem) {
    try {
      if (propertyProcessor.writeIfPrimitive(createValueProvider(tag), descriptor.getQualifiedName(), out, writer, null)) {
        return true;
      }
    }
    catch (InvalidPropertyException e) {
      problemsHolder.add(e);
      return false;
    }

    if (JSCommonTypeNames.ARRAY_CLASS_NAME.equals(descriptor.getQualifiedName())) {
      // see valArr in EmbedSwfAndImageFromCss
      out.write(AmfExtendedTypes.MXML_ARRAY);
      processIdAttributeOfArrayOrVectorDeclaredAsTag(tag, parentContext);
      processTagChildren(tag, null, parentContext, false, PropertyKind.ARRAY, false);
      return true;
    }
    else if (CodeContext.AS3_VEC_VECTOR_QUALIFIED_NAME.equals(descriptor.getQualifiedName())) {
      return processMxmlVector(tag, parentContext);
    }

    final int projectComponentFactoryId;
    try {
      projectComponentFactoryId = InjectionUtil.getProjectComponentFactoryId(descriptor.getQualifiedName(), descriptor.getDeclaration(),
                                                                             propertyProcessor.getUnregisteredDocumentFactories());
    }
    catch (InvalidPropertyException e) {
      problemsHolder.add(e);
      return false;
    }

    final int childDataPosition = out.size();
    if (projectComponentFactoryId != -1) {
      if (!isListItem) {
        // replace Amf3Types.OBJECT to AmfExtendedTypes.DOCUMENT_REFERENCE
        writer.getBlockOut().setPosition(writer.getBlockOut().size() - 1);
      }

      out.write(AmfExtendedTypes.DOCUMENT_REFERENCE);
      out.writeUInt29(projectComponentFactoryId);
    }
    else {
      if (isListItem) {
        out.write(Amf3Types.OBJECT);
      }

      writer.write(descriptor.getQualifiedName());
    }

    return processElements(tag, parentContext, hasStates && isListItem && parentContext != null, childDataPosition, out.allocateClearShort());
  }

  boolean processMxmlVector(XmlTag tag, @Nullable Context parentContext) {
    final XmlAttribute typeAttribute = tag.getAttribute("type");
    final String type;
    if (typeAttribute == null || StringUtil.isEmpty((type = typeAttribute.getDisplayValue()))) {
      LOG.warn("Skip " + tag + ", attribute type must be specified");
      return false;
    }

    final XmlAttribute fixedAttribute = tag.getAttribute("fixed");
    out.write(AmfExtendedTypes.MXML_VECTOR);
    writer.write(type);
    out.write(fixedAttribute != null && fixedAttribute.getDisplayValue().charAt(0) == 't');
    processIdAttributeOfArrayOrVectorDeclaredAsTag(tag, parentContext);
    processTagChildren(tag, null, parentContext, false, PropertyKind.VECTOR, false);
    return true;
  }

  private void processIdAttributeOfArrayOrVectorDeclaredAsTag(XmlTag tag, Context parentContext) {
    final XmlAttribute idAttribute = tag.getAttribute("id");
    if (idAttribute != null) {
      final String id = idAttribute.getDisplayValue();
      if (StringUtil.isEmpty(id)) {
        LOG.warn("Skip process id attribute of " + tag + ", id is empty");
      }
      else {
        // IDEA-73516
        injectedASWriter.processObjectWithExplicitId(id, parentContext);
        out.writeShort(writer.getPreallocatedId() + 1);
        writer.resetPreallocatedId();
        return;
      }
    }

    out.writeShort(0);
  }

  void processDeclarations(XmlTag parent) {
    final int lengthPosition = out.allocateShort();
    int validChildrenCount = 0;
    for (XmlTag tag : parent.getSubTags()) {
      ClassBackedElementDescriptor descriptor = (ClassBackedElementDescriptor)tag.getDescriptor();
      assert descriptor != null;
      if (descriptor.isPredefined()) {
        // todo IDEA-72123
        continue;
      }
      else if (MxmlUtil.isComponentLanguageTag(tag)) {
        // todo IDEA-72151
        continue;
      }

      if (processClassBackedSubTag(tag, descriptor, null, true)) {
        validChildrenCount++;
      }
    }

    out.putShort(validChildrenCount, lengthPosition);
  }

  private static boolean isHaloNavigator(String className, JSClass jsClass) {
    return className.equals(FlexCommonTypeNames.ACCORDION) ||
           className.equals(FlexCommonTypeNames.VIEW_STACK) ||
           JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.ACCORDION) ||
           JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.VIEW_STACK);
  }

  private void addProblem(XmlElement xmlElement, @PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    problemsHolder.add(xmlElement, FlexUIDesignerBundle.message(key, params));
  }

  // descriptor will be null if child is XmlText
  @Nullable
  private PropertyKind processDefaultProperty(XmlTag parentTag, XmlElementValueProvider valueProvider,
                                              @Nullable ClassBackedElementDescriptor descriptor, int childrenLength, Context context,
                                              boolean cssRulesetDefined) {
    ClassBackedElementDescriptor parentDescriptor = (ClassBackedElementDescriptor)parentTag.getDescriptor();
    assert parentDescriptor != null;

    AnnotationBackedDescriptor defaultDescriptor = parentDescriptor.getDefaultPropertyDescriptor();
    final boolean isXmlText = descriptor == null;
    if (defaultDescriptor == null) {
      final JSClass jsClass = (JSClass)parentDescriptor.getDeclaration();
      final String className = parentDescriptor.getQualifiedName();
      final boolean isDirectContainerImpl = className.equals(FlexCommonTypeNames.ICONTAINER);
      if (isDirectContainerImpl || JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.ICONTAINER)) {
        if (isXmlText) {
          addProblem(parentTag, "error.initializer.cannot.be.represented.in.text", parentTag.getLocalName());
          return null;
        }

        if (!isDirectContainerImpl && isHaloNavigator(className, jsClass) &&
            !JSInheritanceUtil.isParentClass((JSClass)descriptor.getDeclaration(), FlexCommonTypeNames.INAVIGATOR_CONTENT)) {
          addProblem(parentTag, "error.children.must.be", parentTag.getLocalName(), FlexCommonTypeNames.INAVIGATOR_CONTENT);
          return null;
        }

        writer.write("0");
        out.write(PropertyClassifier.MX_CONTAINER_CHILDREN);
        return PropertyKind.ARRAY;
      }
      else {
        // http://youtrack.jetbrains.net/issue/IDEA-66565
        addProblem(parentTag, "error.default.property.not.found", parentTag.getLocalName());
      }
    }
    else {
      // xmlText as default property with injection, see BindingToDeferredInstanceFromBytesBase
      if (isXmlText &&
          writeXmlTextAsDefaultPropertyInjectedValue(parentTag, valueProvider, defaultDescriptor, cssRulesetDefined, context)) {
        return null;
      }

      if (defaultDescriptor.getType().equals(JSCommonTypeNames.ARRAY_CLASS_NAME) && defaultDescriptor.getArrayType() != null) {
        final String elementType = defaultDescriptor.getArrayType();
        final boolean isString = elementType.equals(JSCommonTypeNames.STRING_CLASS_NAME);
        if (isString) {
          if (descriptor != null && !descriptor.getQualifiedName().equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
            addProblem(parentTag, "error.children.must.be", parentTag.getLocalName(), elementType);
            return null;
          }
        }
        else {
          if (isXmlText) {
            addProblem(parentTag, "error.initializer.cannot.be.represented.in.text", parentTag.getLocalName());
            return null;
          }
        }
      }

      writer.write(defaultDescriptor.getName());
      out.write(PropertyClassifier.PROPERTY);
      if (defaultDescriptor.isDeferredInstance()) {
        writer.writeDeferredInstanceFromArray();
        return PropertyKind.ARRAY;
      }
      else {
        final String type = defaultDescriptor.getType();
        if (type.equals(JSCommonTypeNames.STRING_CLASS_NAME) || (isXmlText && childrenLength == 1 &&
                                                                 (type.equals(JSCommonTypeNames.OBJECT_CLASS_NAME) ||
                                                                  type.equals(JSCommonTypeNames.ANY_TYPE)))) {
          writeSubstitutedString(valueProvider.getSubstituted());
        }
        else if (defaultDescriptor.contentIsArrayable()) {
          out.write(type.equals(JSCommonTypeNames.ARRAY_CLASS_NAME) ? Amf3Types.ARRAY : AmfExtendedTypes.ARRAY_IF_LENGTH_GREATER_THAN_1);
          return PropertyKind.ARRAY;
        }
        else if (defaultDescriptor.getArrayType() != null /* Vector */) {
          if (isXmlText) {
            LOG.warn("skip " + valueProvider.getElement() + " due to IDEA-73478");
            return null;
          }

          writer.writeVectorHeader(defaultDescriptor.getArrayType());
          return PropertyKind.VECTOR;
        }
        else if (type.equals(JSCommonTypeNames.NUMBER_CLASS_NAME)) {
          out.writeAmfDouble(valueProvider.getTrimmed());
        }
        else if (type.equals(JSCommonTypeNames.BOOLEAN_CLASS_NAME)) {
          out.writeAmfBoolean(valueProvider.getTrimmed());
        }
        else {
          out.write(Amf3Types.OBJECT);
          return PropertyKind.COMPLEX;
        }
      }
    }

    return PropertyKind.PRIMITIVE;
  }

  private void writeSubstitutedString(CharSequence value) {
    if (value == XmlElementValueProvider.EMPTY) {
      writer.writeStringReference(XmlElementValueProvider.EMPTY);
    }
    else {
      writer.writeString(value);
    }
  }

  private void defineInlineCssRuleset(@NotNull PsiElement element) {
    Document document = MxmlUtil.getDocument(element);
    int textOffset = element.getTextOffset();
    out.writeUInt29(document.getLineNumber(textOffset) + 1);
    out.writeUInt29(textOffset);
  }

  private PropertyKind writeProperty(XmlElement element, XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor,
                            boolean cssRulesetDefined, Context context) {
    final int beforePosition = writer.getBlockOut().size();
    try {
      ValueWriter valueWriter = propertyProcessor.process(element, valueProvider, descriptor, context);
      if (valueWriter == null) {
        return PropertyKind.IGNORE;
      }

      writePropertyHeader(propertyProcessor.getName(), element, cssRulesetDefined, propertyProcessor.isStyle());
      return valueWriter.write(descriptor, valueProvider, out, writer, propertyProcessor.isStyle());
    }
    catch (RuntimeException e) {
      problemsHolder.add(element, e, descriptor.getName());
    }
    catch (Throwable e) {
      problemsHolder.add(e);
    }
    
    writer.getBlockOut().setPosition(beforePosition);
    return PropertyKind.IGNORE;
  }

  private void writePropertyHeader(String name, XmlElement element, boolean cssRulesetDefined, boolean isStyle) {
    writer.write(name);
    if (isStyle) {
      out.write(PropertyClassifier.STYLE);
      if (!cssRulesetDefined) {
        defineInlineCssRuleset(element);
      }
    }
    else {
      out.write(PropertyClassifier.PROPERTY);
    }
  }

  private boolean writeXmlTextAsDefaultPropertyInjectedValue(XmlElement element, XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor,
                              boolean cssRulesetDefined, Context context) {
    final int beforePosition = writer.getBlockOut().size();
    try {
      ValueWriter valueWriter = propertyProcessor.processInjected(valueProvider, descriptor, descriptor.isStyle(), context);
      if (valueWriter == null) {
        return false;
      }

      if (valueWriter != InjectedASWriter.IGNORE) {
        writePropertyHeader(descriptor.getName(), element, cssRulesetDefined, descriptor.isStyle());
        valueWriter.write(descriptor, valueProvider, out, writer, propertyProcessor.isStyle());
      }

      return true;
    }
    catch (RuntimeException e) {
      problemsHolder.add(element, e, descriptor.getName());
    }
    catch (Throwable e) {
      problemsHolder.add(e);
    }
    
    writer.getBlockOut().setPosition(beforePosition);
    // true, ignore
    return true;
  }  
}