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

import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

final class GaugeExecutionConfigurationSettingsEditor extends SettingsEditor<GaugeRunConfiguration> {
  private JTextField specification;
  private JTextField environment;
  private JPanel configWindow;
  private JTextField tags;
  private JCheckBox inParallel;
  private JTextField numberOfParallelNodes;
  private CommonProgramParametersPanel commonProgramParameters;
  private JTextField rowsRange;

  @Override
  protected void resetEditorFrom(@NotNull GaugeRunConfiguration config) {
    specification.setText(config.getSpecsToExecute());
    environment.setText(config.getEnvironment());
    tags.setText(config.getTags());
    inParallel.setSelected(config.getExecInParallel());
    numberOfParallelNodes.setText(config.getParallelNodes());
    commonProgramParameters.reset(config.programParameters);
    rowsRange.setText(config.getRowsRange());
  }

  @Override
  protected void applyEditorTo(@NotNull GaugeRunConfiguration config) {
    config.setSpecsToExecute(specification.getText());
    config.setEnvironment(environment.getText());
    config.setTags(tags.getText());
    config.setExecInParallel(inParallel.isSelected());
    config.setParallelNodes(numberOfParallelNodes.getText());
    commonProgramParameters.applyTo(config.programParameters);
    config.setRowsRange(rowsRange.getText());
  }

  @Override
  protected @NotNull JComponent createEditor() {
    return configWindow;
  }
}
