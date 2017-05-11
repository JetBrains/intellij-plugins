package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

public class CucumberElementFactory {

  public static PsiElement createTempPsiFile(@NotNull final Project project, @NotNull final String text) {

    return PsiFileFactory.getInstance(project).createFileFromText("temp." + GherkinFileType.INSTANCE.getDefaultExtension(),
                                                                  GherkinFileType.INSTANCE,
                                                                  text, LocalTimeCounter.currentTime(), true);
  }
}
