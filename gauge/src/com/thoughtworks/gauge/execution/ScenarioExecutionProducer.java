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

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.language.SpecFile;
import com.thoughtworks.gauge.language.psi.impl.SpecScenarioImpl;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

import static com.thoughtworks.gauge.util.GaugeUtil.isSpecFile;

final class ScenarioExecutionProducer extends LazyRunConfigurationProducer<GaugeRunConfiguration> {
  private static final Logger LOG = Logger.getInstance(ScenarioExecutionProducer.class);

  private static final int NO_SCENARIOS = -1;
  private static final int NON_SCENARIO_CONTEXT = -2;

  ScenarioExecutionProducer() {
  }

  @NotNull
  @Override
  public ConfigurationFactory getConfigurationFactory() {
    return new GaugeRunTaskConfigurationType().getConfigurationFactories()[0];
  }

  @Override
  protected boolean setupConfigurationFromContext(@NotNull GaugeRunConfiguration configuration, @NotNull ConfigurationContext context,
                                                  @NotNull Ref<PsiElement> sourceElement) {
    VirtualFile[] selectedFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(context.getDataContext());
    if (selectedFiles == null || selectedFiles.length > 1) {
      return false;
    }

    if (context.getPsiLocation() == null) return false;
    Module module = GaugeUtil.moduleForPsiElement(context.getPsiLocation());
    if (module == null) {
      return false;
    }

    if (context.getPsiLocation() == null ||
        !(isSpecFile(context.getPsiLocation().getContainingFile())) ||
        context.getPsiLocation().getContainingFile().getVirtualFile() == null) {
      return false;
    }
    try {
      String name = context.getPsiLocation().getContainingFile().getVirtualFile().getCanonicalPath();
      int scenarioIdentifier = getScenarioIdentifier(context, context.getPsiLocation().getContainingFile());
      if (scenarioIdentifier == NO_SCENARIOS || scenarioIdentifier == NON_SCENARIO_CONTEXT) {
        return false;
      }
      else {
        String scenarioName = getScenarioName(context);
        configuration.setName(scenarioName);
        configuration.setSpecsToExecute(name + GaugeConstants.SPEC_SCENARIO_DELIMITER + scenarioIdentifier);
      }
      configuration.setModule(module);
      return true;
    }
    catch (Exception ex) {
      LOG.debug(ex);
    }
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(GaugeRunConfiguration configuration, @NotNull ConfigurationContext context) {
    if (!(configuration.getType() instanceof GaugeRunTaskConfigurationType)) return false;

    Location<?> location = context.getLocation();
    if (location == null || location.getVirtualFile() == null || context.getPsiLocation() == null) return false;
    String specsToExecute = configuration.getSpecsToExecute();
    int identifier = getScenarioIdentifier(context, context.getPsiLocation().getContainingFile());
    return specsToExecute != null &&
           specsToExecute.equals(location.getVirtualFile().getPath() + GaugeConstants.SPEC_SCENARIO_DELIMITER + identifier);
  }

  private static String getScenarioName(ConfigurationContext context) {
    PsiElement selectedElement = context.getPsiLocation();
    String scenarioName;

    if (selectedElement == null) return null;
    if (selectedElement.getClass().equals(SpecScenarioImpl.class)) {
      scenarioName = selectedElement.getText();
    }
    else {
      String text = getScenarioHeading(selectedElement).trim();
      if (text.equals("*")) {
        scenarioName = selectedElement.getParent().getParent().getNode().getFirstChildNode().getText();
      }
      else {
        scenarioName = text;
      }
    }
    if (scenarioName.startsWith("##")) {
      scenarioName = scenarioName.replaceFirst("##", "");
    }
    scenarioName = scenarioName.trim();
    return scenarioName.contains("\n") ? scenarioName.substring(0, scenarioName.indexOf("\n")) : scenarioName;
  }

  private static int getScenarioIdentifier(ConfigurationContext context, PsiFile file) {
    int count = NO_SCENARIOS;
    PsiElement selectedElement = context.getPsiLocation();
    if (selectedElement == null) return NON_SCENARIO_CONTEXT;
    String scenarioHeading =
      (!selectedElement.getClass().equals(SpecScenarioImpl.class)) ? getScenarioHeading(selectedElement) : selectedElement.getText();
    if (scenarioHeading.isEmpty()) {
      return getNumberOfScenarios(file) == 0 ? NO_SCENARIOS : NON_SCENARIO_CONTEXT;
    }
    for (PsiElement psiElement : file.getChildren()) {
      if (psiElement.getClass().equals(SpecScenarioImpl.class)) {
        count++;
        if (psiElement.getNode().getFirstChildNode().getText().equals(scenarioHeading)) {
          return StringUtil.offsetToLineNumber(psiElement.getContainingFile().getText(), psiElement.getTextOffset()) + 1;
        }
      }
    }
    return count == NO_SCENARIOS ? NO_SCENARIOS : NON_SCENARIO_CONTEXT;
  }

  private static int getNumberOfScenarios(PsiFile file) {
    int count = 0;
    for (PsiElement psiElement : file.getChildren()) {
      if (psiElement.getClass().equals(SpecScenarioImpl.class)) {
        count++;
      }
    }
    return count;
  }

  private static String getScenarioHeading(PsiElement selectedElement) {
    if (selectedElement == null) return "";
    if (selectedElement.getClass().equals(SpecScenarioImpl.class)) {
      return selectedElement.getNode().getFirstChildNode().getText();
    }
    if (selectedElement.getClass().equals(SpecFile.class)) {
      return "";
    }
    return getScenarioHeading(selectedElement.getParent());
  }
}
