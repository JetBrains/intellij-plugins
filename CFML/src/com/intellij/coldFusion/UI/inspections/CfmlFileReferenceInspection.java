/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 2/13/12
 */
public class CfmlFileReferenceInspection extends LocalInspectionTool {
  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PsiElementVisitor() {
      public void visitElement(final PsiElement element) {
        PsiElement tagParent = PsiTreeUtil.getParentOfType((element), CfmlTag.class);
        if ((element.getNode().getElementType() == CfmlTokenTypes.STRING_TEXT)) {
          if ((tagParent == null ||
               (!((CfmlTag)tagParent).getTagName().equalsIgnoreCase("cfinclude") &&
                !((CfmlTag)tagParent).getTagName().equalsIgnoreCase("cfmodule")))) {
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
                isOnTheFly ? "Path '" + ref.getCanonicalText() + "' not found" : "Path not found",
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

  @Nls
  @NotNull
  public String getDisplayName() {
    return CfmlBundle.message("cfml.file.references.inspection");
  }
}

