package org.intellij.plugins.postcss.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.CssSimpleSelector;
import org.intellij.plugins.postcss.PostCssFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public class PostCssElementGenerator {
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
  public static PostCssCustomSelector createCustomSelector(@NotNull final Project project, String customSelectorName) {
    //noinspection ConstantConditions
    return findChildOfType(createFileFromText(project, "@custom-selector " + customSelectorName + " a;"), PostCssCustomSelector.class);
  }

  @Nullable
  public static PostCssCustomMedia createCustomMedia(@NotNull final Project project, @NotNull final String customMediaName) {
    return findChildOfType(createFileFromText(project, "@custom-media " + customMediaName + " all;"), PostCssCustomMedia.class);
  }

  @NotNull
  private static PostCssFile createFileFromText(@NotNull final Project project, @NotNull final String text) {
    return (PostCssFile)PsiFileFactory.getInstance(project).createFileFromText("foo.pcss", PostCssFileType.POST_CSS, text);
  }
}