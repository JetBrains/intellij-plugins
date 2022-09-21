// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.daemon.Validator;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.javascript.flex.mxml.FlexNameAlias;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.PlatformIcons;
import com.intellij.util.text.StringTokenizer;
import com.intellij.xml.*;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class AnnotationBackedDescriptorImpl extends BasicXmlAttributeDescriptor
  implements Validator<XmlElement>, AnnotationBackedDescriptor, XmlElementDescriptorAwareAboutChildren {

  enum OriginatingElementType {Metadata, VarOrFunction, IdAttribute, Other}

  private static final String[] DEFERRED_IMMEDIATE = {"deferred", "immediate"};
  private static final String[] AUTO_NEVER = {"auto", "never"};
  private static final String[] E4X_XML = {"e4x", "xml"};
  private static final String[] TRUE_FALSE = {"true", "false"};
  private static final String CLEAR_DIRECTIVE = "@Clear()";
  private static final String I_STYLE_CLIENT_CLASS = "mx.styles.IStyleClient";
  // lowercased values listed in flash.css.Descriptor class from Flex compiler
  private static final String[] COLOR_ALIASES = {"black", "blue", "green", "gray", "silver", "lime", "olive", "white", "yellow", "maroon",
    "magenta", "navy", "red", "purple", "teal", "fuchsia", "aqua", "cyan", "halogreen", "haloblue", "haloorange", "halosilver"};

  protected final String name;
  protected final ClassBackedElementDescriptor parentDescriptor;

  protected final boolean predefined;
  protected String type;
  protected String format;

  private boolean myEnumerated;
  private boolean myEnumeratedValuesCaseSensitive = true;
  private String[] myEnumeratedValues;
  private final String arrayElementType;
  @NonNls private static final String ENUMERATION_ATTR_NAME = "enumeration";
  @NonNls private static final String FORMAT_ATTR_NAME = "format";
  @NonNls private static final String TYPE_ATTR_NAME = "type";

  private String percentProxy;
  private boolean myScriptable;
  private boolean myProperty = false;
  private String myAnnotationName;
  @NotNull private final OriginatingElementType myOriginatingElementType;

  private boolean myRichTextContent;
  private boolean myCollapseWhiteSpace;

  private final boolean myDeferredInstance;
  private boolean myIsRequired = false;

  protected AnnotationBackedDescriptorImpl(String _name,
                                           ClassBackedElementDescriptor _parentDescriptor,
                                           boolean _predefined,
                                           String _type,
                                           String _arrayElementType, boolean deferredInstance,
                                           PsiElement originatingElement) {
    name = _name;
    parentDescriptor = _parentDescriptor;
    predefined = _predefined;
    type = _type;

    arrayElementType = _arrayElementType;
    myDeferredInstance = deferredInstance;

    if (originatingElement instanceof JSAttributeNameValuePair) {
      myOriginatingElementType = OriginatingElementType.Metadata;
      final JSAttribute attribute = (JSAttribute)originatingElement.getParent();

      initFromAttribute(attribute);
    }
    else if (originatingElement instanceof JSAttributeListOwner) {
      myOriginatingElementType = OriginatingElementType.VarOrFunction;
      myProperty = originatingElement instanceof JSFunction || originatingElement instanceof JSVariable;
      JSAttribute attribute = findInspectableAttr(originatingElement);
      initFromAttribute(attribute);

      attribute = findAttr((JSAttributeListOwner)originatingElement, FlexAnnotationNames.PERCENT_PROXY);
      if (attribute != null) {
        JSAttributeNameValuePair[] values = attribute.getValues();
        percentProxy = values.length > 0 ? values[0].getSimpleValue() : "";
      }

      if (type == null) {
        type = ClassBackedElementDescriptor.getPropertyType((JSNamedElement)originatingElement);
      }
      initFromType();

      initRichTextContentAndCollapseWhiteSpace((JSAttributeListOwner)originatingElement);
    }
    else if (originatingElement instanceof XmlAttribute &&
             FlexMxmlLanguageAttributeNames.ID.equals(((XmlAttribute)originatingElement).getName())) {
      myOriginatingElementType = OriginatingElementType.IdAttribute;
    }
    else {
      myOriginatingElementType = OriginatingElementType.Other; // dead branch?
    }

    if (predefined) {
      if (FlexStateElementNames.ITEM_CREATION_POLICY.equals(name)) {
        myEnumerated = true;
        myEnumeratedValues = DEFERRED_IMMEDIATE;
      }
      else if (FlexStateElementNames.ITEM_DESTRUCTION_POLICY.equals(name)) {
        myEnumerated = true;
        myEnumeratedValues = AUTO_NEVER;
      }
      else if (CodeContext.FORMAT_ATTR_NAME.equals(name) && MxmlJSClass.XML_TAG_NAME.equals(parentDescriptor.getName())) {
        myEnumerated = true;
        myEnumeratedValues = E4X_XML;
        myEnumeratedValuesCaseSensitive = false;
      }
      else if (CodeContext.TWO_WAY_ATTR_NAME.equals(name) && FlexPredefinedTagNames.BINDING.equals(parentDescriptor.getName())) {
        myEnumerated = true;
        myEnumeratedValues = TRUE_FALSE;
        myEnumeratedValuesCaseSensitive = false;
      }
    }
  }

  protected AnnotationBackedDescriptorImpl(String name,
                                           ClassBackedElementDescriptor parentDescriptor,
                                           boolean predefined,
                                           String type,
                                           String arrayElementType,
                                           PsiElement originatingElement) {
    this(name, parentDescriptor, predefined, type, arrayElementType, false, originatingElement);
  }

  protected AnnotationBackedDescriptorImpl(String _name,
                                           ClassBackedElementDescriptor _parentDescriptor,
                                           AnnotationBackedDescriptorImpl originalDescriptor, JSNamedElement originatingElement) {
    if (originalDescriptor.myOriginatingElementType != OriginatingElementType.VarOrFunction) {
      Logger.getInstance(AnnotationBackedDescriptorImpl.class.getName()).error(originalDescriptor.myOriginatingElementType);
    }
    myOriginatingElementType = originalDescriptor.myOriginatingElementType;

    name = _name;
    parentDescriptor = _parentDescriptor;
    predefined = originalDescriptor.predefined;
    type = originalDescriptor.type;

    arrayElementType = originalDescriptor.arrayElementType;

    myEnumerated = originalDescriptor.myEnumerated;
    myEnumeratedValues = originalDescriptor.getEnumeratedValues();
    myScriptable = originalDescriptor.myScriptable;
    percentProxy = originalDescriptor.percentProxy;
    type = originalDescriptor.type;
    format = originalDescriptor.format;
    myAnnotationName = originalDescriptor.myAnnotationName;
    myProperty = originalDescriptor.myProperty;

    myCollapseWhiteSpace = originalDescriptor.myCollapseWhiteSpace;
    myRichTextContent = originalDescriptor.myRichTextContent;

    myDeferredInstance = originalDescriptor.myDeferredInstance;

    // only function (setter/getter) may be overridden
    if (originatingElement instanceof JSFunction) {
      // mxml compiler doesn't inherit function annotations
      initRichTextContentAndCollapseWhiteSpace((JSFunction)originatingElement);
    }

    if (originatingElement instanceof JSAttributeListOwner) {
      final JSAttribute attribute = findAttr((JSAttributeListOwner)originatingElement, FlexAnnotationNames.PERCENT_PROXY);
      if (attribute != null && percentProxy == null) {
        final JSAttributeNameValuePair[] values = attribute.getValues();
        percentProxy = values.length > 0 ? values[0].getSimpleValue() : "";
      }
    }
  }

  private void initRichTextContentAndCollapseWhiteSpace(JSAttributeListOwner attributeListOwner) {
    if (!myEnumerated) {
      if (contentIsArrayable()) {
        myRichTextContent = findAttr(attributeListOwner, FlexAnnotationNames.RICH_TEXT_CONTENT) != null;
      }

      if (JSCommonTypeNames.STRING_CLASS_NAME.equals(type)) {
        myCollapseWhiteSpace = findAttr(attributeListOwner, FlexAnnotationNames.COLLAPSE_WHITE_SPACE) != null;
      }
    }
  }

  @Override
  @NonNls
  public String getName() {
    return name;
  }

  @Override
  public String getFormat() {
    return format;
  }

  @NotNull
  OriginatingElementType getOriginatingElementType() {
    return myOriginatingElementType;
  }

  boolean isPreferredTo(@NotNull AnnotationBackedDescriptorImpl other) {
    // if there's event and function/variable with the same name then event is preferred
    // in case of style or effect and function/variable with the same name - function/variable is preferred
    assert Comparing.strEqual(name, other.name);
    if (FlexAnnotationNames.EVENT.equals(myAnnotationName) && !FlexAnnotationNames.EVENT.equals(other.myAnnotationName)) {
      return true;
    }
    if (myOriginatingElementType != OriginatingElementType.Metadata && other.myOriginatingElementType == OriginatingElementType.Metadata) {
      return true;
    }
    return false;
  }

  @Override
  @Nullable
  public PsiElement getDeclaration() {
    final PsiElement[] result = new PsiElement[1];

    final PsiElement parentDescriptorDeclaration = parentDescriptor.getDeclaration();
    PsiElement element = parentDescriptorDeclaration;
    if (predefined) return element;

    if (myOriginatingElementType == OriginatingElementType.IdAttribute) {
      return findDeclarationByIdAttributeValue(parentDescriptorDeclaration, new HashSet<>());
    }

    while (element instanceof XmlFile) {
      XmlTag rootTag = ((XmlFile)element).getDocument().getRootTag();
      element = JSResolveUtil.getClassFromTagNameInMxml(rootTag.getFirstChild());
      if (element instanceof XmlBackedJSClassImpl) {
        element = element.getParent().getContainingFile();
      }
    }

    final JSNamedElement jsClass = (JSNamedElement)element;

    final ClassBackedElementDescriptor.AttributedItemsProcessor itemsProcessor =
      new ClassBackedElementDescriptor.AttributedItemsProcessor() {
        @Override
        public boolean process(final JSNamedElement jsNamedElement, final boolean isPackageLocalVisibility) {
          if (myOriginatingElementType != OriginatingElementType.Metadata && name.equals(jsNamedElement.getName())) {
            result[0] = jsNamedElement;
            return false;
          }
          return true;
        }

        @Override
        public boolean process(final JSAttributeNameValuePair pair, final String annotationName, final boolean included) {
          if (myOriginatingElementType == OriginatingElementType.Metadata && name.equals(pair.getSimpleValue()) && included) {
            result[0] = pair;
            return false;
          }
          return true;
        }
      };

    boolean b = jsClass == null || ClassBackedElementDescriptor.processAttributes(jsClass, itemsProcessor);

    if (b && jsClass instanceof JSClass) {
      final JSClass clazz = (JSClass)jsClass;
      final JSClass[] classes = clazz.getSuperClasses();
      if (classes.length > 0 && clazz.getName().equals(classes[0].getName()) && clazz != classes[0]) {
        b = ClassBackedElementDescriptor.processAttributes(classes[0], itemsProcessor);
      }
    }

    if (b && parentDescriptorDeclaration instanceof XmlFile) {
      final JSResolveUtil.JSInjectedFilesVisitor injectedFilesVisitor = new JSResolveUtil.JSInjectedFilesVisitor() {
        @Override
        protected void process(final JSFile file) {
          ClassBackedElementDescriptor.processAttributes(file, itemsProcessor);
        }
      };

      element = parentDescriptorDeclaration;
      while (element instanceof XmlFile) {
        XmlTag rootTag = ((XmlFile)element).getDocument().getRootTag();
        FlexUtils.processMxmlTags(rootTag, true, injectedFilesVisitor);
        if (result[0] != null) break;
        element = JSResolveUtil.getClassFromTagNameInMxml(rootTag.getFirstChild());
        if (element instanceof XmlBackedJSClassImpl) {
          element = element.getParent().getContainingFile();
        }
      }
    }
    return result[0];
  }

  @Override
  public PsiElement getEnumeratedValueDeclaration(XmlElement xmlElement, String value) {
    for (String s : getEnumeratedValues()) {
      if (Comparing.equal(s, value, myEnumeratedValuesCaseSensitive)) {
        return getDeclaration();
      }
    }
    return null;
  }

  @Nullable
  private XmlAttributeValue findDeclarationByIdAttributeValue(final PsiElement descriptorDeclaration, final Set<JSClass> visited) {
    final Ref<XmlAttributeValue> resultRef = new Ref<>(null);

    if (descriptorDeclaration instanceof XmlFile) {
      final XmlDocument document = ((XmlFile)descriptorDeclaration).getDocument();
      final XmlTag rootTag = document == null ? null : document.getRootTag();
      if (rootTag != null) {
        ClassBackedElementDescriptor.processClassBackedTagsWithIdAttribute(rootTag, idAttributeAndItsType -> {
          final XmlAttributeValue xmlAttributeValue = idAttributeAndItsType.first.getValueElement();
          if (xmlAttributeValue != null && xmlAttributeValue.getValue().equals(this.name)) {
            resultRef.set(xmlAttributeValue);
            return false;
          }
          return true;
        });
      }

      if (resultRef.isNull()) {
        final JSClass jsClass = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)descriptorDeclaration);
        if (jsClass != null) {
          return findDeclarationByIdAttributeValueInSuperClass(jsClass, visited);
        }
      }
    }
    else if (descriptorDeclaration instanceof JSClass) {
      return findDeclarationByIdAttributeValueInSuperClass((JSClass)descriptorDeclaration, visited);
    }

    return resultRef.get();
  }

  @Nullable
  private XmlAttributeValue findDeclarationByIdAttributeValueInSuperClass(@NotNull final JSClass jsClass, final Set<JSClass> visited) {
    if (!visited.add(jsClass)) {
      return null;
    }

    for (final JSClass superClass : jsClass.getSuperClasses()) {
      if (superClass instanceof XmlBackedJSClassImpl) {
        final PsiFile psiFile = superClass.getContainingFile();
        if (psiFile instanceof XmlFile) {
          final XmlAttributeValue result = findDeclarationByIdAttributeValue(psiFile, visited);
          if (result != null) {
            return result;
          }
        }
      }
      else {
        return findDeclarationByIdAttributeValueInSuperClass(superClass, visited);
      }
    }
    return null;
  }

  @Override
  public void init(final PsiElement element) {
  }

  private void initFromAttribute(final JSAttribute attribute) {
    if (attribute == null) return;
    myEnumerated = attribute.getValueByName(ENUMERATION_ATTR_NAME) != null;
    JSAttributeNameValuePair pair = attribute.getValueByName(TYPE_ATTR_NAME);
    if (pair != null) type = pair.getSimpleValue();

    pair = attribute.getValueByName(FORMAT_ATTR_NAME);
    if (pair != null) format = pair.getSimpleValue();

    myAnnotationName = attribute.getName();
    myScriptable = FlexAnnotationNames.EVENT.equals(myAnnotationName);

    initFromType();
  }

  private void initFromType() {
    if (JSCommonTypeNames.BOOLEAN_CLASS_NAME.equals(type)) myEnumerated = true;
    if ("Function".equals(type)) myScriptable = true;
  }

  public void setRequired(final boolean isRequired) {
    myIsRequired = isRequired;
  }

  @Override
  public boolean isRequired() {
    return myIsRequired;
  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public boolean hasIdType() {
    return FlexMxmlLanguageAttributeNames.ID.equals(name);
  }

  @Override
  public boolean hasIdRefType() {
    return false;
  }

  @Override
  @Nullable
  public String getDefaultValue() {
    return null;
  }

  @Override
  public boolean isEnumerated() {
    return myEnumerated;
  }

  @Override
  public String[] getEnumeratedValues(@Nullable final XmlElement context) {
    if (context instanceof XmlAttribute && !myEnumerated && "id".equals(name)) {
      // id attribute value completion
      String value = ((XmlAttribute)context).getValue();
      int index = value.indexOf(CompletionInitializationContext.DUMMY_IDENTIFIER);
      value = index == -1 ? value : value.substring(0, index);

      final PsiElement parent = context.getParent();
      final String tagName = parent instanceof XmlTag ? ((XmlTag)parent).getLocalName() : null;
      if (StringUtil.isNotEmpty(tagName)) {
        final String[] suggestedIds = suggestIdValues(value, tagName);
        return makeUnique(suggestedIds, getNamedElementsVisibleAt(context));
      }
    }
    return super.getEnumeratedValues(context);
  }

  @Override
  public String[] getEnumeratedValues() {
    @NonNls String[] enumerationValues = myEnumeratedValues;

    if (enumerationValues == null) {
      enumerationValues = ArrayUtilRt.EMPTY_STRING_ARRAY;

      if (myEnumerated) {
        if (JSCommonTypeNames.BOOLEAN_CLASS_NAME.equals(type)) {
          enumerationValues = TRUE_FALSE;
        }
        else {
          final PsiElement element = getDeclaration();
          JSAttributeNameValuePair pair = null;

          if (element instanceof JSAttributeNameValuePair) {
            pair = ((JSAttribute)element.getParent()).getValueByName(ENUMERATION_ATTR_NAME);
          }
          else if (element instanceof JSAttributeListOwner) {
            JSAttribute inspectableAttr = findInspectableAttr(element);

            if (inspectableAttr != null) {
              pair = inspectableAttr.getValueByName(ENUMERATION_ATTR_NAME);
            }
          }

          if (pair != null) {
            final String simpleValue = pair.getSimpleValue();

            if (simpleValue != null) {
              StringTokenizer tokenizer = new StringTokenizer(simpleValue, ", ");
              enumerationValues = new String[tokenizer.countTokens()];
              int i = 0;
              while (tokenizer.hasMoreElements()) {
                enumerationValues[i++] = tokenizer.nextElement();
              }
            }
          }
        }
      }
      myEnumeratedValues = enumerationValues;
    }

    return enumerationValues;
  }

  @Nullable
  public static JSAttribute findInspectableAttr(final PsiElement element) {
    return findAttr((JSAttributeListOwner)element, FlexAnnotationNames.INSPECTABLE);
  }

  @Nullable
  private static JSAttribute findAttr(final JSAttributeListOwner attributeListOwner, @NotNull @NonNls final String name) {
    final JSAttributeList attributeList = attributeListOwner != null ? attributeListOwner.getAttributeList() : null;
    JSAttribute[] attrs = attributeList != null ? attributeList.getAttributesByName(name) : null;
    final JSAttribute attribute = attrs != null && attrs.length > 0 ? attrs[0] : null;

    if (attribute == null && attributeList != null) {
      if (attributeListOwner instanceof JSFunction && ((JSFunction)attributeListOwner).isSetProperty()) {
        final PsiElement grandParent = attributeListOwner.getParent();

        String propName = attributeListOwner.getName();
        if (grandParent instanceof JSClass && propName != null) {
          final var processor = new SinkResolveProcessor<>(propName, new ResolveResultSink(null, propName));
          processor.setToProcessHierarchy(false);
          grandParent.processDeclarations(processor, ResolveState.initial(), grandParent, attributeListOwner);

          final List<PsiElement> elementList = processor.getResults();
          if (elementList != null && elementList.size() == 2) {
            final PsiElement firstElement = elementList.get(0);
            final PsiElement secondElement = elementList.get(1);

            JSAttributeListOwner chosenElement = null;

            if (firstElement instanceof JSFunction && ((JSFunction)firstElement).isGetProperty()) {
              chosenElement = (JSAttributeListOwner)firstElement;
            }
            else if (secondElement instanceof JSFunction && ((JSFunction)secondElement).isGetProperty()) {
              chosenElement = (JSAttributeListOwner)secondElement;
            }

            if (chosenElement != null) {
              return findAttr(chosenElement, name);
            }
          }
        }
      }
    }
    return attribute;
  }

  @Override
  @Nullable
  public String validateValue(final XmlElement context, String value) {
    final PsiElement parent = context instanceof XmlAttributeValue ? context.getParent() : null;
    if (parent instanceof XmlAttribute && FlexMxmlLanguageAttributeNames.ID.equals(((XmlAttribute)parent).getName())) {
      return LanguageNamesValidation.isIdentifier(JavascriptLanguage.INSTANCE, value, context.getProject()) ? null // ok
                                                                                                                : FlexBundle
               .message("invalid.identifier.value");
    }

    if (value.indexOf('{') != -1) return null; // dynamic values
    if (value.trim().startsWith("@Resource")) return null;

    if (myAnnotationName != null && CLEAR_DIRECTIVE.equals(value)) {
      return checkClearDirectiveContext(context);
    }

    if (isAllowsPercentage() && value.endsWith("%")) {
      value = value.substring(0, value.length() - 1);
    }

    boolean uint = false;
    if ("int".equals(type) || (uint = "uint".equals(type))) {
      try {
        boolean startWithSharp;
        if ((startWithSharp = value.startsWith("#")) || value.startsWith("0x")) {
          if (uint) {
            final long l = Long.parseLong(value.substring(startWithSharp ? 1 : 2), 16);
            if (l < 0 || l > 0xFFFFFFFFL) {
              throw new NumberFormatException("value out of range");
            }
          }
          else {
            Integer.parseInt(value.substring(startWithSharp ? 1 : 2), 16);
          }
        }
        else {
          if ("Color".equals(format) && !StringUtil.isEmptyOrSpaces(value) && !Character.isDigit(value.charAt(0))) {
            return checkColorAlias(value);
          }

          if (uint) {
            final long l = Long.parseLong(value);
            if (l < 0 || l > 0xFFFFFFFFL) {
              throw new NumberFormatException("value out of range");
            }
          }
          else {
            Integer.parseInt(value);
          }
        }
      }
      catch (NumberFormatException ex) {
        return FlexBundle.message("flex.invalid.integer.value");
      }
    }
    else if ("Number".equals(type)) {
      try {
        boolean startWithSharp;
        if ((startWithSharp = value.startsWith("#")) || value.startsWith("0x")) {
          Integer.parseInt(value.substring(startWithSharp ? 1 : 2), 16);
        }
        else {
          Double.parseDouble(value);
        }
      }
      catch (NumberFormatException ex) {
        return FlexBundle.message("flex.invalid.number.value");
      }
    }
    return null;
  }

  @Nullable
  private static String checkColorAlias(final String s) {
    if (!ArrayUtil.contains(StringUtil.toLowerCase(s), COLOR_ALIASES)) {
      return FlexBundle.message("unknown.color.error", s);
    }
    return null;
  }

  @Nullable
  private static String checkClearDirectiveContext(final XmlElement context) {
    final PsiElement attributeOrTag = context instanceof XmlAttributeValue ? context.getParent() : context;
    final String name = attributeOrTag instanceof XmlAttribute
                        ? ((XmlAttribute)attributeOrTag).getName() : attributeOrTag instanceof XmlTag
                                                                     ? ((XmlTag)attributeOrTag).getName() : null;
    if (name != null && !name.contains(".")) {
      return FlexBundle.message("clear.directive.state.specific.error");
    }

    final PsiElement parent = attributeOrTag.getParent();
    final XmlElementDescriptor descriptor = parent instanceof XmlTag ? ((XmlTag)parent).getDescriptor() : null;
    final PsiElement declaration = descriptor instanceof ClassBackedElementDescriptor ? descriptor.getDeclaration() : null;
    final PsiElement iStyleClient = ActionScriptClassResolver.findClassByQNameStatic(I_STYLE_CLIENT_CLASS, attributeOrTag);

    if (!(declaration instanceof JSClass) || !(iStyleClient instanceof JSClass)
        || !JSInheritanceUtil.isParentClass((JSClass)declaration,
                                            (JSClass)iStyleClient)) {
      return FlexBundle.message("clear.directive.IStyleClient.error");
    }
    return null;
  }

  @Override
  @NonNls
  public String getName(final PsiElement context) {
    XmlTag tag = (XmlTag)context;
    if (tag.getName().contains("Rulezz")) {
      // tag name completion
      final String namespaceByPrefix = tag.getPrefixByNamespace(parentDescriptor.context.namespace);
      if (namespaceByPrefix != null && !namespaceByPrefix.isEmpty()) return namespaceByPrefix + ":" + getName();
    }
    return getName();
  }

  @Override
  public String getQualifiedName() {
    return getName();
  }

  @Override
  public String getDefaultName() {
    return getName();
  }

  @Override
  public XmlElementDescriptor[] getElementsDescriptors(final XmlTag context) {
    if (context.getDescriptor() instanceof ClassBackedElementDescriptor) {
      return parentDescriptor.getElementsDescriptors(context);
    }

    if (arrayElementType != null && ActionScriptClassResolver.findClassByQNameStatic(arrayElementType, context) != null) {
      return parentDescriptor.getElementDescriptorsInheritedFromGivenType(arrayElementType);
    }
    if (type != null && ClassBackedElementDescriptor.isAdequateType(type)) {
      return parentDescriptor.getElementDescriptorsInheritedFromGivenType(type);
    }

    if (ClassBackedElementDescriptor.IFACTORY_SHORT_CLASS_NAME.equals(ClassBackedElementDescriptor.className(type))) {
      return EMPTY_ARRAY;
    }

    return parentDescriptor.context.getDescriptorsWithAllowedDeclaration();
  }

  @Override
  public XmlElementDescriptor getElementDescriptor(final XmlTag childTag, XmlTag contextTag) {
    if (MxmlJSClass.isTagThatAllowsAnyXmlContent(contextTag)) {
      return new AnyXmlElementWithAnyChildrenDescriptor();
    }

    final String namespace = childTag.getNamespace();
    XmlElementDescriptor descriptor;

    if (!ClassBackedElementDescriptor.sameNs(namespace, parentDescriptor.context.namespace)) {
      final XmlNSDescriptor nsdescriptor = childTag.getNSDescriptor(namespace, true);
      descriptor = nsdescriptor != null ? nsdescriptor.getElementDescriptor(childTag) : null;
    }
    else {
      descriptor = parentDescriptor.context.getElementDescriptor(childTag.getLocalName(), childTag);
    }

    if (arrayElementType != null) {
      if (descriptor == null ||
          isVectorType(type) && isVectorDescriptor(descriptor) ||
          JSCommonTypeNames.ARRAY_CLASS_NAME.equals(type) && isArrayDescriptor(descriptor) ||
          ActionScriptClassResolver.findClassByQNameStatic(arrayElementType, childTag) == null) {
        return descriptor;
      }

      return ClassBackedElementDescriptor.checkValidDescriptorAccordingToType(arrayElementType, descriptor);
    }

    if (type != null && ClassBackedElementDescriptor.isAdequateType(type)) {
      return ClassBackedElementDescriptor.checkValidDescriptorAccordingToType(type, descriptor);
    }

    if (ClassBackedElementDescriptor.IFACTORY_SHORT_CLASS_NAME.equals(ClassBackedElementDescriptor.className(type))) {
      if (!FlexNameAlias.COMPONENT_TYPE_NAME.equals(childTag.getLocalName()) ||
          !JavaScriptSupportLoader.isLanguageNamespace(childTag.getNamespace())) {
        return null;
      }
    }

    if (descriptor == null) {
      PsiElement element = getDeclaration();
      if (element instanceof JSNamedElement) {
        element = new ClassBackedElementDescriptor(ClassBackedElementDescriptor.getPropertyType((JSNamedElement)element),
                                                   parentDescriptor.context, parentDescriptor.project, true).getDeclaration();
        return parentDescriptor.getClassIfDynamic(childTag.getLocalName(), element);
      }
    }
    return descriptor;
  }

  private static boolean isVectorType(final String type) {
    return type != null &&
           (type.equals(JSCommonTypeNames.VECTOR_CLASS_NAME) || type.startsWith(JSCommonTypeNames.VECTOR_CLASS_NAME + ".<"));
  }

  private static boolean isVectorDescriptor(final XmlElementDescriptor descriptor) {
    return descriptor instanceof ClassBackedElementDescriptor &&
           JSCommonTypeNames.VECTOR_CLASS_NAME.equals(descriptor.getName()) &&
           JavaScriptSupportLoader.MXML_URI3.equals(((ClassBackedElementDescriptor)descriptor).context.namespace);
  }

  private static boolean isArrayDescriptor(final XmlElementDescriptor descriptor) {
    return descriptor instanceof ClassBackedElementDescriptor &&
           JSCommonTypeNames.ARRAY_CLASS_NAME.equals(descriptor.getName()) &&
           JavaScriptSupportLoader.MXML_URI3.equals(((ClassBackedElementDescriptor)descriptor).context.namespace);
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable final XmlTag context) {
    return XmlAttributeDescriptor.EMPTY;
  }

  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attributeName, @Nullable final XmlTag context) {
    return ClassBackedElementDescriptor.isPrivateAttribute(attributeName, context) ? new AnyXmlAttributeDescriptor(attributeName) : null;
  }

  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  @Override
  public XmlNSDescriptor getNSDescriptor() {
    return null;
  }

  @Override
  public XmlElementsGroup getTopGroup() {
    return null;
  }

  @Override
  public int getContentType() {
    return CONTENT_TYPE_UNKNOWN;
  }

  @Override
  public void validate(@NotNull final XmlElement context, @NotNull final ValidationHost host) {
    if (context instanceof XmlTag &&
        FlexSdkUtils.isFlex4Sdk(FlexUtils.getSdkForActiveBC(parentDescriptor.context.module))) {
      MxmlLanguageTagsUtil.checkFlex4Attributes((XmlTag)context, host, false);
    }

    if (predefined) return;
    String value;
    if (context instanceof XmlTag) {
      value = ((XmlTag)context).getValue().getTrimmedText();
    }
    else {
      value = ((XmlAttribute)context).getDisplayValue();
    }

    final String message = validateValue(context, value);
    if (message != null) {
      PsiElement errorElement = context;
      if (context instanceof XmlTag) {
        final XmlText[] textElements = ((XmlTag)context).getValue().getTextElements();
        if (textElements.length == 1 && !StringUtil.isEmptyOrSpaces(textElements[0].getText())) {
          errorElement = textElements[0];
        }
      }
      host.addMessage(errorElement, message, ValidationHost.ErrorType.ERROR);
    }

    if (context instanceof XmlTag &&
        FlexStateElementNames.STATES.equals(((XmlTag)context).getLocalName()) &&
        FlexUtils.isMxmlNs(((XmlTag)context).getNamespace())) {
      XmlTag[] tags = ((XmlTag)context).findSubTags("State", ((XmlTag)context).getNamespace());
      XmlUtil.doDuplicationCheckForElements(tags, new HashMap<>(tags.length), new XmlUtil.DuplicationInfoProvider<>() {
        @Override
        public String getName(@NotNull XmlTag xmlTag) {
          return xmlTag.getAttributeValue(FlexStateElementNames.NAME);
        }

        @Override
        @NotNull
        public String getNameKey(@NotNull XmlTag xmlTag, @NotNull String name) {
          return getName(xmlTag);
        }

        @Override
        @NotNull
        public PsiElement getNodeForMessage(@NotNull XmlTag xmlTag) {
          return xmlTag.getAttribute(FlexStateElementNames.NAME).getValueElement();
        }
      }, host);
    }
  }

  @Override
  public boolean requiresCdataBracesInContext(@NotNull final XmlTag context) {
    return myScriptable;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getArrayType() {
    return arrayElementType;
  }

  @Override
  public boolean allowElementsFromNamespace(String namespace, XmlTag context) {
    //if (type.equals("mx.core.IFactory") && JavaScriptSupportLoader.isLanguageNamespace(namespace)) {
    //  return true;
    //}
    //if (type != null || arrayElementType != null) {
    //  return true;
    //}
    return true;
  }

  @Override
  public String getTypeName() {
    return myProperty ? "Function" : myAnnotationName;
  }

  @Override
  public Icon getIcon() {
    if (myProperty) {
      return PlatformIcons.PROPERTY_ICON;
    }
    if (myAnnotationName == null) return null;
    if (myAnnotationName.equals(FlexAnnotationNames.EVENT)) {
      return PlatformIcons.EXCEPTION_CLASS_ICON;
    }
    if (myAnnotationName.equals(FlexAnnotationNames.STYLE)) {
      return AllIcons.FileTypes.Css;
    }
    if (myAnnotationName.equals(FlexAnnotationNames.EFFECT)) {
      return AllIcons.Actions.Lightning;
    }
    return null;
  }

  @Override
  public boolean isPredefined() {
    return predefined;
  }

  @Override
  public boolean isAllowsPercentage() {
    return percentProxy != null;
  }

  @Override
  public String getPercentProxy() {
    return percentProxy;
  }

  @Override
  public boolean isStyle() {
    return myAnnotationName != null && !myScriptable && myAnnotationName.equals(FlexAnnotationNames.STYLE);
  }

  @Override
  public boolean isRichTextContent() {
    return myRichTextContent;
  }

  @Override
  public boolean isCollapseWhiteSpace() {
    return myCollapseWhiteSpace;
  }

  @Override
  public boolean isDeferredInstance() {
    return myDeferredInstance;
  }

  @Override
  public boolean contentIsArrayable() {
    return type != null &&
           (type.equals(JSCommonTypeNames.ARRAY_CLASS_NAME) ||
            type.equals(JSCommonTypeNames.OBJECT_CLASS_NAME) ||
            type.equals(JSCommonTypeNames.ANY_TYPE));
  }

  public static String[] suggestIdValues(final String value, final String type) {
    final String[] typeParts = getTypeParts(type);
    final String[] result = new String[typeParts.length];
    for (int i = 0; i < result.length; i++) {
      // merge written text with suggestions: "myBu" + "Button" -> "myButton"
      final String lowercasedStart = lowercaseStart(typeParts[i]);
      if (value.isEmpty() || lowercasedStart.startsWith(value)) {
        result[i] = lowercasedStart;
      }
      else {
        int j = 0;
        for (; j < value.length(); j++) {
          String s = value.substring(j);
          if (typeParts[i].startsWith(s)) {
            break;
          }
        }
        final int commonTextLength = value.length() - j;
        result[i] = value.substring(0, value.length() - commonTextLength) + typeParts[i];
      }
    }
    return result;
  }

  private static String[] getTypeParts(final String type) {
    // HTTPService -> HTTPService, Service
    // ButtonBarButton -> ButtonBarButton, BarButton, Button
    final List<String> result = new LinkedList<>();

    result.add(Character.toUpperCase(type.charAt(0)) + (type.length() > 1 ? type.substring(1) : ""));

    for (int i = 1; i < type.length() - 1; i++) {
      if (Character.isUpperCase(type.charAt(i)) && !Character.isUpperCase(type.charAt(i + 1))) {
        result.add(type.substring(i));
      }
    }

    return ArrayUtilRt.toStringArray(result);
  }

  private static String lowercaseStart(final String s) {
    // URL -> url
    // HTTPService -> httpService
    // ButtonBar -> buttonBar
    int i = 0;
    while (i < s.length() && Character.isUpperCase(s.charAt(i))) {
      i++;
    }
    i = i <= 1 ? 1 : i == s.length() ? i : i - 1;
    return StringUtil.toLowerCase(s.substring(0, i)) + s.substring(i);
  }

  private static Set<String> getNamedElementsVisibleAt(@NotNull final PsiElement context) {
    final Set<String> names = new HashSet<>();

    ResolveProcessor processor = new ResolveProcessor(null) {
      @Override
      public boolean execute(@NotNull final PsiElement element, @NotNull final ResolveState state) {
        if (element instanceof JSNamedElementBase) {
          names.add(((JSNamedElementBase)element).getName());
        }
        return true;
      }
    };

    processor.setLocalResolve(true);
    JSResolveUtil.treeWalkUp(processor, context, context.getParent(), context);
    return names;
  }

  private static String[] makeUnique(final String[] names, final Set<String> existingNames) {
    for (int i = 0; i < names.length; i++) {
      String name = names[i];
      int postfix = 2;
      while (existingNames.contains(name)) {
        name = names[i] + postfix++;
      }
      names[i] = name;
    }
    return names;
  }
}
