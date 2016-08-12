package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.CssCustomMixin;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.impl.CssTokenImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.actions.PostCssAddPrefixQuickFix;
import org.intellij.plugins.postcss.actions.PostCssPutCustomPropertiesSetInRootQuickFix;
import org.intellij.plugins.postcss.psi.PostCssApplyAtRule;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.impl.PostCssApplyAtRuleImpl;
import org.intellij.plugins.postcss.psi.impl.PostCssElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomPropertiesSetInspection extends PostCssBaseInspection {
  private static final PostCssPutCustomPropertiesSetInRootQuickFix WRAP_WITH_ROOT = new PostCssPutCustomPropertiesSetInRootQuickFix();

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {
      @Override
      public void visitPostCssApplyAtRule(PostCssApplyAtRuleImpl postCssApplyAtRule) {
        CssTokenImpl identifier = postCssApplyAtRule.getCustomPropertiesSetIdentifier();
        if (identifier == null) return;
        String text = identifier.getText();
        if (text.equals("--")) {
          holder.registerProblem(identifier, PostCssBundle.message("annotator.custom.properties.set.name.should.not.be.empty"));
        }
        else if (!StringUtil.startsWith(text, "--")) {
          PostCssAddPrefixQuickFix quickFix =
            new PostCssAddPrefixQuickFix("annotator.add.prefix.to.custom.properties.set.quickfix.name", "--", PostCssApplyAtRule.class);
          String description = PostCssBundle.message("annotator.custom.properties.set.name.should.start.with");
          TextRange textRange = TextRange.from(identifier.getStartOffsetInParent(), identifier.getTextLength());
          ProblemDescriptor problemDescriptor = holder.getManager().createProblemDescriptor(
            postCssApplyAtRule, textRange, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, quickFix);
          holder.registerProblem(problemDescriptor);
        }
      }

      @Override
      public void visitCustomMixin(@NotNull CssCustomMixin customMixin) {
        if (!PostCssPsiUtil.isInsidePostCss(customMixin)) return;
        CssRuleset parentRuleset = PsiTreeUtil.getParentOfType(customMixin, CssRuleset.class);
        if (parentRuleset == null ||
            parentRuleset.getSelectorList() == null ||
            !parentRuleset.getSelectorList().textMatches(":root")) {
          holder.registerProblem(customMixin, PostCssBundle.message("annotator.custom.properties.set.are.only.allowed.on.root.rules"),
                                 WRAP_WITH_ROOT);
        }
      }
    };
  }
}
