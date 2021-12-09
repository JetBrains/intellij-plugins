/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.wizard;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ModalityUiUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.thoughtworks.gauge.GaugeBootstrapService;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.core.GaugeVersion;
import com.thoughtworks.gauge.core.GaugeVersionInfo;
import com.thoughtworks.gauge.exception.GaugeNotFoundException;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import icons.GaugeIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.thoughtworks.gauge.GaugeConstants.MIN_GAUGE_VERSION;
import static com.thoughtworks.gauge.util.GaugeUtil.getGaugeSettings;

final class GaugeModuleBuilder extends ModuleBuilder {
  private static final String JAVA_LANGUAGE = "java";

  private static final ExtensionPointName<GaugeModuleImporter> EP_NAME = ExtensionPointName.create("com.thoughtworks.gauge.moduleImporter");

  private GaugeTemplate mySelectedTemplate = null;
  private boolean isNewProject = false;

  @Override
  public @NonNls String getBuilderId() {
    return "gauge";
  }

  @Override
  public String getName() {
    return GaugeBundle.GAUGE;
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
    return GaugeBundle.GAUGE;
  }

  @Override
  public @NlsContexts.DetailedDescription String getDescription() {
    return GaugeBundle.message("module.supported.for.writing.gauge.tests");
  }

  @Override
  public Icon getNodeIcon() {
    return GaugeIcons.Gauge;
  }

  @Override
  public int getWeight() {
    return JavaModuleBuilder.BUILD_SYSTEM_WEIGHT;
  }

  @Override
  public String getParentGroup() {
    return JavaModuleType.BUILD_TOOLS_GROUP;
  }

  @Override
  public ModuleType<?> getModuleType() {
    return StdModuleTypes.JAVA;
  }

  @Override
  public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    checkGaugeIsInstalled(); // todo make it possible to setup Gauge from wizard

    Sdk sdk;
    if (getModuleJdk() != null) {
      sdk = getModuleJdk();
    } else {
      sdk = ProjectRootManager.getInstance(modifiableRootModel.getProject()).getProjectSdk();
    }
    if (sdk != null) {
      modifiableRootModel.setSdk(sdk);
    }
    doAddContentEntry(modifiableRootModel);

    gaugeInit(modifiableRootModel);
  }

  @Override
  protected void setupModule(Module module) throws ConfigurationException {
    super.setupModule(module);

    if (isNewProject) {
      Project project = module.getProject();
      project.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, Boolean.TRUE);
      project.putUserData(ExternalSystemDataKeys.NEWLY_IMPORTED_PROJECT, Boolean.TRUE);
    }

    module.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, Boolean.TRUE);

    StartupManager.getInstance(module.getProject()).runAfterOpened(() -> {
      ModalityUiUtil.invokeLaterIfNeeded(ModalityState.NON_MODAL, module.getDisposed(), () -> {
        if (module.isDisposed()) return;

        new ReformatCodeProcessor(module.getProject(), module, false).run();

        importProject(module, mySelectedTemplate);
      });
    });
  }

  private void importProject(Module module, GaugeTemplate selectedTemplate) {
    if (mySelectedTemplate != null) {
      for (GaugeModuleImporter importer : EP_NAME.getExtensions()) {
        if (Objects.equals(importer.getId(), mySelectedTemplate.importerId)) {
          importer.importModule(module, selectedTemplate).onSuccess(noResult -> {
            // schedule bootstrap
            GaugeBootstrapService.getInstance(module.getProject()).moduleAdded(module);
          });
          return;
        }
      }
    }
  }

  private static void checkGaugeIsInstalled() throws ConfigurationException {
    try {
      getGaugeSettings();
      GaugeVersionInfo version = GaugeVersion.getVersion(true);
      if (version.version == null) {
        throw new ConfigurationException(GaugeBundle.message("gauge.not.found"), GaugeBundle.message("dialog.title.gauge.found"));
      }

      if (!GaugeVersion.isGreaterOrEqual(MIN_GAUGE_VERSION, false)) {
        throw new ConfigurationException(
          GaugeBundle.message("dialog.message.gauge.intellij.plugin.only.works.with.version", MIN_GAUGE_VERSION),
          GaugeBundle.message("dialog.title.unsupported.gauge.version"));
      }
    }
    catch (GaugeNotFoundException e) {
      throw new ConfigurationException(e.getMessage(), GaugeBundle.message("dialog.title.gauge.found"));
    }
  }

  @Override
  public boolean isSuitableSdkType(SdkTypeId sdkType) {
    return sdkType instanceof JavaSdkType && !((JavaSdkType)sdkType).isDependent();
  }

  @Override
  public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
    return new GaugeTemplatesStep(context);
  }

  private void gaugeInit(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    String moduleFileDirectory = getModuleFileDirectory();
    if (moduleFileDirectory == null) return;
    if (mySelectedTemplate == null) return;

    File directory = new File(moduleFileDirectory);
    if (GaugeUtil.isGaugeProjectDir(directory)) {
      throw new ConfigurationException(
        GaugeBundle.message("dialog.message.given.location.already.gauge"));
    }
    ProgressManager.getInstance()
      .run(new Task.Modal(modifiableRootModel.getProject(), GaugeBundle.message("dialog.title.initializing.gauge.project"), true) {
        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
          progressIndicator.setIndeterminate(true);
          progressIndicator.setText(GaugeBundle.message("progress.text.installing.gauge.plugin.if.installed", JAVA_LANGUAGE));

          try {
            GaugeSettingsModel settings = getGaugeSettings();

            if (mySelectedTemplate.init) {
              String[] init = {
                settings.getGaugePath(),
                GaugeConstants.INIT_FLAG,
                mySelectedTemplate.templateId
              };

              ProcessBuilder processBuilder = new ProcessBuilder(init);
              processBuilder.directory(directory);
              GaugeUtil.setGaugeEnvironmentsTo(processBuilder, settings);
              Process process = processBuilder.start();

              int exitCode = process.waitFor();
              if (exitCode != 0) {
                throw new RuntimeException(GaugeBundle.message("unable.to.create.project.exit.code", exitCode));
              }

              LocalFileSystem.getInstance().refresh(false);
            }
          }
          catch (IOException | InterruptedException e) {
            throw new RuntimeException(GaugeBundle.message("unable.to.create.project"), e);
          }
          catch (GaugeNotFoundException e) {
            throw new RuntimeException(String.format("%s: %s", GaugeBundle.message("unable.to.create.project"), e.getMessage()), e);
          }
        }
      });
  }

  private static List<GaugeTemplate> getTemplates() {
    return Arrays.asList(
      new GaugeTemplate("Maven", false, "java_maven", "maven"),
      new GaugeTemplate("Maven + Selenium", false, "java_maven_selenium", "maven"),
      new GaugeTemplate("Gradle", true, "java_gradle", "gradle")
    );
  }

  private class GaugeTemplatesStep extends ModuleWizardStep {
    private final JPanel myContent;
    private final WizardContext myContext;

    private GaugeTemplatesStep(WizardContext context) {
      myContext = context;
      List<GaugeTemplate> templates = getTemplates();

      JPanel templatesPanel = new JPanel(new VerticalLayout(UIUtil.DEFAULT_VGAP / 2));
      templatesPanel.setFocusable(false);
      ButtonGroup templatesGroup = new ButtonGroup();

      for (GaugeTemplate template : templates) {
        JBRadioButton option = new JBRadioButton(template.title, false);
        option.getModel().addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            mySelectedTemplate = template;
          }
        });
        if (templatesPanel.getComponentCount() == 0) {
          option.setSelected(true);
          mySelectedTemplate = template;
        }
        templatesPanel.add(option);
        templatesPanel.add(Box.createVerticalStrut(UIUtil.DEFAULT_VGAP));
        templatesGroup.add(option);
      }

      FormBuilder formBuilder = new FormBuilder();

      if (templates.isEmpty()) {
        formBuilder.addComponent(new JLabel(GaugeBundle.message("maven.and.gradle.disabled")));
      }

      JButton checkConfigurationButton = new JButton(GaugeBundle.message("gauge.wizard.check.configuration"));
      checkConfigurationButton.addActionListener(e -> {
        ProgressManager.getInstance()
          .run(new Task.Modal(null, GaugeBundle.message("gauge.status.check.configuration"), false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
              try {
                checkGaugeIsInstalled();
              } catch (ConfigurationException ex) {
                ApplicationManager.getApplication().invokeLater(() -> {
                  Messages.showErrorDialog(ex.getMessage(), ex.getTitle());
                });
                return;
              }

              ApplicationManager.getApplication().invokeLater(() -> {
                GaugeVersionInfo version = GaugeVersion.getVersion(false);
                Messages.showInfoMessage(GaugeBundle.message("gauge.version.found", version.version),
                                         GaugeBundle.message("gauge.found"));
              });
            }
          });
      });
      formBuilder.addComponent(checkConfigurationButton);
      formBuilder.addLabeledComponent(GaugeBundle.message("gauge.wizard.template"), templatesPanel, true);
      formBuilder.addComponentFillVertically(new JLabel(""), 0);

      myContent = formBuilder.getPanel();
      myContent.setBorder(JBUI.Borders.emptyLeft(5));
    }

    @Override
    public JComponent getComponent() {
      return myContent;
    }

    @Override
    public boolean validate() throws ConfigurationException {
      if (getTemplates().isEmpty()) {
        throw new ConfigurationException(GaugeBundle.message("maven.and.gradle.disabled"));
      }
      return true;
    }

    @Override
    public void updateDataModel() {
      isNewProject = myContext.isCreatingNewProject();
    }
  }
}
