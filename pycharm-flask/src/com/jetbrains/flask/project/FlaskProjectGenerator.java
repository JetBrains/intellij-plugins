package com.jetbrains.flask.project;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class FlaskProjectGenerator implements DirectoryProjectGenerator {
  @Nls
  @Override
  public String getName() {
    return "Flask project";
  }

  @Override
  public Object showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
    return null;
  }

  @Override
  public void generateProject(final Project project, final VirtualFile baseDir, Object settings, Module module) {
    final PsiDirectory projectDir = PsiManager.getInstance(project).findDirectory(baseDir);
    new WriteCommandAction.Simple(project) {
      @Override
      protected void run() throws Throwable {
        FileTemplate template = FileTemplateManager.getInstance().getInternalTemplate("Flask Main");
        PsiFile appFile;
        try {
          appFile = (PsiFile)FileTemplateUtil.createFromTemplate(template, baseDir.getName() + ".py", null, projectDir);
        }
        catch (Exception e) {
          Messages.showErrorDialog(project, "Error creating Flask application: " + e.getMessage(), "Create Flask Project");
          return;
        }
        projectDir.createSubdirectory("static");
        projectDir.createSubdirectory("templates");
        appFile.navigate(true);
      }
    }.execute();
  }

  @NotNull
  @Override
  public ValidationResult validate(@NotNull String baseDirPath) {
    return ValidationResult.OK;
  }
}
