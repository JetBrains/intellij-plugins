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

package com.thoughtworks.gauge.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.thoughtworks.gauge.Constants;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.core.GaugeVersion;
import com.thoughtworks.gauge.exception.GaugeNotFoundException;
import com.thoughtworks.gauge.module.lib.GaugeLibHelper;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.thoughtworks.gauge.Constants.MIN_GAUGE_VERSION;
import static com.thoughtworks.gauge.util.GaugeUtil.getGaugeSettings;

public final class GaugeModuleBuilder extends JavaModuleBuilder {
  @Override
  public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    checkGaugeIsInstalled();
    super.setupRootModel(modifiableRootModel);
    gaugeInit(modifiableRootModel);
    new GaugeLibHelper(modifiableRootModel.getModule()).checkDeps();
  }

  private static void checkGaugeIsInstalled() throws ConfigurationException {
    try {
      getGaugeSettings();
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

  @Nullable
  @Override
  public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
    return ProjectWizardStepFactory.getInstance().createJavaSettingsStep(settingsStep, this, this::isSuitableSdkType);
  }

  private void gaugeInit(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    String moduleFileDirectory = getModuleFileDirectory();
    if (moduleFileDirectory == null) return;

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
          progressIndicator.setText(GaugeBundle.message("progress.text.installing.gauge.plugin.if.installed", getLanguage()));
          String failureMessage = "Project initialization unsuccessful";
          try {
            GaugeSettingsModel settings = getGaugeSettings();
            final String[] init = {
              settings.getGaugePath(),
              Constants.INIT_FLAG, getLanguage()
            };
            ProcessBuilder processBuilder = new ProcessBuilder(init);
            processBuilder.directory(directory);
            GaugeUtil.setGaugeEnvironmentsTo(processBuilder, settings);
            Process process = processBuilder.start();
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
              throw new RuntimeException(failureMessage);
            }
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
          }
          catch (IOException | InterruptedException e) {
            throw new RuntimeException(failureMessage, e);
          }
          catch (GaugeNotFoundException e) {
            throw new RuntimeException(String.format("%s: %s", failureMessage, e.getMessage()), e);
          }
        }
      });
  }

  private static String getLanguage() {
    return "java";
  }

  @Override
  public ModuleType<?> getModuleType() {
    return GaugeModuleType.getInstance();
  }

  @Override
  public List<Pair<String, String>> getSourcePaths() {
    List<Pair<String, String>> paths = new ArrayList<>();
    @NonNls String path = getContentEntryPath() + File.separator + "src" + File.separator + "test" + File.separator + "java";
    paths.add(Pair.create(path, ""));
    return paths;
  }
}
