package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.extensions.Extensions;
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

import java.util.Set;

/**
 * @author Andrey.Vokin
 * @since 8/6/12
 */
public class CucumberJavaFeatureRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Override
  protected NullableComputable<String> getStepsGlue(@NotNull final PsiElement element) {
    final PsiFile file = element.getContainingFile();
    if (file instanceof GherkinFile) {
      return () -> {
        final Set<String> glues = getHookGlue(element);
        final CucumberJvmExtensionPoint[] extensions = Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME);
        for (CucumberJvmExtensionPoint extension : extensions) {
          glues.addAll(extension.getGlues((GherkinFile)file, glues));
        }

        return StringUtil.join(glues, " ");
      };
    }

    return null;
  }

  @Override
  protected String getConfigurationName(@NotNull ConfigurationContext context) {
    final VirtualFile featureFile = getFileToRun(context);
    assert featureFile != null;
    return "Feature: " + featureFile.getNameWithoutExtension();
  }

  @Nullable
  @Override
  protected VirtualFile getFileToRun(ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    final GherkinStepsHolder scenario = PsiTreeUtil.getParentOfType(element, GherkinScenario.class, GherkinScenarioOutline.class);
    if (element != null && scenario == null && element.getContainingFile() instanceof GherkinFile) {
      return element.getContainingFile().getVirtualFile();
    }

    return null;
  }
}
