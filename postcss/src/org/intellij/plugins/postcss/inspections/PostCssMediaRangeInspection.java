package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssElementVisitor;
import com.intellij.psi.css.CssMediaFeature;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PostCssMediaRangeInspection extends PostCssBaseInspection {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new CssElementVisitor() {
      @Override
      public void visitMediaFeature(@NotNull CssMediaFeature mediaFeature) {
        if (!PostCssPsiUtil.isInsidePostCss(mediaFeature) || PsiTreeUtil.hasErrorElements(mediaFeature)) return;
        Collection<? extends PsiElement> signs = PostCssPsiUtil.findAllOperatorSigns(mediaFeature);
        if (signs.size() != 2) return;
        boolean hasEqualSign = false;
        for (PsiElement sign : signs) {
          if (getSignDirection(sign) == 0) {
            holder.registerProblem(sign, PostCssBundle.message("annotator.equal.operator.is.not.allowed.in.this.context"));
            hasEqualSign = true;
          }
        }
        if (hasEqualSign) return;
        if (signs.stream().map(PostCssMediaRangeInspection::getSignDirection).mapToInt(i -> i).sum() == 0) {
          for (PsiElement sign : signs) {
            holder.registerProblem(sign, PostCssBundle.message("annotator.media.query.range.operators.should.have.equal.direction"));
          }
        }
      }
    };
  }

  private static int getSignDirection(PsiElement sign) {
    IElementType type = sign.getNode().getElementType();
    return type == CssElementTypes.CSS_EQ ? 0 : (type == CssElementTypes.CSS_GT || type == PostCssTokenTypes.GE) ? 1 : -1;
  }
}