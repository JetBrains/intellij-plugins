package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Andrey.Vokin
 * @since 8/6/12
 */
public class CucumberJavaFeatureRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Override
  protected NullableComputable<String> getGlue() {
    final PsiFile file = mySourceElement.getContainingFile();
    if (file instanceof GherkinFile) {
      return new NullableComputable<String>() {
        @Nullable
        @Override
        public String compute() {
          final Set<String> glues = new LinkedHashSet<String>();

          final CucumberJvmExtensionPoint[] extensions = Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME);
          for (CucumberJvmExtensionPoint extension : extensions) {
            glues.addAll(extension.getGlues((GherkinFile)file));
          }

          return StringUtil.join(glues, " ");
        }
      };
    }

    return null;
  }

  @Override
  protected String getName() {
    return "Feature: " + getFileToRun().getNameWithoutExtension();
  }

  @NotNull
  @Override
  protected VirtualFile getFileToRun() {
    PsiFile psiFile = mySourceElement.getContainingFile();
    assert psiFile != null;
    VirtualFile result = psiFile.getVirtualFile();
    assert result != null;
    return result;
  }

  protected boolean isApplicable(PsiElement locationElement, final Module module) {
    final GherkinStepsHolder scenario = PsiTreeUtil.getParentOfType(mySourceElement, GherkinScenario.class, GherkinScenarioOutline.class);
    return locationElement != null && locationElement.getContainingFile() instanceof GherkinFile && scenario == null;
  }
}
