package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

/**
 * @author Andrey.Vokin
 * @since 8/6/12
 */
public class CucumberJavaFeatureRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Override
  protected String getGlue() {
    PsiFile file = mySourceElement.getContainingFile();
    if (file instanceof GherkinFile) {
      final CucumberJvmExtensionPoint[] extensions = Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME);

      final Ref<String> glue = new Ref<String>(null);
      file.accept(new GherkinRecursiveElementVisitor() {
        @Override
        public void visitStep(GherkinStep step) {
          for (CucumberJvmExtensionPoint e : extensions) {
            String curGlue = e.getGlue(step);
            if (curGlue != null) {
              glue.set(curGlue);
              break;
            }
          }
        }

        @Override
        public void visitElement(PsiElement element) {
          if (glue.get() != null) return;
          super.visitElement(element);
        }
      });

      if (glue.get() != null) {
        return " --glue " + glue.get();
      }
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
    return locationElement != null && locationElement.getContainingFile() instanceof GherkinFile;
  }
}
