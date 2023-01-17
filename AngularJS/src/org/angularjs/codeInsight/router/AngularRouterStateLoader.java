package org.angularjs.codeInsight.router;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularUiRouterGenericStatesIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AngularRouterStateLoader {
  private final @NotNull Project myProject;

  public AngularRouterStateLoader(final @NotNull Project project) {
    myProject = project;
  }

  public List<JSProperty> loadFreelyDefinedStates() {
    final List<JSProperty> states = new ArrayList<>();
    final Collection<String> allKeys = AngularIndexUtil.getAllKeys(AngularUiRouterGenericStatesIndex.KEY, myProject);
    for (String key : allKeys) {
      final List<JSImplicitElement> list = new ArrayList<>();
      AngularIndexUtil.multiResolve(myProject, AngularUiRouterGenericStatesIndex.KEY, key, list::add);
      for (JSImplicitElement element : list) {
        final JSCallExpression callExpression = AngularUiRouterDiagramBuilder.findWrappingCallExpression(element);
        if (callExpression != null) {
          findPossibleReferences(callExpression, object -> {
            final JSProperty nameProp = object.findProperty("name");
            if (nameProp != null && nameProp.getValue() instanceof JSLiteralExpression &&
                ((JSLiteralExpression)nameProp.getValue()).isQuotedLiteral()) {
              states.add(nameProp);
            }
            return true;
          });
        }
      }
    }
    return states;
  }

  private static void findPossibleReferences(final @NotNull JSCallExpression callExpression,
                                             final @NotNull Processor<? super JSObjectLiteralExpression> processor) {
    final JSExpression[] arguments = callExpression.getArguments();
    if (arguments.length == 1 && arguments[0] instanceof JSReferenceExpression) {
      processReference(processor, arguments[0]);
    }
  }

  private static void processReference(@NotNull Processor<? super JSObjectLiteralExpression> processor, JSExpression argument) {
    final JSReferenceExpression reference = (JSReferenceExpression)argument;
    final PsiElement resolved = reference.resolve();
    if (resolved instanceof JSVariable) {
      final JSExpression initializer = ((JSVariable)resolved).getInitializerOrStub();
      if (initializer instanceof JSObjectLiteralExpression) {
        if (processor.process((JSObjectLiteralExpression)initializer)) return;
      }
      final JSElement declarationScope = ObjectUtils.tryCast(((JSVariable)resolved).getDeclarationScope(), JSElement.class);
      if (declarationScope != null) {
        final Collection<JSAssignmentExpression> assignments =
          PsiTreeUtil.findChildrenOfType(declarationScope, JSAssignmentExpression.class);
        assignments.stream().map(as -> as.getDefinitionExpression())
          .filter(definition -> definition != null && StringUtil.equals(reference.getReferenceName(), definition.getName()) &&
                                definition.getInitializerOrStub() instanceof JSObjectLiteralExpression)
          .forEach(definition -> processor.process((JSObjectLiteralExpression)definition.getInitializerOrStub()));
      }
    }
  }
}
