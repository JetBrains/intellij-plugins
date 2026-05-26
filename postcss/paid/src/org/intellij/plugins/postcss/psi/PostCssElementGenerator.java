package org.intellij.plugins.postcss.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.CssSimpleSelector;
import org.intellij.plugins.postcss.PostCssFileType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public final class PostCssElementGenerator {
  public static @NotNull CssSimpleSelector createAmpersandSelector(final @NotNull Project project) {
    //noinspection ConstantConditions
    return findChildOfType(createFileFromText(project, "& {\n foo: bar;\n}"), CssSimpleSelector.class);
  }

  public static @NotNull PostCssNest createAtRuleNest(final @NotNull Project project, final @NotNull String text) {
    //noinspection ConstantConditions
    return findChildOfType(createFileFromText(project, "h1 {\n  @nest " + text + "\n}"), PostCssNest.class);
  }

  private static @NotNull PostCssFile createFileFromText(final @NotNull Project project, final @NotNull String text) {
    return (PostCssFile)PsiFileFactory.getInstance(project).createFileFromText("foo.pcss", PostCssFileType.POST_CSS, text);
  }
}