package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.extensions.Extensions;
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
 * @author Andrey.Vokin
 * @author Sebastian Gr&ouml;bler
 * @since 10/12/12
 */
public class CucumberJavaAllFeaturesInFolderRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Override
  protected NullableComputable<String> getGlue(@NotNull final PsiElement element) {
    final Set<String> hookGlue = getHookGlue(element);
    final Set<String> glues = new HashSet<String>(hookGlue);
    if (element instanceof PsiDirectory) {
      final PsiDirectory dir = (PsiDirectory)element;
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
  protected String getConfigurationName(@NotNull final ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    return CucumberBundle.message("cucumber.run.all.features", ((PsiDirectory)element).getVirtualFile().getName());
  }

  @Nullable
  @Override
  protected VirtualFile getFileToRun(ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    if (element != null && element instanceof PsiDirectory) {
      return ((PsiDirectory) element).getVirtualFile();
    }
    return null;
  }
}
