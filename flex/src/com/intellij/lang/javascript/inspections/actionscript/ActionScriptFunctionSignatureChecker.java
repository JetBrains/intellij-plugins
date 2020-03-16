package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.e4x.JSE4XFilterQueryArgumentList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSSuperExpression;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSGenericTypeEvaluationFunction;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.lang.javascript.psi.types.primitives.JSObjectType;
import com.intellij.lang.javascript.validation.JSFunctionSignatureChecker;
import com.intellij.lang.javascript.validation.JSTypeChecker;
import com.intellij.lang.javascript.validation.ValidateTypesUtil;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateConstructorFix;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.BOOLEAN_CLASS_NAME;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptFunctionSignatureChecker extends JSFunctionSignatureChecker {

  public ActionScriptFunctionSignatureChecker(JSTypeChecker typeChecker) {
    super(typeChecker);
  }

  @Override
  protected void registerProblem(JSCallExpression callExpression, String message, LocalQuickFix... fixes) {
    PsiElement place = ValidateTypesUtil.getPlaceForSignatureProblem(callExpression, callExpression.getArgumentList());
    myTypeChecker.registerProblem(place, message, ProblemHighlightType.GENERIC_ERROR, fixes);
  }

  @Override
  public void checkConstructorCall(@NotNull JSCallExpression node, @NotNull JSClass target) {
    if (node instanceof JSNewExpression || node.getMethodExpression() instanceof JSSuperExpression) {
      final JSArgumentList argumentList = node.getArgumentList();
      final JSExpression[] expressions = argumentList != null ? argumentList.getArguments() : JSExpression.EMPTY_ARRAY;
      if (expressions.length > 0) {
        final ActionScriptCreateConstructorFix fix = ActionScriptCreateConstructorFix.createIfApplicable(node);

        registerProblem(node, JavaScriptBundle.message("javascript.invalid.number.of.parameters", "0"),
                        fix != null ? new LocalQuickFix[]{fix} : LocalQuickFix.EMPTY_ARRAY);
      }
    }
    else {
      reportProblemIfNotExpectedCountOfParameters(node, 1, "one");
    }
  }

  @Override
  protected boolean isCallableType(boolean inNewExpression, @NotNull JSType type, PsiElement context) {
    return JSNamedType.isNamedTypeWithName(type, "Class") ||
           inNewExpression && type instanceof JSObjectType ||
           JSTypeUtils.hasFunctionType(type, false, null);
  }

  @Override
  protected boolean checkCallArgumentType(@NotNull JSParameterItem p,
                                          @Nullable JSExpression expression,
                                          @NotNull JSCallExpression node,
                                          @NotNull ProcessingContext processingContext,
                                          @NotNull JSGenericTypeEvaluationFunction function) {
    if (p instanceof JSParameter) {
      return myTypeChecker.checkExpressionIsAssignableToVariable((JSParameter)p, expression,
                                                                 "javascript.argument.type.mismatch");
    }
    return true;
  }

  @Override
  protected boolean obtainNextMatchedParams(int[] matchedParams, JSParameterItem[] parameters) {
    return false;
  }

  @Override
  protected boolean processMethodExpressionResolveResult(JSCallExpression callExpression, JSReferenceExpression methodExpression, PsiElement resolveResult, JSType type) {
    if (callExpression instanceof JSNewExpression) {
      if (JSResolveUtil.isConstructorFunction(resolveResult)) {
        resolveResult = resolveResult.getParent(); // TODO: there is no need once our stubs for interface will lose constructors for interfaces
      }

      if (resolveResult instanceof JSClass && ((JSClass)resolveResult).isInterface()) {
        final PsiElement referenceNameElement = methodExpression.getReferenceNameElement();

        myTypeChecker.registerProblem(referenceNameElement,
                                      JavaScriptBundle.message("javascript.interface.can.not.be.instantiated.message"),
                                      ProblemHighlightType.ERROR);
        return false;
      }
    }
    final JSArgumentList argumentList = callExpression.getArgumentList();
    if (argumentList instanceof JSE4XFilterQueryArgumentList &&
        (type != null && !(type instanceof JSAnyType) || resolveResult instanceof JSFunction && ((JSFunction)resolveResult).isGetProperty())) {
      checkE4XFilterQuery(callExpression, type.getTypeText(), argumentList);
      return false;
    }
    return true;
  }

  private void checkE4XFilterQuery(JSCallExpression node, String type, JSArgumentList argumentList) {
    if (!ActionScriptResolveUtil.isAssignableType("XML", type, argumentList) &&
        !ActionScriptResolveUtil.isAssignableType("XMLList", type, argumentList)
      ) {
      myTypeChecker.registerProblem(
        node.getMethodExpression(),
        JavaScriptBundle.message("javascript.invalid.e4x.filter.query.receiver", type),
        ProblemHighlightType.GENERIC_ERROR
      );
      return;
    }
    reportProblemIfNotExpectedCountOfParameters(node, 1, "one");
    JSExpression[] arguments = argumentList.getArguments();

    if (arguments.length >= 1) {
      myTypeChecker.checkExpressionIsAssignableToTypeAndReportError(
        arguments[0],
        BOOLEAN_CLASS_NAME,
        "javascript.argument.type.mismatch",
        null, false);
    }
  }
}
