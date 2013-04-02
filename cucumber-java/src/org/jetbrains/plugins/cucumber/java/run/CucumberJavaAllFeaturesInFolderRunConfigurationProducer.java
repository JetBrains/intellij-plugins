package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.Location;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import java.util.HashSet;
import java.util.Set;

/**
 * User: avokin
 * Date: 10/12/12
 */
public class CucumberJavaAllFeaturesInFolderRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Override
  protected NullableComputable<String> getGlue() {
    final Set<String> glues = new HashSet<String>();
    if (mySourceElement instanceof PsiDirectory) {
      final PsiDirectory dir = (PsiDirectory)mySourceElement;
      final CucumberJvmExtensionPoint[] extensions = Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME);

      return new NullableComputable<String>() {
        @Nullable
        @Override
        public String compute() {
          dir.accept(new PsiElementVisitor() {
            @Override
            public void visitFile(final PsiFile file) {
              if (file instanceof GherkinFile) {
                for (CucumberJvmExtensionPoint extension : extensions) {
                  extension.getGlues((GherkinFile)file, glues);
                }
              }
            }

            @Override
            public void visitDirectory(PsiDirectory dir) {
              for (PsiDirectory subDir : dir.getSubdirectories()) {
                subDir.accept(this);
              }

              for (PsiFile file : dir.getFiles()) {
                file.accept(this);
              }
            }
          });

          return StringUtil.join(glues, " ");
        }
      };
    }
    return null;
  }

  @Override
  protected String getName() {
    return CucumberBundle.message("cucumber.run.all.features", ((PsiDirectory) mySourceElement).getVirtualFile().getName());
  }

  @NotNull
  @Override
  protected VirtualFile getFileToRun() {
    return ((PsiDirectory) mySourceElement).getVirtualFile();
  }

  protected boolean isApplicable(PsiElement locationElement, final Module module) {
    return locationElement != null && locationElement instanceof PsiDirectory;
  }

  @Override
  protected RunnerAndConfigurationSettings createConfiguration(Location location, ConfigurationContext context, @NotNull final String mainClassName) {
    RunnerAndConfigurationSettings result = super.createConfiguration(location, context, mainClassName);
    CucumberJavaRunConfiguration runConfiguration = (CucumberJavaRunConfiguration)result.getConfiguration();
    runConfiguration.getEnvs().put("current_dir", getFileToRun().getPath());
    return result;
  }
}
