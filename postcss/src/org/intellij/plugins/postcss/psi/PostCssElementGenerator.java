package org.intellij.plugins.postcss.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.util.ObjectUtils;
import org.intellij.plugins.postcss.PostCssFileType;
import org.jetbrains.annotations.NotNull;

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
  public static PostCssOneLineAtRule createOneLineAtRule(@NotNull final Project project,
                                                         @NotNull final String text,
                                                         @NotNull final Class<? extends PostCssOneLineAtRule> cl) {
    return ObjectUtils.notNull(findChildOfType(createFileFromText(project, "p {" + text + "}"), cl));
  }

  @NotNull
  private static PostCssFile createFileFromText(@NotNull final Project project, @NotNull final String text) {
    return (PostCssFile)PsiFileFactory.getInstance(project).createFileFromText("foo.pcss", PostCssFileType.POST_CSS, text);
  }
}