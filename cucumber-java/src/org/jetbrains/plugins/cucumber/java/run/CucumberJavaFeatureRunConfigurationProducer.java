package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

/**
 * @author Andrey.Vokin
 * @since 8/6/12
 */
public class CucumberJavaFeatureRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Override
  protected String getGlue() {
    return CucumberJavaUtil.getGlue(mySourceElement);
  }

  @Override
  protected String getName() {
    return getFileToRun().getNameWithoutExtension();
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
    return locationElement != null && locationElement.getContainingFile() instanceof GherkinFile;
  }
}
