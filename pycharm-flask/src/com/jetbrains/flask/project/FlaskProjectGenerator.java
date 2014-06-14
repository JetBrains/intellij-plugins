/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.newProject.PyFrameworkProjectGenerator;
import com.jetbrains.python.newProject.PyNewProjectSettings;
import com.jetbrains.python.newProject.PythonProjectGenerator;
import com.jetbrains.python.packaging.PyExternalProcessException;
import com.jetbrains.python.packaging.PyPackageManager;
import com.jetbrains.python.templateLanguages.TemplateLanguagePanel;
import com.jetbrains.python.templateLanguages.TemplateSettingsHolder;
import com.jetbrains.python.templateLanguages.TemplatesService;
import icons.PycharmFlaskIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

/**
 * @author yole
 */
public class FlaskProjectGenerator extends PythonProjectGenerator implements PyFrameworkProjectGenerator<TemplateSettingsHolder> {
  private final boolean myForceInstallFlask;
  private TemplateLanguagePanel myTemplatesPanel;

  public FlaskProjectGenerator() {
    myForceInstallFlask = false;
  }

  public FlaskProjectGenerator(boolean forceInstallFlask) {
    myForceInstallFlask = forceInstallFlask;
  }

  @NotNull
  @Nls
  @Override
  public String getName() {
    return "Flask";
  }

  @Override
  public String getFrameworkTitle() {
    return "Flask";
  }

  @Override
  public boolean isFrameworkInstalled(Sdk sdk) {
    VirtualFile[] roots = sdk.getRootProvider().getFiles(OrderRootType.CLASSES);
    for (VirtualFile root : roots) {
      if (root.isValid() && root.findChild("flask") != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean acceptsRemoteSdk() {
    return true;
  }

  @Override
  public boolean supportsPython3() {
    return true;
  }

  @Override
  @Nullable
  public Icon getLogo() {
    return PycharmFlaskIcons.Flask_logo;
  }

  @Override
  public TemplateSettingsHolder showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
    return null;
  }

  @Override
  @Nullable
  public JComponent getSettingsPanel(File baseDir) throws ProcessCanceledException {
    myTemplatesPanel = new TemplateLanguagePanel();
    myTemplatesPanel.setTemplateLanguage(TemplatesService.JINJA2);
    return myTemplatesPanel;
  }

  @Override
  public PyNewProjectSettings getProjectSettings() {
    final TemplateSettingsHolder settingsHolder = new TemplateSettingsHolder();
    myTemplatesPanel.saveSettings(settingsHolder);
    return settingsHolder;
  }

  @Override
  public void generateProject(@NotNull final Project project, @NotNull final VirtualFile baseDir, final TemplateSettingsHolder settings, final Module module) {
    if (needInstallFlask(settings, module)) {
      ProgressManager.getInstance().run(new Task.Backgroundable(project, "Installing Flask", false) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          indicator.setText("Installing Flask...");
          Sdk targetSdk = settings != null ? settings.getSdk() : ModuleRootManager.getInstance(module).getSdk();
          final PyPackageManager packageManager = PyPackageManager.getInstance(targetSdk);
          try {
            packageManager.install("Flask");
            packageManager.refresh();
          }
          catch (final PyExternalProcessException e) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                packageManager.showInstallationError(project, "Install Flask failed", e.toString());
              }
            });
          }
        }
      });
    }
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      @Override
      public void run() {
        createFlaskMain(module, baseDir, settings);
      }
    });
  }

  private boolean needInstallFlask(PyNewProjectSettings settings, Module module) {
    if (settings != null) {
      return settings.installFramework();
    }
    if (myForceInstallFlask) {
      Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
      return sdk != null && !isFrameworkInstalled(sdk);
    }
    return false;
  }

  private static void createFlaskMain(final Module module, final VirtualFile baseDir, final TemplateSettingsHolder settings) {
    final Project project = module.getProject();
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
        FlaskProjectConfigurator.createFlaskRunConfiguration(module, appFile.getVirtualFile());
        TemplatesService templatesService = TemplatesService.getInstance(module);
        templatesService.generateTemplates(settings, baseDir);
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
