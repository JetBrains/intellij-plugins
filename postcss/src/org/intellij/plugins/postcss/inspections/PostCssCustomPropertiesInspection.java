package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssCustomMixin;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.actions.PostCssWrapWithRootQuickFix;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.impl.PostCssElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomPropertiesInspection extends PostCssBaseInspection {
  private static final PostCssWrapWithRootQuickFix WRAP_WITH_ROOT = new PostCssWrapWithRootQuickFix();

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {

      @Override
      public void visitCustomMixin(@NotNull CssCustomMixin customMixin) {
        if (!PostCssPsiUtil.isInsidePostCss(customMixin)) return;
        if (!isInsideRootRuleset(customMixin)) {
          holder.registerProblem(customMixin, PostCssBundle.message("annotator.custom.property.set.are.only.allowed.on.root.rules"),
                                 WRAP_WITH_ROOT);
        }
      }

      @Override
      public void visitCssDeclaration(CssDeclaration declaration) {
        if (!PostCssPsiUtil.isInsidePostCss(declaration)) return;
        if (declaration.isCustomProperty() && !isInsideRootRuleset(declaration)) {
          holder.registerProblem(declaration, PostCssBundle.message("annotator.custom.properties.are.only.allowed.on.root.rules"));
        }
      }
    };
  }

  private static boolean isInsideRootRuleset(@NotNull final PsiElement element) {
    CssRuleset ruleset = PsiTreeUtil.getParentOfType(element, CssRuleset.class);
    return ruleset != null && ruleset.getSelectorList() != null && ruleset.getSelectorList().textMatches(":root");
  }
}
