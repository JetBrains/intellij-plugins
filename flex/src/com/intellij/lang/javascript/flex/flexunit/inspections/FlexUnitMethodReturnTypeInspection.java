package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSRecursiveElementVisitor;
import com.intellij.lang.javascript.psi.JSReturnStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.fixes.SetMethodReturnTypeFix;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitMethodReturnTypeInspection extends FlexUnitMethodInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return FlexBundle.message("flexunit.inspection.testmethodreturntype.displayname");
  }

  @NotNull
  public String getShortName() {
    return "FlexUnitMethodReturnTypeInspection";
  }

  protected void visitPotentialTestMethod(JSFunction method, ProblemsHolder holder, FlexUnitSupport support) {
    if (FlexUnitSupport.getCustomRunner((JSClass)method.getParent()) != null) return;
    if (method.getKind() != JSFunction.FunctionKind.SIMPLE) return;

    if (support.isFlexUnit1Subclass((JSClass)method.getParent()) || support.isFlunitSubclass((JSClass)method.getParent())) {
      return;
    }

    final String returnType = method.getReturnTypeString();
    if (StringUtil.isNotEmpty(returnType) && !"void".equals(returnType)) {

      final ASTNode nameIdentifier = method.findNameIdentifier();
      if (nameIdentifier != null) {

        LocalQuickFix[] fix = canFix(method) ? new LocalQuickFix[]{new SetMethodReturnTypeFix("void", method.getName())} : LocalQuickFix.EMPTY_ARRAY;
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodreturntype.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fix);
      }
    }
  }

  private boolean canFix(JSFunction method) {
    if (!SetMethodReturnTypeFix.isAvailable(method)) return false;

    final NonVoidReturnVisitor visitor = new NonVoidReturnVisitor(method);
    method.acceptChildren(visitor);
    return !visitor.hasNonVoidReturns();
  }

  private static class NonVoidReturnVisitor extends JSRecursiveElementVisitor {
    private final JSFunction myFunction;
    private boolean myHasNonVoidReturns = false;

    NonVoidReturnVisitor(JSFunction myFunction) {
      this.myFunction = myFunction;
    }

    @Override
    public void visitJSReturnStatement(JSReturnStatement statement) {
      super.visitJSReturnStatement(statement);
      if (statement.getExpression() != null) {
        final JSFunction containingFunction = PsiTreeUtil.getParentOfType(statement, JSFunction.class);
        if (myFunction.equals(containingFunction)) {
          myHasNonVoidReturns = true;
        }
      }
    }

    @Override
    public void visitJSFunctionDeclaration(JSFunction function) {
      // do nothing, so that it doesn't drill into nested functions
    }

    public boolean hasNonVoidReturns() {
      return myHasNonVoidReturns;
    }

  }
}
