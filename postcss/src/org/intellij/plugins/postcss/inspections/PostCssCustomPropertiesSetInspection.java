package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssCustomMixin;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.actions.PostCssPutCustomPropertiesSetInRootQuickFix;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.impl.PostCssElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomPropertiesSetInspection extends PostCssBaseInspection {
  private static final PostCssPutCustomPropertiesSetInRootQuickFix WRAP_WITH_ROOT = new PostCssPutCustomPropertiesSetInRootQuickFix();

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {

      @Override
      public void visitCustomMixin(@NotNull CssCustomMixin customMixin) {
        if (!PostCssPsiUtil.isInsidePostCss(customMixin)) return;
        CssRuleset parentRuleset = PsiTreeUtil.getParentOfType(customMixin, CssRuleset.class);
        if (parentRuleset == null ||
            parentRuleset.getSelectorList() == null ||
            !parentRuleset.getSelectorList().textMatches(":root")) {
          holder.registerProblem(customMixin, PostCssBundle.message("annotator.custom.property.set.are.only.allowed.on.root.rules"),
                                 WRAP_WITH_ROOT);
        }
      }
    };
  }
}
