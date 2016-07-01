package org.angularjs.codeInsight.router;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.Processor;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularUiRouterGenericStatesIndex;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Irina.Chernushina on 6/29/2016.
 */
public class AngularRouterStateLoader {
  public static final String STATE_PROVIDER = "$stateProvider";
  @NotNull private final Project myProject;
  private String myStateName;
  private final static Set<String> STATE_FIELDS = new HashSet<>();
  private final static Set<String> ARRAY_ITERATE_METHODS = new HashSet<>();
  static {
    STATE_FIELDS.addAll(Arrays.asList("template", "templateUrl", "templateProvider", "views", "url", "controller", "controllerAs"));
    ARRAY_ITERATE_METHODS.addAll(Arrays.asList("forEach", "map", "reduce", "reduceRight", "every", "filter", "some"));
  }

  public AngularRouterStateLoader(final @NotNull Project project) {
    myProject = project;
  }

  public AngularRouterStateLoader setStateName(String stateName) {
    myStateName = stateName;
    return this;
  }

  public List<JSObjectLiteralExpression> loadFreelyDefinedStates() {
    final List<JSObjectLiteralExpression> states = new ArrayList<>();
    final Collection<String> allKeys = AngularIndexUtil.getAllKeys(AngularUiRouterGenericStatesIndex.KEY, myProject);
    for (String key : allKeys) {
      final List<JSImplicitElement> list = new ArrayList<>();
      AngularIndexUtil.multiResolve(myProject, AngularUiRouterGenericStatesIndex.KEY, key, list::add);
      for (JSImplicitElement element : list) {
        final JSCallExpression callExpression = AngularUiRouterDiagramBuilder.findWrappingCallExpression(element);
        if (callExpression != null) {
          findPossibleReferences(callExpression, object -> {
            final JSProperty name = object.findProperty("name");
            if (name != null && name.getValue() instanceof JSLiteralExpression && ((JSLiteralExpression)name.getValue()).isQuotedLiteral()) {
              for (String field : STATE_FIELDS) {
                if (object.findProperty(field) != null && (myStateName == null || myStateName.endsWith(StringUtil.unquoteString(name.getValue().getText())))) {
                  states.add(object);
                  return true;
                }
              }
            }
            return false;
          });
          if (myStateName != null && !states.isEmpty()) return states;
        }
      }
    }
    return states;
  }

  private static void findPossibleReferences(@NotNull final JSCallExpression callExpression,
                                             @NotNull final Processor<JSObjectLiteralExpression> processor) {
    final JSExpression[] arguments = callExpression.getArguments();
    if (arguments.length == 1 && arguments[0] instanceof JSReferenceExpression) {
      processReference(processor, arguments[0], 0);
    }
  }

  private static void processReference(@NotNull Processor<JSObjectLiteralExpression> processor, JSExpression argument, int deepness) {
    if (deepness > 3) return;
    final JSReferenceExpression reference = (JSReferenceExpression)argument;
    final PsiElement resolved = reference.resolve();
    if (resolved != null) {
      if (resolved instanceof JSVariable && ((JSVariable)resolved).getInitializer() != null) {
        final JSExpression initializer = ((JSVariable)resolved).getInitializer();
        if (initializer instanceof JSObjectLiteralExpression) {
          if (processor.process((JSObjectLiteralExpression)initializer)) return;
        }
      }
      //find possible assignments
      final Collection<PsiReference> references = JSDefaultRenameProcessor.findReferencesForScope(resolved, false, resolved.getUseScope());
      for (PsiReference psiReference : references) {
        if (!(psiReference instanceof JSElement)) continue;
        final JSElement element = (JSElement)psiReference;

        if (element.getParent() instanceof JSDefinitionExpression &&
            ((JSDefinitionExpression)element.getParent()).getExpression() == element &&
            element.getParent().getParent() instanceof JSAssignmentExpression) {
          final JSAssignmentExpression assignment = (JSAssignmentExpression)element.getParent().getParent();
          if (assignment.getDefinitionExpression() != null) {
            final JSExpression initializer = assignment.getDefinitionExpression().getInitializer();
            if (initializer instanceof JSObjectLiteralExpression) {
              processor.process((JSObjectLiteralExpression)initializer);
            }
          }
        }
      }

      //find possible wrapping array iteration where this was a callback
      if (resolved.getParent() instanceof JSParameterList && resolved.getParent().getParent() instanceof JSFunction) {
        final JSFunction function = (JSFunction)resolved.getParent().getParent();
        if (function.getParent() instanceof JSArgumentList && function.getParent().getParent() instanceof JSCallExpression) {
          final JSCallExpression call = (JSCallExpression)function.getParent().getParent();
          final JSExpression methodExpression = call.getMethodExpression();
          if (methodExpression instanceof JSReferenceExpression) {
            if (ARRAY_ITERATE_METHODS.contains(((JSReferenceExpression)methodExpression).getReferenceName())) {
              final JSExpression qualifier = ((JSReferenceExpression)methodExpression).getQualifier();
              if (qualifier instanceof JSArrayLiteralExpression) {
                processArrayWithIterateCallback(processor, deepness, (JSArrayLiteralExpression)qualifier);
              } else if (qualifier instanceof JSReferenceExpression) {
                final PsiElement resolve = ((JSReferenceExpression)qualifier).resolve();
                if (resolve != null && resolve.isValid() && resolve instanceof JSVariable) {
                  if(((JSVariable)resolve).getInitializer() instanceof JSArrayLiteralExpression) {
                    processArrayWithIterateCallback(processor, deepness, (JSArrayLiteralExpression)((JSVariable)resolve).getInitializer());
                  }
                } else {
                  processReference(processor, qualifier, deepness + 1);
                }
              }
            }
          }
        }
      }
    }
  }

  private static void processArrayWithIterateCallback(@NotNull Processor<JSObjectLiteralExpression> processor,
                                                      int deepness,
                                                      JSArrayLiteralExpression qualifier) {
    final JSExpression[] expressions = qualifier.getExpressions();
    for (JSExpression expression : expressions) {
      if (expression instanceof JSObjectLiteralExpression) {
        processor.process((JSObjectLiteralExpression)expression);
      } else if (expression instanceof JSReferenceExpression) {
        processReference(processor, expression, deepness + 1);
      }
    }
  }
}
