// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMirrorElement;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrUnaryExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;

/**
 * @author Max Medvedev
 */
@NotNullByDefault
public final class GrCucumberUtil {

  public static final String[] HOOKS = new String[]{"Before", "After"};

  public static boolean isStepDefinition(PsiElement element) {
    return element instanceof GrMethodCall call &&
           getCucumberStepRef(call) != null &&
           getStepDefinitionPatternText(call) != null;
  }

  public static @Nullable GrReferenceExpression getCucumberStepRef(GrMethodCall stepDefinition) {
    final GrExpression ref = stepDefinition.getInvokedExpression();
    if (!(ref instanceof GrReferenceExpression expression)) return null;

    final PsiMethod method = stepDefinition.resolveMethod();
    if (method == null) return null;

    final PsiClass containingClass = method.getContainingClass();
    if (containingClass == null) return null;

    final String qName = containingClass.getQualifiedName();
    if (qName == null) return null;

    final String packageName = StringUtil.getPackageName(qName);

    if (!GrCucumberCommonClassNames.isCucumberRuntimeGroovyPackage(packageName)) return null;

    return expression;
  }

  public static @Nullable String getStepDefinitionPatternText(GrMethodCall stepDefinition) {
    return ReadAction.compute(() -> {
      GrLiteral pattern = getStepDefinitionPattern(stepDefinition);
      if (pattern == null) return null;
      Object value = pattern.getValue();
      return value instanceof String s ? s : null;
    });
  }

  public static @Nullable GrLiteral getStepDefinitionPattern(GrMethodCall stepDefinition) {
    return ReadAction.compute(() -> {
      GrArgumentList argumentList = stepDefinition.getArgumentList();

      GroovyPsiElement[] arguments = argumentList.getAllArguments();
      if (arguments.length == 0 || arguments.length > 2) return null;

      GroovyPsiElement arg = arguments[0];
      if (!(arg instanceof GrUnaryExpression expression && expression.getOperationTokenType() == GroovyTokenTypes.mBNOT)) return null;

      GrExpression operand = expression.getOperand();
      if (!(operand instanceof GrLiteral literal)) return null;

      Object value = literal.getValue();
      return value instanceof String ? literal : null;
    });
  }

  public static boolean isHook(GrMethodCall methodCall) {
    PsiMethod method = methodCall.resolveMethod();
    if (method instanceof PsiMirrorElement element) {
      final PsiElement prototype = element.getPrototype();
      if (!(prototype instanceof PsiMethod psiMethod)) return false;

      method = psiMethod;
    }

    if (method == null) return false;

    if (!ArrayUtil.contains(method.getName(), HOOKS)) return false;

    PsiClass containingClass = method.getContainingClass();
    if (containingClass == null) return false;

    return GrCucumberCommonClassNames.isHookClassName(containingClass.getQualifiedName());
  }
}
