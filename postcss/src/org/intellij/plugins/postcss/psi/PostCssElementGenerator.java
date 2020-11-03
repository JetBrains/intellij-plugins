package org.intellij.plugins.postcss.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.CssSimpleSelector;
import org.intellij.plugins.postcss.PostCssFileType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public final class PostCssElementGenerator {
  @NotNull
  public static CssSimpleSelector createAmpersandSelector(@NotNull final Project project) {
    //noinspection ConstantConditions
    return findChildOfType(createFileFromText(project, "& {\n foo: bar;\n}"), CssSimpleSelector.class);
  }

  @NotNull
  public static PostCssNest createAtRuleNest(@NotNull final Project project, @NotNull final String text) {
    //noinspection ConstantConditions
    return findChildOfType(createFileFromText(project, "h1 {\n  @nest " + text + "\n}"), PostCssNest.class);
  }

  @NotNull
  private static PostCssFile createFileFromText(@NotNull final Project project, @NotNull final String text) {
    return (PostCssFile)PsiFileFactory.getInstance(project).createFileFromText("foo.pcss", PostCssFileType.POST_CSS, text);
  }
}