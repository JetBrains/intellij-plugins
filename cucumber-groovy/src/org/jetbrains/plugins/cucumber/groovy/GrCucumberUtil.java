package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ArrayUtil;
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
public class GrCucumberUtil {

  public static final String[] HOOKS = new String[]{"Before", "After"};

  public static boolean isStepDefinition(PsiElement element) {
    return element instanceof GrMethodCall &&
           getCucumberStepRef((GrMethodCall)element) != null &&
           getStepDefinitionPatternText((GrMethodCall)element) != null;
  }

  @Nullable
  public static GrReferenceExpression getCucumberStepRef(final GrMethodCall stepDefinition) {
    return ApplicationManager.getApplication().runReadAction(new NullableComputable<GrReferenceExpression>() {
      @Override
      public GrReferenceExpression compute() {
        final GrExpression ref = stepDefinition.getInvokedExpression();
        if (!(ref instanceof GrReferenceExpression)) return null;

        final PsiMethod method = stepDefinition.resolveMethod();
        if (method == null) return null;

        final PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) return null;

        final String qName = containingClass.getQualifiedName();
        if (qName == null) return null;

        final String packageName = StringUtil.getPackageName(qName);

        if (!GrCucumberCommonClassNames.CUCUMBER_RUNTIME_GROOVY.equals(packageName)) return null;

        return (GrReferenceExpression)ref;
      }
    });
  }

  @Nullable
  public static String getStepDefinitionPatternText(final GrMethodCall stepDefinition) {
    return ApplicationManager.getApplication().runReadAction(new NullableComputable<String>() {
      @Nullable
      @Override
      public String compute() {
        GrLiteral pattern = getStepDefinitionPattern(stepDefinition);
        if (pattern == null) return null;
        Object value = pattern.getValue();
        return value instanceof String ? (String)value : null;
      }
    });
  }

  @Nullable
  public static GrLiteral getStepDefinitionPattern(final GrMethodCall stepDefinition) {
    return ApplicationManager.getApplication().runReadAction(new NullableComputable<GrLiteral>() {
      @Nullable
      @Override
      public GrLiteral compute() {
        GrArgumentList argumentList = stepDefinition.getArgumentList();
        if (argumentList == null) return null;

        GroovyPsiElement[] arguments = argumentList.getAllArguments();
        if (arguments.length != 1) return null;

        GroovyPsiElement arg = arguments[0];
        if (!(arg instanceof GrUnaryExpression && ((GrUnaryExpression)arg).getOperationTokenType() == GroovyTokenTypes.mBNOT)) return null;

        GrExpression operand = ((GrUnaryExpression)arg).getOperand();
        if (!(operand instanceof GrLiteral)) return null;

        Object value = ((GrLiteral)operand).getValue();
        return value instanceof String ? ((GrLiteral)operand) : null;
      }
    });
  }

  public static boolean isHook(GrMethodCall methodCall) {
    PsiMethod method = methodCall.resolveMethod();
    if (method == null) return false;

    if (!ArrayUtil.contains(method.getName(), HOOKS)) return false;

    PsiClass containingClass = method.getContainingClass();
    if (containingClass == null) return false;

    return GrCucumberCommonClassNames.CUCUMBER_RUNTIME_GROOVY_HOOKS.equals(containingClass.getQualifiedName());
  }
}
