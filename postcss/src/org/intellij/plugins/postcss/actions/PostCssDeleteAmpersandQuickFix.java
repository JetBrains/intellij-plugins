package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.intellij.plugins.postcss.PostCssBundle;
import org.jetbrains.annotations.NotNull;

public class PostCssDeleteAmpersandQuickFix extends LocalQuickFixBase {
  public PostCssDeleteAmpersandQuickFix() {
    super(PostCssBundle.message("annotator.delete.ampersand.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    descriptor.getStartElement().delete();
  }
}