// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.pom.PomDeclarationSearcher;
import com.intellij.pom.PomTarget;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.java.steps.Java8StepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaParameterPomTarget;

/// Provides "Find Usages" functionality for the definition of a Cucumber `ParameterType`.
///
/// ### Example
///
/// To see this in action, run "Find Usages" on the first argument of the `ParameterType` constructor (`"isoDate"`)":
///
/// ```
/// ParameterType<Date> parameterType = new ParameterType<>(
///         "isoDate",
///         "\\d{4}-\\d{2}-\\d{2}",
///         Date.class,
///         (String s) -> new SimpleDateFormat("yyyy-mm-dd").parse(s)
/// );
/// ```
///
/// Assuming the following step definitions were in the project:
///
/// ```
/// public class Steps
///   @And("today is {isoDate}")
///   public void today(Date arg1) {}
///
///   @And("yeserday was {isoDate}, and tomorrow will be {isoDate}")
///   public void yesterday_and_tomorrow(Date yesterday, Date tomorrow) {}
/// }
/// ```
///
/// we would find *three* usages of the `"isoDate"` parameter.
public final class CucumberJavaPomDeclarationSearcher extends PomDeclarationSearcher {
  @Override
  public void findDeclarationsAt(@NotNull PsiElement element, int offsetInElement, @NotNull Consumer<? super PomTarget> consumer) {
    handleJava8StepDeclaration(element, consumer);
    handleParameterTypeDeclaration(element, consumer);
  }

  private static void handleJava8StepDeclaration(@NotNull PsiElement element, @NotNull Consumer<? super PomTarget> consumer) {
    if (!(element instanceof PsiLiteralExpression literalExpression)) return;
    final PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(literalExpression, PsiMethodCallExpression.class);
    if (methodCallExpression == null) return;
    if (CucumberJavaUtil.isJava8StepDefinition(methodCallExpression)) {
      consumer.consume(Java8StepDefinition.create(methodCallExpression));
    }
  }

  private static void handleParameterTypeDeclaration(@NotNull PsiElement element, @NotNull Consumer<? super PomTarget> consumer) {
    if (!(element instanceof PsiLiteralExpression literalExpression)) return;
    final Object value = literalExpression.getValue();
    if (!(value instanceof String stringValue)) return;

    final PsiNewExpression newExpression = PsiTreeUtil.getParentOfType(element, PsiNewExpression.class);
    if (newExpression != null) {
      if (!isFirstConstructorArgument(element, newExpression)) {
        return;
      }
      final PsiJavaCodeReferenceElement classReference = newExpression.getClassReference();
      if (classReference != null) {
        final String fqn = classReference.getQualifiedName();
        if (CucumberJavaUtil.PARAMETER_TYPE_CLASS.equals(fqn)) {
          consumer.consume(new CucumberJavaParameterPomTarget(element, stringValue));
        }
      }
    }
  }

  private static boolean isFirstConstructorArgument(@NotNull PsiElement element, @NotNull PsiNewExpression newExp) {
    final PsiExpressionList argumentList = newExp.getArgumentList();
    if (argumentList == null) return false;
    if (argumentList.getExpressionCount() == 0) return false;
    if (argumentList.getExpressions()[0] != element) return false;
    return true;
  }
}
