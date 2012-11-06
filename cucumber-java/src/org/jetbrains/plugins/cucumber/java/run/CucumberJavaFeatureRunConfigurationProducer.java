package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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

      final Set<String> glues = new LinkedHashSet<String>();
      file.accept(new GherkinRecursiveElementVisitor() {
        @Override
        public void visitStep(GherkinStep step) {
          for (CucumberJvmExtensionPoint e : extensions) {
            boolean covered = false;
            final String glue = e.getGlue(step);
            if (glue != null) {
              final Set<String> toRemove = new HashSet<String>();
              for (String existedGlue : glues) {
                if (glue.startsWith(existedGlue + ".")) {
                  covered = true;
                  break;
                } else if (existedGlue.startsWith(glue + ".")) {
                  toRemove.add(existedGlue);
                }
              }

              for (String removing : toRemove) {
                glues.remove(removing);
              }

              if (!covered) {
                glues.add(glue);
              }
              break;
            }
          }
        }
      });

      if (!glues.isEmpty()) {
        StringBuilder buffer = new StringBuilder();
        for (String glue : glues) {
          if (buffer.length() > 0) {
            buffer.append(" ");
          }
          buffer.append(glue);
        }
        return buffer.toString();
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
