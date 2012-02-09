package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.ByteRange;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
  static final Logger LOG = Logger.getInstance(MxmlWriter.class.getName());

  private final PrimitiveAmfOutputStream out;

  private final BaseWriter writer;
  private StateWriter stateWriter;
  private final InjectedASWriter injectedASWriter;
  private final PropertyProcessor propertyProcessor;

  private boolean hasStates;

  private final MxmlObjectReferenceProviderImpl tagAttributeProcessContext;

  final ProblemsHolder problemsHolder;

  final ValueProviderFactory valueProviderFactory = new ValueProviderFactory();
  private final List<RangeMarker> rangeMarkers = new ArrayList<RangeMarker>();

  final ProjectDocumentReferenceCounter projectComponentReferenceCounter = new ProjectDocumentReferenceCounter();

  Document document;

  public MxmlWriter(PrimitiveAmfOutputStream out, ProblemsHolder problemsHolder, AssetCounter assetCounter) {
    this.out = out;
    this.problemsHolder = problemsHolder;

    writer = new BaseWriter(out, assetCounter);
    injectedASWriter = new InjectedASWriter(writer, problemsHolder);
    propertyProcessor = new PropertyProcessor(injectedASWriter, writer, this);
    tagAttributeProcessContext = new MxmlObjectReferenceProviderImpl(writer);
  }

  @Nullable
  public Pair<ProjectDocumentReferenceCounter, List<RangeMarker>> write(XmlFile psiFile) throws IOException {
    final AccessToken token = ReadAction.start();
    try {
      final VirtualFile virtualFile = psiFile.getVirtualFile();
      assert virtualFile != null;
      problemsHolder.setCurrentFile(virtualFile);

      document = FileDocumentManager.getInstance().getDocument(virtualFile);

      XmlTag rootTag = psiFile.getRootTag();
      assert rootTag != null;
      ClassBackedElementDescriptor rootTagDescriptor = (ClassBackedElementDescriptor)rootTag.getDescriptor();
      assert rootTagDescriptor != null;

      final int projectComponentFactoryId;
      try {
        projectComponentFactoryId = InjectionUtil.getProjectComponentFactoryId(rootTagDescriptor.getQualifiedName(),
                                                                               rootTagDescriptor.getDeclaration(),
                                                                               projectComponentReferenceCounter);
      }
      catch (InvalidPropertyException e) {
        problemsHolder.add(e);
        return null;
      }

      if (projectComponentFactoryId == -1) {
        out.write(Amf3Types.OBJECT);
        writer.mxmlObjectHeader(rootTagDescriptor.getQualifiedName());
      }
      else {
        writer.documentReference(projectComponentFactoryId);
        out.allocateClearShort();
      }

      processElements(rootTag, null, false, -1, out.size() - 2, true, null);

      writer.endObject();

      if (stateWriter != null) {
        stateWriter.write();
        hasStates = false;
      }
      else {
        out.write(0);
      }

      injectedASWriter.write();
      writer.endMessage(projectComponentReferenceCounter);
      return new Pair<ProjectDocumentReferenceCounter, List<RangeMarker>>(projectComponentReferenceCounter, rangeMarkers);
    }
    finally {
      token.finish();
      problemsHolder.setCurrentFile(null);
      writer.resetAfterMessage();
    }
  }

  private boolean processElements(final XmlTag parent,
                                  final @Nullable Context parentContext,
                                  final boolean allowIncludeInExludeFrom,
                                  final int dataPosition,
                                  final int referencePosition,
                                  final boolean writeLocation,
                                  final @Nullable Condition<AnnotationBackedDescriptor> propertyFilter) {
    boolean cssRulesetDefined = false;
    boolean staticChild = true;

    Context context = null;
    ByteRange dataRange = null;
    // if state specific property before includeIn, state override data range wil be added before object data range, so,
    // we keep current index and insert at the specified position
    final int dataRangeIndex = writer.getBlockOut().getNextMarkerIndex();

    if (writeLocation) {
      out.writeUInt29(writer.P_FUD_RANGE_ID);
      out.writeUInt29(rangeMarkers.size());
      rangeMarkers.add(document.createRangeMarker(parent.getTextOffset(), parent.getTextOffset() + parent.getTextLength()));
    }

    for (final XmlAttribute attribute : parent.getAttributes()) {
      final XmlAttributeDescriptor attributeDescriptor = attribute.getDescriptor();
      final AnnotationBackedDescriptor descriptor;
      if (attributeDescriptor instanceof AnnotationBackedDescriptor) {
        descriptor = (AnnotationBackedDescriptor)attributeDescriptor;

        // id and includeIn/excludeFrom only as attribute, not as tag
        if (descriptor.isPredefined()) {
          if (descriptor.hasIdType()) {
            processObjectWithExplicitId(attribute.getValue(), context);
          }
          else if (allowIncludeInExludeFrom) {
            String name = descriptor.getName();
            boolean excludeFrom = false;
            if (name.equals(FlexStateElementNames.INCLUDE_IN) || (excludeFrom = name.equals(FlexStateElementNames.EXCLUDE_FROM))) {
              if (context == null) {
                context = writer.createDynamicObjectContext(tagAttributeProcessContext.reference);
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
                  context = writer.createDynamicObjectContext(tagAttributeProcessContext.reference);
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
        else if (descriptor.hasIdType() && MxmlUtil.isIdLanguageIdAttribute(attribute)) {
          String explicitId = attribute.getValue();
          writer.idMxmlProperty(explicitId);
          processObjectWithExplicitId(explicitId, context);
        }
        else if (descriptor.getTypeName() == null) {
          //IDEA-73453
          // skip
          LOG.warn("Skip " + descriptor.getName() + " in " + parent.getText() + " due to IDEA-73453");
        }
        //else if (descriptor.getTypeName().equals(FlexAnnotationNames.EVENT) ||
        //         descriptor.getTypeName().equals(FlexAnnotationNames.BINDABLE)) {
        //  // skip
        //}
        else if (hasStates && stateWriter.checkStateSpecificPropertyValue(this, propertyProcessor, attribute,
                                                                          valueProviderFactory.create(attribute),
                                                                          descriptor, context, tagAttributeProcessContext
          .getEffectiveObjectReferenceProvider(context))) {
          // skip
        }
        else {
          cssRulesetDefined =
            writeAttributeBackedProperty(attribute, descriptor, tagAttributeProcessContext.getEffectiveObjectReferenceProvider(context),
                                         context, cssRulesetDefined, true);
        }
      }
      else if (attributeDescriptor instanceof AnyXmlAttributeDescriptor) {
        writeAttributeBackedProperty(attribute, new AnyXmlAttributeDescriptorWrapper(attributeDescriptor),
                                     tagAttributeProcessContext.getEffectiveObjectReferenceProvider(context),
                                     context, false, true);
      }
      else if (!attribute.isNamespaceDeclaration()) {
        LOG.warn("unknown attribute (" +
                 attribute.getText() +
                 ") descriptor: " +
                 (attributeDescriptor == null ? "null" : attributeDescriptor.toString()) +
                 " of tag " +
                 parent.getText());
      }
    }

    if (hasStates) {
      if (context == null) {
        context = stateWriter.createContextForStaticBackSibling(allowIncludeInExludeFrom, referencePosition, parentContext);
        stateWriter.finalizeStateSpecificAttributesForStaticContext((StaticObjectContext)context, parentContext, this);
      }
    }
    else {
      assert context == null;
      context = writer.createStaticContext(parentContext, referencePosition);
    }

    tagAttributeProcessContext.reference = null;
    writer.resetPreallocatedId();

    processTagChildren(parent, context, parentContext, true, null, cssRulesetDefined);
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

  void writeSimpleAttributeBackedProperty(XmlAttribute attribute, AnnotationBackedDescriptor descriptor,
                                             @NotNull MxmlObjectReferenceProvider mxmlObjectReferenceProvider) {
    writeAttributeBackedProperty(attribute, descriptor, mxmlObjectReferenceProvider, null, false, false);
  }

  private boolean writeAttributeBackedProperty(XmlAttribute attribute, AnnotationBackedDescriptor descriptor,
                                               @NotNull MxmlObjectReferenceProvider mxmlObjectReferenceProvider,
                                               @Nullable Context parentContext,
                                               boolean cssRulesetDefined, boolean isMxmlProperty) {
    final int beforePosition = out.size();
    final PropertyKind propertyKind = writeProperty(attribute, valueProviderFactory.create(attribute), descriptor,
                                                    mxmlObjectReferenceProvider, parentContext, cssRulesetDefined,
                                                    isMxmlProperty);
    if (propertyKind != PropertyKind.IGNORE) {
      if (propertyProcessor.isStyle()) {
        cssRulesetDefined = true;
      }
      if (propertyKind.isComplex()) {
        writer.getBlockOut().setPosition(beforePosition);
        addProblem(attribute, "error.unknown.attribute.value.type", descriptor.getType());
      }
    }
    return cssRulesetDefined;
  }

  void processTagChildren(final XmlTag parent, final @NotNull Context context, final @Nullable Context parentContext,
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
          final ClassBackedElementDescriptor classBackedDescriptor = (ClassBackedElementDescriptor)descriptor;
          if (classBackedDescriptor.isPredefined()) {
            if (parent.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) &&
                parent.getLocalName().equals(JSCommonTypeNames.OBJECT_CLASS_NAME)) {
              // IDEA-73482
              processPropertyTag(tag, new AnyXmlAttributeDescriptorWrapper(descriptor), context, false);
            }
            else if (descriptor.getQualifiedName().equals(FlexPredefinedTagNames.DECLARATIONS)) {
              injectedASWriter.readDeclarations(this, tag);
            }

            continue;
          }
          else if (MxmlUtil.isAbstract(classBackedDescriptor)) {
            addProblem(child, "error.abstract.class", classBackedDescriptor.getQualifiedName());
            continue;
          }
          
          if (explicitContentOccured == 1) {
            LOG.warn("Default content already processed, skip " + child);
            continue;
          }

          if (propertiesExpected && explicitContentOccured == -1) {
            explicitContentOccured = 0;
            final PropertyKind defaultPropertyKind = processDefaultProperty(parent, valueProviderFactory.create(tag), classBackedDescriptor,
                                                                            children.length, context, cssRulesetDefined);
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
          else {
            cssRulesetDefined = processPropertyTag(tag, annotationBackedDescriptor, context, cssRulesetDefined);
          }
        }
      }
      else if (child instanceof XmlText && !MxmlUtil.containsOnlyWhitespace(child)) {
        if (explicitContentOccured == 1) {
          LOG.warn("Default content already processed, skip '" + child.getText().trim() + "'");
          continue;
        }

        if (context.getChildrenType() != null && !context.getChildrenType().equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
          LOG.warn("Illegal child type, skip '" + child.getText().trim() + "'");
          continue;
        }

        if (propertiesExpected && explicitContentOccured == -1) {
          explicitContentOccured = 0;
          final XmlElementValueProvider valueProvider = valueProviderFactory.create((XmlText)child);
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
          }
        }
        else if (listKind == PropertyKind.VECTOR) {
          LOG.warn("skip " + child + " due to IDEA-73478");
          // IDEA-73478, XmlText allowed only for fx:Array, but not for fx:Vector (even with type String)
          break;
        }

        writer.string(((XmlText)child).getValue());
        validAndStaticChildrenCount++;
      }
    }

    if (listKind != null) {
      endList(listKind, validAndStaticChildrenCount, lengthPosition);
    }
    else if (!propertiesExpected && validAndStaticChildrenCount == 0) {
      // PropertyAsTagWithCommentedValueAsTag, replace Amf3Types.OBJECT to Amf3Types.NULL
      out.putByte(Amf3Types.NULL, out.size() - 1);
    }
  }

  private boolean processPropertyTag(XmlTag tag, AnnotationBackedDescriptor annotationBackedDescriptor, @NotNull Context context,
                                  boolean cssRulesetDefined) {
    if (hasStates && stateWriter.checkStateSpecificPropertyValue(this, propertyProcessor, tag, valueProviderFactory.create(tag),
                                                                 annotationBackedDescriptor, context, context)) {
      return cssRulesetDefined;
    }
    final PropertyKind propertyKind = writeProperty(tag, valueProviderFactory.create(tag), annotationBackedDescriptor,
                                                    context, context, cssRulesetDefined, true);
    if (propertyKind != PropertyKind.IGNORE) {
      if (propertyProcessor.isStyle()) {
        cssRulesetDefined = true;
      }

      if (propertyKind.isComplex()) {
        processPropertyTagValue(tag, context, propertyKind);
      }
    }

    return cssRulesetDefined;
  }

  private void endList(@Nullable PropertyKind listKind, int validChildrenCount, int lengthPosition) {
    if (listKind != null) {
      assert validChildrenCount < 65535;
      out.putShort(validChildrenCount, lengthPosition);
    }
  }

  // process tag value, opposite to processTagChildren expects only ClassBackedSubTag or XmlText (attributes already processed or isn't expected)
  void processPropertyTagValue(final XmlTag parent, final @NotNull Context parentContext, @Nullable final PropertyKind propertyKind) {
    processTagChildren(parent, parentContext, null, false, propertyKind != null && propertyKind.isList() ? propertyKind : null, false);
  }

  private boolean processClassBackedSubTag(final XmlTag tag, final ClassBackedElementDescriptor descriptor, @Nullable final Context parentContext,
                                           final boolean isListItem) {
    final boolean allowIncludeInExludeFrom = hasStates && isListItem && parentContext != null;
    final Trinity<Integer,String,Condition<AnnotationBackedDescriptor>> effectiveClassInfo;
    try {
      if (propertyProcessor.writeTagIfFx(tag, descriptor.getQualifiedName(), out, parentContext, allowIncludeInExludeFrom)) {
        return true;
      }

      effectiveClassInfo = MxmlUtil.computeEffectiveClass(tag, descriptor.getDeclaration(), projectComponentReferenceCounter, true);
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

      writer.classOrPropertyName(effectiveClassInfo.second == null ? descriptor.getQualifiedName() : effectiveClassInfo.second);
    }
    else {
      if (!isListItem) {
        // replace Amf3Types.OBJECT to AmfExtendedTypes.DOCUMENT_REFERENCE
        writer.getBlockOut().setPosition(writer.getBlockOut().size() - 1);
      }

      writer.documentReference(effectiveClassInfo.first);
    }

    return processElements(tag, parentContext, allowIncludeInExludeFrom, childDataPosition, out.allocateClearShort(),
                           JSResolveUtil.isAssignableType(FlexCommonTypeNames.IVISUAL_ELEMENT, descriptor.getQualifiedName(),
                                                          descriptor.getDeclaration()) ||
                           JSResolveUtil.isAssignableType(FlexCommonTypeNames.FLASH_DISPLAY_OBJECT, descriptor.getQualifiedName(),
                                                          descriptor.getDeclaration()), effectiveClassInfo.third);
  }

  boolean processMxmlVector(XmlTag tag, @Nullable Context parentContext, boolean allowIncludeInExludeFrom) {
    final XmlAttribute typeAttribute = tag.getAttribute("type");
    final String type;
    if (typeAttribute == null || StringUtil.isEmpty((type = typeAttribute.getDisplayValue()))) {
      LOG.warn("Skip " + tag + ", attribute type must be specified");
      return false;
    }

    final XmlAttribute fixedAttribute = tag.getAttribute("fixed");
    out.write(AmfExtendedTypes.MXML_VECTOR);
    writer.classOrPropertyName(type);
    out.write(fixedAttribute != null && fixedAttribute.getDisplayValue().charAt(0) == 't');
    processTagChildren(tag, processIdAttributeOfFxTag(tag, parentContext, allowIncludeInExludeFrom), parentContext, false, PropertyKind.VECTOR, false);
    return true;
  }

  StaticObjectContext processIdAttributeOfFxTag(XmlTag tag, @Nullable Context parentContext, boolean allowIncludeInExludeFrom) {
    final StaticObjectContext context;
    final int referencePosition = out.allocateClearShort();
    if (hasStates) {
      context = stateWriter.createContextForStaticBackSibling(allowIncludeInExludeFrom, referencePosition, parentContext);
    }
    else {
      context = writer.createStaticContext(parentContext, referencePosition);
    }

    final XmlAttribute idAttribute = tag.getAttribute("id");
    if (idAttribute != null) {
      final String id = idAttribute.getDisplayValue();
      if (StringUtil.isEmpty(id)) {
        LOG.warn("Skip id attribute of " + tag + ", id is empty");
      }
      else {
        // IDEA-73516
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

  private void addProblem(XmlElement xmlElement, @PropertyKey(resourceBundle = FlashUIDesignerBundle.BUNDLE) String key, Object... params) {
    problemsHolder.add(xmlElement, FlashUIDesignerBundle.message(key, params));
  }

  // descriptor will be null if child is XmlText
  @Nullable
  private PropertyKind processDefaultProperty(XmlTag parentTag, XmlElementValueProvider valueProvider,
                                              @Nullable ClassBackedElementDescriptor descriptor, int childrenLength, Context context,
                                              boolean cssRulesetDefined) {
    final ClassBackedElementDescriptor parentDescriptor = (ClassBackedElementDescriptor)parentTag.getDescriptor();
    assert parentDescriptor != null;

    final AnnotationBackedDescriptor defaultDescriptor = parentDescriptor.getDefaultPropertyDescriptor();
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

        writer.classOrPropertyName("0");
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
        context.setChildrenType(elementType);
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

      writer.classOrPropertyName(defaultDescriptor.getName());
      out.write(PropertyClassifier.PROPERTY);
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

   private static class MxmlObjectReferenceProviderImpl implements MxmlObjectReferenceProvider {
     private MxmlObjectReference reference;
     private final BaseWriter writer;

     public MxmlObjectReferenceProviderImpl(BaseWriter writer) {
       this.writer = writer;
     }

     private MxmlObjectReference createObjectReferenceForExplicit(@Nullable Context context) {
       if (context != null) {
         return context.getMxmlObjectReference();
       }

       if (reference == null) {
        reference = new MxmlObjectReference(writer.preallocateIdIfNeed());
       }
       
       return reference;
     }

     private MxmlObjectReferenceProvider getEffectiveObjectReferenceProvider(@Nullable Context context) {
       return context == null ? this : context;
     }

    @Override
    public MxmlObjectReference getMxmlObjectReference() {
      if (reference == null) {
        reference = new MxmlObjectReference(writer.preallocateIdIfNeed());
      }

      return reference;
    }
  }

  private void processObjectWithExplicitId(String explicitId, Context context) {
    injectedASWriter.putMxmlObjectReference(explicitId, tagAttributeProcessContext.createObjectReferenceForExplicit(context));
  }

  private PropertyKind writeProperty(XmlElement element, XmlElementValueProvider valueProvider,
                                     AnnotationBackedDescriptor descriptor,
                                     @NotNull MxmlObjectReferenceProvider objectReferenceProvider, @Nullable Context parentContext,
                                     boolean cssRulesetDefined, boolean isMxmlProperty) {
    final int beforePosition = writer.getBlockOut().size();
    try {
      ValueWriter valueWriter = propertyProcessor.process(element, valueProvider, descriptor, objectReferenceProvider);
      if (valueWriter == null) {
        return PropertyKind.IGNORE;
      }

      if (isMxmlProperty) {
        writePropertyHeader(propertyProcessor.getName(), element, cssRulesetDefined, propertyProcessor.isStyle());
      }
      else {
        writer.property(propertyProcessor.getName());
      }
      // parentContext required only for process tag chilrent, for attribute it can be null
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

  private void writePropertyHeader(String name, XmlElement element, boolean cssRulesetDefined, boolean isStyle) {
    writer.classOrPropertyName(name);
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
                              boolean cssRulesetDefined, @NotNull Context context) {
    final int beforePosition = writer.getBlockOut().size();
    try {
      ValueWriter valueWriter = propertyProcessor.processInjected(valueProvider, descriptor, descriptor.isStyle(), context);
      if (valueWriter == null) {
        return false;
      }

      if (valueWriter != InjectedASWriter.IGNORE) {
        writePropertyHeader(descriptor.getName(), element, cssRulesetDefined, descriptor.isStyle());
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
    assert tagAttributeProcessContext.reference.id == referenceInstance;
    tagAttributeProcessContext.reference.staticReferenceInDeferredParentInstance = staticReferenceInDeferredParentInstance;

    tagAttributeProcessContext.reference = null;
  }
}