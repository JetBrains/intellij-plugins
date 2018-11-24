package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.AssetCounter;
import com.intellij.flex.uiDesigner.FlashUIDesignerBundle;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.ProblemsHolder;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.ByteRange;
import com.intellij.flex.uiDesigner.io.Marker;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.javascript.flex.mxml.schema.MxmlBackedElementDescriptor;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.*;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.flex.uiDesigner.mxml.PropertyProcessor.PropertyKind;

public class MxmlWriter {
  static final Logger LOG = Logger.getInstance(MxmlWriter.class);

  private final PrimitiveAmfOutputStream out;

  private final BaseWriter writer;
  private StateWriter stateWriter;
  private final InjectedASWriter injectedASWriter;
  private final PropertyProcessor propertyProcessor;

  private final NullContext tagAttributeProcessContext;
  private boolean hasStates;

  final ProblemsHolder problemsHolder;

  final ValueProviderFactory valueProviderFactory = new ValueProviderFactory();
  private final List<RangeMarker> rangeMarkers = new ArrayList<>();

  final ProjectComponentReferenceCounter projectComponentReferenceCounter = new ProjectComponentReferenceCounter();

  private Document document;

  public MxmlWriter(PrimitiveAmfOutputStream out, ProblemsHolder problemsHolder, AssetCounter assetCounter) {
    this.out = out;
    this.problemsHolder = problemsHolder;

    writer = new BaseWriter(out, assetCounter);
    tagAttributeProcessContext = writer.nullContext;
    injectedASWriter = new InjectedASWriter(writer, problemsHolder);
    propertyProcessor = new PropertyProcessor(injectedASWriter, writer, this);
  }

  @Nullable
  public Pair<ProjectComponentReferenceCounter, List<RangeMarker>> write(XmlFile psiFile) throws IOException {
    document = MxmlUtil.getDocumentAndWaitIfNotCommitted(psiFile);
    final AccessToken token = ReadAction.start();
    try {
      VirtualFile virtualFile = psiFile.getVirtualFile();
      LOG.assertTrue(virtualFile != null);
      problemsHolder.setCurrentFile(virtualFile);

      XmlTag tag = psiFile.getRootTag();
      XmlElementDescriptor untypedDescriptor = tag == null ? null : tag.getDescriptor();
      final ClassBackedElementDescriptor descriptor;
      if (untypedDescriptor instanceof ClassBackedElementDescriptor) {
        descriptor = (ClassBackedElementDescriptor)untypedDescriptor;
      }
      else {
        return null;
      }

      final Trinity<Integer, String, Condition<AnnotationBackedDescriptor>> effectiveClassInfo;
      try {
        PsiElement declaration = descriptor.getDeclaration();
        if (declaration == null) {
          return null;
        }
        effectiveClassInfo = MxmlUtil.computeEffectiveClass(tag, declaration, projectComponentReferenceCounter, true);
      }
      catch (InvalidPropertyException e) {
        problemsHolder.add(e);
        return null;
      }

      if (effectiveClassInfo.first == -1) {
        out.write(Amf3Types.OBJECT);
        writer.mxmlObjectHeader(effectiveClassInfo.second == null ? descriptor.getQualifiedName() : effectiveClassInfo.second);
      }
      else {
        writer.documentReference(effectiveClassInfo.first);
        out.allocateClearShort();
      }

      processElements(tag, null, false, -1, out.size() - 2, true, effectiveClassInfo.third);

      writer.endObject();

      if (stateWriter != null) {
        stateWriter.write();
        hasStates = false;
      }
      else {
        out.write(0);
      }

      injectedASWriter.write();
      writer.writeMessageHeader(projectComponentReferenceCounter);
      return Pair.create(projectComponentReferenceCounter, rangeMarkers);
    }
    finally {
      token.finish();
      problemsHolder.setCurrentFile(null);
      writer.resetAfterMessage();
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  private boolean processElements(final XmlTag tag,
                                  @Nullable final Context parentContext,
                                  final boolean allowIncludeInExcludeFrom,
                                  final int dataPosition,
                                  final int referencePosition,
                                  final boolean writeLocation,
                                  @Nullable final Condition<AnnotationBackedDescriptor> propertyFilter) {
    boolean staticChild = true;
    ByteRange dataRange = null;
    // if state specific property before includeIn, state override data range wil be added before object data range, so,
    // we keep current index and insert at the specified position
    final Marker dataRangeAfterAnchor = writer.getBlockOut().getLastMarker();

    if (writeLocation) {
      out.writeUInt29(writer.P_FUD_RANGE_ID);
      out.writeUInt29(rangeMarkers.size());
      rangeMarkers.add(document.createRangeMarker(tag.getTextOffset(), tag.getTextOffset() + tag.getTextLength()));
    }

    assert !tagAttributeProcessContext.isCssRulesetDefined();
    Context context = tagAttributeProcessContext;
    if (parentContext != null && parentContext.getScope().staticObjectPointToScope) {
      tagAttributeProcessContext.setTempParentScope(parentContext.getScope());
    }

    for (final XmlAttribute attribute : tag.getAttributes()) {
      if (attribute.getValueElement() == null) {
        // skip invalid - "<Button label/>"
        continue;
      }

      final XmlAttributeDescriptor attributeDescriptor = attribute.getDescriptor();
      final AnnotationBackedDescriptor descriptor;
      if (attributeDescriptor instanceof AnnotationBackedDescriptor) {
        descriptor = (AnnotationBackedDescriptor)attributeDescriptor;

        // id and includeIn/excludeFrom only as attribute, not as tag
        if (descriptor.isPredefined()) {
          if (descriptor.hasIdType()) {
            processObjectWithExplicitId(attribute.getValue(), context);
          }
          else if (allowIncludeInExcludeFrom) {
            String name = descriptor.getName();
            boolean excludeFrom = false;
            if (name.equals(FlexStateElementNames.INCLUDE_IN) || (excludeFrom = name.equals(FlexStateElementNames.EXCLUDE_FROM))) {
              if (context == tagAttributeProcessContext) {
                context = new DynamicObjectContext(tagAttributeProcessContext);
              }

              // must be before stateWriter.includeIn - start object data range before state data range
              dataRange = writer.getBlockOut().startRange(dataPosition, dataRangeAfterAnchor);
              ((DynamicObjectContext)context).setDataRange(dataRange);

              stateWriter.includeInOrExcludeFrom(attribute.getValueElement(), parentContext, (DynamicObjectContext)context, excludeFrom);
              staticChild = false;
            }
            else if (name.equals(FlexStateElementNames.ITEM_CREATION_POLICY)) {
              if (attribute.getValue().charAt(0) == 'i') {
                if (context == tagAttributeProcessContext) {
                  context = new DynamicObjectContext(tagAttributeProcessContext);
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
        else if (propertyFilter != null && !propertyFilter.value(descriptor)) {
          // skip
        }
        else if (MxmlUtil.isIdLanguageAttribute(attribute, descriptor)) {
          String explicitId = attribute.getValue();
          writer.idMxmlProperty(explicitId);
          processObjectWithExplicitId(explicitId, context);
        }
        else if (descriptor.getTypeName() == null) {
          //IDEA-73453
          // skip
          LOG.warn("Skip " + descriptor.getName() + " in " + tag.getText() + " due to IDEA-73453");
        }
        else if (hasStates && stateWriter.checkStateSpecificPropertyValue(this, propertyProcessor,
                                                                          valueProviderFactory.create(attribute), descriptor, context)) {
          // skip
        }
        else {
          writeAttributeBackedProperty(attribute, descriptor, context, context);
        }
      }
      else if (attributeDescriptor instanceof AnyXmlAttributeDescriptor) {
        writeAttributeBackedProperty(attribute, new AnyXmlAttributeDescriptorWrapper(attributeDescriptor), context, context);
      }
      else if (!attribute.isNamespaceDeclaration()) {
        LOG.warn("unknown attribute (" +
                 attribute.getText() +
                 ") descriptor: " +
                 (attributeDescriptor == null ? "null" : attributeDescriptor.toString()) +
                 " of tag " +
                 tag.getText());
      }
    }

    if (!hasStates) {
      assert context == tagAttributeProcessContext;
      context = writer.createStaticContext(parentContext, referencePosition);
      if (tagAttributeProcessContext.isCssRulesetDefined()) {
        context.markCssRulesetDefined();
      }
    }
    else if (context == tagAttributeProcessContext) {
      context = stateWriter.createContextForStaticBackSibling(allowIncludeInExcludeFrom, referencePosition, parentContext);
      stateWriter.finalizeStateSpecificAttributesForStaticContext((StaticObjectContext)context, parentContext, this);
      if (tagAttributeProcessContext.isCssRulesetDefined()) {
        context.markCssRulesetDefined();
      }
    }

    tagAttributeProcessContext.reset();

    processTagChildren(tag, context, parentContext, true, null);
    // initializeReference must be after process all elements - after sub tag also, due to <RadioButton id="visa" label="Visa"
    // width="150"><group>{cardtype} !!id (for binding target, RadioButton id="visa") allocation here!!</group></RadioButton>
    if (dataPosition != -1) {
      writer.endObject();
      if (dataRange != null) {
        writer.getBlockOut().endRange(dataRange);
      }
    }

    return staticChild;
  }

  void writeSimpleAttributeBackedProperty(XmlAttribute attribute,
                                          AnnotationBackedDescriptor descriptor,
                                          @NotNull MxmlObjectReferenceProvider mxmlObjectReferenceProvider) {
    writeAttributeBackedProperty(attribute, descriptor, mxmlObjectReferenceProvider, null);
  }

  // parentContext nullable only if simple attribute (@see writeSimpleAttributeBackedProperty)
  private void writeAttributeBackedProperty(XmlAttribute attribute,
                                            AnnotationBackedDescriptor descriptor,
                                            @NotNull MxmlObjectReferenceProvider mxmlObjectReferenceProvider,
                                            @Nullable Context parentContext) {
    final int beforePosition = out.size();
    final PropertyKind propertyKind = writeProperty(attribute, valueProviderFactory.create(attribute), descriptor, mxmlObjectReferenceProvider, parentContext);
    if (propertyKind != PropertyKind.IGNORE) {
      if (propertyKind.isComplex()) {
        writer.getBlockOut().setPosition(beforePosition);
        addProblem(attribute, "unknown.attribute.value.type", descriptor.getType());
      }
    }
  }

  boolean processTagChildren(final XmlTag tag, @NotNull final Context context, @Nullable final Context parentContext,
                          final boolean propertiesExpected, @Nullable PropertyKind propertyKind) {
    int lengthPosition = propertyKind != null && propertyKind.isList() ? out.allocateShort() : 0;
    int explicitContentOccurred = -1;
    int validAndStaticChildrenCount = 0;
    final XmlTagChild[] children = tag.getValue().getChildren();

    // if we process property tag value - if we cannot set value due to invalid content, so, we don't write property,
    // otherwise if there is no content, we write explicit null
    boolean invalidValue = false;

    for (XmlTagChild child : children) {
      if (child instanceof XmlTag) {
        XmlTag childTag = (XmlTag)child;
        XmlElementDescriptor descriptor = childTag.getDescriptor();
        if (descriptor == null) {
          LOG.warn("Descriptor is null, skip " + child);
          invalidValue = true;
          continue;
        }

        assert descriptor != null;
        if (descriptor instanceof ClassBackedElementDescriptor) {
          final ClassBackedElementDescriptor classBackedDescriptor = (ClassBackedElementDescriptor)descriptor;
          if (classBackedDescriptor.isPredefined()) {
            if (MxmlUtil.isObjectLanguageTag(tag)) {
              // IDEA-73482
              processPropertyTag(childTag, new AnyXmlAttributeDescriptorWrapper(descriptor), context);
            }
            else if (descriptor.getQualifiedName().equals(FlexPredefinedTagNames.DECLARATIONS)) {
              injectedASWriter.readDeclarations(this, childTag);
            }

            continue;
          }
          else if (MxmlUtil.isAbstract(classBackedDescriptor)) {
            addProblem(child, "abstract.class", classBackedDescriptor.getQualifiedName());
            continue;
          }
          
          if (explicitContentOccurred == 1) {
            LOG.warn("Default content already processed, skip " + child);
            continue;
          }

          if (propertiesExpected && explicitContentOccurred == -1) {
            explicitContentOccurred = 0;
            final PropertyKind defaultPropertyKind = processDefaultProperty(tag, valueProviderFactory.create(childTag), classBackedDescriptor,
                                                                            children.length, context);
            if (defaultPropertyKind == null) {
              continue;
            }
            else if (defaultPropertyKind.isList()) {
              lengthPosition = out.allocateShort();
              propertyKind = defaultPropertyKind;
            }
            else if (defaultPropertyKind == PropertyKind.PRIMITIVE) {
              validAndStaticChildrenCount++;
              continue;
            }
          }

          if (processClassBackedSubTag(childTag, classBackedDescriptor, context, propertyKind != null && propertyKind.isList())) {
            validAndStaticChildrenCount++;
          }
        }
        else if (propertiesExpected && descriptor instanceof AnnotationBackedDescriptor) {
          AnnotationBackedDescriptor annotationBackedDescriptor = (AnnotationBackedDescriptor)descriptor;
          // explicit content after contiguous child elements serving as the default property value
          // skip invalid, contiguous child elements already processed and explicit content (i.e. AnnotationBackedDescriptor, property childTag) was occurred
          if (explicitContentOccurred == 0) {
            explicitContentOccurred = 1;
            if (propertyKind != null && propertyKind.isList()) {
              endList(validAndStaticChildrenCount, lengthPosition);
            }
          }

          if (childTag.getNamespace().equals(MxmlJSClass.MXML_URI4) && childTag.getLocalName().equals(FlexStateElementNames.STATES)) {
            if (childTag.getSubTags().length != 0) {
              hasStates = true;
              assert parentContext == null;
              if (stateWriter == null) {
                stateWriter = new StateWriter(writer);
              }
              stateWriter.readDeclaration(childTag);
            }
          }
          else {
            processPropertyTag(childTag, annotationBackedDescriptor, context);
          }
        }
      }
      else if (child instanceof XmlText && !MxmlUtil.containsOnlyWhitespace(child)) {
        if (explicitContentOccurred == 1) {
          LOG.warn("Default content already processed, skip '" + child.getText().trim() + "'");
          continue;
        }

        if (context.getChildrenType() != null && !context.getChildrenType().equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
          LOG.warn("Illegal child type, skip '" + child.getText().trim() + "'");
          continue;
        }

        if (propertiesExpected && explicitContentOccurred == -1) {
          explicitContentOccurred = 0;
          final XmlElementValueProvider valueProvider = valueProviderFactory.create((XmlText)child);
          final PropertyKind defaultPropertyKind = processDefaultProperty(tag, valueProvider, null, children.length, context);
          if (defaultPropertyKind == PropertyKind.IGNORE) {
            explicitContentOccurred = -1;
            continue;
          }
          else if (defaultPropertyKind == null) {
            continue;
          }
          else if (defaultPropertyKind.isList()) {
            lengthPosition = out.allocateShort();
            propertyKind = defaultPropertyKind;
          }
          else if (defaultPropertyKind == PropertyKind.PRIMITIVE) {
            validAndStaticChildrenCount++;
            continue;
          }
          else {
            final ValueWriter valueWriter;
            try {
              valueWriter = propertyProcessor.processXmlTextAsDefaultPropertyWithComplexType(valueProvider, tag, context);
            }
            catch (InvalidPropertyException e) {
              // we don't need any out rollback - nothing is written yet
              problemsHolder.add(e);
              continue;
            }

            if (valueWriter == null) {
              throw new IllegalArgumentException("unexpected default property kind " + defaultPropertyKind);
            }
            else if (valueWriter == InjectedASWriter.IGNORE) {
              continue;
            }
          }
        }
        else if (propertyKind == PropertyKind.VECTOR) {
          LOG.warn("skip " + child + " due to IDEA-73478");
          // IDEA-73478, XmlText allowed only for fx:Array, but not for fx:Vector (even with type String)
          break;
        }

        if (propertyKind != null && propertyKind == PropertyKind.COMPLEX) {
          invalidValue = true;
          LOG.warn("Text is not expected" + child);
        }
        else {
          writer.string(((XmlText)child).getValue());
          validAndStaticChildrenCount++;
        }
      }
    }

    if (propertyKind != null && propertyKind.isList()) {
      endList(validAndStaticChildrenCount, lengthPosition);
    }
    else if (!propertiesExpected && validAndStaticChildrenCount == 0) {
      if (invalidValue) {
        return false;
      }

      // PropertyAsTagWithCommentedValueAsTag, replace Amf3Types.OBJECT to Amf3Types.NULL
      out.putByte(Amf3Types.NULL, out.size() - 1);
    }

    return true;
  }

  private void processPropertyTag(XmlTag tag, AnnotationBackedDescriptor annotationBackedDescriptor, @NotNull Context parentContext) {
    if (hasStates &&
        stateWriter.checkStateSpecificPropertyValue(this, propertyProcessor, valueProviderFactory.create(tag), annotationBackedDescriptor,
                                                    parentContext)) {
      return;
    }
    final int beforePosition = writer.getBlockOut().size();
    final PropertyKind propertyKind = writeProperty(tag, valueProviderFactory.create(tag), annotationBackedDescriptor, parentContext, parentContext);
    if (propertyKind.isComplex()) {
      if (!processPropertyTagValue(annotationBackedDescriptor, tag, parentContext, propertyKind)) {
        writer.getBlockOut().setPosition(beforePosition);
      }
    }
  }

  private void endList(int validChildrenCount, int lengthPosition) {
    assert validChildrenCount < 65535;
    out.putShort(validChildrenCount, lengthPosition);
  }

  // process tag value, opposite to processTagChildren expects only ClassBackedSubTag or XmlText (attributes already processed or isn't expected)
  boolean processPropertyTagValue(@Nullable AnnotationBackedDescriptor descriptor, @NotNull XmlTag tag, @NotNull Context parentContext, @NotNull PropertyKind propertyKind) {
    PropertyKind listKind = propertyKind.isList() ? propertyKind : null;
    if (listKind != null && descriptor != null) {
      parentContext.processingPropertyName = descriptor.getName();
    }
    boolean result = processTagChildren(tag, parentContext, null, false, propertyKind);
    parentContext.processingPropertyName = null;
    return result;
  }

  private boolean processClassBackedSubTag(final XmlTag tag, final ClassBackedElementDescriptor descriptor, @Nullable final Context parentContext,
                                           final boolean isListItem) {
    final boolean allowIncludeInExcludeFrom = hasStates && isListItem && parentContext != null;
    final Trinity<Integer,String,Condition<AnnotationBackedDescriptor>> effectiveClassInfo;
    final String effectiveClassName;
    try {
      if (propertyProcessor.writeTagIfFxOrFxg(tag, descriptor, parentContext, allowIncludeInExcludeFrom, out)) {
        return true;
      }

      effectiveClassInfo = MxmlUtil.computeEffectiveClass(tag, descriptor.getDeclaration(), projectComponentReferenceCounter, true);
      if ("mx.core.UIComponent".equals(effectiveClassInfo.second)) {
        effectiveClassName = MxmlUtil.UNKNOWN_COMPONENT_CLASS_NAME;
      }
      else {
        effectiveClassName = effectiveClassInfo.second;
      }
    }
    catch (InvalidPropertyException e) {
      problemsHolder.add(e);
      return false;
    }

    final int childDataPosition = out.size();
    if (effectiveClassInfo.first == -1) {
      if (isListItem) {
        out.write(Amf3Types.OBJECT);
      }

      writer.classOrPropertyName(effectiveClassName == null ? descriptor.getQualifiedName() : effectiveClassName);
    }
    else {
      if (!isListItem) {
        // replace Amf3Types.OBJECT to AmfExtendedTypes.DOCUMENT_REFERENCE
        writer.getBlockOut().setPosition(writer.getBlockOut().size() - 1);
      }

      writer.documentReference(effectiveClassInfo.first);
    }

    return processElements(tag, parentContext, allowIncludeInExcludeFrom, childDataPosition, out.allocateClearShort(),
                           JSResolveUtil.isAssignableType(FlexCommonTypeNames.IVISUAL_ELEMENT, descriptor.getQualifiedName(),
                                                          descriptor.getDeclaration()) ||
                           JSResolveUtil.isAssignableType(FlexCommonTypeNames.FLASH_DISPLAY_OBJECT, descriptor.getQualifiedName(),
                                                          descriptor.getDeclaration()), effectiveClassInfo.third);
  }

  boolean processMxmlVector(XmlTag tag, @Nullable Context parentContext, boolean allowIncludeInExcludeFrom) {
    final XmlAttribute typeAttribute = tag.getAttribute("type");
    final String type;
    if (typeAttribute == null || StringUtil.isEmpty((type = typeAttribute.getDisplayValue()))) {
      LOG.warn("Skip " + tag + ", attribute type must be specified");
      return false;
    }

    final XmlAttribute fixedAttribute = tag.getAttribute("fixed");
    out.write(AmfExtendedTypes.MXML_VECTOR);
    writer.classOrPropertyName(type);
    String displayValue = fixedAttribute == null ? null : StringUtil.nullize(fixedAttribute.getDisplayValue());
    out.write(displayValue != null && displayValue.charAt(0) == 't');
    processTagChildren(tag, processIdAttributeOfFxTag(tag, parentContext, allowIncludeInExcludeFrom), parentContext, false, PropertyKind.VECTOR);
    return true;
  }

  StaticObjectContext processIdAttributeOfFxTag(XmlTag tag, @Nullable Context parentContext, boolean allowIncludeInExcludeFrom) {
    final StaticObjectContext context;
    final int referencePosition = out.allocateClearShort();
    if (hasStates) {
      context = stateWriter.createContextForStaticBackSibling(allowIncludeInExcludeFrom, referencePosition, parentContext);
    }
    else {
      context = writer.createStaticContext(parentContext, referencePosition);
    }

    XmlAttribute idAttribute = tag.getAttribute("id");
    if (idAttribute != null) {
      String id = idAttribute.getDisplayValue();
      if (StringUtil.isEmpty(id)) {
        LOG.warn("Skip id attribute of " + tag + ", id is empty");
      }
      else {
        // IDEA-73516
        //noinspection ConstantConditions
        injectedASWriter.putMxmlObjectReference(id, context.getMxmlObjectReference());
      }
    }

    return context;
  }

  void processDeclarations(XmlTag parent) {
    final int lengthPosition = out.allocateShort();
    int validChildrenCount = 0;
    for (XmlTag tag : parent.getSubTags()) {
      ClassBackedElementDescriptor descriptor = (ClassBackedElementDescriptor)tag.getDescriptor();
      assert descriptor != null;
      if (descriptor.isPredefined()) {
        // todo IDEA-72123
        if (descriptor.getName().equals(FlexPredefinedTagNames.MODEL)) {
          if (propertyProcessor.processFxModel(tag)) {
            validChildrenCount++;
          }
        }
        continue;
      }
      else if (MxmlUtil.isComponentLanguageTag(tag)) {
        if (propertyProcessor.processFxComponent(tag, false)) {
          validChildrenCount++;
        }

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
           ActionScriptClassResolver.isParentClass(jsClass, FlexCommonTypeNames.ACCORDION) ||
           ActionScriptClassResolver.isParentClass(jsClass, FlexCommonTypeNames.VIEW_STACK);
  }

  private void addProblem(XmlElement xmlElement, @PropertyKey(resourceBundle = FlashUIDesignerBundle.BUNDLE) String key, Object... params) {
    problemsHolder.add(xmlElement, FlashUIDesignerBundle.message(key, params));
  }

  // descriptor will be null if child is XmlText
  @Nullable
  private PropertyKind processDefaultProperty(XmlTag parentTag, XmlElementValueProvider valueProvider,
                                              @Nullable ClassBackedElementDescriptor descriptor, int childrenLength, @NotNull Context context) {
    final ClassBackedElementDescriptor parentDescriptor = (ClassBackedElementDescriptor)parentTag.getDescriptor();
    assert parentDescriptor != null;

    final AnnotationBackedDescriptor defaultDescriptor = parentDescriptor.getDefaultPropertyDescriptor();
    final boolean isXmlText = descriptor == null;
    if (defaultDescriptor == null) {
      final String className = parentDescriptor.getQualifiedName();
      final JSClass jsClass;
      if (parentDescriptor instanceof MxmlBackedElementDescriptor) {
        jsClass = (JSClass)ActionScriptClassResolver.findClassByQNameStatic(className, parentTag);
      }
      else {
        jsClass = (JSClass)parentDescriptor.getDeclaration();
      }

      final boolean isDirectContainerImpl = className.equals(FlexCommonTypeNames.ICONTAINER);
      if (isDirectContainerImpl || ActionScriptClassResolver.isParentClass(jsClass, FlexCommonTypeNames.ICONTAINER)) {
        if (isXmlText) {
          addProblem(parentTag, "initializer.cannot.be.represented.in.text", parentTag.getLocalName());
          return null;
        }

        if (!isDirectContainerImpl && isHaloNavigator(className, jsClass) &&
            !ActionScriptClassResolver.isParentClass((JSClass)descriptor.getDeclaration(), FlexCommonTypeNames.INAVIGATOR_CONTENT)) {
          addProblem(parentTag, "children.must.be", parentTag.getLocalName(), FlexCommonTypeNames.INAVIGATOR_CONTENT);
          return null;
        }

        writer.classOrPropertyName("0");
        out.write(AmfExtendedTypes.MX_CONTAINER_CHILDREN);
        return PropertyKind.ARRAY;
      }
      else {
        // http://youtrack.jetbrains.net/issue/IDEA-66565
        addProblem(parentTag, "default.property.not.found", parentTag.getLocalName());
      }
    }
    else {
      // xmlText as default property with injection, see BindingToDeferredInstanceFromBytesBase
      if (isXmlText &&
          writeXmlTextAsDefaultPropertyInjectedValue(parentTag, valueProvider, defaultDescriptor, context)) {
        return null;
      }

      if (defaultDescriptor.getType().equals(JSCommonTypeNames.ARRAY_CLASS_NAME) && defaultDescriptor.getArrayType() != null) {
        final String elementType = defaultDescriptor.getArrayType();
        context.setChildrenType(elementType);
        final boolean isString = elementType.equals(JSCommonTypeNames.STRING_CLASS_NAME);
        if (isString) {
          if (descriptor != null && !descriptor.getQualifiedName().equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
            addProblem(parentTag, "children.must.be", parentTag.getLocalName(), elementType);
            return null;
          }
        }
        else {
          if (isXmlText) {
            addProblem(parentTag, "initializer.cannot.be.represented.in.text", parentTag.getLocalName());
            return PropertyKind.IGNORE;
          }
        }
      }

      writer.classOrPropertyName(defaultDescriptor.getName());
      if (defaultDescriptor.isDeferredInstance()) {
        writer.newInstance("com.intellij.flex.uiDesigner.flex.DeferredInstanceFromArray", 1, false).typeMarker(Amf3Types.ARRAY);
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

          writer.vectorHeader(defaultDescriptor.getArrayType());
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
      writer.stringReference(XmlElementValueProvider.EMPTY);
    }
    else {
      writer.string(value);
    }
  }

  private void defineInlineCssRuleset(@NotNull PsiElement element) {
    int textOffset = element.getTextOffset();
    out.writeUInt29(document.getLineNumber(textOffset) + 1);
    out.writeUInt29(textOffset);
  }

  private void processObjectWithExplicitId(String explicitId, @NotNull Context context) {
    injectedASWriter.putMxmlObjectReference(explicitId, context.getMxmlObjectReference());
  }

  private PropertyKind writeProperty(XmlElement element, XmlElementValueProvider valueProvider,
                                     AnnotationBackedDescriptor descriptor,
                                     @NotNull MxmlObjectReferenceProvider objectReferenceProvider, @Nullable Context parentContext) {
    final int beforePosition = writer.getBlockOut().size();
    try {
      ValueWriter valueWriter = propertyProcessor.process(element, valueProvider, descriptor, objectReferenceProvider);
      if (valueWriter == null) {
        return PropertyKind.IGNORE;
      }

      if (parentContext != null) {
        writePropertyHeader(propertyProcessor.getName(), element, parentContext, propertyProcessor.isStyle());
      }
      else {
        writer.property(propertyProcessor.getName());
      }
      // parentContext required only for process tag children, for attribute it can be null
      return valueWriter.write(descriptor, valueProvider, out, writer, propertyProcessor.isStyle(), parentContext);
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

  private void writePropertyHeader(String name, XmlElement element, @NotNull Context context, boolean isStyle) {
    writer.classOrPropertyName(name);
    if (isStyle) {
      out.write(AmfExtendedTypes.STYLE);
      if (!context.isCssRulesetDefined()) {
        defineInlineCssRuleset(element);
        context.markCssRulesetDefined();
      }
    }
  }

  private boolean writeXmlTextAsDefaultPropertyInjectedValue(XmlElement element, XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor, @NotNull Context context) {
    final int beforePosition = writer.getBlockOut().size();
    try {
      ValueWriter valueWriter = propertyProcessor.processInjected(valueProvider, descriptor, descriptor.isStyle(), context);
      if (valueWriter == null) {
        return false;
      }

      if (valueWriter != InjectedASWriter.IGNORE) {
        writePropertyHeader(descriptor.getName(), element, context, descriptor.isStyle());
        valueWriter.write(descriptor, valueProvider, out, writer, propertyProcessor.isStyle(), context);
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

  void setDeferredReferenceForObjectWithExplicitIdOrBinding(StaticInstanceReferenceInDeferredParentInstance staticReferenceInDeferredParentInstance, int referenceInstance) {
    assert tagAttributeProcessContext.mxmlObjectReference.id == referenceInstance;
    tagAttributeProcessContext.mxmlObjectReference.staticReferenceInDeferredParentInstance = staticReferenceInDeferredParentInstance;
    tagAttributeProcessContext.mxmlObjectReference = null;
  }
}