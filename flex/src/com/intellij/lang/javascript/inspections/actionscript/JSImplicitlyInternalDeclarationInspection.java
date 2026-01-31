// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSLocalVariable;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.JSChangeVisibilityUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 */
public final class JSImplicitlyInternalDeclarationInspection extends JSInspection {

  @Override
  protected @NotNull JSElementVisitor createVisitor(final @NotNull ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override
      public void visitJSClass(@NotNull JSClass aClass) {
        process(aClass, holder);
      }

      @Override
      public void visitJSLocalVariable(@NotNull JSLocalVariable var) {}

      @Override public void visitJSVariable(final @NotNull JSVariable node) {
        process(node, holder);
      }

      @Override public void visitJSFunctionDeclaration(final @NotNull JSFunction node) {
        process(node, holder);
      }
    };
  }

  private static void process(final JSNamedElement node, final ProblemsHolder holder) {
    if (!DialectDetector.isActionScript(node)) return;
    JSFunction fun = PsiTreeUtil.getParentOfType(node, JSFunction.class);
    if (fun != null) return;
    ASTNode nameIdentifier = node.findNameIdentifier();
    if (nameIdentifier == null) return;
    JSClass clazz = JSResolveUtil.getClassOfContext(node);
    if (clazz == null) {
      PsiElement parent = JSResolveUtil.findParent(node);
      if (!(parent instanceof JSPackageStatement)) return;
    }

    JSAttributeList attributeList = ((JSAttributeListOwner)node).getAttributeList();
    JSAttributeList.AccessType accessType = attributeList != null ? attributeList.getAccessType():null;
    if (accessType == JSAttributeList.AccessType.PACKAGE_LOCAL &&
        attributeList.findAccessTypeElement() == null &&
        ActionScriptPsiImplUtil.getNamespaceElement(attributeList) == null &&
        !JSResolveUtil.isConstructorFunction(node)) {
      holder.registerProblem(
        nameIdentifier.getPsi(),
        FlexBundle.message("js.implicitly.internal.declaration.problem"),
        new LocalQuickFix() {
          @Override
          public @NotNull String getFamilyName() {
            return FlexBundle.message("js.implicitly.internal.declaration.problem.add.internal.fix");
          }

          @Override
          public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement anchor = descriptor.getPsiElement();
            JSChangeVisibilityUtil.setVisibility((JSAttributeListOwner)anchor.getParent(), JSAttributeList.AccessType.PACKAGE_LOCAL);
          }
        }
      );
    }
  }
}
