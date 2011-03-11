package com.intellij.javascript.flex.css;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSClassImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.*;
import com.intellij.psi.css.impl.CssTermTypes;
import com.intellij.psi.css.impl.util.references.HtmlCssClassOrIdReference;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.HashSet;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexCssElementDescriptorProvider implements CssElementDescriptorProvider {  
  public boolean isMyContext(@Nullable PsiElement context) {
    if (context == null) return false;
    PsiFile file = context.getContainingFile();
    if (file == null) return false;
    if (HtmlUtil.hasHtml(file)) return false;

    Module module = ModuleUtil.findModuleForPsiElement(context);
    if (module == null) {
      PsiFile topLevelFile = InjectedLanguageUtil.getTopLevelFile(context);
      if (topLevelFile != null) {
        module = ModuleUtil.findModuleForPsiElement(topLevelFile);
      }
    }
    if (module == null || !FlexUtils.isFlexModuleOrContainsFlexFacet(module)) {
      return false;
    }
    //return !isLinkedFromHtmlOnly(file, module.getProject());
    return true;
  }

  private static boolean isLinkedFromHtmlOnly(@NotNull PsiFile file, @NotNull Project project) {
    file = file.getOriginalFile();
    if (file.getFileType() != CssSupportLoader.CSS_FILE_TYPE) {
      return false;
    }
    if (InjectedLanguageManager.getInstance(project).isInjectedFragment(file)) {
      return false;
    }
    final boolean[] result = {false};
    ReferencesSearch.search(file, new MyGlobalSearchScope(project), true).forEach(new Processor<PsiReference>() {
      @Override
      public boolean process(PsiReference ref) {
        if (ref != null) {
          PsiElement element = ref.getElement();
          if (element != null) {
            PsiFile refFile = element.getContainingFile();
            if (refFile != null) {
              if (HtmlUtil.hasHtml(refFile)) {
                result[0] = true;
              }
              if (JavaScriptSupportLoader.isFlexMxmFile(refFile)) {
                result[0] = false;
                return false;
              }
            }
          }
        }
        return true;
      }
    });
    return result[0];
  }

  @Nullable
  private static String findJsClassOrFile(@NotNull JSClass root, Set<JSClass> visited, Set<String> possibleQNames) {
    if (!visited.add(root)) return null;
    String qName = root.getQualifiedName();
    if (qName != null && possibleQNames.contains(qName)) {
      return qName;
    }
    Set<String> includes = new HashSet<String>();
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
                                                 List<String> selectors,
                                                 @NotNull GlobalSearchScope scope) {
    Set<String> allNames = new HashSet<String>();
    for (Collection<FlexStyleIndexInfo> collection : collections) {
      for (FlexStyleIndexInfo info : collection) {
        allNames.add(info.getClassOrFileName());
      }
    }
    Set<String> namesFromSelectors = null;
    if (selectors.size() > 0 && !containsGlobalSelectors(selectors)) {
      namesFromSelectors = new HashSet<String>();
      for (String selector : selectors) {
        Collection<JSQualifiedNamedElement> elements = JSResolveUtil.findElementsByName(selector, scope.getProject(), scope);
        for (PsiElement element : elements) {
          if (element instanceof JSClass) {
            String classOrFileName = findJsClassOrFile((JSClass)element, new HashSet<JSClass>(), allNames);
            if (classOrFileName != null) {
              namesFromSelectors.add(classOrFileName);
            }
          }
        }
      }
    }
    List<FlexStyleIndexInfo> result = new ArrayList<FlexStyleIndexInfo>();
    for (Collection<FlexStyleIndexInfo> collection : collections) {
      for (FlexStyleIndexInfo info : collection) {
        if (namesFromSelectors == null || namesFromSelectors.contains(info.getClassOrFileName())) {
          result.add(info);
        }
      }
    }
    return result;
  }

  public PsiElement getDocumentationElementForSelector(@NotNull String text, @Nullable PsiElement context) {
    if (context != null) {
      Collection<JSQualifiedNamedElement> classes = getClasses(text, context);
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

  public CssPropertyDescriptor getPropertyDescriptor(@NotNull String propertyName, @Nullable PsiElement context) {
    if (context != null) {
      Module module = ModuleUtil.findModuleForPsiElement(context);
      GlobalSearchScope scope = module != null ? module.getModuleWithDependenciesAndLibrariesScope(false):context.getResolveScope();
      List<Set<FlexStyleIndexInfo>> lists = FileBasedIndex.getInstance().getValues(FlexStyleIndex.INDEX_ID, propertyName, scope);
      List<String> selectors = findSimpleSelectorsAbove(context);
      List<FlexStyleIndexInfo> infos = filter(lists, selectors, scope);
      if (infos.size() > 0) {
        return new FlexCssPropertyDescriptor(infos);
      }
    }
    return null;
  }

  public boolean isPossibleSelector(@NotNull String selector, @NotNull PsiElement context) {
    if (selector.equals("global")) return true;
    Module module = ModuleUtil.findModuleForPsiElement(context);
    GlobalSearchScope scope = module != null ? module.getModuleWithDependenciesAndLibrariesScope(false) :context.getResolveScope();
    Collection<JSQualifiedNamedElement> classes = JSResolveUtil.findElementsByName(selector, context.getProject(), scope);
    for (JSQualifiedNamedElement c : classes) {
      if (c instanceof JSClass) {
        return true;
      }
    }
    return false;
  }

  public boolean isPossiblePseudoClass(@NotNull String pseudoClass, @NotNull PsiElement context) {
    return true;
  }

  @NotNull
  public String[] getPossiblePseudoClasses(@NotNull PsiElement context) {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  private static boolean isInClassicForm(String propertyName) {
    return propertyName.indexOf('-') >= 0;
  }

  private static void fillPropertyNamesDinamically(@NotNull JSClass jsClass, Set<JSClass> visited, final Set<String> result) {
    if (!visited.add(jsClass)) return;
    FlexUtils.processMetaAttributesForClass(jsClass, new JSResolveUtil.MetaDataProcessor() {
      public boolean process(@NotNull JSAttribute jsAttribute) {
        if (FlexAnnotationNames.STYLE.equals(jsAttribute.getName())) {
          JSAttributeNameValuePair pair = jsAttribute.getValueByName("name");
          String styleName = pair != null ? pair.getSimpleValue() : null;
          if (styleName != null) {
            result.add(styleName);
          }
        }
        return true;
      }

      public boolean handleOtherElement(PsiElement el, PsiElement context, @Nullable Ref<PsiElement> continuePassElement) {
        return true;
      }
    });
    for (JSClass jsSuper : jsClass.getSupers()) {
      if (jsSuper != null) {
        fillPropertyNamesDinamically(jsSuper, visited, result);
      }
    }
  }

  @NotNull
  private static String[] getPropertyNamesDynamically(@NotNull List<String> shortClassNames, @NotNull Module module) {
    FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
    GlobalSearchScope scope = module.getModuleWithDependenciesAndLibrariesScope(false);
    Set<JSClass> visited = new HashSet<JSClass>();
    Set<String> result = new HashSet<String>();
    Project project = module.getProject();
    for (String shortClassName : shortClassNames) {
      Collection<JSQualifiedNamedElement> candidates = JSResolveUtil.findElementsByName(shortClassName, project, scope);
      for (JSQualifiedNamedElement candidate : candidates) {
        if (candidate instanceof JSClass) {
          fillPropertyNamesDinamically((JSClass)candidate, visited, result);
        }
      }
    }
    for (Iterator<String> iterator = result.iterator(); iterator.hasNext();) {
      String propertyName = iterator.next();
      List<Set<FlexStyleIndexInfo>> values = fileBasedIndex.getValues(FlexStyleIndex.INDEX_ID, propertyName, scope);
      if (values.size() == 0) {
        iterator.remove();
      }
    }
    return ArrayUtil.toStringArray(result);
  }

  private static boolean containsGlobalSelectors(@NotNull Collection<String> selectors) {
    return selectors.contains("") || selectors.contains("global") || selectors.contains("*");
  }

  @NotNull
  public String[] getPropertyNames(@NotNull PsiElement context) {
    List<String> simpleSelectors = findSimpleSelectorsAbove(context);
    if (simpleSelectors.size() > 0 && !containsGlobalSelectors(simpleSelectors)) {
      Module module = ModuleUtil.findModuleForPsiElement(context);
      if (module != null) {
        return getPropertyNamesDynamically(simpleSelectors, module);
      }
    }
    FileBasedIndex index = FileBasedIndex.getInstance();
    Collection<String> keys = index.getAllKeys(FlexStyleIndex.INDEX_ID, context.getProject());
    List<String> result = new ArrayList<String>();
    for (String key : keys) {
      if (!isInClassicForm(key)) {
        result.add(key);
      }
    }
    return ArrayUtil.toStringArray(result);
  }

  @NotNull
  public String[] getSimpleSelectors(@NotNull PsiElement context) {
    Module module = ModuleUtil.findModuleForPsiElement(context);
    if (module == null) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
    CodeContext codeContext = CodeContext.getContext(JavaScriptSupportLoader.MXML_URI, module);
    XmlElementDescriptor[] descriptors = codeContext.getAllDescriptors();
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
    Module module = ModuleUtil.findModuleForPsiElement(context);
    GlobalSearchScope scope = module != null ? module.getModuleWithDependenciesAndLibrariesScope(false) : context.getResolveScope();
    return JSResolveUtil.findElementsByName(className, context.getProject(), scope);
  }
  
  @Nullable
  public static XmlElementDescriptor getTypeSelectorDescriptor(@NotNull CssSimpleSelector selector, @NotNull Module module) {
    CssNamespace namespace = ((CssFile)selector.getContainingFile()).getStylesheet().getNamespace(selector.getNamespaceName());
    if (namespace != null && namespace.getUri() != null) {
      return CodeContext.getContext(namespace.getUri(), module).getElementDescriptor(selector.getElementName(), (XmlTag)null);
    }
    else {
      return null;
    }
  }

  @NotNull
  public PsiElement[] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
    // flex 4
    Module module = ModuleUtil.findModuleForPsiElement(selector);
    // only for project files, due to unknown code context otherwise
    if (module != null) {
      XmlElementDescriptor elementDescriptor = getTypeSelectorDescriptor(selector, module);
      if (elementDescriptor != null) {
        PsiElement jsClass = elementDescriptor.getDeclaration();
        if (jsClass instanceof JSClass) {
          return new PsiElement[]{jsClass};
        }
      }
    }
    
    // flex 3 or file not in project files
    return getDeclarationsForSimpleSelector(selector.getElementName(), selector);
  }

  @NotNull
  private static PsiElement getReferencedElement(@NotNull PsiElement element) {
    if (element instanceof JSClassImpl) {
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

  @NotNull
  private static PsiElement[] getDeclarationsForSimpleSelector(@NotNull String className, @NotNull PsiElement context) {  
    Collection<JSQualifiedNamedElement> elements = getClasses(className, context);
    if (elements != null && elements.size() > 0) {
      List<PsiElement> result = new ArrayList<PsiElement>();
      Set<String> qNames = new HashSet<String>();
      for (JSQualifiedNamedElement c : elements) {
        if (c instanceof JSClass) {
          // do not add classes with same qualified names
          String qName = c.getQualifiedName();
          if (qNames.add(qName)) {
            result.add(getReferencedElement(c));
          }
        }
      }
      return PsiUtilBase.toPsiElementArray(result);
    }
    return PsiElement.EMPTY_ARRAY;
  }

  public boolean providesClassicCss() {
    return false;
  }

  public String generateDocForSelector(@NotNull String s, @NotNull PsiElement context) {
    PsiElement[] declarations = getDeclarationsForSimpleSelector(s, context);
    JSClass[] classes = new JSClass[declarations.length];
    for (int i = 0; i < declarations.length; i++) {
      PsiElement declaration = declarations[i];
      assert declaration instanceof JSClass;
      classes[i] = (JSClass)declaration;
    }
    Arrays.sort(classes, new Comparator<JSClass>() {
      public int compare(JSClass c1, JSClass c2) {
        return Comparing.compare(c1.getQualifiedName(), c2.getQualifiedName());
      }
    });

    StringBuilder builder = new StringBuilder();
    for (int i = 0, n = classes.length; i < n; i++) {
      JSClass jsClass = classes[i];
      PsiFile file = jsClass.getContainingFile();
      if (file != null) {
        DocumentationProvider provider = DocumentationManager.getProviderFromElement(jsClass);
        String docForDeclaration = provider.generateDoc(jsClass, jsClass);
        if (docForDeclaration != null) {
          builder.append(docForDeclaration);
          if (i != n - 1) {
            builder.append("<br><br>\n\n");
          }
        }
      }
    }
    return builder.toString();
  }

  @NotNull
  private static List<String> findSimpleSelectorsAbove(@NotNull PsiElement context) {
    List<String> result = new ArrayList<String>();
    CssRuleset ruleset = PsiTreeUtil.getParentOfType(context, CssRuleset.class);
    if (ruleset != null) {
      CssSelectorList selectorList = ruleset.getSelectorList();
      if (selectorList != null) {
        for (CssSelector selector : selectorList.getSelectors()) {
          CssSimpleSelector simpleSelector = null;
          for (PsiElement child : selector.getChildren()) {
            if (child instanceof CssSimpleSelector) {
              simpleSelector = (CssSimpleSelector)child;
            }
          }
          if (simpleSelector != null) {
            String elementName = simpleSelector.getElementName();
            if (elementName != null) {
              result.add(elementName);
            }
          }
        }
      }
    }
    return result;
  }

  @NotNull
  public PsiReference getStyleReference(PsiElement element, int start, int end, boolean caseSensitive) {
    return new HtmlCssClassOrIdReference(element, start, end, caseSensitive, false);
  }

  @Override
  public Color getColorByValue(@NotNull String value) {
    try {
      return new Color(Integer.parseInt(value));
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public boolean isColorTerm(@NotNull CssTerm term) {
    return term.getTermType() == CssTermTypes.NUMBER;
  }

  private static class MyGlobalSearchScope extends GlobalSearchScope {

    private final GlobalSearchScope myDelegate;

    public MyGlobalSearchScope(Project project) {
      myDelegate = ProjectScope.getProjectScope(project);
    }

    @Override
    public boolean contains(VirtualFile file) {
      if (!myDelegate.contains(file)) {
        return false;
      }
      FileType type = file.getFileType();
      return type == StdFileTypes.HTML || type == StdFileTypes.XHTML ||
             type == StdFileTypes.JSP || type == StdFileTypes.JSPX ||
             type == StdFileTypes.XML;
    }

    @Override
    public int compare(VirtualFile file1, VirtualFile file2) {
      return 0;
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull Module module) {
      return true;
    }

    @Override
    public boolean isSearchInLibraries() {
      return false;
    }
  }
}
