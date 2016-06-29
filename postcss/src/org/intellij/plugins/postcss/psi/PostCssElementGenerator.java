package org.intellij.plugins.postcss.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.psi.impl.PostCssDirectNestImpl;
import org.intellij.plugins.postcss.psi.impl.PostCssNestSymImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.findChildOfType;

public class PostCssElementGenerator {
  @Nullable
  public static PostCssDirectNestImpl createAmpersand(@NotNull final Project project) {
    return findChildOfType(createFileFromText(project, "& h1 {\n foo: bar;\n}"), PostCssDirectNestImpl.class);
  }

  @Nullable
  public static PostCssNestSymImpl createAtRuleNest(@NotNull final Project project) {
    return findChildOfType(createFileFromText(project, "@nest & h1 {\n foo: bar;\n}"), PostCssNestSymImpl.class);
  }

  @NotNull
  private static PostCssFile createFileFromText(@NotNull final Project project, @NotNull final String text) {
    return (PostCssFile)PsiFileFactory.getInstance(project).createFileFromText("foo.pcss", PostCssFileType.POST_CSS, text);
  }
}