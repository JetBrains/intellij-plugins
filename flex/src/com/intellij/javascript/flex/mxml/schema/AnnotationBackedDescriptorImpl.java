package com.intellij.javascript.flex.mxml.schema;

import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.daemon.Validator;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.javascript.flex.mxml.FlexNameAlias;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.css.CssFileType;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Icons;
import com.intellij.util.Processor;
import com.intellij.util.containers.HashSet;
import com.intellij.util.text.StringTokenizer;
import com.intellij.xml.*;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import com.intellij.xml.util.XmlUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Maxim.Mossienko
 */
public class AnnotationBackedDescriptorImpl extends BasicXmlAttributeDescriptor
  implements Validator<XmlElement>, AnnotationBackedDescriptor, XmlElementDescriptorAwareAboutChildren {
  private static final String[] DEFERRED_IMMEDIATE = {"deferred", "immediate"};
  private static final String[] AUTO_NEVER = {"auto", "never"};
  private static final String[] E4X_XML = {"e4x", "xml"};
  private static final String[] TRUE_FALSE = {"true", "false"};
  protected final String name;
  protected final ClassBackedElementDescriptor parentDescriptor;

  protected final boolean predefined;
  protected String type;
  protected String format;

  private boolean myEnumerated;
  private boolean myEnumeratedValuesCaseSensitive = true;
  private String[] myEnumeratedValues;
  private final String arrayElementType;
  private static final @NonNls String ENUMERATION_ATTR_NAME = "enumeration";
  private static final @NonNls String FORMAT_ATTR_NAME = "format";
  private static final @NonNls String TYPE_ATTR_NAME = "type";
  private static final Icon EFFECT_ICON = IconLoader.getIcon("/actions/lightning.png");

  private String percentProxy;
  private boolean myScriptable;
  private boolean myProperty = false;
  private String myAnnotationName;
  private boolean myDefinedByMetadata = false;
  private boolean myDefinedByIdAttribute = false;

  private boolean myRichTextContent;
  private boolean myCollapseWhiteSpace;

  private final boolean myDeferredInstance;

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
      myDefinedByMetadata = true;
      final JSAttribute attribute = (JSAttribute)originatingElement.getParent();

      initFromAttribute(attribute);
    }
    else if (originatingElement instanceof JSAttributeListOwner) {
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
      myDefinedByIdAttribute = true;
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
      else if (CodeContext.FORMAT_ATTR_NAME.equals(name) && XmlBackedJSClassImpl.XML_TAG_NAME.equals(parentDescriptor.getName())) {
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

  @NonNls
  public String getName() {
    return name;
  }

  public String getFormat() {
    return format;
  }

  boolean isPreferredTo(@NotNull AnnotationBackedDescriptorImpl other) {
    // if there's event and function/variable with the same name then event is preferred
    // in case of style or effect and function/variable with the same name - function/variable is preferred
    assert Comparing.strEqual(name, other.name);
    if (FlexAnnotationNames.EVENT.equals(myAnnotationName) && !FlexAnnotationNames.EVENT.equals(other.myAnnotationName)) {
      return true;
    }
    if (!myDefinedByMetadata && other.myDefinedByMetadata) {
      return true;
    }
    return false;
  }

  @Nullable
  public PsiElement getDeclaration() {
    final PsiElement[] result = new PsiElement[1];

    final PsiElement parentDescriptorDeclaration = parentDescriptor.getDeclaration();
    PsiElement element = parentDescriptorDeclaration;
    if (predefined) return element;

    if (myDefinedByIdAttribute) {
      return findDeclarationByIdAttributeValue(parentDescriptorDeclaration, new THashSet<JSClass>());
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
        public boolean process(final JSNamedElement jsNamedElement, final boolean isPackageLocalVisibility) {
          if (!myDefinedByMetadata && name.equals(jsNamedElement.getName())) {
            result[0] = jsNamedElement;
            return false;
          }
          return true;
        }

        public boolean process(final JSAttributeNameValuePair pair, final String annotationName, final boolean included) {
          if (myDefinedByMetadata && name.equals(pair.getSimpleValue()) && included) {
            result[0] = pair;
            return false;
          }
          return true;
        }
      };

    boolean b = jsClass == null || ClassBackedElementDescriptor.processAttributes(jsClass, itemsProcessor);

    if (b && jsClass != null) {
      final PsiElement _clazz = JSResolveUtil.unwrapProxy(jsClass);
      final JSClass clazz = _clazz instanceof JSClass ? (JSClass)_clazz : null;

      if (clazz != null) {
        final JSClass[] classes = clazz.getSuperClasses();
        if (classes.length > 0 && clazz.getName().equals(classes[0].getName()) && clazz != classes[0]) {
          b = ClassBackedElementDescriptor.processAttributes(classes[0], itemsProcessor);
        }
      }
    }

    if (b && parentDescriptorDeclaration instanceof XmlFile) {
      final JSResolveUtil.JSInjectedFilesVisitor injectedFilesVisitor = new JSResolveUtil.JSInjectedFilesVisitor() {
        protected void process(final JSFile file) {
          ClassBackedElementDescriptor.processAttributes(file, itemsProcessor);
        }
      };

      element = parentDescriptorDeclaration;
      while (element instanceof XmlFile) {
        XmlTag rootTag = ((XmlFile)element).getDocument().getRootTag();
        FlexUtils.processMxmlTags(rootTag, injectedFilesVisitor);
        if (result[0] != null) break;
        element = JSResolveUtil.getClassFromTagNameInMxml(rootTag.getFirstChild());
        if (element instanceof XmlBackedJSClassImpl) {
          element = element.getParent().getContainingFile();
        }
      }
    }
    return result[0];
  }

  @Nullable
  private XmlAttributeValue findDeclarationByIdAttributeValue(final PsiElement descriptorDeclaration, final Set<JSClass> visited) {
    final Ref<XmlAttributeValue> resultRef = new Ref<XmlAttributeValue>(null);

    if (descriptorDeclaration instanceof XmlFile) {
      final XmlDocument document = ((XmlFile)descriptorDeclaration).getDocument();
      final XmlTag rootTag = document == null ? null : document.getRootTag();
      if (rootTag != null) {
        ClassBackedElementDescriptor.processClassBackedTagsWithIdAttribute(rootTag, new Processor<Pair<XmlAttribute, String>>() {
          public boolean process(final Pair<XmlAttribute, String> idAttributeAndItsType) {
            final XmlAttributeValue xmlAttributeValue = idAttributeAndItsType.first.getValueElement();
            if (xmlAttributeValue != null && xmlAttributeValue.getValue().equals(AnnotationBackedDescriptorImpl.this.name)) {
              resultRef.set(xmlAttributeValue);
              return false;
            }
            return true;
          }
        });
      }

      if (resultRef.isNull()) {
        final JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)descriptorDeclaration);
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
  private XmlAttributeValue findDeclarationByIdAttributeValueInSuperClass(final @NotNull JSClass jsClass, final Set<JSClass> visited) {
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

  public boolean isRequired() {
    return false;
  }

  public boolean isFixed() {
    return false;
  }

  public boolean hasIdType() {
    return FlexMxmlLanguageAttributeNames.ID.equals(name);
  }

  public boolean hasIdRefType() {
    return false;
  }

  @Nullable
  public String getDefaultValue() {
    return null;
  }

  public boolean isEnumerated() {
    return myEnumerated;
  }

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

  public String[] getEnumeratedValues() {
    @NonNls String[] enumerationValues = myEnumeratedValues;

    if (enumerationValues == null) {
      enumerationValues = ArrayUtil.EMPTY_STRING_ARRAY;

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
  private static JSAttribute findAttr(final JSAttributeListOwner attributeListOwner, final @NonNls @NotNull String name) {
    final JSAttributeList attributeList = attributeListOwner != null ? attributeListOwner.getAttributeList() : null;
    JSAttribute[] attrs = attributeList != null ? attributeList.getAttributesByName(name) : null;
    final JSAttribute attribute = attrs != null && attrs.length > 0 ? attrs[0] : null;

    if (attribute == null && attributeList != null) {
      if (attributeListOwner instanceof JSFunction && ((JSFunction)attributeListOwner).isSetProperty()) {
        final PsiElement grandParent = attributeListOwner.getParent();

        if (grandParent instanceof JSClass) {
          final ResolveProcessor processor = new ResolveProcessor((attributeListOwner).getName());
          processor.setToProcessHierarchy(false);
          grandParent.processDeclarations(processor, ResolveState.initial(), grandParent, attributeListOwner);

          final List<PsiElement> elementList = processor.getResults();
          if (elementList != null && elementList.size() == 2) {
            final PsiElement firstElement = elementList.get(0);
            final PsiElement secondElement = elementList.get(1);

            JSAttributeListOwner chosedElement = null;

            if (firstElement instanceof JSFunction && ((JSFunction)firstElement).isGetProperty()) {
              chosedElement = (JSAttributeListOwner)firstElement;
            }
            else if (secondElement instanceof JSFunction && ((JSFunction)secondElement).isGetProperty()) {
              chosedElement = (JSAttributeListOwner)secondElement;
            }

            if (chosedElement != null) {
              return findAttr(chosedElement, name);
            }
          }
        }
      }
    }
    return attribute;
  }

  @Nullable
  public String validateValue(final XmlElement context, String value) {
    if (hasIdType()) {
      final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage());
      if (!namesValidator.isIdentifier(value, context.getProject())) {
        return JSBundle.message("invalid.identifier.value");
      }
    }

    if (value.indexOf('{') != -1) return null; // dynamic values

    if (isEnumerated()) {
      boolean inEnumeration = false;

      for (String s : getEnumeratedValues()) {
        if (Comparing.strEqual(value, s, myEnumeratedValuesCaseSensitive)) {
          inEnumeration = true;
          break;
        }
      }

      if (!inEnumeration) return FlexBundle.message("flex.invalid.enumeration.value", value);
    }

    if (isAllowsPercentage() && value != null && value.endsWith("%")) value = value.substring(0, value.length() - 1);

    boolean uint = false;
    if ("int".equals(type) || (uint = "uint".equals(type))) {
      try {
        boolean startWithSharp = false;
        if (value != null && ((startWithSharp = value.startsWith("#")) || value.startsWith("0x"))) {
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
        else if (!"Color".equals(format) || (value != null && value.length() > 0 && !Character.isLetter(value.charAt(0)))) {
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
        boolean startWithSharp = false;
        if (value != null && ((startWithSharp = value.startsWith("#")) || value.startsWith("0x"))) {
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

  @NonNls
  public String getName(final PsiElement context) {
    XmlTag tag = (XmlTag)context;
    if (tag.getName().indexOf("Rulezz") != -1) {
      // tag name completion
      final String namespaceByPrefix = tag.getPrefixByNamespace(parentDescriptor.context.namespace);
      if (namespaceByPrefix != null && namespaceByPrefix.length() > 0) return namespaceByPrefix + ":" + getName();
    }
    return getName();
  }

  public Object[] getDependences() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public String getQualifiedName() {
    return getName();
  }

  public String getDefaultName() {
    return getName();
  }

  public XmlElementDescriptor[] getElementsDescriptors(final XmlTag context) {
    if (context.getDescriptor() instanceof ClassBackedElementDescriptor) {
      return parentDescriptor.getElementsDescriptors(context);
    }

    if (arrayElementType != null) {
      return parentDescriptor.getElementDescriptorsInheritedFromGivenType(arrayElementType);
    }
    if (type != null && ClassBackedElementDescriptor.isAdequateType(type)) {
      return parentDescriptor.getElementDescriptorsInheritedFromGivenType(type);
    }

    if (ClassBackedElementDescriptor.IFACTORY_SHORT_CLASS_NAME.equals(ClassBackedElementDescriptor.className(type))) {
      return EMPTY_ARRAY;
    }

    return parentDescriptor.context.getAllDescriptors();
  }

  public XmlElementDescriptor getElementDescriptor(final XmlTag childTag, XmlTag contextTag) {
    if (XmlBackedJSClassImpl.isTagThatAllowsAnyXmlContent(contextTag)) {
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
      PsiElement element = JSResolveUtil.unwrapProxy(getDeclaration());
      if (element instanceof JSNamedElement) {
        element = new ClassBackedElementDescriptor(ClassBackedElementDescriptor.getPropertyType((JSNamedElement)element),
                                                   parentDescriptor.context, parentDescriptor.project, true).getDeclaration();
        return parentDescriptor.getClassIfDynamic(childTag.getLocalName(), element);
      }
    }
    return descriptor;
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(final @Nullable XmlTag context) {
    return XmlAttributeDescriptor.EMPTY;
  }

  public XmlAttributeDescriptor getAttributeDescriptor(final String attributeName, final @Nullable XmlTag context) {
    return ClassBackedElementDescriptor.isPrivateAttribute(attributeName, context) ? new AnyXmlAttributeDescriptor(attributeName) : null;
  }

  public XmlAttributeDescriptor getAttributeDescriptor(final XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  public XmlNSDescriptor getNSDescriptor() {
    return null;
  }

  @Override
  public XmlElementsGroup getTopGroup() {
    return null;
  }

  public int getContentType() {
    return CONTENT_TYPE_UNKNOWN;
  }

  public void validate(@NotNull final XmlElement context, @NotNull final ValidationHost host) {
    if (context instanceof XmlTag &&
        FlexSdkUtils.isFlex4Sdk(FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(parentDescriptor.context.module))) {
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
    if (message != null) host.addMessage(context, message, ValidationHost.ERROR);

    if (context instanceof XmlTag &&
        FlexStateElementNames.STATES.equals(((XmlTag)context).getLocalName()) &&
        JavaScriptSupportLoader.isMxmlNs(((XmlTag)context).getNamespace())) {
      XmlTag[] tags = ((XmlTag)context).findSubTags("State", ((XmlTag)context).getNamespace());
      XmlUtil.doDuplicationCheckForElements(tags, new HashMap<String, XmlTag>(tags.length), new XmlUtil.DuplicationInfoProvider<XmlTag>() {
        public String getName(@NotNull XmlTag xmlTag) {
          return xmlTag.getAttributeValue(FlexStateElementNames.NAME);
        }

        @NotNull
        public String getNameKey(@NotNull XmlTag xmlTag, @NotNull String name) {
          return getName(xmlTag);
        }

        @NotNull
        public PsiElement getNodeForMessage(@NotNull XmlTag xmlTag) {
          return xmlTag.getAttribute(FlexStateElementNames.NAME).getValueElement();
        }
      }, host);
    }
  }

  public boolean requiresCdataBracesInContext(@NotNull final XmlTag context) {
    return myScriptable;
  }

  public String getType() {
    return type;
  }

  public String getArrayType() {
    return arrayElementType;
  }

  public boolean allowElementsFromNamespace(String namespace, XmlTag context) {
    //if (type.equals("mx.core.IFactory") && JavaScriptSupportLoader.isLanguageNamespace(namespace)) {
    //  return true;
    //}
    //if (type != null || arrayElementType != null) {
    //  return true;
    //}
    return true;
  }

  public String getTypeName() {
    return myProperty ? "Function" : myAnnotationName;
  }

  public Icon getIcon() {
    if (myProperty) {
      return Icons.PROPERTY_ICON;
    }
    if (myAnnotationName == null) return null;
    if (myAnnotationName.equals(FlexAnnotationNames.EVENT)) {
      return Icons.EXCEPTION_CLASS_ICON;
    }
    if (myAnnotationName.equals(FlexAnnotationNames.STYLE)) {
      return CssFileType.ICON;
    }
    if (myAnnotationName.equals(FlexAnnotationNames.EFFECT)) {
      return EFFECT_ICON;
    }
    return null;
  }

  public boolean isPredefined() {
    return predefined;
  }

  public boolean isAllowsPercentage() {
    return percentProxy != null;
  }

  public String getPercentProxy() {
    return percentProxy;
  }

  public boolean isStyle() {
    return myAnnotationName != null && !myScriptable && myAnnotationName.equals(FlexAnnotationNames.STYLE);
  }

  public boolean isRichTextContent() {
    return myRichTextContent;
  }

  public boolean isCollapseWhiteSpace() {
    return myCollapseWhiteSpace;
  }

  public boolean isDeferredInstance() {
    return myDeferredInstance;
  }

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
    final List<String> result = new LinkedList<String>();

    result.add(Character.toUpperCase(type.charAt(0)) + (type.length() > 1 ? type.substring(1) : ""));

    for (int i = 1; i < type.length() - 1; i++) {
      if (Character.isUpperCase(type.charAt(i)) && !Character.isUpperCase(type.charAt(i + 1))) {
        result.add(type.substring(i));
      }
    }

    return result.toArray(new String[result.size()]);
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
    return s.substring(0, i).toLowerCase() + s.substring(i);
  }

  private static Set<String> getNamedElementsVisibleAt(final @NotNull PsiElement context) {
    final Set<String> names = new HashSet<String>();

    ResolveProcessor processor = new ResolveProcessor(null) {
      public boolean execute(final PsiElement element, final ResolveState state) {
        if (element instanceof JSNamedElement) {
          names.add(((JSNamedElement)element).getName());
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
