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
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType;
import com.intellij.lang.javascript.validation.fixes.JSChangeTypeFix;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class FlexUnitMethodReturnTypeInspection extends FlexUnitMethodInspectionBase {

  @Override
  @NotNull
  public String getShortName() {
    return "FlexUnitMethodReturnTypeInspection";
  }

  @Override
  protected void visitPotentialTestMethod(JSFunction method, ProblemsHolder holder, FlexUnitSupport support) {
    if (FlexUnitSupport.getCustomRunner((JSClass)method.getParent()) != null) return;
    if (method.getKind() != JSFunction.FunctionKind.SIMPLE) return;

    if (support.isFlexUnit1Subclass((JSClass)method.getParent()) || support.isFlunitSubclass((JSClass)method.getParent())) {
      return;
    }

    final JSType returnType = method.getReturnType();
    if (returnType != null && !(returnType instanceof JSVoidType)) {

      final ASTNode nameIdentifier = method.findNameIdentifier();
      if (nameIdentifier != null) {

        LocalQuickFix[] fix = canFix(method)
                              ? new LocalQuickFix[]{
          new JSChangeTypeFix(method, "void", "javascript.fix.set.method.return.type")} : LocalQuickFix.EMPTY_ARRAY;
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodreturntype.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fix);
      }
    }
  }

  private static boolean canFix(JSFunction method) {
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
    public void visitJSReturnStatement(@NotNull JSReturnStatement statement) {
      super.visitJSReturnStatement(statement);
      if (statement.getExpression() != null) {
        final JSFunction containingFunction = PsiTreeUtil.getParentOfType(statement, JSFunction.class);
        if (myFunction.equals(containingFunction)) {
          myHasNonVoidReturns = true;
        }
      }
    }

    @Override
    public void visitJSFunctionDeclaration(@NotNull JSFunction function) {
      // do nothing, so that it doesn't drill into nested functions
    }

    public boolean hasNonVoidReturns() {
      return myHasNonVoidReturns;
    }

  }
}
