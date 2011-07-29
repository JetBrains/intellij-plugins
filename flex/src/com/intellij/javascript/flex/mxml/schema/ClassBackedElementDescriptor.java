package com.intellij.javascript.flex.mxml.schema;

import com.intellij.codeInsight.daemon.Validator;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.ide.IconProvider;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.javascript.flex.mxml.FlexNameAlias;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.index.JSTypeEvaluateManager;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.*;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import com.intellij.xml.util.XmlUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.*;

/**
 * @author Maxim.Mossienko
*/
public class ClassBackedElementDescriptor extends IconProvider implements XmlElementDescriptor, Validator<XmlTag>,
                                                                          XmlElementDescriptorWithCDataContent,
                                                                          XmlElementDescriptorAwareAboutChildren
{
  protected final @NonNls String className;
  protected final @NonNls String name;
  protected final @NonNls String iconPath;
  protected final Project project;
  protected final CodeContext context;
  private final boolean predefined;

  private Map<String, AnnotationBackedDescriptor> myDescriptors; // can be both XML attributes and elements
  private Map<String, Map<String, AnnotationBackedDescriptor>> myPackageToInternalDescriptors; // These descriptors are resolved only if MXML file is in the same package as descriptor originating element. Can be both XML attributes and elements.
  private Map<String, AnnotationBackedDescriptor> myPredefinedDescriptors; // can be XML attributes, but not elements

  @NonNls private static final String ARRAY_TYPE_ANNOTATION_PARAMETER = "arrayType";

  @NonNls private static final String RADIO_BUTTON_GROUP_CLASS = "mx.controls.RadioButtonGroup";

  private final static XmlUtil.DuplicationInfoProvider<XmlElement> myDuplicationInfoProvider = new XmlUtil.DuplicationInfoProvider<XmlElement>() {
    public String getName(@NotNull final XmlElement xmlElement) {
      if (xmlElement instanceof XmlTag) return ((XmlTag)xmlElement).getLocalName();
      return ((XmlAttribute)xmlElement).getName();
    }

    @NotNull
    public String getNameKey(@NotNull final XmlElement xmlElement, final @NotNull String name) {
      return name;
    }

    @NotNull
    public PsiElement getNodeForMessage(@NotNull final XmlElement xmlElement) {
      return xmlElement;
    }
  };

  private static final String IMPLEMENTS_ATTR_NAME = "implements";
  private AnnotationBackedDescriptor defaultPropertyDescriptor;
  private boolean defaultPropertyDescriptorInitialized;
  private boolean isContainerClass;
  private boolean isContainerClassInitialized;
  private static final String CONTAINER_CLASS_NAME_1 = "mx.core.IContainer";
  private static final String CONTAINER_CLASS_NAME_2 = "mx.core.IVisualElementContainer";
  @NonNls
  static final String IFACTORY_SHORT_CLASS_NAME = "IFactory";
  public static final String UI_COMPONENT_BASE_INTERFACE = "mx.core.IUIComponent";
  private static final String PRIMITIVE_GRAPHIC_ELEMENT_BASE_CLASS = "spark.primitives.supportClasses.GraphicElement";

  ClassBackedElementDescriptor(String name, String _classname, CodeContext _context, Project _project) {
    this(name, _classname,_context,_project, false, null);
  }

  ClassBackedElementDescriptor(String _classname, CodeContext _context, Project _project, boolean _predefined) {
    this(null,  _classname, _context, _project, _predefined, null);
  }

  ClassBackedElementDescriptor(String _name, String _classname, CodeContext _context, Project _project, boolean _predefined, String _iconPath) {
    context = _context;
    className = _classname;
    project = _project;
    predefined = _predefined;
    iconPath = _iconPath;
    name = _name;
  }

  public String getQualifiedName() {
    return className;
  }

  public String getDefaultName() {
    return getName();
  }

  public boolean isPredefined(){
    return predefined;
  }

  public XmlElementDescriptor[] getElementsDescriptors(final XmlTag _context) {
    if (XmlBackedJSClassImpl.isTagOrInsideTagThatAllowsAnyXmlContent(_context) || MxmlLanguageTagsUtil.isFxReparentTag(_context)) {
      return EMPTY_ARRAY;
    }

    if (MxmlLanguageTagsUtil.isFxLibraryTag(_context)) {
      final XmlElementDescriptor definitionDescriptor = context.getElementDescriptor(CodeContext.DEFINITION_TAG_NAME, (XmlTag)null);
      return definitionDescriptor == null ? EMPTY_ARRAY : new XmlElementDescriptor[]{definitionDescriptor};
    }

    final XmlElementDescriptor _parentDescriptor = _context.getDescriptor();

    if (_parentDescriptor instanceof AnnotationBackedDescriptorImpl) {
      final String arrayType = ((AnnotationBackedDescriptorImpl)_parentDescriptor).getArrayType();
      if (arrayType != null) {
        return getElementDescriptorsInheritedFromGivenType(arrayType);
      }

      final String type = ((AnnotationBackedDescriptorImpl)_parentDescriptor).getType();
      if (type != null && isAdequateType(type)) {
        return getElementDescriptorsInheritedFromGivenType(type);
      }

      if (IFACTORY_SHORT_CLASS_NAME.equals(className(type)) &&
          JavaScriptSupportLoader.isLanguageNamespace(context.namespace)) {
        final XmlElementDescriptor descriptor = context.getElementDescriptor(FlexNameAlias.COMPONENT_TYPE_NAME, (XmlTag)null);
        return descriptor == null ? EMPTY_ARRAY : new XmlElementDescriptor[]{descriptor};
      }
    }

    if (!(_parentDescriptor instanceof ClassBackedElementDescriptor)) {
      return EMPTY_ARRAY;
    }

    final ClassBackedElementDescriptor parentDescriptor = (ClassBackedElementDescriptor)_parentDescriptor;

    getAttributesDescriptors(_context);
    List<XmlElementDescriptor> resultList =
      new ArrayList<XmlElementDescriptor>(myDescriptors == null ? 0 : myDescriptors.size() + context.getAllDescriptorsSize());
    final boolean isComponentTag = MxmlLanguageTagsUtil.isComponentTag(_context);
    boolean includeProperties = (parentDescriptor == this) && !isComponentTag;

    if (isComponentTag) {
      ContainerUtil.addAll(resultList, getElementDescriptorsInheritedFromGivenType(UI_COMPONENT_BASE_INTERFACE));
      ContainerUtil.addAll(resultList, getElementDescriptorsInheritedFromGivenType(PRIMITIVE_GRAPHIC_ELEMENT_BASE_CLASS));
    }
    else if (parentDescriptor.getDefaultPropertyDescriptor() != null && parentDescriptor.defaultPropertyDescriptor.getType() != null) {
      final PsiElement contextParent = _context.getParent();
      if (contextParent instanceof XmlDocument && JavaScriptSupportLoader.isLanguageNamespace(_context.getNamespace())) {
        // Predefined tags like <fx:Declaration/> can be children of a tag with [DefaultProperty] annotation if this tag is root tag in the mxml file
        for (XmlElementDescriptor descriptor : context.getAllDescriptors()) {
          if (descriptor instanceof ClassBackedElementDescriptor && ((ClassBackedElementDescriptor)descriptor).predefined) {
            resultList.add(descriptor);
          }
        }
      }

      final String type = parentDescriptor.getDefaultPropertyType();
      ContainerUtil.addAll(resultList, isAdequateType(type)
                                       ? getElementDescriptorsInheritedFromGivenType(type)
                                       : context.getAllDescriptors());

      if (JavaScriptSupportLoader.isLanguageNamespace(context.namespace)) {
        ContainerUtil.addIfNotNull(context.getElementDescriptor(FlexPredefinedTagNames.SCRIPT, (XmlTag)null), resultList);
        ContainerUtil.addIfNotNull(context.getElementDescriptor(CodeContext.REPARENT_TAG_NAME, (XmlTag)null), resultList);

        if (contextParent instanceof XmlTag && MxmlLanguageTagsUtil.isComponentTag((XmlTag)contextParent)) {
          ContainerUtil.addIfNotNull(context.getElementDescriptor(FlexPredefinedTagNames.DECLARATIONS, (XmlTag)null), resultList);
          ContainerUtil.addIfNotNull(context.getElementDescriptor(FlexPredefinedTagNames.BINDING, (XmlTag)null), resultList);
          ContainerUtil.addIfNotNull(context.getElementDescriptor(FlexPredefinedTagNames.STYLE, (XmlTag)null), resultList);
          ContainerUtil.addIfNotNull(context.getElementDescriptor(FlexPredefinedTagNames.METADATA, (XmlTag)null), resultList);
        }
      }
    }
    else if (parentDescriptor.predefined || isContainerClass(parentDescriptor)) {
      context.appendAllDescriptors(resultList);
    }
    if (includeProperties && myDescriptors != null) {
      resultList.addAll(myDescriptors.values());

      if (!myPackageToInternalDescriptors.isEmpty()) {
        final String contextPackage = JSResolveUtil.getPackageNameFromPlace(_context);
        final Map<String, AnnotationBackedDescriptor> internalDescriptors = myPackageToInternalDescriptors.get(contextPackage);
        if (internalDescriptors != null) {
          resultList.addAll(internalDescriptors.values());
        }
      }
    }
    return resultList.toArray(new XmlElementDescriptor[resultList.size()]);
  }

  private static boolean isContainerClass(final ClassBackedElementDescriptor descriptor) {
    if (!descriptor.isContainerClassInitialized) {
      if (descriptor.predefined) {
        descriptor.isContainerClass = false;
      }
      else {
        final PsiElement declaration = descriptor.getDeclaration();
        descriptor.isContainerClass = JSResolveUtil.isAssignableType(CONTAINER_CLASS_NAME_1, descriptor.className, declaration) ||
                                      JSResolveUtil.isAssignableType(CONTAINER_CLASS_NAME_2, descriptor.className, declaration);
      }
      descriptor.isContainerClassInitialized = true;
    }
    return descriptor.isContainerClass;
  }

  private String getDefaultPropertyType() {
    return defaultPropertyDescriptor.getArrayType() != null ? defaultPropertyDescriptor.getArrayType() : defaultPropertyDescriptor.getType();
  }

  @Nullable
  public XmlElementDescriptor getElementDescriptor(final XmlTag childTag, final XmlTag contextTag) {
    if (XmlBackedJSClassImpl.isTagThatAllowsAnyXmlContent(contextTag)) {
      return new AnyXmlElementWithAnyChildrenDescriptor();
    }

    if (MxmlLanguageTagsUtil.isFxReparentTag(contextTag)) {
      return null;
    }

    final XmlElementDescriptor descriptor = _getElementDescriptor(childTag, contextTag);
    if (descriptor instanceof AnnotationBackedDescriptorImpl && ((AnnotationBackedDescriptorImpl)descriptor).isPredefined()) {
      return null;
    }

    if (MxmlLanguageTagsUtil.isComponentTag(contextTag) &&
        descriptor instanceof ClassBackedElementDescriptor &&
        ((ClassBackedElementDescriptor)descriptor).isPredefined()) {
      return null;
    }

    if (MxmlLanguageTagsUtil.isFxReparentTag(childTag) ||
        MxmlLanguageTagsUtil.isScriptTag(childTag) ||
        MxmlLanguageTagsUtil.isDesignLayerTag(childTag)) {
      return descriptor;
    }

    if (descriptor == null && JavaScriptSupportLoader.MXML_URI3.equals(childTag.getNamespace()) && contextTag != null) {
      return FxDefinitionBackedDescriptor.getFxDefinitionBackedDescriptor(context.module, childTag);
    }

    final XmlElementDescriptor parentDescriptor = contextTag.getDescriptor();

    if (MxmlLanguageTagsUtil.isComponentTag(contextTag)) {
      final XmlElementDescriptor checkedDescriptor = checkValidDescriptorAccordingToType(UI_COMPONENT_BASE_INTERFACE, descriptor);
      return checkedDescriptor != null ? checkedDescriptor
                                       : checkValidDescriptorAccordingToType(PRIMITIVE_GRAPHIC_ELEMENT_BASE_CLASS, descriptor);
    }
    else if (getDefaultPropertyDescriptor() != null && defaultPropertyDescriptor.getType() != null) {
      if (descriptor instanceof ClassBackedElementDescriptor && ((ClassBackedElementDescriptor)descriptor).predefined) {
        final PsiElement contextParent = contextTag.getParent();
        if (contextParent instanceof XmlDocument) {
          return MxmlLanguageTagsUtil.isLanguageTagAllowedUnderRootTag(childTag) ? descriptor : null;
        }
        else if (contextParent instanceof XmlTag && MxmlLanguageTagsUtil.isComponentTag((XmlTag)contextParent)) {
          return MxmlLanguageTagsUtil.isLanguageTagAllowedUnderInlineComponentRootTag(childTag) ? descriptor : null;
        }
      }

      return checkValidDescriptorAccordingToType(getDefaultPropertyType(), descriptor);
    }
    else if (needToCheckThatChildIsUiComponent(contextTag, parentDescriptor, descriptor)) {
      return checkValidDescriptorAccordingToType(UI_COMPONENT_BASE_INTERFACE, descriptor);
    }

    // Ideally all not-null results should be returned in if's above and the last statement should be 'return null'.
    return descriptor;
  }

  private static boolean needToCheckThatChildIsUiComponent(final XmlTag parentTag,
                                                           final XmlElementDescriptor _parentDescriptor,
                                                           final XmlElementDescriptor _childDescriptor) {
    // non-visual components in Flex 3 are allowed only under document root tag and under inline component root tag;
    //     in Flex 4 - only inside <fx:Declarations> or <fx:Definition>
    // mx.controls.RadioButtonGroup is a compiler-level exception
    if (!(_childDescriptor instanceof ClassBackedElementDescriptor) || !(_parentDescriptor instanceof ClassBackedElementDescriptor)) {
      return false;
    }
    final ClassBackedElementDescriptor childDescriptor = (ClassBackedElementDescriptor)_childDescriptor;
    final ClassBackedElementDescriptor parentDescriptor = (ClassBackedElementDescriptor)_parentDescriptor;

    if (childDescriptor.predefined) {
      return false;
    }

    if (MxmlLanguageTagsUtil.isFxDeclarationsTag(parentTag) || MxmlLanguageTagsUtil.isFxDefinitionTag(parentTag)) {
      return false;
    }

    final PsiElement parent = parentTag.getParent();
    if (!ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI3, parentTag.knownNamespaces()) &&
        (parent instanceof XmlDocument || (parent instanceof XmlTag && MxmlLanguageTagsUtil.isComponentTag((XmlTag)parent)))) {
      return false;
    }

    if (JSResolveUtil.isAssignableType(RADIO_BUTTON_GROUP_CLASS, childDescriptor.className, parentTag)) {
      return false;
    }

    return isContainerClass(parentDescriptor);
  }

  @Nullable
  private XmlElementDescriptor _getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    final String childNs = childTag.getNamespace();

    if (!sameNs(childNs, context.namespace)) {
      final XmlNSDescriptor descriptor = childTag.getNSDescriptor(childNs, true);
      return descriptor != null ? descriptor.getElementDescriptor(childTag):null;
    }

    getAttributesDescriptors(childTag);
    @NonNls String localName = childTag.getLocalName();
    localName = skipStateNamePart(localName);

    XmlElementDescriptor descriptor = myDescriptors != null ? myDescriptors.get(localName) : null;

    if (descriptor == null && myPackageToInternalDescriptors != null && !myPackageToInternalDescriptors.isEmpty()) {
      final String contextPackage = JSResolveUtil.getPackageNameFromPlace(childTag);
      final Map<String, AnnotationBackedDescriptor> internalDescriptors = myPackageToInternalDescriptors.get(contextPackage);
      if (internalDescriptors != null) {
        descriptor = internalDescriptors.get(localName);
      }
    }

    if (descriptor != null) return descriptor;

    final @NonNls String name = getName();
    if ("WebService".equals(name) && "operation".equals(localName)) {
      return context.getElementDescriptor("WebServiceOperation", (XmlTag)null);
    } else if ("RemoteObject".equals(name) && "method".equals(localName)) {
      return context.getElementDescriptor("RemoteObjectOperation", (XmlTag)null);
    }

    XmlElementDescriptor xmlElementDescriptor = context.getElementDescriptor(localName, childTag);

    if (xmlElementDescriptor == null && !predefined) {
      xmlElementDescriptor = getClassIfDynamic(localName, getDeclaration());
    }
    return xmlElementDescriptor;
  }

  static boolean sameNs(final String childNs, final String namespace) {
    final boolean b = childNs.equals(namespace);
    if (!b) {
      return JavaScriptSupportLoader.isLanguageNamespace(childNs) && JavaScriptSupportLoader.isLanguageNamespace(namespace);
    }
    return b;
  }

  @Nullable
  XmlElementDescriptor getClassIfDynamic(final String localName, PsiElement element) {
    if (isDynamicClass(element)) {
      return new ClassBackedElementDescriptor(
        localName.length() > 0 && Character.isUpperCase(localName.charAt(0)) ? localName : OBJECT_CLASS_NAME, context, project, true);
    }
    return null;
  }

  static boolean isDynamicClass(PsiElement element) {
    element = JSResolveUtil.unwrapProxy(element);
    JSAttributeList attrList;
    return element instanceof JSClass &&
        (attrList = ((JSClass)element).getAttributeList()) != null &&
        attrList.hasModifier(JSAttributeList.ModifierType.DYNAMIC);
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(final @Nullable XmlTag _context) {
    if (MxmlLanguageTagsUtil.isFxPrivateTag(_context) || MxmlLanguageTagsUtil.isFxLibraryTag(_context)) {
      return XmlAttributeDescriptor.EMPTY;
    }

    if (MxmlLanguageTagsUtil.isFxDefinitionTag(_context)) {
      return new XmlAttributeDescriptor[]{
        new AnnotationBackedDescriptorImpl(MxmlLanguageTagsUtil.NAME_ATTRIBUTE, this, true, null, null, null)};
    }

    if (_context != null && MxmlLanguageTagsUtil.isComponentTag(_context)) {
      return new XmlAttributeDescriptor[]{
        new ClassNameAttributeDescriptor(null),
        new AnnotationBackedDescriptorImpl(FlexMxmlLanguageAttributeNames.ID, this, true, null, null, null)
      };
    }

    if (myDescriptors == null || myPackageToInternalDescriptors == null) {
      PsiElement element = getDeclaration();
      if (element == null) {
        myDescriptors = Collections.emptyMap();
        myPackageToInternalDescriptors = Collections.emptyMap();
      }
      else {
        ensureDescriptorsMapsInitialized(element, null);
      }
    }

    final Collection<AnnotationBackedDescriptor> descriptors = new ArrayList<AnnotationBackedDescriptor>(myDescriptors.values());

    if (_context != null && !myPackageToInternalDescriptors.isEmpty()) {
      final String contextPackage = JSResolveUtil.getPackageNameFromPlace(_context);
      final Map<String, AnnotationBackedDescriptor> internalDescriptors = myPackageToInternalDescriptors.get(contextPackage);
      if (internalDescriptors != null) {
        descriptors.addAll(internalDescriptors.values());
      }
    }

    if (_context != null && _context.getParent() instanceof XmlDocument) {
      final AnnotationBackedDescriptor idDescriptor = myDescriptors.get(FlexMxmlLanguageAttributeNames.ID);
      if (idDescriptor != null) {
        descriptors.remove(idDescriptor);
      }
      descriptors.add(new AnnotationBackedDescriptorImpl(IMPLEMENTS_ATTR_NAME, this, true, null, null, null));

      // do not include id, includeIn, excludeFrom, itemCreationPolicy, itemDestructionPolicy attributes for root tag
      if (myPredefinedDescriptors != null) {
        for (final AnnotationBackedDescriptor descriptor : myPredefinedDescriptors.values()) {
          if (!FlexMxmlLanguageAttributeNames.ID.equals(descriptor.getName())
              && !ArrayUtil.contains(descriptor.getName(), CodeContext.GUMBO_ATTRIBUTES)) {
            descriptors.add(descriptor);
          }
        }
      }
    }
    else if (myPredefinedDescriptors != null) {
      descriptors.addAll(myPredefinedDescriptors.values());
    }
    return descriptors.toArray(new XmlAttributeDescriptor[descriptors.size()]);
  }

  public void addPredefinedMemberDescriptor(@NotNull AnnotationBackedDescriptor descriptor) {
    if (predefined) {
      if (myDescriptors == null) {
        myDescriptors = new THashMap<String, AnnotationBackedDescriptor>();
        myPackageToInternalDescriptors = Collections.emptyMap();
      }
      myDescriptors.put(descriptor.getName(), descriptor);
    } else {
      if (myPredefinedDescriptors == null) myPredefinedDescriptors = new THashMap<String, AnnotationBackedDescriptor>();
      myPredefinedDescriptors.put(descriptor.getName(), descriptor);
    }
  }

  private void ensureDescriptorsMapsInitialized(PsiElement element, @Nullable Set<JSClass> visited) {
    Map<String, AnnotationBackedDescriptor> map;
    Map<String, Map<String, AnnotationBackedDescriptor>> packageToInternalDescriptors;

    synchronized (context) {
      map = myDescriptors;
      packageToInternalDescriptors = myPackageToInternalDescriptors;
      if (map != null && packageToInternalDescriptors != null) return;

      map = new THashMap<String, AnnotationBackedDescriptor>();
      packageToInternalDescriptors = new THashMap<String, Map<String, AnnotationBackedDescriptor>>();
      Set<PsiElement> processedElements = null;

      if (element instanceof XmlBackedJSClassImpl) {
        element = element.getParent().getContainingFile(); // TODO: make this code and following loop better
      }

      if (element instanceof XmlFile && JavaScriptSupportLoader.isFxgFile((PsiFile)element)) {
        element = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)element);
      }

      while (element instanceof XmlFile) {
        final XmlDocument document = ((XmlFile)element).getDocument();
        final XmlTag rootTag = document != null ? document.getRootTag():null;
        final XmlElementDescriptor descriptor = rootTag != null ? rootTag.getDescriptor():null;
        if (processedElements == null) processedElements = new THashSet<PsiElement>();
        processedElements.add(element);

        element = descriptor != null ? descriptor.getDeclaration():null;
        if (processedElements.contains(element)) break;
        collectMxmlAttributes(map, packageToInternalDescriptors, rootTag);
      }

      if (element instanceof JSNamedElement) {
        JSNamedElement jsClass = (JSNamedElement)element;
        jsClass = (JSNamedElement)JSResolveUtil.unwrapProxy(jsClass);

        if (visited == null || !visited.contains(jsClass)) {
          if (!XmlBackedJSClassImpl.XML_TAG_NAME.equals(jsClass.getName()) && !XmlBackedJSClassImpl.XMLLIST_TAG_NAME.equals(jsClass.getName())) {
            JSReferenceList extendsList = jsClass instanceof JSClass ? ((JSClass)jsClass).getExtendsList():null;
            if (extendsList != null) {
              final JSClass clazz = (JSClass)jsClass;
              if (visited == null) {
                visited = new THashSet<JSClass>();
              }
              visited.add(clazz);

              for(JSClass superClazz: clazz.getSuperClasses()) {
                appendSuperClassDescriptors(map, packageToInternalDescriptors, superClazz, visited);
              }
            } else if (!OBJECT_CLASS_NAME.equals(jsClass.getName()) && CodeContext.isStdNamespace(context.namespace)) {
              appendSuperClassDescriptors(
                  map,
                  packageToInternalDescriptors,
                  JSResolveUtil.unwrapProxy(JSResolveUtil.findClassByQName(OBJECT_CLASS_NAME, jsClass)),
                  visited);
            }
          }

          collectMyAttributes(jsClass, map, packageToInternalDescriptors);
        }
      }
      myDescriptors = map;
      myPackageToInternalDescriptors = packageToInternalDescriptors;
    }
  }

  private void collectMxmlAttributes(final Map<String, AnnotationBackedDescriptor> map,
                                     final Map<String, Map<String, AnnotationBackedDescriptor>> packageToInternalDescriptors,
                                     final XmlTag rootTag) {
    if (rootTag != null) {
      final JSResolveUtil.JSInjectedFilesVisitor injectedFilesVisitor = new JSResolveUtil.JSInjectedFilesVisitor() {
        protected void process(final JSFile file) {
          collectMyAttributes(file, map, packageToInternalDescriptors);
        }
      };
      FlexUtils.processMxmlTags(rootTag, injectedFilesVisitor);

      processClassBackedTagsWithIdAttribute(rootTag, new Processor<Pair<XmlAttribute, String>>() {
        public boolean process(final Pair<XmlAttribute, String> idAttributeAndItsType) {
          final XmlAttribute idAttribute = idAttributeAndItsType.first;
          final String idAttributeValue = idAttribute.getValue();
          final String type = idAttributeAndItsType.second;
          map.put(idAttributeValue,
                  new AnnotationBackedDescriptorImpl(idAttributeValue, ClassBackedElementDescriptor.this, false, type, null, idAttribute));
          return true;
        }
      });
    }
  }

  static boolean processClassBackedTagsWithIdAttribute(final @NotNull XmlTag tag, final Processor<Pair<XmlAttribute, String>> processor) {
    boolean toContinue = true;
    final XmlElementDescriptor tagDescriptor = tag.getDescriptor();

    if (tagDescriptor instanceof ClassBackedElementDescriptor) {
      final XmlAttribute idAttribute = tag.getAttribute(FlexMxmlLanguageAttributeNames.ID);
      if (idAttribute != null && !StringUtil.isEmpty(idAttribute.getValue())) {
        toContinue = processor.process(Pair.create(idAttribute, tagDescriptor.getQualifiedName()));
      }

      if (toContinue) {
        for (final XmlTag childTag : tag.getSubTags()) {
          if (toContinue) {
            toContinue = processClassBackedTagsWithIdAttribute(childTag, processor);
          }
        }
      }
    }

    return toContinue;
  }

  private void collectMyAttributes(final PsiElement jsClass,
                                   final Map<String, AnnotationBackedDescriptor> map,
                                   final Map<String, Map<String, AnnotationBackedDescriptor>> packageToInternalDescriptors) {
    processAttributes(jsClass, new AttributedItemsProcessor() {
      public boolean process(final JSNamedElement jsNamedElement, final boolean isPackageLocalVisibility) {
        String name = jsNamedElement.getName();
        if (name != null) {
          if (jsNamedElement instanceof JSVariable && ((JSVariable)jsNamedElement).isConst()) return true;
          String propertyType = getPropertyType(jsNamedElement);
          boolean deferredInstance = false;
          final JSAttributeList attributeList = ((JSAttributeListOwner)jsNamedElement).getAttributeList();

          if (attributeList != null) {
            String instanceType = JSPsiImplUtils.getTypeFromAnnotationParameter(attributeList, "InstanceType", null);
            if (instanceType != null) {
              deferredInstance = true;
              propertyType = instanceType;
            }
          }

          if (propertyType != null && propertyType.length() > 0 &&
              ( Character.isUpperCase(propertyType.charAt(0)) || propertyType.indexOf('.') >= 0 ) &&
              !STRING_CLASS_NAME.equals(propertyType) &&
              !NUMBER_CLASS_NAME.equals(propertyType) &&
              !BOOLEAN_CLASS_NAME.equals(propertyType)) {
            String arrayType = null;

            if(JSTypeEvaluateManager.isArrayType(propertyType)) {
              arrayType = JSTypeEvaluateManager.getComponentType(propertyType);
            } else if (ARRAY_CLASS_NAME.equals(propertyType)) {
              if (attributeList != null) {
                arrayType = JSPsiImplUtils.getTypeFromAnnotationParameter(attributeList, FlexAnnotationNames.INSPECTABLE, ARRAY_TYPE_ANNOTATION_PARAMETER);
                if (arrayType == null) {
                  arrayType = JSPsiImplUtils.getArrayElementTypeFromAnnotation(attributeList);
                }
              }

              // getter may have annotation which is applicable for setter
              if (arrayType == null &&
                  jsClass instanceof JSClass &&
                  jsNamedElement instanceof JSFunction &&
                  ((JSFunction)jsNamedElement).isSetProperty()) {
                final JSFunction getter =
                  ((JSClass)jsClass).findFunctionByNameAndKind(jsNamedElement.getName(), JSFunction.FunctionKind.GETTER);
                final JSAttributeList getterAttributeList = getter == null ? null : getter.getAttributeList();
                if (getterAttributeList != null) {
                  arrayType = JSPsiImplUtils
                    .getTypeFromAnnotationParameter(getterAttributeList, FlexAnnotationNames.INSPECTABLE, ARRAY_TYPE_ANNOTATION_PARAMETER);
                  if (arrayType == null) {
                    arrayType = JSPsiImplUtils.getArrayElementTypeFromAnnotation(getterAttributeList);
                  }
                }
              }

            } else {
              propertyType = JSImportHandlingUtil.resolveTypeName(propertyType, jsNamedElement);
            }

            if (arrayType != null) {
              arrayType = JSImportHandlingUtil.resolveTypeName(arrayType, jsNamedElement);
            }

            putDescriptor(jsNamedElement, name, propertyType, arrayType, deferredInstance, isPackageLocalVisibility, map,
                          packageToInternalDescriptors);
          }
          else {
            putDescriptor(jsNamedElement, name, propertyType, null, deferredInstance, isPackageLocalVisibility, map,
                          packageToInternalDescriptors);
          }
        }
        return true;
      }

      public boolean process(final JSAttributeNameValuePair pair, final String annotationName, final boolean included) {
        String name = pair.getSimpleValue();

        if (name != null) {
          if (included) {
            final AnnotationBackedDescriptorImpl previousDescriptor = (AnnotationBackedDescriptorImpl)map.get(name);
            final AnnotationBackedDescriptorImpl descriptor =
              new AnnotationBackedDescriptorImpl(name, ClassBackedElementDescriptor.this, false, null, null, pair);
            if (previousDescriptor == null || !previousDescriptor.isPreferredTo(descriptor)) {
              map.put(name, descriptor);
            }
          }
          else map.remove(name);
        }
        return true;
      }
    });

    if (predefined &&
        (FlexPredefinedTagNames.SCRIPT.equals(className) || FlexPredefinedTagNames.STYLE.equals(className))) {
      map.put(FlexReferenceContributor.SOURCE_ATTR_NAME,
              new AnnotationBackedDescriptorImpl(FlexReferenceContributor.SOURCE_ATTR_NAME, this, true, null, null, null));
    }

    if (!predefined && map.get(FlexMxmlLanguageAttributeNames.ID) == null) {
      addPredefinedMemberDescriptor(new AnnotationBackedDescriptorImpl(FlexMxmlLanguageAttributeNames.ID, this, true, null, null, null));
    }
  }

  private void putDescriptor(final JSNamedElement jsNamedElement,
                             final String name,
                             final String propertyType,
                             final String arrayType,
                             final boolean deferredInstance,
                             final boolean isPackageLocalVisibility,
                             final Map<String, AnnotationBackedDescriptor> map,
                             final Map<String, Map<String, AnnotationBackedDescriptor>> packageToInternalDescriptors) {
    AnnotationBackedDescriptorImpl previousDescriptor = (AnnotationBackedDescriptorImpl)map.get(name);
    AnnotationBackedDescriptorImpl descriptor;

    if (previousDescriptor != null &&
        previousDescriptor.parentDescriptor != this &&
        jsNamedElement instanceof JSAttributeListOwner &&
        AnnotationBackedDescriptorImpl.findInspectableAttr(jsNamedElement) == null
       ) {
      descriptor = new AnnotationBackedDescriptorImpl(name, this, previousDescriptor, jsNamedElement);
    } else {
      descriptor = new AnnotationBackedDescriptorImpl(name, this, false, propertyType, arrayType, deferredInstance, jsNamedElement);
    }

    if (previousDescriptor == null || !previousDescriptor.isPreferredTo(descriptor)) {
      if (isPackageLocalVisibility) {
        final String packageName = JSResolveUtil.getPackageNameFromPlace(jsNamedElement);
        if (packageName != null) {
          Map<String, AnnotationBackedDescriptor> descriptorMap = packageToInternalDescriptors.get(packageName);
          if (descriptorMap == null) {
            descriptorMap = new THashMap<String, AnnotationBackedDescriptor>();
            packageToInternalDescriptors.put(packageName, descriptorMap);
          }
          descriptorMap.put(name, descriptor);
        }
      }
      else {
        map.put(name, descriptor);
      }
    }
  }

  static String getPropertyType(final JSNamedElement jsNamedElement) {
    return jsNamedElement instanceof JSVariable ? ((JSVariable)jsNamedElement).getTypeString() : JSResolveUtil.getTypeFromSetAccessor(jsNamedElement);
  }

  private void appendSuperClassDescriptors(final Map<String, AnnotationBackedDescriptor> map,
                                           final Map<String, Map<String, AnnotationBackedDescriptor>> packageToInternalDescriptors,
                                           final PsiElement _clazz,
                                           @Nullable Set<JSClass> visited) {
    if (_clazz instanceof JSClass) {
      final JSClass clazz = (JSClass)_clazz;

      ClassBackedElementDescriptor parentDescriptor = context.getElementDescriptor(clazz.getName(), clazz.getQualifiedName());
      if (parentDescriptor == null) {
        parentDescriptor = new ClassBackedElementDescriptor(null, clazz.getQualifiedName(), context, project);
      }

      parentDescriptor.ensureDescriptorsMapsInitialized(clazz, visited);

      map.putAll(parentDescriptor.myDescriptors);

      for (final Map.Entry<String, Map<String, AnnotationBackedDescriptor>> entry : parentDescriptor.myPackageToInternalDescriptors
        .entrySet()) {

        Map<String, AnnotationBackedDescriptor> descriptorMap = packageToInternalDescriptors.get(entry.getKey());
        if (descriptorMap == null) {
          descriptorMap = new THashMap<String, AnnotationBackedDescriptor>();
          packageToInternalDescriptors.put(entry.getKey(), descriptorMap);
        }
        descriptorMap.putAll(entry.getValue());
      }
    }
  }

  public void validate(@NotNull final XmlTag tag, @NotNull final ValidationHost host) {
    if (FlexSdkUtils.isFlex4Sdk(FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(context.module))) {
      MxmlLanguageTagsUtil.checkFlex4Attributes(tag, host, true);
    }

    if (MxmlLanguageTagsUtil.isFxPrivateTag(tag)) {
      MxmlLanguageTagsUtil.validateFxPrivateTag(tag, host);
      return;
    }

    if (MxmlLanguageTagsUtil.isFxLibraryTag(tag)) {
      MxmlLanguageTagsUtil.validateFxLibraryTag(tag, host);
      return;
    }

    if (MxmlLanguageTagsUtil.isFxDefinitionTag(tag)) {
      MxmlLanguageTagsUtil.validateFxDefinitionTag(tag, host);
      return;
    }

    if (MxmlLanguageTagsUtil.isFxReparentTag(tag)) {
      MxmlLanguageTagsUtil.validateFxReparentTag(tag, host);
      return;
    }

    for(XmlTag subtag:tag.getSubTags()) {
      final String s = subtag.getLocalName();

      if (s.length() > 0 && Character.isLowerCase(s.charAt(0))) {
        if (tag.getAttributeValue(s) != null) {
          final List<XmlElement> elements = new ArrayList<XmlElement>();
          collectLowerCasedElements(elements, tag.getAttributes());
          collectLowerCasedElements(elements, tag.getSubTags());

          XmlUtil.doDuplicationCheckForElements(
            elements.toArray(new XmlElement[elements.size()]),
            new THashMap<String, XmlElement>(elements.size()), myDuplicationInfoProvider,
            host
          );

          break;
        }
      }
    }

    if (tag.getParent() instanceof XmlDocument) {
      final JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)tag.getContainingFile());
      final JSReferenceList list = jsClass.getImplementsList();
      final MxmlErrorReportingClient reportingClient = new MxmlErrorReportingClient(host);

      if (list != null) {
        JSAnnotatingVisitor.checkImplementedMethods(jsClass, reportingClient);
        for(JSReferenceExpression expr:list.getExpressions()) {
          PsiElement element = expr.resolve();
          if (element == jsClass) {
            reportingClient.reportError(
              refToImplementsNode(tag), // TODO: list is artificial node without context, cannot bind to it
              JSBundle.message("javascript.validation.message.circular.dependency"),
              JSAnnotatingVisitor.ErrorReportingClient.ProblemKind.ERROR);
            continue;
          }

          if (element instanceof JSClass && !((JSClass)element).isInterface()) {
            reportingClient.reportError(
              refToImplementsNode(tag),
              JSBundle.message("javascript.validation.message.interface.name.expected.here"),
              JSAnnotatingVisitor.ErrorReportingClient.ProblemKind.ERROR);
          }
        }
      }

      JSClass[] classes = jsClass.getSuperClasses();
      if (classes.length > 0 && classes[0] == jsClass) {
        reportingClient.reportError(
          tag.getNode(),
          JSBundle.message("javascript.validation.message.circular.dependency"),
          JSAnnotatingVisitor.ErrorReportingClient.ProblemKind.ERROR);
      }

      JSAnnotatingVisitor.checkFileUnderSourceRoot(jsClass, reportingClient);
    }

    if (MxmlLanguageTagsUtil.isComponentTag(tag)) {
      if (tag.getSubTags().length == 0) {
        host.addMessage(
          tag,
          JSBundle.message("javascript.validation.empty.component.type"),
          ValidationHost.ErrorType.ERROR
        );
      }
    }

    //if (defaultPropertyDescriptor != null && defaultPropertyDescriptor.getArrayType() == null) {
    //  // TODO
    //  for(XmlTag subtag:tag.getSubTags()) {
    //    final String s = subtag.getLocalName();
    //  }
    //}
  }

  private static ASTNode refToImplementsNode(XmlTag tag) {
    return tag.getAttribute("implements").getValueElement().getChildren()[1].getNode();
  }

  private static <T extends XmlElement & PsiNamedElement> void collectLowerCasedElements(final List<XmlElement> elements, final T[] xmlAttributes) {
    for(T a: xmlAttributes) {
      final String name = a instanceof XmlTag ? ((XmlTag)a).getLocalName() : a.getName();
      if (name != null && name.length() > 0 && Character.isLowerCase(name.charAt(0))) elements.add(a);
    }
  }

  public boolean requiresCdataBracesInContext(@NotNull final XmlTag context) {
    return predefined && XmlBackedJSClassImpl.SCRIPT_TAG_NAME.equals(context.getLocalName());
  }

  public Icon getIcon(@NotNull final PsiElement element, final int flags) {
    if (iconPath != null) {
      final PsiFile containingFile = element.getContainingFile();
      final VirtualFile file = containingFile.getVirtualFile();

      if (file != null) {
        final VirtualFile relativeFile = VfsUtil.findRelativeFile(iconPath, file.getParent());
        if (relativeFile != null) {
          try {
            return new ImageIcon(new URL(VfsUtil.fixIDEAUrl(relativeFile.getUrl())));
          }
          catch (MalformedURLException ignored) {}
        }
      }
    }
    return null;
  }

  XmlElementDescriptor[] getElementDescriptorsInheritedFromGivenType(@NotNull String arrayElementType) {
    final PsiElement declaration = getDeclaration();
    if (declaration == null) {
      return EMPTY_ARRAY;
    }

    final PsiElement clazz = JSResolveUtil.unwrapProxy(JSResolveUtil.findClassByQName(arrayElementType, declaration));
    if (!(clazz instanceof JSClass)) {
      return EMPTY_ARRAY;
    }

    final JSClass jsClass = (JSClass)clazz;
    final JSAttributeList attributeList = jsClass.getAttributeList();
    final boolean isFinalClass = attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.FINAL);
    final List<XmlElementDescriptor> result = new ArrayList<XmlElementDescriptor>();

    if (isFinalClass) {
      final String jsClassName = jsClass.getName();
      if (jsClassName != null) {
        final XmlElementDescriptor descriptor = context.getElementDescriptor(jsClassName, jsClass.getQualifiedName());
        ContainerUtil.addIfNotNull(descriptor, result);
      }
    }
    else {
      for (final XmlElementDescriptor descriptor : context.getAllDescriptors()) {
        if (descriptor instanceof ClassBackedElementDescriptor && !((ClassBackedElementDescriptor)descriptor).predefined) {
          final PsiElement decl = descriptor.getDeclaration();
          if (decl instanceof JSClass &&
              (decl.isEquivalentTo(jsClass) || JSResolveUtil.checkClassHasParentOfAnotherOne((JSClass)decl, jsClass, null))) {
            result.add(descriptor);
          }
        }
      }
    }

    return result.toArray(new XmlElementDescriptor[result.size()]);
  }

  @Nullable
  static XmlElementDescriptor checkValidDescriptorAccordingToType(String arrayElementType, XmlElementDescriptor descriptor) {
    if (descriptor != null) {
      if (isAdequateType(arrayElementType)) {
        PsiElement declaration = descriptor.getDeclaration();
        if (declaration instanceof XmlFile) {
          declaration = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)declaration);
        }

        if (declaration == null) {
          return null;
        }

        PsiElement element = JSResolveUtil.unwrapProxy(declaration);
        if (element instanceof JSClass) {
          if (!JSResolveUtil.isAssignableType(arrayElementType, ((JSClass)element).getQualifiedName(), element)) {
            return null;
          }
        }
      }
    }
    return descriptor;
  }

  @Nullable
  public AnnotationBackedDescriptor getDefaultPropertyDescriptor() {
    if (!defaultPropertyDescriptorInitialized) {
      PsiElement element = predefined ? null : JSResolveUtil.unwrapProxy(getDeclaration());

      if (element instanceof JSClass) {
        JSClass jsClass = (JSClass)element;
        do {
          AnnotationBackedDescriptor descriptor = getDefaultPropertyDescriptor(jsClass);
          if (descriptor == null) {
            jsClass = ArrayUtil.getFirstElement(jsClass.getSuperClasses());
          }
          else {
            defaultPropertyDescriptor = descriptor;
            break;
          }
        }
        while (jsClass != null);
      }

      defaultPropertyDescriptorInitialized = true;
    }
    return defaultPropertyDescriptor;
  }

  @Nullable
  private AnnotationBackedDescriptor getDefaultPropertyDescriptor(final JSClass jsClass) {
    JSAttributeList attributeList = jsClass.getAttributeList();

    if (attributeList != null) {
      JSAttribute[] attributes = attributeList.getAttributesByName("DefaultProperty");

      if (attributes.length > 0) {
        JSAttributeNameValuePair pair = attributes[0].getValueByName(null);
        if (pair != null) {
          String s = pair.getSimpleValue();

          if (s != null) {
            getAttributesDescriptors(null);
            XmlAttributeDescriptor descriptor = getAttributeDescriptor(s, null);

            if (descriptor instanceof AnnotationBackedDescriptor) {
              return (AnnotationBackedDescriptor)descriptor;
            }
            else {
              return new AnnotationBackedDescriptorImpl("any", this, true, "NonExistentClass!", null, null);
            }
          }
        }
      }
    }
    return null;
  }

  public boolean allowElementsFromNamespace(final String namespace, final XmlTag context) {
    if (XmlBackedJSClassImpl.isTagOrInsideTagThatAllowsAnyXmlContent(context)) {
      return false;
    }

    if (MxmlLanguageTagsUtil.isFxLibraryTag(context)) {
      return JavaScriptSupportLoader.MXML_URI3.equals(namespace);
    }

    final XmlElementDescriptor _descriptor = context.getDescriptor();
    if (_descriptor instanceof ClassBackedElementDescriptor) {
      final ClassBackedElementDescriptor descriptor = (ClassBackedElementDescriptor)_descriptor;

      if (context.getParent() instanceof XmlDocument && JavaScriptSupportLoader.isLanguageNamespace(namespace)) {
        return true;
      }

      if (MxmlLanguageTagsUtil.isComponentTag(context) ||
          (descriptor.getDefaultPropertyDescriptor() != null && descriptor.defaultPropertyDescriptor.getType() != null)) {
        return true;
      }

      if (!descriptor.predefined && !isContainerClass(descriptor)) {
        return false;
      }
    }

    return true;
  }

  static boolean isAdequateType(final String type) {
    if (ARRAY_CLASS_NAME.equals(type) || OBJECT_CLASS_NAME.equals(type) || ANY_TYPE.equals(type)) {
      return false;
    }

    final String className = className(type);
    if ("IDeferredInstance".equals(className)) {
      return false;
    }

    return true;
  }

  static String className(String type) {
    return type != null ? type.substring(type.lastIndexOf('.') + 1):null;
  }

  private static class MxmlErrorReportingClient implements JSAnnotatingVisitor.ErrorReportingClient {
    private final Validator.ValidationHost myHost;

    public MxmlErrorReportingClient(final Validator.ValidationHost host) {
      myHost = host;
    }

    public void reportError(final ASTNode nameIdentifier, final String s, ProblemKind kind, final IntentionAction... fixes) {
      final ValidationHost.ErrorType errorType = kind == ProblemKind.ERROR ? ValidationHost.ErrorType.ERROR: ValidationHost.ErrorType.WARNING;
      myHost.addMessage(nameIdentifier.getPsi(), s, errorType, fixes);
    }
  }

  interface AttributedItemsProcessor {
    boolean process(JSNamedElement element, boolean isPackageLocalVisibility);
    boolean process(JSAttributeNameValuePair pair, final String annotationName, final boolean included);
  }

  static boolean processAttributes(PsiElement jsClass, AttributedItemsProcessor processor) {
    jsClass = JSResolveUtil.unwrapProxy(jsClass);

    return doProcess(jsClass, processor);
  }

  private static boolean doProcess(final PsiElement jsClass, final AttributedItemsProcessor processor) {
    return JSResolveUtil.processMetaAttributesForClass(jsClass, new JSResolveUtil.MetaDataProcessor() {
      public boolean process(final @NotNull JSAttribute attr) {
        final String attrName = attr.getName();
        boolean skipped = false;

        if (FlexAnnotationNames.EVENT.equals(attrName) ||
            FlexAnnotationNames.STYLE.equals(attrName) ||
            FlexAnnotationNames.EFFECT.equals(attrName) ||
            (skipped = FlexAnnotationNames.EXCLUDE.equals(attrName))) {

          JSAttributeNameValuePair jsAttributeNameValuePair = attr.getValueByName("name");
          // [Event("someEvent")] equivalent to [Event(name="someEvent")]
          if (jsAttributeNameValuePair == null) jsAttributeNameValuePair = attr.getValueByName(null);

          if (jsAttributeNameValuePair != null && !processor.process(jsAttributeNameValuePair, attrName, !skipped)) return false;
        }
        return true;
      }

      public boolean handleOtherElement(final PsiElement el, final PsiElement context, Ref<PsiElement> continuePassElement) {
        if (continuePassElement != null) {
          if (el instanceof JSVarStatement) {
            JSVariable[] jsVariables = ((JSVarStatement)el).getVariables();
            if(jsVariables.length > 0) continuePassElement.set(jsVariables[0]);
          } else {
            continuePassElement.set(el);
          }
        } else {
          final JSAttributeList.AccessType accessType = ((JSAttributeList)el).getAccessType();
          if ( (accessType == JSAttributeList.AccessType.PUBLIC || accessType == JSAttributeList.AccessType.PACKAGE_LOCAL) &&
               !((JSAttributeList)el).hasModifier(JSAttributeList.ModifierType.STATIC) &&
            (context instanceof JSVariable || (context instanceof JSFunction && ((JSFunction)context).isSetProperty()))) {
            if(!processor.process((JSNamedElement)context, accessType == JSAttributeList.AccessType.PACKAGE_LOCAL)) return false;
          }
        }

        return true;
      }
    });

  }

  public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, final @Nullable XmlTag context) {
    if (isPrivateAttribute(attributeName, context)) {
      return new AnyXmlAttributeDescriptor(attributeName);
    }

    if (context != null && context.getParent() instanceof XmlDocument) {
      if (FlexMxmlLanguageAttributeNames.ID.equals(attributeName) || ArrayUtil.contains(attributeName, CodeContext.GUMBO_ATTRIBUTES)) {
        // id, includeIn, excludeFrom, itemCreationPolicy, itemDestructionPolicy attributes are not allowed for the root tag
        return null;
      }
    }

    PsiElement element = getDeclaration();
    if (element == null) return null;
    attributeName = skipStateNamePart(attributeName);

    ensureDescriptorsMapsInitialized(element, null);

    AnnotationBackedDescriptor descriptor = myDescriptors.get(attributeName);

    if (descriptor == null && context != null && !myPackageToInternalDescriptors.isEmpty()) {
      final String contextPackage = JSResolveUtil.getPackageNameFromPlace(context);
      final Map<String, AnnotationBackedDescriptor> internalDescriptors = myPackageToInternalDescriptors.get(contextPackage);
      if (internalDescriptors != null) {
        descriptor = internalDescriptors.get(attributeName);
      }
    }

    if (descriptor == null && myPredefinedDescriptors != null) {
      descriptor = myPredefinedDescriptors.get(attributeName);
    }

    if (descriptor == null) {
      if (IMPLEMENTS_ATTR_NAME.equals(attributeName) && context != null && context.getParent() instanceof XmlDocument) {
        descriptor = new AnnotationBackedDescriptorImpl(IMPLEMENTS_ATTR_NAME, this, true, null, null, null);
      }
      else if (XmlBackedJSClassImpl.CLASS_NAME_ATTRIBUTE_NAME.equals(attributeName) && MxmlLanguageTagsUtil.isComponentTag(context)) {
        descriptor = new ClassNameAttributeDescriptor(this);
      }
      else if (!predefined &&
               !FlexMxmlLanguageAttributeNames.ID.equals(attributeName) &&
               !MxmlLanguageTagsUtil.isXmlOrXmlListTag(context) &&
               isDynamicClass(element)) {
        return new AnyXmlAttributeDescriptor(attributeName);
      }
    }

    return descriptor;
  }

  static boolean isPrivateAttribute(final String attributeName, final XmlTag context) {
    final int colonIndex = attributeName.indexOf(':');
    final String namespacePrefix = colonIndex == -1 ? null : attributeName.substring(0, colonIndex);
    if (namespacePrefix != null && context != null && ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI3, context.knownNamespaces())) {
      final String namespace = context.getNamespaceByPrefix(namespacePrefix);
      if (!StringUtil.isEmpty(namespace) &&
          !namespace.equals(JavaScriptSupportLoader.MXML_URI3) &&
          !namespace.equals(context.getNamespace())) {
        return true;
      }
    }
    return false;
  }

  private static String skipStateNamePart(final String attributeName) {
    int dotPos = attributeName.lastIndexOf('.');
    if (dotPos != -1 && !attributeName.startsWith("id.")) {
      return attributeName.substring(0, dotPos);
    }
    return attributeName;
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

  @Nullable
  public PsiElement getDeclaration() {
    String className = predefined ? OBJECT_CLASS_NAME :this.className;
    if (className.equals(CodeContext.AS3_VEC_VECTOR_QUALIFIED_NAME)) className = VECTOR_CLASS_NAME;
    return JSResolveUtil.findClassByQName(className, JavaScriptIndex.getInstance(project), context.module);
  }

  @NonNls
  public String getName(final PsiElement context) {
    String prefix = null;
    XmlTag tag = (XmlTag)context;
    String name = getName();

    int packageIndex = className.lastIndexOf(name);
    String packageName = packageIndex > 0? className.substring(0, packageIndex - 1):"";
    if (packageName.length() > 0) packageName += ".*";
    else packageName = "*";
    XmlNSDescriptor nsDescriptor = tag.getPrefixByNamespace(packageName) != null ? tag.getNSDescriptor(packageName, true):null;

    if (nsDescriptor instanceof FlexMxmlNSDescriptor) {
      if(((FlexMxmlNSDescriptor)nsDescriptor).hasElementDescriptorWithName(name, className)) {
        prefix = tag.getPrefixByNamespace(packageName);
      }
    }

    if (prefix == null) {
      final String preferredNamespace = this.context.namespace;
      final String[] knownNamespaces = tag.knownNamespaces();

      if (preferredNamespace.indexOf('*') == -1 && ArrayUtil.contains(preferredNamespace, knownNamespaces)) {
        nsDescriptor = tag.getNSDescriptor(preferredNamespace, true);

        if (nsDescriptor instanceof FlexMxmlNSDescriptor &&
            ((FlexMxmlNSDescriptor)nsDescriptor).hasElementDescriptorWithName(name, className)) {
          prefix = tag.getPrefixByNamespace(preferredNamespace);
        }
      }

      if (prefix == null) {
        for (String namespace : knownNamespaces) {
          if (namespace.equals(preferredNamespace) || namespace.indexOf('*') != -1) {
            continue;
          }

          nsDescriptor = tag.getNSDescriptor(namespace, true);

          if (nsDescriptor instanceof FlexMxmlNSDescriptor &&
              ((FlexMxmlNSDescriptor)nsDescriptor).hasElementDescriptorWithName(name, className)) {
            prefix = tag.getPrefixByNamespace(namespace);
            break;
          }
        }
      }
    }

    if (prefix == null || prefix.length() == 0) return name;
    return prefix + ":" + name;
  }

  @NonNls
  public String getName() {
    return name == null ? getNameFromQName():name;
  }

  private String getNameFromQName() {
    return className(className);
  }

  public void init(final PsiElement element) {
  }

  public Object[] getDependences() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  private static class ClassNameAttributeDescriptor extends AnnotationBackedDescriptorImpl {
    protected ClassNameAttributeDescriptor(ClassBackedElementDescriptor parentDescriptor) {
      super(XmlBackedJSClassImpl.CLASS_NAME_ATTRIBUTE_NAME, parentDescriptor, true, null, null, null);
    }

    @Override
    public String validateValue(XmlElement context, String value) {
      final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage());
      if (!namesValidator.isIdentifier(value, context.getProject())) {
        return JSBundle.message("invalid.identifier.value");
      }
      return null;
    }
  }

}
