package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssElementVisitor;
import com.intellij.psi.css.CssMediaFeature;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PostCssMediaRangeInspection extends PostCssBaseInspection {
  private static final Logger LOG = Logger.getInstance(PostCssMediaRangeInspection.class);

  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
    return new CssElementVisitor() {
      @Override
      public void visitMediaFeature(@NotNull CssMediaFeature mediaFeature) {
        if (!PostCssPsiUtil.isInsidePostCss(mediaFeature) || PsiTreeUtil.hasErrorElements(mediaFeature)) return;
        List<? extends PsiElement> comparisonOperators = PostCssPsiUtil.findAllComparisonOperators(mediaFeature);
        if (comparisonOperators.size() != 2) return;
        boolean hasEqualSign = false;
        for (PsiElement operator : comparisonOperators) {
          if (getComparisonOperatorDirection(operator) == 0) {
            holder.registerProblem(operator, PostCssBundle.message("annotator.equal.operator.is.not.allowed.in.this.context"));
            hasEqualSign = true;
          }
        }
        if (hasEqualSign) return;
        if (getComparisonOperatorDirection(comparisonOperators.get(0)) != getComparisonOperatorDirection(comparisonOperators.get(1))) {
          holder.registerProblem(comparisonOperators.get(1),
                                 PostCssBundle.message("annotator.media.query.range.operators.should.have.equal.direction"));
        }
      }
    };
  }

  private static int getComparisonOperatorDirection(final @NotNull PsiElement comparisonOperator) {
    final IElementType type = comparisonOperator.getNode().getElementType();

    if (type == CssElementTypes.CSS_GT || type == CssElementTypes.CSS_GE) return 1;
    if (type == CssElementTypes.CSS_LT || type == CssElementTypes.CSS_LE) return -1;
    if (type == CssElementTypes.CSS_EQ) return 0;

    LOG.error("Expected comparison operator, got " + type);
    return 0;
  }
}