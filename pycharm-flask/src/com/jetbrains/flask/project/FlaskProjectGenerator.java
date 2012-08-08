package com.jetbrains.flask.project;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.newProject.PyFrameworkProjectGenerator;
import com.jetbrains.python.newProject.PyNewProjectSettings;
import com.jetbrains.python.packaging.PyExternalProcessException;
import com.jetbrains.python.packaging.PyPackageManager;
import com.jetbrains.python.psi.PyPsiFacade;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author yole
 */
public class FlaskProjectGenerator implements PyFrameworkProjectGenerator<PyNewProjectSettings> {
  @Nls
  @Override
  public String getName() {
    return "Flask project";
  }

  @Override
  public String getFrameworkTitle() {
    return "Flask";
  }

  @Override
  public boolean isFrameworkInstalled(Project project, Sdk sdk) {
    return PyPsiFacade.getInstance(project).qualifiedNameResolver("flask").fromSdk(project, sdk).firstResult() != null;
  }

  @Override
  public PyNewProjectSettings showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
    return new PyNewProjectSettings();
  }

  @Override
  public void generateProject(final Project project, final VirtualFile baseDir, final PyNewProjectSettings settings, Module module) {
    if (settings.installFramework()) {
      ProgressManager.getInstance().run(new Task.Backgroundable(project, "Installing Flask", false) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          indicator.setText("Installing Flask...");
          final PyPackageManager packageManager = PyPackageManager.getInstance(settings.getSdk());
          try {
            packageManager.install("Flask");
          }
          catch (final PyExternalProcessException e) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                packageManager.showInstallationError(project, "Install Flask failed", e.toString());
              }
            });
          }
        }});
    }
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
