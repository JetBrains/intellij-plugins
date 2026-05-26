package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssElementVisitor;
import com.intellij.psi.css.CssValueImport;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.resolve.CssValueReference;
import com.intellij.psi.impl.source.tree.LeafElement;
import org.jetbrains.annotations.NotNull;

public class PostCssUnresolvedModuleValueReferenceInspection extends PostCssBaseInspection {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new CssElementVisitor() {
      @Override
      public void visitValueImport(CssValueImport valueImport) {
        for (PsiElement child : valueImport.getChildren()) {
          if (child instanceof LeafElement && child.getNode().getElementType() == CssElementTypes.CSS_IDENT) {
            for (PsiReference reference : child.getReferences()) {
              if (reference instanceof CssValueReference) {
                if (reference.resolve() == null) {
                  holder.registerProblem(reference);
                }
              }
            }
          }
        }
      }
    };
  }
}
