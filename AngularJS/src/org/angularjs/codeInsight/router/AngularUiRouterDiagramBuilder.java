// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.router;

import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.include.FileIncludeManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.index.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Irina.Chernushina on 3/8/2016.
 */
public class AngularUiRouterDiagramBuilder {
  private final List<UiRouterState> myStates;
  private final Map<VirtualFile, Template> myTemplatesMap;
  private final Map<VirtualFile, RootTemplate> myRootTemplates;
  private final @NotNull Project myProject;
  private final SmartPointerManager mySmartPointerManager;
  private final Map<PsiFile, Set<VirtualFile>> myModuleRecursiveDependencies;
  private Map<VirtualFile, Map<String, UiRouterState>> myRootTemplates2States;
  private Map<VirtualFile, Map<String, UiRouterState>> myDefiningFiles2States;

  // todo different scope
  public AngularUiRouterDiagramBuilder(final @NotNull Project project) {
    myProject = project;
    myStates = new ArrayList<>();
    myTemplatesMap = new HashMap<>();
    myRootTemplates = new HashMap<>();
    mySmartPointerManager = SmartPointerManager.getInstance(myProject);
    myModuleRecursiveDependencies = new HashMap<>();
  }

  public void build() {
    addStatesFromIndex();
    addGenericStates();
    getRootPages();
    groupStates();
  }

  private void addStatesFromIndex() {
    final Collection<String> stateIds = AngularIndexUtil.getAllKeys(AngularUiRouterStatesIndex.KEY, myProject);
    for (String id : stateIds) {
      if (id.startsWith(".")) continue;
      final CommonProcessors.CollectProcessor<JSImplicitElement> processor = new CommonProcessors.CollectProcessor<>();
      AngularIndexUtil.multiResolve(myProject, AngularUiRouterStatesIndex.KEY, id, processor);
      for (JSImplicitElement element : processor.getResults()) {
        final UiRouterState state = new UiRouterState(id, element.getContainingFile().getVirtualFile());
        if (!element.getContainingFile().getLanguage().isKindOf(JavascriptLanguage.INSTANCE)
            && PsiTreeUtil.getParentOfType(element, JSEmbeddedContent.class) != null) {
          createRootTemplatesForEmbedded(element.getContainingFile());
        }

        final JSCallExpression call = findWrappingCallExpression(element);
        if (call != null) {
          final JSReferenceExpression methodExpression = ObjectUtils.tryCast(call.getMethodExpression(), JSReferenceExpression.class);
          if (methodExpression != null &&
              methodExpression.getQualifier() != null &&
              "state".equals(methodExpression.getReferenceName())) {
            final JSExpression[] arguments = call.getArguments();
            if (arguments.length > 0 && PsiTreeUtil.isAncestor(arguments[0], element.getNavigationElement(), false)) {
              state.setPointer(mySmartPointerManager.createSmartPsiElementPointer(arguments[0]));

              if (arguments.length > 1 && arguments[1] instanceof JSObjectLiteralExpression) {
                final JSObjectLiteralExpression object = (JSObjectLiteralExpression)arguments[1];
                fillStateParameters(state, object);
              }
              else if (arguments[0] instanceof JSObjectLiteralExpression) {
                final JSObjectLiteralExpression object = (JSObjectLiteralExpression)arguments[0];
                final JSProperty name = object.findProperty("name");
                if (name != null && PsiTreeUtil.isAncestor(name, element.getNavigationElement(), false)) {
                  fillStateParameters(state, object);
                }
              }
            }
          }
        }
        myStates.add(state);
      }
    }
  }

  private void addGenericStates() {
    final List<JSProperty> freeStates = new AngularRouterStateLoader(myProject).loadFreelyDefinedStates();
    for (JSProperty property : freeStates) {
      if (property.getValue() == null || !(property.getParent() instanceof JSObjectLiteralExpression)) continue;
      final String name = StringUtil.unquoteString(property.getValue().getText());
      final UiRouterState uiState = new UiRouterState(name, property.getContainingFile().getViewProvider().getVirtualFile());
      uiState.setGeneric(true);
      uiState.setPointer(mySmartPointerManager.createSmartPsiElementPointer(property));
      fillStateParameters(uiState, (JSObjectLiteralExpression)property.getParent());
      if (!myStates.contains(uiState)) myStates.add(uiState);
    }
  }

  public static @Nullable JSCallExpression findWrappingCallExpression(JSImplicitElement element) {
    if (element.getNavigationElement() instanceof JSCallExpression) return (JSCallExpression)element.getNavigationElement();
    return ObjectUtils.tryCast(JSStubBasedPsiTreeUtil.getContextOfType(element, TokenSet.create(JSStubElementTypes.CALL_EXPRESSION), true),
                               JSCallExpression.class);
  }

  private void groupStates() {
    // root template file vs. state
    // but the same state can be used for several root templates
    myRootTemplates2States = new HashMap<>();
    final Set<UiRouterState> statesUsedInRoots = new HashSet<>();
    for (Map.Entry<VirtualFile, RootTemplate> entry : myRootTemplates.entrySet()) {
      final Set<VirtualFile> modulesFiles = entry.getValue().getModulesFiles();
      for (UiRouterState state : myStates) {
        final PsiElement element = entry.getValue().getPointer().getElement();
        if (modulesFiles.contains(state.getFile()) ||
            element != null && element.getContainingFile().getVirtualFile().equals(state.getFile())) {
          putState2map(entry.getKey(), state, myRootTemplates2States);
          statesUsedInRoots.add(state);
        }
      }
    }

    myDefiningFiles2States = new HashMap<>();
    for (UiRouterState state : myStates) {
      if (statesUsedInRoots.contains(state)) continue;
      if (state.isGeneric()) {
        putState2map(myRootTemplates.keySet().iterator().next(), state, myRootTemplates2States);
      }
      else {
        putState2map(state.getFile(), state, myDefiningFiles2States);
      }
    }
  }

  private static void putState2map(final @NotNull VirtualFile rootFile, final @NotNull UiRouterState state,
                                   final @NotNull Map<VirtualFile, Map<String, UiRouterState>> rootMap) {
    Map<String, UiRouterState> map = rootMap.get(rootFile);
    if (map == null) rootMap.put(rootFile, (map = new HashMap<>()));
    if (map.containsKey(state.getName())) {
      final UiRouterState existing = map.get(state.getName());
      if (!Comparing.equal(existing.getPointer(), state.getPointer()) && state.getPointer() != null) {
        existing.addDuplicateDefinition(state);
      }
    }
    else {
      map.put(state.getName(), state);
    }
  }

  private void getRootPages() {
    final List<VirtualFile> roots = new ArrayList<>();

    final Map<PsiFile, AngularNamedItemDefinition> files = new HashMap<>();
    final FileBasedIndex instance = FileBasedIndex.getInstance();
    final Collection<String> keys = instance.getAllKeys(AngularAppIndex.ANGULAR_APP_INDEX, myProject);
    if (keys.isEmpty()) return;

    final PsiManager psiManager = PsiManager.getInstance(myProject);
    final GlobalSearchScope projectScope = GlobalSearchScope.projectScope(myProject);
    for (String key : keys) {
      instance.processValues(AngularAppIndex.ANGULAR_APP_INDEX, key, null, (file, value) -> {
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile != null) {
          files.put(psiFile, value);
        }
        return true;
      }, projectScope);
    }
    for (Map.Entry<PsiFile, AngularNamedItemDefinition> entry : files.entrySet()) {
      final PsiFile file = entry.getKey();
      final String relativeUrl = findPossibleRelativeUrl(roots, file.getVirtualFile());
      // not clear how then it can be part of application
      if (relativeUrl == null) continue;
      final Template template = readTemplateFromFile(myProject, relativeUrl, file);

      final String mainModule = entry.getValue().getName();
      final Set<VirtualFile> moduleFiles = getModuleFiles(file, mainModule);

      final RootTemplate rootTemplate = new RootTemplate(mySmartPointerManager.createSmartPsiElementPointer(file),
                                                         relativeUrl, template, moduleFiles);
      myRootTemplates.put(file.getVirtualFile(), rootTemplate);
    }
  }

  private void createRootTemplatesForEmbedded(@NotNull PsiFile containingFile) {
    final Template template = readTemplateFromFile(myProject, "/", containingFile);
    final RootTemplate rootTemplate = new RootTemplate(mySmartPointerManager.createSmartPsiElementPointer(containingFile),
                                                       "/", template, Collections.singleton(containingFile.getVirtualFile()));
    myRootTemplates.put(containingFile.getVirtualFile(), rootTemplate);
  }

  private static class NonCyclicQueue<T> {
    private final Set<T> processed = new HashSet<>();
    private final ArrayDeque<T> toProcess = new ArrayDeque<>();

    public void add(@NotNull T t) {
      if (processed.contains(t) || !check(t)) return;
      processed.add(t);
      toProcess.add(t);
    }

    protected boolean check(T t) {
      return true;
    }

    public void addAll(final Collection<? extends T> collection) {
      for (T t : collection) {
        add(t);
      }
    }

    public boolean isEmpty() {
      return toProcess.isEmpty();
    }

    public @Nullable T removeNext() {
      return toProcess.isEmpty() ? null : toProcess.remove();
    }

    public Set<T> getProcessed() {
      return processed;
    }
  }

  private @NotNull Set<VirtualFile> getModuleFiles(PsiFile file, String mainModule) {
    Set<VirtualFile> moduleFiles = myModuleRecursiveDependencies.get(file);
    if (moduleFiles != null) return moduleFiles;

    final NonCyclicQueue<String> modulesQueue = new NonCyclicQueue<>();
    final NonCyclicQueue<VirtualFile> filesQueue = new NonCyclicQueue<VirtualFile>() {
      @Override
      protected boolean check(VirtualFile file) {
        // do not add lib (especially angular) files
        return !NodeModuleUtil.isFromNodeModules(myProject, file);
      }
    };

    if (!StringUtil.isEmptyOrSpaces(mainModule)) {
      modulesQueue.add(mainModule);
    }
    filesQueue.add(file.getVirtualFile());

    while (!modulesQueue.isEmpty()) {
      final String moduleName = modulesQueue.removeNext();
      moduleDependenciesStep(moduleName, filesQueue, modulesQueue);
    }
    while (!filesQueue.isEmpty()) {
      final VirtualFile moduleFile = filesQueue.removeNext();
      filesDependenciesStep(moduleFile, filesQueue);
    }
    Set<VirtualFile> processed = filesQueue.getProcessed();

    final GlobalSearchScope projectScope = GlobalSearchScope.projectScope(myProject);
    processed =
      new HashSet<>(ContainerUtil.filter(processed, file1 -> file1.getFileType() instanceof LanguageFileType && ((LanguageFileType)file1
        .getFileType()).getLanguage().isKindOf(
        JavascriptLanguage.INSTANCE) && projectScope.contains(file1)));
    myModuleRecursiveDependencies.put(file, processed);
    return processed;
  }

  private void moduleDependenciesStep(String mainModule,
                                      NonCyclicQueue<? super VirtualFile> filesQueue,
                                      NonCyclicQueue<? super String> modulesQueue) {
    addContainingFile(filesQueue, mainModule);
    if (!StringUtil.isEmptyOrSpaces(mainModule)) {
      final JSImplicitElement element = AngularIndexUtil.resolve(myProject, AngularModuleIndex.KEY, mainModule);
      if (element != null) {
        final JSCallExpression callExpression = PsiTreeUtil.getParentOfType(element, JSCallExpression.class);
        if (callExpression == null) return;
        final List<String> dependenciesInModuleDeclaration = AngularModuleIndex.findDependenciesInModuleDeclaration(callExpression);
        if (dependenciesInModuleDeclaration != null) {
          for (String module : dependenciesInModuleDeclaration) {
            modulesQueue.add(module);
            addContainingFile(filesQueue, module);
          }
        }
      }
    }
  }

  private void addContainingFile(final @NotNull NonCyclicQueue<? super VirtualFile> filesQueue, final @NotNull String module) {
    final CommonProcessors.CollectProcessor<JSImplicitElement> collectProcessor = new CommonProcessors.CollectProcessor<>();

    AngularIndexUtil.multiResolve(myProject, AngularModuleIndex.KEY, module, collectProcessor);
    if (collectProcessor.getResults().size() != 1) return;
    for (JSImplicitElement element : collectProcessor.getResults()) {
      if (element != null && element.getNavigationElement() != null && element.getNavigationElement().getContainingFile() != null) {
        final VirtualFile file = element.getNavigationElement().getContainingFile().getVirtualFile();
        // prefer library resolves
        if (NodeModuleUtil.isFromNodeModules(myProject, file)) return;
      }
    }

    final JSImplicitElement element = collectProcessor.getResults().iterator().next();
    if (element != null && element.getNavigationElement() != null && element.getNavigationElement().getContainingFile() != null) {
      filesQueue.add(element.getNavigationElement().getContainingFile().getVirtualFile());
    }
  }

  private void filesDependenciesStep(VirtualFile file, NonCyclicQueue<? super VirtualFile> filesQueue) {
    final VirtualFile[] includedFiles = FileIncludeManager.getManager(myProject).getIncludedFiles(file, true, true);
    //take all included, since there can be also html includes (??? exclude css & like)
    filesQueue.addAll(Arrays.asList(includedFiles));
  }

  private String findPossibleRelativeUrl(final @NotNull List<? extends VirtualFile> roots, final @NotNull VirtualFile file) {
    VirtualFile contentRoot = null;
    for (VirtualFile root : roots) {
      if (root.equals(VfsUtilCore.getCommonAncestor(root, file))) {
        contentRoot = root;
        break;
      }
    }

    final VirtualFile ancestor = contentRoot == null ? myProject.getBaseDir() : contentRoot;
    if (ancestor == null) return null;
    final String relativePath = VfsUtilCore.getRelativePath(file, ancestor);
    return relativePath == null ? null : AngularUiRouterGraphBuilder.normalizeTemplateUrl(relativePath);
  }

  private void fillStateParameters(UiRouterState state, JSObjectLiteralExpression object) {
    final String url = getPropertyValueIfExists(object, "url");
    if (url != null) {
      state.setUrl(StringUtil.unquoteString(url));
    }
    final String parentKey = getPropertyValueIfExists(object, "parent");
    if (parentKey != null) {
      state.setParentName(parentKey);
    }
    final String templateUrl = getPropertyValueIfExists(object, "templateUrl");
    VirtualFile templateFile = null;
    if (templateUrl != null) {
      state.setTemplateUrl(templateUrl);
      final JSProperty urlProperty = object.findProperty("templateUrl");
      state.setTemplateFile(parseTemplate(templateUrl, urlProperty));
    }
    final JSProperty template = object.findProperty("template");
    if (templateUrl == null && object.findProperty("templateUrl") != null ||
        template != null || object.findProperty("templateProvider") != null) {
      state.setHasTemplateDefined(true);
    }
    if (template != null) {
      final PsiElement templateDefinition = findTemplateDefinitionObject(template);
      if (templateDefinition != null) state.setTemplatePointer(mySmartPointerManager.createSmartPsiElementPointer(templateDefinition));
    }
    final JSProperty views = object.findProperty("views");
    if (views != null) {
      final JSExpression value = views.getValue();
      if (value instanceof JSObjectLiteralExpression) {
        final JSProperty[] viewsProperties = ((JSObjectLiteralExpression)value).getProperties();
        if (viewsProperties != null && viewsProperties.length > 0) {
          final List<UiView> viewsList = new ArrayList<>();
          for (JSProperty property : viewsProperties) {
            if (property.getName() != null && property.getValue() != null) {
              viewsList.add(processView(property));
            }
          }
          state.setViews(viewsList);
        }
      }
    }
    final JSProperty abstractProperty = object.findProperty("abstract");
    if (abstractProperty != null && abstractProperty.getValue() instanceof JSLiteralExpression &&
        ((JSLiteralExpression)abstractProperty.getValue()).isBooleanLiteral() &&
        Boolean.TRUE.equals(((JSLiteralExpression)abstractProperty.getValue()).getValue())) {
      state.setAbstract(true);
    }
  }

  private static @Nullable PsiElement findTemplateDefinitionObject(final @NotNull JSProperty template) {
    final JSExpression value = template.getValue();
    if (value instanceof JSLiteralExpression) return value;
    if (value instanceof JSReferenceExpression) {
      final PsiElement resolve = ((JSReferenceExpression)value).resolve();
      if (resolve != null && resolve.isValid() && resolve instanceof JSVariable) {
        if (((JSVariable)resolve).getInitializer() instanceof JSLiteralExpression) return ((JSVariable)resolve).getInitializer();
      }
    }
    return null;
  }

  private @Nullable VirtualFile parseTemplate(final @NotNull String url, @Nullable JSProperty urlProperty) {
    PsiFile templateFile = null;
    Template template = null;
    if (urlProperty != null && urlProperty.getValue() != null) {
      int offset = urlProperty.getValue().getTextRange().getEndOffset() - 1;
      final PsiReference reference = urlProperty.getContainingFile().findReferenceAt(offset);
      if (reference != null) {
        final PsiElement templateFileElement = reference.resolve();
        if (templateFileElement != null && templateFileElement.isValid()) {
          templateFile = templateFileElement.getContainingFile();
          if (myTemplatesMap.containsKey(templateFile.getVirtualFile())) return templateFile.getVirtualFile();
          template = readTemplateFromFile(urlProperty.getProject(), url, templateFile);
          myTemplatesMap.put(templateFile.getVirtualFile(), template);
          return templateFile.getVirtualFile();
        }
      }
    }
    return null;
  }

  static @NotNull Template readTemplateFromFile(@NotNull Project project, @NotNull String url, PsiElement templateElement) {
    final PsiFile templateFile = templateElement.getContainingFile();
    final Map<String, SmartPsiElementPointer<PsiElement>> placeholders = new HashMap<>();
    final Set<String> placeholdersSet = new HashSet<>();
    final FileBasedIndex instance = FileBasedIndex.getInstance();
    final GlobalSearchScope scope = GlobalSearchScope.fileScope(project, templateFile.getVirtualFile());
    instance.processAllKeys(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, view -> {
      placeholdersSet.add(view);
      return true;
    }, scope, null);
    final SmartPointerManager smartPointerManager = SmartPointerManager.getInstance(project);
    for (String key : placeholdersSet) {
      instance.processValues(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, key, null,
                             (file, value) -> {
                               final JSImplicitElementImpl.Builder builder = new JSImplicitElementImpl.Builder(
                                 JSQualifiedNameImpl.fromQualifiedName(key), null);
                               final JSOffsetBasedImplicitElement implicitElement =
                                 new JSOffsetBasedImplicitElement(builder, (int)value.getStartOffset(), templateFile);
                               if (templateElement instanceof PsiFile ||
                                   PsiTreeUtil.isAncestor(templateElement, implicitElement, false)) {
                                 placeholders.put(key, smartPointerManager.createSmartPsiElementPointer(implicitElement));
                               }
                               return true;
                             }, scope);
    }
    final Template template = new Template(url, smartPointerManager.createSmartPsiElementPointer(templateElement));
    template.setViewPlaceholders(placeholders);
    return template;
  }

  private UiView processView(final @NotNull JSProperty property) {
    final String name = property.getName();
    final JSExpression value = property.getValue();
    final JSObjectLiteralExpression expression = ObjectUtils.tryCast(value, JSObjectLiteralExpression.class);
    String templateUrl = null;
    VirtualFile templateFile = null;
    if (expression != null) {
      templateUrl = getPropertyValueIfExists(expression, "templateUrl");
      if (templateUrl != null) {
        final JSProperty urlProperty = expression.findProperty("templateUrl");
        templateFile = parseTemplate(templateUrl, urlProperty);
      }
    }
    final UiView view = new UiView(name, templateUrl, templateFile,
                                   property.getNameIdentifier() == null
                                   ? null
                                   : mySmartPointerManager.createSmartPsiElementPointer(property.getNameIdentifier()));
    if (expression != null) {
      final JSProperty template = expression.findProperty("template");
      if (template != null) {
        final PsiElement templateDefinition = findTemplateDefinitionObject(template);
        if (templateDefinition != null) view.setTemplatePointer(mySmartPointerManager.createSmartPsiElementPointer(templateDefinition));
      }
    }
    return view;
  }

  private static @Nullable String getPropertyValueIfExists(final @NotNull JSObjectLiteralExpression object, final @NotNull String name) {
    final JSProperty urlProperty = object.findProperty(name);
    if (urlProperty != null && urlProperty.getValue() instanceof JSLiteralExpression &&
        ((JSLiteralExpression)urlProperty.getValue()).isQuotedLiteral()) {
      return StringUtil.unquoteString(urlProperty.getValue().getText());
    }
    return null;
  }

  public Map<VirtualFile, Template> getTemplatesMap() {
    return myTemplatesMap;
  }

  public Map<VirtualFile, RootTemplate> getRootTemplates() {
    return myRootTemplates;
  }

  public Map<VirtualFile, Map<String, UiRouterState>> getRootTemplates2States() {
    return myRootTemplates2States;
  }

  public Map<VirtualFile, Map<String, UiRouterState>> getDefiningFiles2States() {
    return myDefiningFiles2States;
  }
}
