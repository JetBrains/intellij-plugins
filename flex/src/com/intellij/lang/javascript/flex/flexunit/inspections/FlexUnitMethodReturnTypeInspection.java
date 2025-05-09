// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.flexunit.inspections;

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
import com.intellij.modcommand.ModCommandAction;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public final class FlexUnitMethodReturnTypeInspection extends FlexUnitMethodInspectionBase {

  @Override
  public @NotNull String getShortName() {
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

        ModCommandAction fix = canFix(method)
                               ? new JSChangeTypeFix(method, "void", "javascript.fix.set.method.return.type") : null;
        holder.problem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testmethodreturntype.message"))
          .maybeFix(fix).register();
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
