package org.intellij.plugins.postcss.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.inspections.fixes.CssAddPrefixQuickFix;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.PostCssCustomMediaAtRule;
import org.intellij.plugins.postcss.psi.impl.PostCssCustomMediaAtRuleImpl;
import org.intellij.plugins.postcss.psi.impl.PostCssElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomMediaInspection extends PostCssBaseInspection {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new PostCssElementVisitor() {
      @Override
      public void visitPostCssCustomMediaAtRule(PostCssCustomMediaAtRuleImpl postCssCustomMediaAtRule) {
        PostCssCustomMedia customMedia = postCssCustomMediaAtRule.getCustomMedia();
        if (customMedia == null) return;
        String text = customMedia.getText();
        if (text.equals("--")) {
          holder.registerProblem(customMedia, PostCssBundle.message("annotator.custom.media.name.should.not.be.empty"));
        }
        else if (!StringUtil.startsWith(text, "--")) {
          String description = PostCssBundle.message("annotator.custom.media.name.should.start.with");
          TextRange textRange = TextRange.from(customMedia.getStartOffsetInParent(), customMedia.getTextLength());
          LocalQuickFix quickFix = new CssAddPrefixQuickFix("--", PostCssCustomMediaAtRule.class,
                                                            PostCssBundle.message("annotator.add.prefix.to.custom.media.quickfix.name"));
          ProblemDescriptor problemDescriptor = holder.getManager().createProblemDescriptor(
            postCssCustomMediaAtRule, textRange, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, quickFix);
          holder.registerProblem(problemDescriptor);
        }
      }
    };
  }
}