package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.FlexUIDesignerBundle;
import com.intellij.flex.uiDesigner.InjectionUtil;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.ProblemsHolder;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.ByteRange;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.*;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.flex.uiDesigner.mxml.PropertyProcessor.IGNORE;
import static com.intellij.flex.uiDesigner.mxml.PropertyProcessor.PRIMITIVE;

public class MxmlWriter {
  private static final Pattern FLEX_SDK_ABSTRACT_CLASSES = Pattern.compile("^(mx|spark)\\.(.*)?Base$");

  static final int EMPTY_CLASS_OR_PROPERTY_NAME = 0;

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
    propertyProcessor = new PropertyProcessor(injectedASWriter, writer);
  }

  public XmlFile[] write(@NotNull final XmlFile psiFile, @NotNull final ProblemsHolder problemsHolder) throws IOException {
    try {
      this.problemsHolder = problemsHolder;
      problemsHolder.setCurrentFile(psiFile.getVirtualFile());
      injectedASWriter.setProblemsHolder(problemsHolder);
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

      out.write(EMPTY_CLASS_OR_PROPERTY_NAME);

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

  private void processElements(final XmlTag parent, final @Nullable Context parentContext, final boolean allowIncludeInExludeFrom,
                               int dataPosition, final int referencePosition) {
    boolean cssDeclarationSourceDefined = false;

    Context context = null;
    ByteRange dataRange = null;
    // if state specific property before includeIn, state override data range wil be added before object data range, so,
    // we keep current index and insert at the specified position
    final int dataRangeIndex = out.getBlockOut().getNextMarkerIndex();

    out.writeUInt29(writer.P_FUD_POSITION);
    out.writeUInt29(parent.getTextOffset());

    for (final XmlAttribute attribute : parent.getAttributes()) {
      XmlAttributeDescriptor attributeDescriptor = attribute.getDescriptor();
      if (attributeDescriptor instanceof AnnotationBackedDescriptor) {
        AnnotationBackedDescriptor descriptor = (AnnotationBackedDescriptor)attributeDescriptor;
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

              //out.getBlockOut().moveBack(dataPosition, 1);

              // must be before stateWriter.includeIn — start object data range before state data range
              dataRange = out.getBlockOut().startRange(dataPosition, dataRangeIndex);
              ((DynamicObjectContext)context).setDataRange(dataRange);

              stateWriter.includeInOrExcludeFrom(attribute.getValueElement(), parentContext, (DynamicObjectContext)context, excludeFrom);
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
          int beforePosition = out.size();
          int type = writeProperty(attribute, createValueProvider(attribute), descriptor, cssDeclarationSourceDefined, context);
          if (type != IGNORE) {
            if (propertyProcessor.isStyle()) {
              cssDeclarationSourceDefined = true;
            }
            if (type < PRIMITIVE) {
              writer.getBlockOut().setPosition(beforePosition);
              addProblem(attribute, "error.unknown.attribute.value.type", descriptor.getType());
            }
          }
        }
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

    processSubTags(parent, context, parentContext, cssDeclarationSourceDefined);

    // initializeReference must be after process all elements — after sub tag also, due to <RadioButton id="visa" label="Visa" 
    // width="150"><group>{cardtype} !!id (for binding target, RadioButton id="visa") allocation here!!</group></RadioButton>
    if (dataPosition != -1) {
      out.write(EMPTY_CLASS_OR_PROPERTY_NAME);
      if (dataRange != null) {
        out.getBlockOut().endRange(dataRange);
      }
    }
  }

  private void processSubTags(final XmlTag parent, final @Nullable Context context, final @Nullable Context parentContext,
                              boolean cssDeclarationSourceDefined) {
    int closeObjectLevel = 0;
    for (XmlTagChild child : parent.getValue().getChildren()) {
      if (child instanceof XmlText) {
        if (!MxmlUtil.containsOnlyWhitespace(child)) {
          if (closeObjectLevel == 0) {
            closeObjectLevel = processDefaultProperty(parent, createValueProvider((XmlText)child), null);
            if (closeObjectLevel == -1) {
              closeObjectLevel = 0;
              continue;
            }
          }

          writer.writeString(((XmlText)child).getValue());
        }
      }
      else if (child instanceof XmlTag) {
        XmlTag tag = (XmlTag)child;
        XmlElementDescriptor descriptor = tag.getDescriptor();
        assert descriptor != null;
        if (descriptor instanceof ClassBackedElementDescriptor) {
          ClassBackedElementDescriptor classBackedDescriptor = (ClassBackedElementDescriptor)descriptor;
          if (classBackedDescriptor.isPredefined()) {
            if (descriptor.getQualifiedName().equals(FlexPredefinedTagNames.DECLARATIONS)) {
              injectedASWriter.readDeclarations(this, tag);
            }

            continue;
          }
          else if (isAbstract(classBackedDescriptor)) {
            addProblem(child, "error.abstract.class", classBackedDescriptor.getQualifiedName());
            continue;
          }

          if (closeObjectLevel == 0) {
            closeObjectLevel = processDefaultProperty(parent, createValueProvider(tag), classBackedDescriptor);
            if (closeObjectLevel == -1) {
              closeObjectLevel = 0;
              continue;
            }
          }

          processClassBackedSubTag(tag, classBackedDescriptor, context, closeObjectLevel > 0);
        }
        else if (descriptor instanceof AnnotationBackedDescriptor) {
          AnnotationBackedDescriptor annotationBackedDescriptor = (AnnotationBackedDescriptor)descriptor;
          // explicit content after contiguous child elements serving as the default property value
          while (closeObjectLevel > 0) {
            out.write(EMPTY_CLASS_OR_PROPERTY_NAME);
            closeObjectLevel--;
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
          else if (hasStates &&
                   stateWriter.checkStateSpecificPropertyValue(this, propertyProcessor, tag, createValueProvider(tag), 
                                                               annotationBackedDescriptor, context, parentContext)) {
            // skip
          }
          else {
            int type = writeProperty(tag, createValueProvider(tag), annotationBackedDescriptor, cssDeclarationSourceDefined, context);
            if (type != IGNORE) {
              if (propertyProcessor.isStyle()) {
                cssDeclarationSourceDefined = true;
              }

              if (type < PRIMITIVE) {
                assert context != null;
                processPropertyTagValue(tag, context, type == PropertyProcessor.ARRAY);
              }
            }
          }
        }
      }
    }

    while (closeObjectLevel > 0) {
      out.write(EMPTY_CLASS_OR_PROPERTY_NAME);
      closeObjectLevel--;
    }
  }

  private static boolean isAbstract(ClassBackedElementDescriptor classBackedDescriptor) {
    return FLEX_SDK_ABSTRACT_CLASSES.matcher(classBackedDescriptor.getQualifiedName()).matches();
  }

  private void processClassBackedSubTag(XmlTag tag, ClassBackedElementDescriptor descriptor, @Nullable Context parentContext,
                                        boolean isArray) {
    if (writeIfPrimitive(tag, descriptor)) {
      return;
    }

    if (JSCommonTypeNames.ARRAY_CLASS_NAME.equals(descriptor.getQualifiedName())) {
      processSubTags(tag, parentContext, null, false);
      return;
    }

    final int projectComponentFactoryId;
    try {
      projectComponentFactoryId = InjectionUtil.getProjectComponentFactoryId(descriptor.getQualifiedName(), descriptor.getDeclaration(),
                                                                             propertyProcessor.getUnregisteredDocumentFactories());
    }
    catch (InvalidPropertyException e) {
      problemsHolder.add(e);
      return;
    }

    final int childDataPosition = out.size();
    if (projectComponentFactoryId != -1) {
      if (!isArray) {
        // replace Amf3Types.OBJECT to AmfExtendedTypes.DOCUMENT_REFERENCE
        writer.getBlockOut().setPosition(writer.getBlockOut().size() - 1);
      }

      out.write(AmfExtendedTypes.DOCUMENT_REFERENCE);
      out.writeUInt29(projectComponentFactoryId);
    }
    else {
      if (isArray) {
        out.write(Amf3Types.OBJECT);
      }

      writer.write(descriptor.getQualifiedName());
    }

    processElements(tag, parentContext, hasStates && isArray && parentContext != null, childDataPosition, out.getByteOut().allocate(2));
  }

  void processPropertyTagValue(XmlTag parent, @Nullable Context parentContext, boolean isArray) {
    for (XmlTag tag : parent.getSubTags()) {
      processClassBackedSubTag(tag, (ClassBackedElementDescriptor)tag.getDescriptor(), parentContext, isArray);
    }

    if (isArray) {
      out.write(EMPTY_CLASS_OR_PROPERTY_NAME);
    }
  }

  void processDeclarations(XmlTag parent) {
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

      processClassBackedSubTag(tag, descriptor, null, true);
    }

    out.write(EMPTY_CLASS_OR_PROPERTY_NAME);
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

  // childDescriptor will be null if child is XmlText
  private int processDefaultProperty(XmlTag tag, XmlElementValueProvider valueProvider, @Nullable ClassBackedElementDescriptor childDescriptor) {
    ClassBackedElementDescriptor descriptor = (ClassBackedElementDescriptor)tag.getDescriptor();
    assert descriptor != null;

    if (JSCommonTypeNames.ARRAY_CLASS_NAME.equals(descriptor.getQualifiedName())) {
      out.write(Amf3Types.ARRAY);
      return 1;
    }

    AnnotationBackedDescriptor defaultDescriptor = descriptor.getDefaultPropertyDescriptor();
    if (defaultDescriptor == null) {
      final JSClass jsClass = (JSClass)descriptor.getDeclaration();
      final String className = descriptor.getQualifiedName();
      final boolean isDirectContainerImpl = className.equals(FlexCommonTypeNames.ICONTAINER);
      if (isDirectContainerImpl || JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.ICONTAINER)) {
        if (childDescriptor == null) {
          addProblem(tag, "error.initializer.cannot.be.represented.in.text", tag.getLocalName());
          return -1;
        }

        if (!isDirectContainerImpl && isHaloNavigator(className, jsClass) &&
            !JSInheritanceUtil.isParentClass((JSClass)childDescriptor.getDeclaration(), FlexCommonTypeNames.INAVIGATOR_CONTENT)) {
          addProblem(tag, "error.children.must.be", tag.getLocalName(), FlexCommonTypeNames.INAVIGATOR_CONTENT);
          return -1;
        }

        writer.write("0");
        out.write(PropertyClassifier.MX_CONTAINER_CHILDREN);
        return 1;
      }
      else {
        // http://youtrack.jetbrains.net/issue/IDEA-66565
        addProblem(tag, "error.default.property.not.found", tag.getLocalName());
      }
    }
    else {
      if (defaultDescriptor.getType().equals(JSCommonTypeNames.ARRAY_CLASS_NAME) && defaultDescriptor.getArrayType() != null) {
        final String elementType = defaultDescriptor.getArrayType();
        final boolean isString = elementType.equals(JSCommonTypeNames.STRING_CLASS_NAME);
        if (isString) {
          if (childDescriptor != null && !childDescriptor.getQualifiedName().equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
            addProblem(tag, "error.children.must.be", tag.getLocalName(), elementType);
            return -1;
          }
        }
        else {
          if (childDescriptor == null) {
            addProblem(tag, "error.initializer.cannot.be.represented.in.text", tag.getLocalName());
            return -1;
          }
        }
      }

      writer.write(defaultDescriptor.getName());
      out.write(PropertyClassifier.PROPERTY);
      if (defaultDescriptor.isDeferredInstance()) {
        writer.writeDeferredInstanceFromArray();
        return 1;
      }
      else {
        String type = defaultDescriptor.getType();
        if (defaultDescriptor.contentIsArrayable()) {
          out.write(Amf3Types.ARRAY);
          return 1;
        }
        else if (type.equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
          writeSubstitutedString(valueProvider.getSubstituted());
        }
        else if (type.equals(JSCommonTypeNames.NUMBER_CLASS_NAME)) {
          out.writeAmfDouble(valueProvider.getTrimmed());
        }
        else if (type.equals(JSCommonTypeNames.BOOLEAN_CLASS_NAME)) {
          out.writeAmfBoolean(valueProvider.getTrimmed());
        }
        else {
          out.write(Amf3Types.OBJECT);
          return 0;
        }
      }
    }

    return -1;
  }

  private void writeSubstitutedString(CharSequence value) {
    if (value == XmlElementValueProvider.EMPTY) {
      writer.writeStringReference(XmlElementValueProvider.EMPTY);
    }
    else {
      writer.writeString(value);
    }
  }

  private boolean writeIfPrimitive(XmlTag tag, ClassBackedElementDescriptor descriptor) {
    final String fqn = descriptor.getQualifiedName();
    if (fqn.equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
      writeSubstitutedString(XmlTagValueProvider.getDisplay(tag));
    }
    else if (fqn.equals(JSCommonTypeNames.NUMBER_CLASS_NAME)) {
      out.writeAmfDouble(tag.getValue().getTrimmedText());
    }
    else if (fqn.equals(JSCommonTypeNames.BOOLEAN_CLASS_NAME)) {
      out.write(tag.getValue().getTrimmedText().charAt(0) == 't' ? Amf3Types.TRUE : Amf3Types.FALSE);
    }
    else {
      return false;
    }

    return true;
  }

  private void defineInlineCssDeclaration(@NotNull PsiElement element) {
    Document document = MxmlUtil.getDocument(element);
    int textOffset = element.getTextOffset();
    out.writeUInt29(document.getLineNumber(textOffset) + 1);
    out.writeUInt29(textOffset);
  }

  private int writeProperty(XmlElement element, XmlElementValueProvider valueProvider, AnnotationBackedDescriptor descriptor,
                            boolean cssDeclarationSourceDefined, Context context) {
    final int beforePosition = writer.getBlockOut().size();
    try {
      ValueWriter valueWriter = propertyProcessor.process(element, valueProvider, descriptor, context);
      if (valueWriter == null) {
        return IGNORE;
      }

      writer.write(propertyProcessor.getName());
      if (propertyProcessor.isStyle()) {
        out.write(PropertyClassifier.STYLE);
        if (!cssDeclarationSourceDefined) {
          defineInlineCssDeclaration(element);
        }
      }
      else {
        out.write(PropertyClassifier.PROPERTY);
      }

      return valueWriter.write(out, writer, propertyProcessor.isStyle());
    }
    catch (RuntimeException e) {
      problemsHolder.add(element, e, descriptor.getName());
    }
    catch (Throwable e) {
      problemsHolder.add(e);
    }
    
    writer.getBlockOut().setPosition(beforePosition);
    return IGNORE;
  }
}