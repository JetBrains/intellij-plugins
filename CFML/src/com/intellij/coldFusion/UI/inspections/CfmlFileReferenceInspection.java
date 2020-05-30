// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeInsight.daemon.quickFix.CreateFileFix;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class CfmlFileReferenceInspection extends LocalInspectionTool {
  @Override
  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull final PsiElement element) {
        CfmlTag tagParent = PsiTreeUtil.getParentOfType((element), CfmlTag.class);
        if ((element.getNode().getElementType() == CfmlTokenTypes.STRING_TEXT)) {
          if ((tagParent == null ||
               (!tagParent.getTagName().equalsIgnoreCase("cfinclude") &&
                !tagParent.getTagName().equalsIgnoreCase("cfmodule")))) {
            PsiElement superParent = element.getParent() != null ? element.getParent().getParent() : null;
            ASTNode superParentNode = superParent != null ? superParent.getNode() : null;
            if ((superParentNode == null || superParentNode.getElementType() != CfmlElementTypes.INCLUDEEXPRESSION)) {
              return;
            }
          }
          final PsiReference[] refs = element.getParent().getReferences();
          for (int i = 0, refsLength = refs.length; i < refsLength; i++) {
            PsiReference ref = refs[i];
            if (!(ref instanceof FileReference)) continue;
            if (ref.resolve() == null) {
              PsiDirectory dir;
              if (i > 0) {
                final PsiElement target = refs[i - 1].resolve();
                dir = target instanceof PsiDirectory ? (PsiDirectory)target : null;
              }
              else {
                dir = element.getContainingFile().getParent();
              }

              holder.registerProblem(
                ref.getElement(), ref.getRangeInElement(),
                isOnTheFly ? CfmlBundle.message("problem.message.inspection.cfml.file.reference.description2", ref.getCanonicalText())
                           : CfmlBundle.message("problem.message.inspection.cfml.file.reference.description"),
                isOnTheFly && dir != null
                ? new LocalQuickFix[]{new CreateFileFix(i < refs.length - 1, ref.getCanonicalText(), dir)}
                : LocalQuickFix.EMPTY_ARRAY
              );
              //holder.registerProblem(element, "Can't resolve '" + element.getText() + "'",
              //                       ProblemHighlightType.ERROR);
              break;
            }
          }
        }
      }
    };
  }
}

