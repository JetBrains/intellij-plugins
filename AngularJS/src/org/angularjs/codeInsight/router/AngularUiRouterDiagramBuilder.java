package org.angularjs.codeInsight.router;

import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.include.FileIncludeManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
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
  private final Map<String, UiRouterState> myStatesMap;
  private final Map<String, Template> myTemplatesMap;
  private final Map<VirtualFile, RootTemplate> myRootTemplates;
  @NotNull private final Project myProject;
  private SmartPointerManager mySmartPointerManager;

  // todo different scope
  public AngularUiRouterDiagramBuilder(@NotNull final Project project) {
    myProject = project;
    myStatesMap = new HashMap<>();
    myTemplatesMap = new HashMap<>();
    myRootTemplates = new HashMap<>();
    mySmartPointerManager = SmartPointerManager.getInstance(myProject);
  }

  public void build() {
    final Collection<String> stateIds = AngularIndexUtil.getAllKeys(AngularUiRouterStatesIndex.KEY, myProject);

    for (String id : stateIds) {
      if (id.startsWith(".")) continue;
      AngularIndexUtil.multiResolve(myProject, AngularUiRouterStatesIndex.KEY, id, new Processor<JSImplicitElement>() {
        @Override
        public boolean process(JSImplicitElement element) {
          final UiRouterState state = new UiRouterState(id, element.getContainingFile().getVirtualFile());

          JSCallExpression call = PsiTreeUtil.getParentOfType(element.getNavigationElement(), JSCallExpression.class);
          if (call == null) {
            final PsiElement elementAt = element.getContainingFile().findElementAt(element.getNavigationElement().getTextRange().getEndOffset() - 1);
            if (elementAt != null) {
              call = PsiTreeUtil.getParentOfType(elementAt, JSCallExpression.class);
            }
          }
          if (call != null) {
            final JSReferenceExpression methodExpression = ObjectUtils.tryCast(call.getMethodExpression(), JSReferenceExpression.class);
            if (methodExpression != null && methodExpression.getQualifier() != null && "state".equals(methodExpression.getReferenceName())) {
              final JSExpression[] arguments = call.getArguments();
              if (arguments.length > 0 && PsiTreeUtil.isAncestor(arguments[0], element.getNavigationElement(), false)) {
                state.setPointer(mySmartPointerManager.createSmartPsiElementPointer(arguments[0]));

                if (arguments.length > 1 && arguments[1] instanceof JSObjectLiteralExpression) {
                  final JSObjectLiteralExpression object = (JSObjectLiteralExpression)arguments[1];
                  fillStateParameters(state, object);
                }
              }
            }
          }
          myStatesMap.put(id, state);
          return true;
        }
      });
    }
    getRootPages();
  }

  private void getRootPages() {
    final List<VirtualFile> roots = new ArrayList<>();
    Collections.sort(roots, new Comparator<VirtualFile>() {
      @Override
      public int compare(VirtualFile o1, VirtualFile o2) {
        return Integer.compare(o2.getUrl().length(), o1.getUrl().length());
      }
    });

    final Map<PsiFile, AngularNamedItemDefinition> files = new HashMap<>();
    final FileBasedIndex instance = FileBasedIndex.getInstance();
    final Collection<String> keys = instance.getAllKeys(AngularAppIndex.ANGULAR_APP_INDEX, myProject);
    if (keys.isEmpty()) return;

    final PsiManager psiManager = PsiManager.getInstance(myProject);
    final GlobalSearchScope projectScope = GlobalSearchScope.projectScope(myProject);
    for (String key : keys) {
      instance.processValues(AngularAppIndex.ANGULAR_APP_INDEX, key, null, new FileBasedIndex.ValueProcessor<AngularNamedItemDefinition>() {
        @Override
        public boolean process(VirtualFile file, AngularNamedItemDefinition value) {
          final PsiFile psiFile = psiManager.findFile(file);
          if (psiFile != null) {
            files.put(psiFile, value);
          }
          return true;
        }
      }, projectScope);
    }
    for (Map.Entry<PsiFile, AngularNamedItemDefinition> entry : files.entrySet()) {
      final PsiFile file = entry.getKey();
      final String relativeUrl = findPossibleRelativeUrl(roots, file.getVirtualFile());
      // not clear how then it can be part of application
      if (relativeUrl == null) continue;
      final Template template = readTemplateFromFile(myProject, relativeUrl, file);
      //todo determine all files states from where relates to this template
      final Set<VirtualFile> moduleFiles = new HashSet<>();
      final String mainModule = entry.getValue().getName();
      if (!StringUtil.isEmptyOrSpaces(mainModule)) {
        final List<List<String>> values = instance
          .getValues(AngularModuleDependencyIndex.ANGULAR_MODULE_DEPENDENCY_INDEX, mainModule, GlobalSearchScope.projectScope(myProject));
        for (List<String> value : values) {
          for (String module : value) {
            final JSImplicitElement element = AngularIndexUtil.resolve(myProject, AngularModuleIndex.KEY, module);
            if (element != null && element.getNavigationElement() != null && element.getNavigationElement().getContainingFile() != null) {
              moduleFiles.add(element.getNavigationElement().getContainingFile().getVirtualFile());
            }
          }
        }
      } else {
        final VirtualFile[] includedFiles = FileIncludeManager.getManager(myProject).getIncludedFiles(file.getVirtualFile(), false, true);
        moduleFiles.addAll(ContainerUtil.filter(includedFiles, new Condition<VirtualFile>() {
          @Override
          public boolean value(VirtualFile file) {
            return file.getFileType() instanceof LanguageFileType && ((LanguageFileType)file.getFileType()).getLanguage().isKindOf(
              JavascriptLanguage.INSTANCE);
          }
        }));
      }
      // todo additionally pointer could point to ui-view place in file
      final RootTemplate rootTemplate = new RootTemplate(mySmartPointerManager.createSmartPsiElementPointer(file),
                                                         relativeUrl, template, moduleFiles);
      myRootTemplates.put(file.getVirtualFile(), rootTemplate);
    }
  }

  private String findPossibleRelativeUrl(@NotNull final List<VirtualFile> roots, @NotNull final VirtualFile file) {
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
    if (templateUrl != null) {
      state.setTemplateUrl(templateUrl);
      final JSProperty urlProperty = object.findProperty("templateUrl");
      parseTemplate(templateUrl, urlProperty);
    }
    final JSProperty views = object.findProperty("views");
    if (views != null) {
      final JSExpression value = views.getValue();
      if (value != null && value instanceof JSObjectLiteralExpression) {
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

  private void parseTemplate(@NotNull final String url, @Nullable JSProperty urlProperty) {
    final String normalizedUrl = AngularUiRouterGraphBuilder.normalizeTemplateUrl(url);

    PsiFile templateFile = null;
    Template template = null;
    if (urlProperty != null && urlProperty.getValue() != null) {
      int offset = urlProperty.getValue().getTextRange().getEndOffset() - 1;
      final PsiReference reference = urlProperty.getContainingFile().findReferenceAt(offset);
      if (reference != null) {
        final PsiElement templateFileElement = reference.resolve();
        if (templateFileElement != null && templateFileElement.isValid()) {
          templateFile = templateFileElement.getContainingFile();
          template = readTemplateFromFile(urlProperty.getProject(), url, templateFile);
        }
      }
    }
    // todo do state links later
    myTemplatesMap.put(normalizedUrl, template == null ? new Template(normalizedUrl, null) : template);
  }

  @NotNull
  private Template readTemplateFromFile(@NotNull Project project, @NotNull String url, PsiFile templateFile) {
    final Map<String, SmartPsiElementPointer<PsiElement>> placeholders = new HashMap<>();
    final Set<String> placeholdersSet = new HashSet<>();
    final FileBasedIndex instance = FileBasedIndex.getInstance();
    final GlobalSearchScope scope = GlobalSearchScope.fileScope(project, templateFile.getVirtualFile());
    instance.processAllKeys(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, new Processor<String>() {
      @Override
      public boolean process(String view) {
        placeholdersSet.add(view);
        return true;
      }
    }, scope, null);
    final PsiFile finalTemplateFile = templateFile;
    for (String key : placeholdersSet) {
      instance.processValues(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, key, null,
                             new FileBasedIndex.ValueProcessor<AngularNamedItemDefinition>() {
                               @Override
                               public boolean process(VirtualFile file, AngularNamedItemDefinition value) {
                                 final JSImplicitElementImpl.Builder builder = new JSImplicitElementImpl.Builder(
                                   JSQualifiedNameImpl.fromQualifiedName(key), null);
                                 final JSOffsetBasedImplicitElement implicitElement =
                                   new JSOffsetBasedImplicitElement(builder, (int)value.getStartOffset(), finalTemplateFile);
                                 placeholders.put(key, mySmartPointerManager.createSmartPsiElementPointer(implicitElement));
                                 return true;
                               }
                             }, scope);
    }
    final Template template = new Template(url, mySmartPointerManager.createSmartPsiElementPointer(templateFile));
    template.setViewPlaceholders(placeholders);
    return template;
  }

  private UiView processView(@NotNull final JSProperty property) {
    final String name = property.getName();
    final JSExpression value = property.getValue();
    final JSObjectLiteralExpression expression = ObjectUtils.tryCast(value, JSObjectLiteralExpression.class);
    String templateUrl = null;
    if (expression != null) {
      templateUrl = getPropertyValueIfExists(expression, "templateUrl");
      if (templateUrl != null) {
        final JSProperty urlProperty = expression.findProperty("templateUrl");
        parseTemplate(templateUrl, urlProperty);
      }
    }
    return new UiView(name, templateUrl,
                      property.getNameIdentifier() == null ? null : mySmartPointerManager.createSmartPsiElementPointer(property.getNameIdentifier()));
  }

  @Nullable
  private static String getPropertyValueIfExists(@NotNull final JSObjectLiteralExpression object, @NotNull final String name) {
    final JSProperty urlProperty = object.findProperty(name);
    if (urlProperty != null && urlProperty.getValue() instanceof JSLiteralExpression &&
        ((JSLiteralExpression)urlProperty.getValue()).isQuotedLiteral()) {
      return StringUtil.unquoteString(urlProperty.getValue().getText());
    }
    return null;
  }

  public Map<String, UiRouterState> getStatesMap() {
    return myStatesMap;
  }

  public Map<String, Template> getTemplatesMap() {
    return myTemplatesMap;
  }

  public Map<VirtualFile, RootTemplate> getRootTemplates() {
    return myRootTemplates;
  }
}
