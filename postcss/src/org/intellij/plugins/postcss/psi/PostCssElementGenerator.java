package org.intellij.plugins.postcss.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.impl.CssSimpleSelectorImpl;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.psi.impl.PostCssNestImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public class PostCssElementGenerator {
  @Nullable
  public static CssSimpleSelectorImpl createAmpersand(@NotNull final Project project) {
    return findChildOfType(createFileFromText(project, "& {\n foo: bar;\n}"), CssSimpleSelectorImpl.class);
  }

  @Nullable
  public static PostCssNestImpl createAtRuleNest(@NotNull final Project project, @NotNull final String text) {
    return findChildOfType(createFileFromText(project, "h1 {\n" + text + "\n}"), PostCssNestImpl.class);
  }

  @NotNull
  private static PostCssFile createFileFromText(@NotNull final Project project, @NotNull final String text) {
    return (PostCssFile)PsiFileFactory.getInstance(project).createFileFromText("foo.pcss", PostCssFileType.POST_CSS, text);
  }
}