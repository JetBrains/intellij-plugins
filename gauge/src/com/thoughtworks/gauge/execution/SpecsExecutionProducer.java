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

package com.thoughtworks.gauge.execution;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.thoughtworks.gauge.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.thoughtworks.gauge.util.GaugeUtil.isSpecFile;

final class SpecsExecutionProducer extends RunConfigurationProducer {

  public static final String DEFAULT_CONFIGURATION_NAME = "Specifications";

  SpecsExecutionProducer() {
    super(new GaugeRunTaskConfigurationType());
  }

  @Override
  protected boolean setupConfigurationFromContext(@NotNull RunConfiguration configuration,
                                                  ConfigurationContext configurationContext,
                                                  @NotNull Ref ref) {
    VirtualFile[] selectedFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(configurationContext.getDataContext());
    Module module = configurationContext.getModule();
    if (selectedFiles == null || module == null) {
      return false;
    }
    if (selectedFiles.length == 1) {
      if (!selectedFiles[0].isDirectory()) {
        return false;
      }
      else if (selectedFiles[0].getPath().equals(configurationContext.getProject().getBasePath())) {
        configuration.setName(DEFAULT_CONFIGURATION_NAME);
        ((GaugeRunConfiguration)configuration).setModule(module);
        return true;
      }
    }

    List<String> specsToExecute = getSpecs(selectedFiles);
    if (specsToExecute.size() == 0) {
      return false;
    }
    configuration.setName(DEFAULT_CONFIGURATION_NAME);
    ((GaugeRunConfiguration)configuration).setModule(module);
    ((GaugeRunConfiguration)configuration).setSpecsArrayToExecute(specsToExecute);
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(RunConfiguration config, @NotNull ConfigurationContext context) {
    if (!(config.getType() instanceof GaugeRunTaskConfigurationType)) return false;
    if (!(context.getPsiLocation() instanceof PsiDirectory) && !(context.getPsiLocation() instanceof PsiFile)) {
      return false;
    }
    VirtualFile[] selectedFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(context.getDataContext());
    if (selectedFiles == null) return false;
    String specs = ((GaugeRunConfiguration)config).getSpecsToExecute();
    return StringUtil.join(getSpecs(selectedFiles), Constants.SPEC_FILE_DELIMITER).equals(specs);
  }

  @NotNull
  private static List<String> getSpecs(VirtualFile[] selectedFiles) {
    List<String> specsToExecute = new ArrayList<>();
    for (VirtualFile selectedFile : selectedFiles) {
      if (isSpecFile(selectedFile)) {
        specsToExecute.add(selectedFile.getPath());
      }
      else if (selectedFile.isDirectory() && shouldAddDirToExecute(selectedFile)) {
        specsToExecute.add(selectedFile.getPath());
      }
    }
    return specsToExecute;
  }

  private static boolean shouldAddDirToExecute(VirtualFile selectedFile) {
    return numberOfSpecFiles(selectedFile) != 0;
  }

  private static int numberOfSpecFiles(VirtualFile directory) {
    int numberOfSpecs = 0;
    for (VirtualFile file : directory.getChildren()) {
      if (isSpecFile(file)) {
        numberOfSpecs++;
      }
    }
    return numberOfSpecs;
  }
}
