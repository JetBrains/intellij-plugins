package com.intellij.javascript.flex.css;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSClassImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.command.undo.BasicUndoableAction;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UnexpectedUndoException;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
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
import com.intellij.psi.css.impl.util.table.CssElementDescriptorProviderImpl;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.HashSet;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.intellij.openapi.module.ModuleUtilCore.findModuleForFile;
import static com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement;
import static com.intellij.psi.util.PsiUtilCore.toPsiElementArray;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexCssElementDescriptorProvider extends CssElementDescriptorProvider {
  public boolean isMyContext(@Nullable PsiElement context) {
    if (context == null) return false;
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
      file = InjectedLanguageUtil.getTopLevelFile(context);
      if (file != null) {
        module = findModuleForPsiElement(file);
      }
    }
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) {
      return false;
    }

    if (vFile != null) {
      final CssDialect dialect = CssDialectMappings.getInstance(context.getProject()).getMapping(vFile);
      return dialect != CssDialect.CLASSIC;
    }

    return true;
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
                                                 List<CssSimpleSelector> selectors,
                                                 @NotNull GlobalSearchScope scope,
                                                 @Nullable Module module) {
    Set<String> allNames = new HashSet<String>();
    for (Collection<FlexStyleIndexInfo> collection : collections) {
      for (FlexStyleIndexInfo info : collection) {
        allNames.add(info.getClassOrFileName());
      }
    }
    Set<String> namesFromSelectors = null;
    if (selectors.size() > 0 && !containsGlobalSelectors(selectors)) {
      namesFromSelectors = new HashSet<String>();
      for (CssSimpleSelector selector : selectors) {

        if (module != null) {
          final JSClass jsClass = getClassFromMxmlDescriptor(selector, module);
          if (jsClass != null) {
            String classOrFileName = findJsClassOrFile(jsClass, new HashSet<JSClass>(), allNames);
            if (classOrFileName != null) {
              namesFromSelectors.add(classOrFileName);
            }
            continue;
          }
        }

        final String selectorName = selector.getElementName();
        if (selectorName != null) {
          Collection<JSQualifiedNamedElement> elements = JSResolveUtil.findElementsByName(selectorName, scope.getProject(), scope);
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
      Module module = findModuleForPsiElement(context);
      GlobalSearchScope scope = FlexCssUtil.getResolveScope(context);
      List<Set<FlexStyleIndexInfo>> lists = FileBasedIndex.getInstance().getValues(FlexStyleIndex.INDEX_ID, propertyName, scope);
      List<CssSimpleSelector> selectors = findSimpleSelectorsAbove(context);
      List<FlexStyleIndexInfo> infos = filter(lists, selectors, scope, module);
      if (infos.size() > 0) {
        return new FlexCssPropertyDescriptor(infos);
      }
    }
    return null;
  }

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
  private static String[] getPropertyNamesDynamically(@NotNull List<CssSimpleSelector> selectors, @NotNull Module module) {
    FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
    GlobalSearchScope scope = module.getModuleWithDependenciesAndLibrariesScope(false);
    Set<JSClass> visited = new HashSet<JSClass>();
    Set<String> result = new HashSet<String>();
    Project project = module.getProject();

    for (CssSimpleSelector selector : selectors) {

      final JSClass jsClass = getClassFromMxmlDescriptor(selector, module);
      if (jsClass != null) {
        fillPropertyNamesDinamically(jsClass, visited, result);
        continue;
      }

      final String shortClassName = selector.getElementName();
      if (shortClassName == null) {
        continue;
      }

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

  private static boolean containsGlobalSelectors(@NotNull List<CssSimpleSelector> selectors) {
    for (CssSimpleSelector selector : selectors) {
      final String elementName = selector.getElementName();
      if ("".equals(elementName) || "global".equals(elementName) || "*".equals(elementName)) {
        return  true;
      }
    }
    return false;
  }

  @NotNull
  public String[] getPropertyNames(@Nullable PsiElement context) {
    if(context == null) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
    List<CssSimpleSelector> simpleSelectors = findSimpleSelectorsAbove(context);
    if (simpleSelectors.size() > 0 && !containsGlobalSelectors(simpleSelectors)) {
      Module module = findModuleForPsiElement(context);
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
  public String[] getSimpleSelectors(@Nullable PsiElement context) {
    if (context == null) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }
    Module module = findModuleForPsiElement(context);
    if (module == null) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
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
    CssNamespace namespace = ((CssFile)selector.getContainingFile()).getStylesheet().getNamespace(selector.getNamespaceName());
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

  @NotNull
  public PsiElement[] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
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
      return toPsiElementArray(result);
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
  private static List<CssSimpleSelector> findSimpleSelectorsAbove(@NotNull PsiElement context) {
    List<CssSimpleSelector> result = new ArrayList<CssSimpleSelector>();
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
            result.add(simpleSelector);
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

  @NotNull
  @Override
  public LocalQuickFix[] getQuickFixesForUnknownProperty(@NotNull String propertyName, @NotNull PsiElement context, boolean isOnTheFly) {
    if (!isOnTheFly) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final VirtualFile vFile = checkForQuickFixAndGetVFile(context);
    if (vFile == null) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final CssDialect dialect = CssDialectMappings.getInstance(context.getProject()).getMapping(vFile);
    if (dialect == CssDialect.CLASSIC) {
      final CssPropertyDescriptor flexDescriptor = getPropertyDescriptor(propertyName, context);
      if (flexDescriptor != null) {
        return new LocalQuickFix[]{new MySwitchToCssDialectQuickFix(CssDialect.FLEX, vFile)};
      }
    }
    else {
      final CssElementDescriptorProviderImpl classicCssDescriptorProvider =
        CssElementDescriptorProvider.EP_NAME.findExtension(CssElementDescriptorProviderImpl.class);
      if (classicCssDescriptorProvider != null) {
        final CssPropertyDescriptor classicDescriptor = classicCssDescriptorProvider.getPropertyDescriptor(propertyName, context);
        if (classicDescriptor != null) {
          return new LocalQuickFix[]{new MySwitchToCssDialectQuickFix(CssDialect.CLASSIC, vFile)};
        }
      }
    }
    return LocalQuickFix.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public LocalQuickFix[] getQuickFixesForUnknownSimpleSelector(@NotNull String selectorName,
                                                               @NotNull PsiElement context,
                                                               boolean isOnTheFly) {
    if (!isOnTheFly) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final VirtualFile vFile = checkForQuickFixAndGetVFile(context);
    if (vFile == null) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final CssDialect dialect = CssDialectMappings.getInstance(context.getProject()).getMapping(vFile);
    if (dialect == CssDialect.CLASSIC) {
      if (isPossibleSelector(selectorName, context)) {
        return new LocalQuickFix[]{new MySwitchToCssDialectQuickFix(CssDialect.FLEX, vFile)};
      }
    }
    else {
      final CssElementDescriptorProviderImpl classicCssDescriptorProvider =
        CssElementDescriptorProvider.EP_NAME.findExtension(CssElementDescriptorProviderImpl.class);
      if (classicCssDescriptorProvider != null && classicCssDescriptorProvider.isPossibleSelector(selectorName, context)) {
        return new LocalQuickFix[]{new MySwitchToCssDialectQuickFix(CssDialect.CLASSIC, vFile)};
      }
    }
    return LocalQuickFix.EMPTY_ARRAY;
  }

  @Override
  public boolean supportColorTerms() {
    return false;
  }

  @Nullable
  private static VirtualFile checkForQuickFixAndGetVFile(@NotNull PsiElement context) {
    final PsiFile file = InjectedLanguageUtil.getTopLevelFile(context);
    if (file == null) {
      return null;
    }

    final VirtualFile vFile = file.getOriginalFile().getVirtualFile();
    if (vFile == null || !CssDialectsConfigurable.canBeConfigured(vFile)) {
      return null;
    }

    final Module module = findModuleForFile(vFile, context.getProject());
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) {
      return null;
    }

    return vFile;
  }

  private static class MySwitchToCssDialectQuickFix implements LocalQuickFix, IntentionAction, HighPriorityAction {
    private final CssDialect myDialect;
    private final VirtualFile myVirtualFile;

    private MySwitchToCssDialectQuickFix(CssDialect dialect, VirtualFile virtualFile) {
      myDialect = dialect;
      myVirtualFile = virtualFile;
    }

    @NotNull
    @Override
    public String getName() {
      return FlexBundle.message("switch.to.css.dialect.quickfix.name", myVirtualFile.getName(), myDialect.getDisplayName());
    }

    @NotNull
    @Override
    public String getText() {
      return getName();
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return getName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
      return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      doApplyFix(project);
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
      doApplyFix(project);
    }

    private void doApplyFix(final Project project) {
      final CssDialectMappings mappings = CssDialectMappings.getInstance(project);
      final CssDialect oldDialect = mappings.getMapping(myVirtualFile);

      UndoManager.getInstance(project).undoableActionPerformed(new BasicUndoableAction() {
        @Override
        public void undo() throws UnexpectedUndoException {
          CssDialectMappings.getInstance(project).setMapping(myVirtualFile, oldDialect);
        }

        @Override
        public void redo() throws UnexpectedUndoException {
          CssDialectMappings.getInstance(project).setMapping(myVirtualFile, myDialect);
        }
      });

      mappings.setMapping(myVirtualFile, myDialect);
    }
  }
}
