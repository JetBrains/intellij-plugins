// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.codeInsight.daemon.IdeValidationHost;
import com.intellij.codeInsight.daemon.Validator;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.ide.IconProvider;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.mxml.FlexNameAlias;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.index.JSTypeEvaluateManager;
import com.intellij.lang.javascript.inspections.actionscript.ActionScriptAnnotatingVisitor;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
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

  private final static XmlUtil.DuplicationInfoProvider<XmlElement> myDuplicationInfoProvider = new XmlUtil.DuplicationInfoProvider<>() {
    @Override
    public String getName(@NotNull final XmlElement xmlElement) {
      if (xmlElement instanceof XmlTag) return ((XmlTag)xmlElement).getLocalName();
      return ((XmlAttribute)xmlElement).getName();
    }

    @Override
    @NotNull
    public String getNameKey(@NotNull final XmlElement xmlElement, final @NotNull String name) {
      return name;
    }

    @Override
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
  private static final String CONTAINER_CLASS_NAME_2 = "mx.core.IVisualElementContainer";
  @NonNls
  static final String IFACTORY_SHORT_CLASS_NAME = "IFactory";
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

  @Override
  public String getQualifiedName() {
    return className;
  }

  @Override
  public String getDefaultName() {
    return getName();
  }

  public boolean isPredefined(){
    return predefined;
  }

  @Override
  public XmlElementDescriptor[] getElementsDescriptors(final XmlTag _context) {
    if (MxmlJSClass.isTagOrInsideTagThatAllowsAnyXmlContent(_context) || MxmlLanguageTagsUtil.isFxReparentTag(_context)) {
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
      new ArrayList<>(myDescriptors == null ? 0 : myDescriptors.size() + context.getAllDescriptorsSize());
    final boolean isComponentTag = MxmlLanguageTagsUtil.isComponentTag(_context);
    boolean includeProperties = (parentDescriptor == this) && !isComponentTag;

    if (isComponentTag) {
      ContainerUtil.addAll(resultList, getElementDescriptorsInheritedFromGivenType(FlexCommonTypeNames.IUI_COMPONENT));
      ContainerUtil.addAll(resultList, getElementDescriptorsInheritedFromGivenType(PRIMITIVE_GRAPHIC_ELEMENT_BASE_CLASS));
    }
    else if (parentDescriptor.getDefaultPropertyDescriptor() != null && parentDescriptor.defaultPropertyDescriptor.getType() != null) {
      final PsiElement contextParent = _context.getParent();
      if (contextParent instanceof XmlDocument && JavaScriptSupportLoader.isLanguageNamespace(_context.getNamespace())) {
        // Predefined tags like <fx:Declaration/> can be children of a tag with [DefaultProperty] annotation if this tag is root tag in the mxml file
        for (XmlElementDescriptor descriptor : context.getDescriptorsWithAllowedDeclaration()) {
          if (descriptor instanceof ClassBackedElementDescriptor && ((ClassBackedElementDescriptor)descriptor).predefined) {
            resultList.add(descriptor);
          }
        }
      }

      final String type = parentDescriptor.getDefaultPropertyType();
      ContainerUtil.addAll(resultList, isAdequateType(type)
                                       ? getElementDescriptorsInheritedFromGivenType(type)
                                       : context.getDescriptorsWithAllowedDeclaration());

      if (JavaScriptSupportLoader.isLanguageNamespace(context.namespace)) {
        ContainerUtil.addIfNotNull(resultList, context.getElementDescriptor(FlexPredefinedTagNames.SCRIPT, (XmlTag)null));
        ContainerUtil.addIfNotNull(resultList, context.getElementDescriptor(CodeContext.REPARENT_TAG_NAME, (XmlTag)null));

        if (contextParent instanceof XmlTag && MxmlLanguageTagsUtil.isComponentTag((XmlTag)contextParent)) {
          ContainerUtil.addIfNotNull(resultList, context.getElementDescriptor(FlexPredefinedTagNames.DECLARATIONS, (XmlTag)null));
          ContainerUtil.addIfNotNull(resultList, context.getElementDescriptor(FlexPredefinedTagNames.BINDING, (XmlTag)null));
          ContainerUtil.addIfNotNull(resultList, context.getElementDescriptor(FlexPredefinedTagNames.STYLE, (XmlTag)null));
          ContainerUtil.addIfNotNull(resultList, context.getElementDescriptor(FlexPredefinedTagNames.METADATA, (XmlTag)null));
        }
      }
    }
    else if (parentDescriptor.predefined || isContainerClass(parentDescriptor)) {
      context.appendDescriptorsWithAllowedDeclaration(resultList);
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
    return resultList.toArray(XmlElementDescriptor.EMPTY_ARRAY);
  }

  private static boolean isContainerClass(final ClassBackedElementDescriptor descriptor) {
    if (!descriptor.isContainerClassInitialized) {
      if (descriptor.predefined) {
        descriptor.isContainerClass = false;
      }
      else {
        final PsiElement declaration = descriptor.getDeclaration();
        descriptor.isContainerClass =
          ActionScriptResolveUtil.isAssignableType(FlexCommonTypeNames.ICONTAINER, descriptor.className, declaration) ||
          ActionScriptResolveUtil.isAssignableType(CONTAINER_CLASS_NAME_2, descriptor.className, declaration);
      }
      descriptor.isContainerClassInitialized = true;
    }
    return descriptor.isContainerClass;
  }

  private String getDefaultPropertyType() {
    return defaultPropertyDescriptor.getArrayType() != null ? defaultPropertyDescriptor.getArrayType() : defaultPropertyDescriptor.getType();
  }

  @Override
  @Nullable
  public XmlElementDescriptor getElementDescriptor(final XmlTag childTag, final XmlTag contextTag) {
    if (MxmlJSClass.isTagThatAllowsAnyXmlContent(contextTag)) {
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
      final XmlElementDescriptor checkedDescriptor = checkValidDescriptorAccordingToType(FlexCommonTypeNames.IUI_COMPONENT, descriptor);
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
      return checkValidDescriptorAccordingToType(FlexCommonTypeNames.IUI_COMPONENT, descriptor);
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

    if (ActionScriptResolveUtil.isAssignableType(RADIO_BUTTON_GROUP_CLASS, childDescriptor.className, parentTag)) {
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
    if (childNs.equals(namespace)) return true;
    return JavaScriptSupportLoader.isLanguageNamespace(childNs) && JavaScriptSupportLoader.isLanguageNamespace(namespace);
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
    JSAttributeList attrList;
    return element instanceof JSClass &&
        (attrList = ((JSClass)element).getAttributeList()) != null &&
        attrList.hasModifier(JSAttributeList.ModifierType.DYNAMIC);
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(final @Nullable XmlTag _context) {
    if (MxmlLanguageTagsUtil.isFxPrivateTag(_context) || MxmlLanguageTagsUtil.isFxLibraryTag(_context)) {
      return XmlAttributeDescriptor.EMPTY;
    }

    if (MxmlLanguageTagsUtil.isFxDefinitionTag(_context)) {
      return new XmlAttributeDescriptor[]{
        new AnnotationBackedDescriptorImpl(MxmlLanguageTagsUtil.NAME_ATTRIBUTE, this, true, null, null, null)};
    }

    if (MxmlLanguageTagsUtil.isComponentTag(_context)) {
      return new XmlAttributeDescriptor[]{
        new ClassNameAttributeDescriptor(this),
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

    final Collection<AnnotationBackedDescriptor> descriptors = new ArrayList<>(myDescriptors.values());

    if (_context != null && !myPackageToInternalDescriptors.isEmpty()) {
      final String contextPackage = JSResolveUtil.getPackageNameFromPlace(_context);
      final Map<String, AnnotationBackedDescriptor> internalDescriptors = myPackageToInternalDescriptors.get(contextPackage);
      if (internalDescriptors != null) {
        descriptors.addAll(internalDescriptors.values());
      }
    }

    if (_context != null && MxmlLanguageTagsUtil.isComponentTag(_context.getParentTag())) {
      descriptors.add(new AnnotationBackedDescriptorImpl(IMPLEMENTS_ATTR_NAME, this, true, null, null, null));
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
    return descriptors.toArray(XmlAttributeDescriptor.EMPTY);
  }

  public void addPredefinedMemberDescriptor(@NotNull AnnotationBackedDescriptor descriptor) {
    if (predefined) {
      if (myDescriptors == null) {
        myDescriptors = new THashMap<>();
        myPackageToInternalDescriptors = Collections.emptyMap();
      }
      myDescriptors.put(descriptor.getName(), descriptor);
    } else {
      if (myPredefinedDescriptors == null) myPredefinedDescriptors = new THashMap<>();
      myPredefinedDescriptors.put(descriptor.getName(), descriptor);
    }
  }

  private void ensureDescriptorsMapsInitialized(PsiElement element, @Nullable Set<JSClass> visited) {
    Map<String, AnnotationBackedDescriptor> map;
    Map<String, Map<String, AnnotationBackedDescriptor>> packageToInternalDescriptors;

    synchronized (CodeContext.class) {
      map = myDescriptors;
      packageToInternalDescriptors = myPackageToInternalDescriptors;
      if (map != null && packageToInternalDescriptors != null) return;

      map = new THashMap<>();
      packageToInternalDescriptors = new THashMap<>();
      Set<PsiElement> processedElements = null;

      if (element instanceof XmlBackedJSClassImpl) {
        element = element.getParent().getContainingFile(); // TODO: make this code and following loop better
      }

      if (element instanceof XmlFile && MxmlJSClass.isFxgFile((PsiFile)element)) {
        element = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)element);
      }

      while (element instanceof XmlFile) {
        final XmlDocument document = ((XmlFile)element).getDocument();
        final XmlTag rootTag = document != null ? document.getRootTag():null;
        final XmlElementDescriptor descriptor = rootTag != null ? rootTag.getDescriptor():null;
        if (processedElements == null) processedElements = new THashSet<>();
        processedElements.add(element);

        element = descriptor != null ? descriptor.getDeclaration():null;
        if (processedElements.contains(element)) break;
        collectMxmlAttributes(map, packageToInternalDescriptors, rootTag);
      }

      if (element instanceof JSNamedElement) {
        JSNamedElement jsClass = (JSNamedElement)element;

        if (visited == null || !visited.contains(jsClass)) {
          if (!MxmlJSClass.XML_TAG_NAME.equals(jsClass.getName()) && !MxmlJSClass.XMLLIST_TAG_NAME.equals(jsClass.getName())) {
            JSReferenceList extendsList = jsClass instanceof JSClass ? ((JSClass)jsClass).getExtendsList():null;
            if (extendsList != null) {
              final JSClass clazz = (JSClass)jsClass;
              if (visited == null) {
                visited = new THashSet<>();
              }
              visited.add(clazz);

              for(JSClass superClazz: clazz.getSuperClasses()) {
                appendSuperClassDescriptors(map, packageToInternalDescriptors, superClazz, visited);
              }
            } else if (!OBJECT_CLASS_NAME.equals(jsClass.getName()) && CodeContext.isStdNamespace(context.namespace)) {
              appendSuperClassDescriptors(
                  map,
                  packageToInternalDescriptors,
                  ActionScriptClassResolver.findClassByQNameStatic(OBJECT_CLASS_NAME, jsClass),
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
        @Override
        protected void process(final JSFile file) {
          collectMyAttributes(file, map, packageToInternalDescriptors);
        }
      };
      FlexUtils.processMxmlTags(rootTag, true, injectedFilesVisitor);

      processClassBackedTagsWithIdAttribute(rootTag, idAttributeAndItsType -> {
        final XmlAttribute idAttribute = idAttributeAndItsType.first;
        final String idAttributeValue = idAttribute.getValue();
        final String type = idAttributeAndItsType.second;
        map.put(idAttributeValue,
                new AnnotationBackedDescriptorImpl(idAttributeValue, this, false, type, null, idAttribute));
        return true;
      });
    }
  }

  static boolean processClassBackedTagsWithIdAttribute(final @NotNull XmlTag tag, final Processor<? super Pair<XmlAttribute, String>> processor) {
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
      @Override
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
              arrayType = JSTypeEvaluateManager.getComponentType(jsNamedElement.getProject(), propertyType);
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

      @Override
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
        previousDescriptor.getOriginatingElementType() == AnnotationBackedDescriptorImpl.OriginatingElementType.VarOrFunction &&
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
            descriptorMap = new THashMap<>();
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
    if (jsNamedElement instanceof JSVariable) {
      JSType jsType = ((JSVariable)jsNamedElement).getJSType();
      return jsType == null ? null : jsType.getTypeText();
    }
    else if (jsNamedElement instanceof JSFunctionItem) {
      final JSType type = JSResolveUtil.getTypeFromSetAccessor((JSFunctionItem)jsNamedElement);
      if (type != null) {
        return type.getTypeText();
      }
    }
    return null;
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
          descriptorMap = new THashMap<>();
          packageToInternalDescriptors.put(entry.getKey(), descriptorMap);
        }
        descriptorMap.putAll(entry.getValue());
      }
    }
  }

  @Override
  public void validate(@NotNull final XmlTag tag, @NotNull final ValidationHost host) {
    if (FlexSdkUtils.isFlex4Sdk(FlexUtils.getSdkForActiveBC(context.module))) {
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
          final List<XmlElement> elements = new ArrayList<>();
          collectLowerCasedElements(elements, tag.getAttributes());
          collectLowerCasedElements(elements, tag.getSubTags());

          XmlUtil.doDuplicationCheckForElements(
            elements.toArray(XmlElement.EMPTY_ARRAY),
            new THashMap<>(elements.size()), myDuplicationInfoProvider,
            host
          );

          break;
        }
      }
    }

    final PsiElement declaration = getDeclaration();
    if ((declaration instanceof JSClass) && !CodeContext.hasDefaultConstructor((JSClass)declaration)) {
      host.addMessage(tag, FlexBundle.message("class.0.does.not.have.default.constructor", ((JSClass)declaration).getQualifiedName()),
                      Validator.ValidationHost.ErrorType.ERROR);
    }

    if (tag.getParent() instanceof XmlDocument) {
      final JSClass jsClass = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)tag.getContainingFile());
      final JSReferenceList list = jsClass.getImplementsList();
      final MxmlErrorReportingClient reportingClient = new MxmlErrorReportingClient(host);

      if (list != null) {
        ActionScriptAnnotatingVisitor.checkActionScriptImplementedMethods(jsClass, reportingClient);

        for (JSClass element : list.getReferencedClasses()) {
          if (element == jsClass) {
            reportingClient.reportError(
              refToImplementsNode(tag), // TODO: list is artificial node without context, cannot bind to it
              JavaScriptBundle.message("javascript.validation.message.circular.dependency"),
              JSAnnotatingVisitor.ErrorReportingClient.ProblemKind.ERROR);
            continue;
          }

          if (element != null && !element.isInterface()) {
            reportingClient.reportError(
              refToImplementsNode(tag),
              JavaScriptBundle.message("javascript.validation.message.interface.name.expected.here"),
              JSAnnotatingVisitor.ErrorReportingClient.ProblemKind.ERROR);
          }
        }
      }

      JSClass[] classes = jsClass.getSuperClasses();
      if (classes.length > 0 && classes[0] == jsClass) {
        reportingClient.reportError(
          tag.getNode(),
          JavaScriptBundle.message("javascript.validation.message.circular.dependency"),
          JSAnnotatingVisitor.ErrorReportingClient.ProblemKind.ERROR);
      }

      ActionScriptAnnotatingVisitor.checkFileUnderSourceRoot(jsClass, reportingClient);
    }

    if (MxmlLanguageTagsUtil.isComponentTag(tag)) {
      if (tag.getSubTags().length == 0) {
        host.addMessage(
          tag,
          FlexBundle.message("javascript.validation.empty.component.type"),
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

  private static <T extends XmlElement & PsiNamedElement> void collectLowerCasedElements(final List<? super XmlElement> elements, final T[] xmlAttributes) {
    for(T a: xmlAttributes) {
      if (a instanceof XmlAttribute && FlexMxmlLanguageAttributeNames.ID.equals(a.getName())) continue;

      final String name = a instanceof XmlTag ? ((XmlTag)a).getLocalName() : a.getName();
      if (name != null && name.length() > 0 && Character.isLowerCase(name.charAt(0))) elements.add(a);
    }
  }

  @Override
  public boolean requiresCdataBracesInContext(@NotNull final XmlTag context) {
    return predefined && XmlBackedJSClassImpl.SCRIPT_TAG_NAME.equals(context.getLocalName());
  }

  @Override
  public Icon getIcon(@NotNull final PsiElement element, final int flags) {
    if (iconPath != null) {
      final PsiFile containingFile = element.getContainingFile();
      final VirtualFile file = containingFile.getVirtualFile();

      if (file != null) {
        final VirtualFile relativeFile = VfsUtilCore.findRelativeFile(iconPath, file.getParent());
        if (relativeFile != null) {
          try {
            return new ImageIcon(new URL(VfsUtilCore.fixIDEAUrl(relativeFile.getUrl())));
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

    final PsiElement clazz = ActionScriptClassResolver.findClassByQNameStatic(arrayElementType, declaration);
    if (!(clazz instanceof JSClass)) {
      return EMPTY_ARRAY;
    }

    final JSClass jsClass = (JSClass)clazz;
    final JSAttributeList attributeList = jsClass.getAttributeList();
    final boolean isFinalClass = attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.FINAL);
    final List<XmlElementDescriptor> result = new ArrayList<>();

    if (isFinalClass) {
      final String jsClassName = jsClass.getName();
      if (jsClassName != null) {
        final ClassBackedElementDescriptor descriptor = context.getElementDescriptor(jsClassName, jsClass.getQualifiedName());
        if (descriptor != null && CodeContext.checkDeclaration(descriptor)) {
          result.add(descriptor);
        }
      }
    }
    else {
      for (final XmlElementDescriptor descriptor : context.getDescriptorsWithAllowedDeclaration()) {
        if (descriptor instanceof ClassBackedElementDescriptor && !((ClassBackedElementDescriptor)descriptor).predefined) {
          final PsiElement decl = descriptor.getDeclaration();
          if (decl instanceof JSClass && JSInheritanceUtil.isParentClass((JSClass)decl, jsClass, false)) {
            result.add(descriptor);
          }
        }
      }
    }

    return result.toArray(XmlElementDescriptor.EMPTY_ARRAY);
  }

  @Nullable
  static XmlElementDescriptor checkValidDescriptorAccordingToType(String arrayElementType, XmlElementDescriptor descriptor) {
    // no need to check AnnotationBackedDescriptors
    if (descriptor instanceof ClassBackedElementDescriptor) {
      if (isAdequateType(arrayElementType)) {
        PsiElement declaration = descriptor.getDeclaration();
        if (declaration instanceof XmlFile) {
          declaration = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)declaration);
        }

        if (declaration == null) {
          return null;
        }

        if (declaration instanceof JSClass) {
          if (!ActionScriptResolveUtil.isAssignableType(arrayElementType, ((JSClass)declaration).getQualifiedName(), declaration)) {
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
      PsiElement element = predefined ? null : getDeclaration();

      if (element instanceof XmlFile) {
        element = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)element);
      }

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
    final Ref<JSAttribute> dpAttributeRef = Ref.create();

    if (jsClass instanceof XmlBackedJSClassImpl) {
      final XmlFile xmlFile = (XmlFile)jsClass.getContainingFile();
      final XmlTag rootTag = xmlFile.getRootTag();
      if (rootTag != null) {
        for (XmlTag metadataTag : rootTag.findSubTags(FlexPredefinedTagNames.METADATA, JavaScriptSupportLoader.MXML_URI3)) {
          JSResolveUtil.processInjectedFileForTag(metadataTag, new JSResolveUtil.JSInjectedFilesVisitor() {
            @Override
            protected void process(final JSFile file) {
              for (PsiElement elt : file.getChildren()) {
                if (elt instanceof JSAttributeList) {
                  final JSAttribute dpAttribute = ((JSAttributeList)elt).findAttributeByName("DefaultProperty");
                  if (dpAttributeRef.isNull() && dpAttribute != null) {
                    dpAttributeRef.set(dpAttribute);
                    return;
                  }
                }
              }
            }
          });
        }
      }
    }
    else {
      final JSAttributeList attributeList = jsClass.getAttributeList();

      if (attributeList != null) {
        dpAttributeRef.set(attributeList.findAttributeByName("DefaultProperty"));
      }
    }

    final JSAttribute attribute = dpAttributeRef.get();

    if (attribute != null) {
      JSAttributeNameValuePair pair = attribute.getValueByName(null);
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
    return null;
  }

  @Override
  public boolean allowElementsFromNamespace(final String namespace, final XmlTag context) {
    if (MxmlJSClass.isTagOrInsideTagThatAllowsAnyXmlContent(context)) {
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

    MxmlErrorReportingClient(final Validator.ValidationHost host) {
      myHost = host;
    }

    @Override
    public void reportError(ASTNode nameIdentifier, @NotNull @InspectionMessage String message, ProblemKind kind, final IntentionAction @NotNull ... fixes) {
      final ValidationHost.ErrorType errorType = kind == ProblemKind.ERROR ? ValidationHost.ErrorType.ERROR: ValidationHost.ErrorType.WARNING;
      if (myHost instanceof IdeValidationHost) {
        ((IdeValidationHost) myHost).addMessageWithFixes(nameIdentifier.getPsi(), message, errorType, fixes);
      }
      else {
        myHost.addMessage(nameIdentifier.getPsi(), message, errorType);
      }
    }
  }

  interface AttributedItemsProcessor {
    boolean process(JSNamedElement element, boolean isPackageLocalVisibility);
    boolean process(JSAttributeNameValuePair pair, final String annotationName, final boolean included);
  }

  static boolean processAttributes(PsiElement jsClass, AttributedItemsProcessor processor) {
    return doProcess(jsClass, processor);
  }

  private static boolean doProcess(final PsiElement jsClass, final AttributedItemsProcessor processor) {
    return ActionScriptResolveUtil.processMetaAttributesForClass(jsClass, new ActionScriptResolveUtil.MetaDataProcessor() {
      @Override
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

      @Override
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

  @Override
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

    if (descriptor == null && context != null) {
      final String prefix = context.getNamespacePrefix();
      if (StringUtil.isNotEmpty(prefix) && attributeName.startsWith(prefix + ":")){
        descriptor = myDescriptors.get(attributeName.substring(prefix.length() + 1));
      }
    }

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
      if (IMPLEMENTS_ATTR_NAME.equals(attributeName) && context != null) {
        final PsiElement parent = context.getParent();
        if (parent instanceof XmlDocument || (parent instanceof XmlTag && MxmlLanguageTagsUtil.isComponentTag((XmlTag)parent))) {
          descriptor = new AnnotationBackedDescriptorImpl(IMPLEMENTS_ATTR_NAME, this, true, null, null, null);
        }
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
  public String getDefaultValue() {
    return null;
  }

  @Override
  @Nullable
  public PsiElement getDeclaration() {
    String className = predefined ? OBJECT_CLASS_NAME :this.className;
    if (className.equals(CodeContext.AS3_VEC_VECTOR_QUALIFIED_NAME)) className = VECTOR_CLASS_NAME;
    GlobalSearchScope scope = ObjectUtils.notNull(JSInheritanceUtil.getEnforcedScope(), context.scope);
    PsiElement jsClass = ActionScriptClassResolver.findClassByQNameStatic(className, scope);
    final PsiFile file = jsClass == null ? null : jsClass.getContainingFile();
    // can be MXML file listed as a component in the manifest file
    return (file != null && JavaScriptSupportLoader.isMxmlOrFxgFile(file)) ? file : jsClass;
  }

  @Override
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

  @Override
  @NonNls
  public String getName() {
    return name == null ? getNameFromQName():name;
  }

  private String getNameFromQName() {
    return className(className);
  }

  @Override
  public void init(final PsiElement element) {
  }

  private static class ClassNameAttributeDescriptor extends AnnotationBackedDescriptorImpl {
    protected ClassNameAttributeDescriptor(ClassBackedElementDescriptor parentDescriptor) {
      super(XmlBackedJSClassImpl.CLASS_NAME_ATTRIBUTE_NAME, parentDescriptor, true, null, null, null);
    }

    @Override
    public String validateValue(XmlElement context, String value) {
      if (!LanguageNamesValidation.isIdentifier(JavascriptLanguage.INSTANCE, value, context.getProject())) {
        return FlexBundle.message("invalid.identifier.value");
      }
      return null;
    }
  }

}
