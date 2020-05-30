// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.css;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.css.CssDialect;
import com.intellij.lang.css.CssDialectMappings;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptClassImpl;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.*;
import com.intellij.psi.css.descriptor.CssFunctionDescriptor;
import com.intellij.psi.css.descriptor.CssFunctionDescriptorStub;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptorStub;
import com.intellij.psi.css.descriptor.value.CssNullValue;
import com.intellij.psi.css.descriptor.value.CssValueDescriptor;
import com.intellij.psi.css.descriptor.value.CssValueValidator;
import com.intellij.psi.css.impl.CssTermTypes;
import com.intellij.psi.css.impl.descriptor.value.CssGroupValue;
import com.intellij.psi.css.impl.descriptor.value.CssStringValue;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorProviderImpl;
import com.intellij.psi.css.resolve.HtmlCssClassOrIdReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.intellij.openapi.module.ModuleUtilCore.findModuleForFile;
import static com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement;
import static com.intellij.psi.util.PsiUtilCore.toPsiElementArray;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexCssElementDescriptorProvider extends CssElementDescriptorProvider {
  private final FlexCssValueValidator FLEX_CSS_VALUE_VALIDATOR = new FlexCssValueValidator(this);
  private final Map<String, CssFunctionDescriptor> myFunctionDescriptors = new HashMap<>();

  public FlexCssElementDescriptorProvider() {
    CssElementDescriptorFactory2 descriptorFactory = CssElementDescriptorFactory2.getInstance();
    CssStringValue singleStringValue = descriptorFactory.createStringValueDescriptor(null, 1, 1, null);

    CssGroupValue embedFunctionValue = descriptorFactory.createGroupValue(CssGroupValue.Type.OR, 1, 1, null, null);

    CssValueDescriptor commaSeparator = descriptorFactory.createTextValueDescriptor(",", 1, 1, null);
    CssGroupValue attributes = descriptorFactory.createGroupValue(CssGroupValue.Type.AND, 1, 1, embedFunctionValue, commaSeparator);
    attributes.addChild(createAttributeValueDescriptor("source", true, descriptorFactory, attributes));
    attributes.addChild(createAttributeValueDescriptor("mimeType", false, descriptorFactory, attributes));
    attributes.addChild(createBooleanAttributeValueDescriptor("smoothing", false, descriptorFactory, attributes));
    attributes.addChild(createBooleanAttributeValueDescriptor("compression", false, descriptorFactory, attributes));
    attributes.addChild(createAttributeValueDescriptor("quality", false, descriptorFactory, attributes));
    attributes.addChild(createAttributeValueDescriptor("scaleGridTop", false, descriptorFactory, attributes));
    attributes.addChild(createAttributeValueDescriptor("scaleGridBottom", false, descriptorFactory, attributes));
    attributes.addChild(createAttributeValueDescriptor("scaleGridLeft", false, descriptorFactory, attributes));
    attributes.addChild(createAttributeValueDescriptor("scaleGridRight", false, descriptorFactory, attributes));
    attributes.addChild(createAttributeValueDescriptor("symbol", false, descriptorFactory, attributes));

    embedFunctionValue.addChild(descriptorFactory.createStringValueDescriptor(null, 1, 1, embedFunctionValue));
    embedFunctionValue.addChild(attributes);

    myFunctionDescriptors.put("Embed", new CssFunctionDescriptorStub("Embed", embedFunctionValue));
    myFunctionDescriptors.put("ClassReference", new CssFunctionDescriptorStub("ClassReference", singleStringValue));
    myFunctionDescriptors.put("PropertyReference", new CssFunctionDescriptorStub("PropertyReference", singleStringValue));
  }

  private static CssGroupValue createAttributeValueDescriptor(@NotNull String attributeName, boolean required,
                                                              @NotNull CssElementDescriptorFactory2 descriptorFactory,
                                                              @NotNull CssGroupValue parent) {
    CssGroupValue attributeValue = descriptorFactory.createGroupValue(CssGroupValue.Type.ALL, required ? 1 : 0, 1, parent, null);
    attributeValue.addChild(descriptorFactory.createNameValueDescriptor(attributeName, attributeName, 1, 1, attributeValue));
    attributeValue.addChild(descriptorFactory.createTextValueDescriptor("=", 1, 1, attributeValue));
    attributeValue.addChild(descriptorFactory.createStringValueDescriptor(null, 1, 1, attributeValue));
    return attributeValue;
  }

  private static CssGroupValue createBooleanAttributeValueDescriptor(@NotNull String attributeName, boolean required,
                                                                     @NotNull CssElementDescriptorFactory2 descriptorFactory,
                                                                     @NotNull CssGroupValue parent) {
    CssGroupValue attributeValue = descriptorFactory.createGroupValue(CssGroupValue.Type.ALL, required ? 1 : 0, 1, parent, null);
    attributeValue.addChild(descriptorFactory.createNameValueDescriptor(attributeName, attributeName, 1, 1, attributeValue));
    attributeValue.addChild(descriptorFactory.createTextValueDescriptor("=", 1, 1, attributeValue));

    CssGroupValue booleanValue = descriptorFactory.createGroupValue(CssGroupValue.Type.OR, 1, 1, attributeValue, null);
    booleanValue.addChild(descriptorFactory.createStringValueDescriptor("true", 1, 1, booleanValue));
    booleanValue.addChild(descriptorFactory.createStringValueDescriptor("false", 1, 1, booleanValue));

    attributeValue.addChild(booleanValue);
    return attributeValue;
  }

  @Override
  public boolean isMyContext(@Nullable PsiElement context) {
    if (context == null || !context.isValid()) return false;
    PsiFile file = context.getContainingFile();
    if (file == null) return false;
    if (HtmlUtil.hasHtml(file)) return false;

    final VirtualFile vFile = file.getOriginalFile().getVirtualFile();
    if (vFile != null) {
      final FileType type = vFile.getFileType();
      if (type instanceof LanguageFileType) {
        Language lang = ((LanguageFileType)type).getLanguage();
        if (lang.isKindOf(CSSLanguage.INSTANCE) && !lang.is(CSSLanguage.INSTANCE)) return false;
      }
    }

    Module module = findModuleForPsiElement(file);
    if (module == null) {
      file = InjectedLanguageManager.getInstance(file.getProject()).getTopLevelFile(context);
      if (file != null) {
        module = findModuleForPsiElement(file);
      }
    }
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) {
      return false;
    }

    if (vFile != null) {
      String dialectName = CssDialectMappings.getInstance(context.getProject()).getMapping(vFile);
      return dialectName == null || dialectName.equals(FlexCSSDialect.getInstance().getName());
    }

    return true;
  }

  @NotNull
  @Override
  public CssValueValidator getValueValidator() {
    return FLEX_CSS_VALUE_VALIDATOR;
  }

  @Override
  public boolean shouldAskOtherProviders(PsiElement context) {
    return false;
  }

  @Nullable
  private static String findJsClassOrFile(@NotNull JSClass root, Set<JSClass> visited, Set<String> possibleQNames) {
    if (!visited.add(root)) return null;
    String qName = root.getQualifiedName();
    if (qName != null && possibleQNames.contains(qName)) {
      return qName;
    }
    Set<String> includes = new LinkedHashSet<>();
    FlexCssUtil.collectAllIncludes(root, includes);
    String fileName = null;
    for (String include : includes) {
      if (possibleQNames.contains(include)) {
        fileName = include;
      }
    }
    if (fileName != null) {
      return fileName;
    }
    for (JSClass jsSuper : root.getSupers()) {
      String result = findJsClassOrFile(jsSuper, visited, possibleQNames);
      if (result != null) return result;
    }
    return null;
  }

  private static List<FlexStyleIndexInfo> filter(Collection<? extends Collection<FlexStyleIndexInfo>> collections,
                                                 List<CssSimpleSelector> selectors,
                                                 @NotNull GlobalSearchScope scope,
                                                 @Nullable Module module) {
    Set<String> allNames = new LinkedHashSet<>();
    for (Collection<FlexStyleIndexInfo> collection : collections) {
      for (FlexStyleIndexInfo info : collection) {
        allNames.add(info.getClassOrFileName());
      }
    }
    Set<String> namesFromSelectors = null;
    if (selectors.size() > 0 && !containsGlobalSelectors(selectors)) {
      namesFromSelectors = new LinkedHashSet<>();
      for (CssSimpleSelector selector : selectors) {

        if (module != null) {
          final JSClass jsClass = getClassFromMxmlDescriptor(selector, module);
          if (jsClass != null) {
            String classOrFileName = findJsClassOrFile(jsClass, new LinkedHashSet<>(), allNames);
            if (classOrFileName != null) {
              namesFromSelectors.add(classOrFileName);
            }
            continue;
          }
        }

        final String selectorName = selector.getElementName();
        Collection<JSQualifiedNamedElement> elements = JSResolveUtil.findElementsByName(selectorName, scope.getProject(), scope);
        for (PsiElement element : elements) {
          if (element instanceof JSClass) {
            String classOrFileName = findJsClassOrFile((JSClass)element, new LinkedHashSet<>(), allNames);
            if (classOrFileName != null) {
              namesFromSelectors.add(classOrFileName);
            }
          }
        }
      }
    }
    List<FlexStyleIndexInfo> result = new ArrayList<>();
    for (Collection<FlexStyleIndexInfo> collection : collections) {
      for (FlexStyleIndexInfo info : collection) {
        if (namesFromSelectors == null || namesFromSelectors.contains(info.getClassOrFileName())) {
          result.add(info);
        }
      }
    }
    return result;
  }

  @Override
  public PsiElement getDocumentationElementForSelector(@NotNull String selectorName, @Nullable PsiElement context) {
    if (context != null) {
      Collection<JSQualifiedNamedElement> classes = getClasses(selectorName, context);
      if (classes != null) {
        for (JSQualifiedNamedElement c : classes) {
          if (c instanceof JSClass) {
            return c;
          }
        }
      }
    }
    return null;
  }

  @NotNull
  @Override
  public Collection<? extends CssPseudoSelectorDescriptor> findPseudoSelectorDescriptors(@NotNull String name, @Nullable PsiElement context) {
    return Collections.singletonList(new CssPseudoSelectorDescriptorStub(name));
  }

  @NotNull
  @Override
  public Collection<? extends CssValueDescriptor> getNamedValueDescriptors(@NotNull String name, @Nullable CssValueDescriptor parent) {
    return Collections.singletonList(new CssNullValue(parent));
  }

  @NotNull
  @Override
  public Collection<? extends CssPropertyDescriptor> findPropertyDescriptors(@NotNull String propertyName, PsiElement context) {
    if (context != null) {
      Module module = findModuleForPsiElement(context);
      GlobalSearchScope scope = FlexCssUtil.getResolveScope(context);
      List<Set<FlexStyleIndexInfo>> lists = FileBasedIndex.getInstance().getValues(FlexStyleIndex.INDEX_ID, propertyName, scope);
      List<CssSimpleSelector> selectors = findSimpleSelectorsAbove(context);
      List<FlexStyleIndexInfo> infos = filter(lists, selectors, scope, module);
      if (infos.size() > 0) {
        return Collections.singletonList(new FlexCssPropertyDescriptor(infos));
      }
    }
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public Collection<? extends CssFunctionDescriptor> findFunctionDescriptors(@NotNull String functionName, @Nullable PsiElement context) {
    return ContainerUtil.createMaybeSingletonList(myFunctionDescriptors.get(functionName));
  }

  @Override
  public boolean isPossibleSelector(@NotNull String selector, @NotNull PsiElement context) {
    if (selector.equals("global")) return true;
    GlobalSearchScope scope = FlexCssUtil.getResolveScope(context);
    Collection<JSQualifiedNamedElement> classes = JSResolveUtil.findElementsByName(selector, context.getProject(), scope);
    for (JSQualifiedNamedElement c : classes) {
      if (c instanceof JSClass) {
        return true;
      }
    }
    return false;
  }

  public boolean isPossiblePseudoSelector(@NotNull String selectorName, @Nullable PsiElement context) {
    return false;
  }

  private static boolean isInClassicForm(String propertyName) {
    return propertyName.indexOf('-') >= 0;
  }

  private static void fillPropertyDescriptorsDynamically(@NotNull final JSClass jsClass, Set<JSClass> visited, final Set<CssPropertyDescriptor> result) {
    if (!visited.add(jsClass)) return;
    FlexUtils.processMetaAttributesForClass(jsClass, new ActionScriptResolveUtil.MetaDataProcessor() {
      @Override
      public boolean process(@NotNull JSAttribute jsAttribute) {
        if (FlexAnnotationNames.STYLE.equals(jsAttribute.getName())) {
          JSAttributeNameValuePair pair = jsAttribute.getValueByName("name");
          String styleName = pair != null ? pair.getSimpleValue() : null;
          String qualifiedName = jsClass.getQualifiedName();
          if (styleName != null && qualifiedName != null) {
            result.add(new FlexCssPropertyDescriptor(ContainerUtil.newLinkedHashSet(
              FlexStyleIndexInfo.create(qualifiedName, styleName, jsAttribute, true))));
          }
        }
        return true;
      }

      @Override
      public boolean handleOtherElement(PsiElement el, PsiElement context, @Nullable Ref<PsiElement> continuePassElement) {
        return true;
      }
    });
    for (JSClass jsSuper : jsClass.getSupers()) {
      if (jsSuper != null) {
        fillPropertyDescriptorsDynamically(jsSuper, visited, result);
      }
    }
  }

  @NotNull
  private static Collection<? extends CssPropertyDescriptor> getPropertyDescriptorsDynamically(@NotNull List<CssSimpleSelector> selectors,
                                                                                               @NotNull Module module) {
    FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
    GlobalSearchScope scope = module.getModuleWithDependenciesAndLibrariesScope(false);
    Set<JSClass> visited = new LinkedHashSet<>();
    Set<CssPropertyDescriptor> result = new LinkedHashSet<>();
    Project project = module.getProject();

    for (CssSimpleSelector selector : selectors) {
      final JSClass jsClass = getClassFromMxmlDescriptor(selector, module);
      if (jsClass != null) {
        fillPropertyDescriptorsDynamically(jsClass, visited, result);
        continue;
      }

      final String shortClassName = selector.getElementName();
      Collection<JSQualifiedNamedElement> candidates = JSResolveUtil.findElementsByName(shortClassName, project, scope);
      for (JSQualifiedNamedElement candidate : candidates) {
        if (candidate instanceof JSClass) {
          fillPropertyDescriptorsDynamically((JSClass)candidate, visited, result);
        }
      }
    }

    for (Iterator<CssPropertyDescriptor> iterator = result.iterator(); iterator.hasNext();) {
      CssPropertyDescriptor propertyDescriptor = iterator.next();
      List<Set<FlexStyleIndexInfo>> values = fileBasedIndex.getValues(FlexStyleIndex.INDEX_ID, propertyDescriptor.getPropertyName(), scope);
      if (values.size() == 0) {
        iterator.remove();
      }
    }
    return result;
  }

  private static boolean containsGlobalSelectors(@NotNull List<CssSimpleSelector> selectors) {
    for (CssSimpleSelector selector : selectors) {
      final String elementName = selector.getElementName();
      if (elementName.isEmpty() || "global".equals(elementName) || "*".equals(elementName)) {
        return  true;
      }
    }
    return false;
  }

  @Override
  @NotNull
  public Collection<? extends CssPropertyDescriptor> getAllPropertyDescriptors(@Nullable PsiElement context) {
    if(context == null || DumbService.getInstance(context.getProject()).isDumb()) {
      return Collections.emptyList();
    }
    Module module = findModuleForPsiElement(context);
    List<CssSimpleSelector> simpleSelectors = findSimpleSelectorsAbove(context);
    if (simpleSelectors.size() > 0 && !containsGlobalSelectors(simpleSelectors)) {
      if (module != null) {
        return getPropertyDescriptorsDynamically(simpleSelectors, module);
      }
    }
    FileBasedIndex index = FileBasedIndex.getInstance();
    Collection<String> keys = ContainerUtil.sorted(index.getAllKeys(FlexStyleIndex.INDEX_ID, context.getProject()));
    List<FlexCssPropertyDescriptor> result = new ArrayList<>();
    GlobalSearchScope scope = FlexCssUtil.getResolveScope(context);
    for (String key : keys) {
      if (!isInClassicForm(key)) {
        for (Set<FlexStyleIndexInfo> infos : index.getValues(FlexStyleIndex.INDEX_ID, key, scope)) {
          result.add(new FlexCssPropertyDescriptor(infos));
        }
      }
    }
    return result;
  }

  @Override
  public String @NotNull [] getSimpleSelectors(@NotNull PsiElement context) {
    Module module = findModuleForPsiElement(context);
    if (module == null) {
      return ArrayUtilRt.EMPTY_STRING_ARRAY;
    }
    CodeContext codeContext = CodeContext.getContext(JavaScriptSupportLoader.MXML_URI, module);
    XmlElementDescriptor[] descriptors = codeContext.getDescriptorsWithAllowedDeclaration();
    String[] selectors = new String[descriptors.length + 1];
    selectors[0] = "global";
    int i = 1;
    for (XmlElementDescriptor descriptor : descriptors) {
      selectors[i++] = descriptor.getName();
    }
    return selectors;
  }

  @Nullable
  private static Collection<JSQualifiedNamedElement> getClasses(String className, PsiElement context) {
    if (context == null) return null;
    Module module = findModuleForPsiElement(context);
    GlobalSearchScope scope = module != null ? module.getModuleWithDependenciesAndLibrariesScope(false) : context.getResolveScope();
    return JSResolveUtil.findElementsByName(className, context.getProject(), scope);
  }

  @Nullable
  public static XmlElementDescriptor getTypeSelectorDescriptor(@NotNull CssSimpleSelector selector, @NotNull Module module) {
    CssStylesheet stylesheet = ((StylesheetFile)selector.getContainingFile()).getStylesheet();
    CssNamespace namespace = stylesheet != null ? stylesheet.getNamespace(selector.getNamespaceName()) : null;
    if (namespace != null && namespace.getUri() != null) {
      return CodeContext.getContext(namespace.getUri(), module).getElementDescriptor(selector.getElementName(), (XmlTag)null);
    }
    else {
      return null;
    }
  }

  @Nullable
  private static JSClass getClassFromMxmlDescriptor(@NotNull CssSimpleSelector selector, @NotNull Module module) {
    final XmlElementDescriptor xmlElementDescriptor = getTypeSelectorDescriptor(selector, module);
    if (xmlElementDescriptor == null) {
      return null;
    }

    final PsiElement declaration = xmlElementDescriptor.getDeclaration();
    return declaration instanceof JSClass ? (JSClass)declaration : null;
  }

  @Override
  public PsiElement @NotNull [] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
    // flex 4
    Module module = findModuleForPsiElement(selector);
    // only for project files, due to unknown code context otherwise
    if (module != null) {
      final JSClass jsClass = getClassFromMxmlDescriptor(selector, module);
      if (jsClass != null) {
        return new PsiElement[]{jsClass};
      }
    }

    // flex 3 or file not in project files
    return getDeclarationsForSimpleSelector(selector.getElementName(), selector);
  }

  @NotNull
  private static PsiElement getReferencedElement(@NotNull PsiElement element) {
    if (element instanceof ActionScriptClassImpl) {
      return element.getNavigationElement();
    }
    else if (element instanceof XmlBackedJSClassImpl) {
      PsiElement parent = element.getParent();
      if (parent != null) {
        PsiFile file = parent.getContainingFile();
        if (file != null) {
          return file;
        }
      }
    }
    return element;
  }

  private static PsiElement @NotNull [] getDeclarationsForSimpleSelector(@NotNull String className, @Nullable PsiElement context) {
    Collection<JSQualifiedNamedElement> elements = getClasses(className, context);
    if (elements != null && elements.size() > 0) {
      List<PsiElement> result = new ArrayList<>();
      Set<String> qNames = new LinkedHashSet<>();
      for (JSQualifiedNamedElement c : elements) {
        if (c instanceof JSClass) {
          // do not add classes with same qualified names
          String qName = c.getQualifiedName();
          if (qNames.add(qName)) {
            result.add(getReferencedElement(c));
          }
        }
      }
      return toPsiElementArray(result);
    }
    return PsiElement.EMPTY_ARRAY;
  }

  @Override
  public boolean providesClassicCss() {
    return false;
  }

  @Override
  public String generateDocForSelector(@NotNull String selectorName, @Nullable PsiElement context) {
    PsiElement[] declarations = getDeclarationsForSimpleSelector(selectorName, context);
    JSClass[] classes = new JSClass[declarations.length];
    for (int i = 0; i < declarations.length; i++) {
      PsiElement declaration = declarations[i];
      assert declaration instanceof JSClass;
      classes[i] = (JSClass)declaration;
    }
    Arrays.sort(classes, (c1, c2) -> Comparing.compare(c1.getQualifiedName(), c2.getQualifiedName()));

    StringBuilder builder = new StringBuilder();
    for (int i = 0, n = classes.length; i < n; i++) {
      JSClass jsClass = classes[i];
      PsiFile file = jsClass.getContainingFile();
      if (file != null) {
        DocumentationProvider provider = DocumentationManager.getProviderFromElement(jsClass);
        String docForDeclaration = provider.generateDoc(jsClass, jsClass);
        if (docForDeclaration != null) {
          if (i > 0) {
            int definitionEnd = docForDeclaration.indexOf(DocumentationMarkup.DEFINITION_END);
            if (definitionEnd > 0) {
              docForDeclaration = docForDeclaration.substring(definitionEnd + DocumentationMarkup.DEFINITION_END.length());
            }
          }
          builder.append(docForDeclaration);
        }
      }
    }
    return builder.toString();
  }

  @NotNull
  private static List<CssSimpleSelector> findSimpleSelectorsAbove(@NotNull PsiElement context) {
    List<CssSimpleSelector> result = new ArrayList<>();
    CssRuleset ruleset = PsiTreeUtil.getParentOfType(context, CssRuleset.class);
    if (ruleset != null) {
      for (CssSelector selector : ruleset.getSelectors()) {
        CssSimpleSelector simpleSelector = null;
        for (PsiElement child : selector.getChildren()) {
          if (child instanceof CssSimpleSelector) {
            simpleSelector = (CssSimpleSelector)child;
          }
        }
        if (simpleSelector != null) {
          result.add(simpleSelector);
        }
      }
    }
    return result;
  }

  @Override
  @NotNull
  public PsiReference getStyleReference(PsiElement element, int start, int end, boolean caseSensitive) {
    return new HtmlCssClassOrIdReference(element, start, end, caseSensitive, false);
  }

  @Override
  public Color getColorByValue(@NotNull String value) {
    try {
      int rgb = Integer.parseInt(value);
      return new JBColor(rgb, rgb);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public boolean isColorTerm(@NotNull CssTerm term) {
    return term.getTermType() == CssTermTypes.NUMBER;
  }

  @Override
  public LocalQuickFix @NotNull [] getQuickFixesForUnknownProperty(@NotNull String propertyName, @NotNull PsiElement context, boolean isOnTheFly) {
    if (!isOnTheFly) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final VirtualFile vFile = checkForQuickFixAndGetVFile(context);
    if (vFile == null) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final String dialectName = CssDialectMappings.getInstance(context.getProject()).getMapping(vFile);
    if (CssDialect.CLASSIC.getName().equals(dialectName)) {
      final Collection<? extends CssPropertyDescriptor> flexDescriptor = findPropertyDescriptors(propertyName, context);
      if (!flexDescriptor.isEmpty()) {
        return new LocalQuickFix[]{new SwitchToCssDialectQuickFix(FlexCSSDialect.getInstance())};
      }
    }
    else {
      final CssElementDescriptorProviderImpl classicCssDescriptorProvider =
        CssElementDescriptorProvider.EP_NAME.findExtension(CssElementDescriptorProviderImpl.class);
      if (classicCssDescriptorProvider != null) {
        Collection<? extends CssPropertyDescriptor> classicDescriptors = classicCssDescriptorProvider.findPropertyDescriptors(propertyName,
                                                                                                                              context);
        if (!classicDescriptors.isEmpty()) {
          return new LocalQuickFix[]{new SwitchToCssDialectQuickFix(CssDialect.CLASSIC)};
        }
      }
    }
    return LocalQuickFix.EMPTY_ARRAY;
  }

  @Override
  public LocalQuickFix @NotNull [] getQuickFixesForUnknownSimpleSelector(@NotNull String selectorName,
                                                                         @NotNull PsiElement context,
                                                                         boolean isOnTheFly) {
    if (!isOnTheFly) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final VirtualFile vFile = checkForQuickFixAndGetVFile(context);
    if (vFile == null) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final String dialectName = CssDialectMappings.getInstance(context.getProject()).getMapping(vFile);
    if (CssDialect.CLASSIC.getName().equals(dialectName)) {
      if (isPossibleSelector(selectorName, context)) {
        return new LocalQuickFix[]{new SwitchToCssDialectQuickFix(FlexCSSDialect.getInstance())};
      }
    }
    else {
      final CssElementDescriptorProviderImpl classicCssDescriptorProvider =
        CssElementDescriptorProvider.EP_NAME.findExtension(CssElementDescriptorProviderImpl.class);
      if (classicCssDescriptorProvider != null && classicCssDescriptorProvider.isPossibleSelector(selectorName, context)) {
        return new LocalQuickFix[]{new SwitchToCssDialectQuickFix(CssDialect.CLASSIC)};
      }
    }
    return LocalQuickFix.EMPTY_ARRAY;
  }

  @Override
  public boolean isColorTermsSupported() {
    return false;
  }

  @Nullable
  private static VirtualFile checkForQuickFixAndGetVFile(@NotNull PsiElement context) {
    final PsiFile file = InjectedLanguageManager.getInstance(context.getProject()).getTopLevelFile(context);
    if (file == null) {
      return null;
    }

    final VirtualFile vFile = file.getOriginalFile().getVirtualFile();
    if (vFile == null || !(FileTypeRegistry.getInstance().isFileOfType(vFile, CssFileType.INSTANCE))) {
      return null;
    }

    final Module module = findModuleForFile(vFile, context.getProject());
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) {
      return null;
    }

    return vFile;
  }
}
