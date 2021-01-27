/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.intellij.lang.javascript.psi.*;
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
public class JSImplicitlyInternalDeclarationInspection extends JSInspection {

  @Override
  @NotNull
  protected JSElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override
      public void visitJSClass(JSClass aClass) {
        process(aClass, holder);
      }

      @Override
      public void visitJSLocalVariable(JSLocalVariable var) {}

      @Override public void visitJSVariable(final JSVariable node) {
        process(node, holder);
      }

      @Override public void visitJSFunctionDeclaration(final JSFunction node) {
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
          @NotNull
          @Override
          public String getFamilyName() {
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
