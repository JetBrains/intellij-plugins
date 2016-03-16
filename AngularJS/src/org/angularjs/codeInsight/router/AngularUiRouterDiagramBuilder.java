package org.angularjs.codeInsight.router;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularUiRouterStatesIndex;
import org.angularjs.index.AngularUiRouterViewsIndex;
import org.angularjs.index.AngularViewDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Irina.Chernushina on 3/8/2016.
 */
public class AngularUiRouterDiagramBuilder {
  private final Map<String, UiRouterState> myStatesMap;
  private final Map<String, Template> myTemplatesMap;
  @NotNull private final Project myProject;
  private SmartPointerManager mySmartPointerManager;

  // todo different scope
  public AngularUiRouterDiagramBuilder(@NotNull final Project project) {
    myProject = project;
    myStatesMap = new HashMap<>();
    myTemplatesMap = new HashMap<>();
    mySmartPointerManager = SmartPointerManager.getInstance(myProject);
  }

  public void build() {
    final Collection<String> stateIds = AngularIndexUtil.getAllKeys(AngularUiRouterStatesIndex.KEY, myProject);

    for (String id : stateIds) {
      if (id.startsWith(".")) continue;
      AngularIndexUtil.multiResolve(myProject, AngularUiRouterStatesIndex.KEY, id, new Processor<JSImplicitElement>() {
        @Override
        public boolean process(JSImplicitElement element) {
          final UiRouterState state = new UiRouterState(id);
          final JSCallExpression call = PsiTreeUtil.getParentOfType(element.getNavigationElement(), JSCallExpression.class);
          if (call != null) {
            final JSReferenceExpression methodExpression = ObjectUtils.tryCast(call.getMethodExpression(), JSReferenceExpression.class);
            if (methodExpression != null && methodExpression.getQualifier() != null && "state".equals(methodExpression.getReferenceName())) {
              final JSExpression[] arguments = call.getArguments();
              if (arguments.length > 1 && arguments[1] instanceof JSObjectLiteralExpression &&
                  PsiTreeUtil.isAncestor(arguments[0], element.getNavigationElement(), false)) {
                state.setPointer(mySmartPointerManager.createSmartPsiElementPointer(arguments[0]));

                final JSObjectLiteralExpression object = (JSObjectLiteralExpression)arguments[1];
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
                  parseTemplate(call.getContainingFile().getVirtualFile(), templateUrl, urlProperty);
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
            }
          }
          // todo check duplicates???
          myStatesMap.put(id, state);
          return true;
        }
      });
    }
  }

  private void parseTemplate(@NotNull final VirtualFile fromFile, @NotNull final String url, @Nullable JSProperty urlProperty) {
    PsiFile templateFile = null;
    final Map<String, SmartPsiElementPointer<PsiElement>> placeholders = new HashMap<>();
    if (urlProperty != null && urlProperty.getValue() != null) {
      int offset = urlProperty.getValue().getTextRange().getEndOffset() - 1;
      final PsiReference reference = urlProperty.getContainingFile().findReferenceAt(offset);
      if (reference != null) {
        final PsiElement templateFileElement = reference.resolve();
        if (templateFileElement != null && templateFileElement.isValid()) {
          templateFile = templateFileElement.getContainingFile();
          final Set<String> placeholdersSet = new HashSet<>();
          final FileBasedIndex instance = FileBasedIndex.getInstance();
          final GlobalSearchScope scope = GlobalSearchScope.fileScope(urlProperty.getProject(), templateFile.getVirtualFile());
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
                                   new FileBasedIndex.ValueProcessor<AngularViewDefinition>() {
                                     @Override
                                     public boolean process(VirtualFile file, AngularViewDefinition value) {
                                       final JSImplicitElementImpl.Builder builder = new JSImplicitElementImpl.Builder(JSQualifiedNameImpl.fromQualifiedName(key), null);
                                       final JSOffsetBasedImplicitElement implicitElement =
                                         new JSOffsetBasedImplicitElement(builder, (int)value.getStartOffset(), finalTemplateFile);
                                       placeholders.put(key, mySmartPointerManager.createSmartPsiElementPointer(implicitElement));
                                       return true;
                                     }
                                   }, scope);
          }
        }
        }
    }
    final Template template = new Template(url, templateFile == null ? null : mySmartPointerManager.createSmartPsiElementPointer(templateFile));
    // todo do state links later
    template.setViewPlaceholders(placeholders);
    myTemplatesMap.put(AngularUiRouterGraphBuilder.normalizeTemplateUrl(url), template);
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
        parseTemplate(property.getContainingFile().getVirtualFile(), templateUrl, urlProperty);
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
}
