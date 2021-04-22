/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.settings;

import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.components.BorderLayoutPanel;
import com.intellij.protobuf.ide.PbIdeBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/** The protobuf text format language settings panel. */
public class PbTextLanguageSettingsForm implements ConfigurableUi<PbTextLanguageSettings> {

  private JPanel panel;
  private JCheckBox missingSchemaWarningCheckbox;

  PbTextLanguageSettingsForm(Project project) {
    initComponent();
  }

  @Override
  public void reset(@NotNull PbTextLanguageSettings settings) {
    setMissingSchemaWarningEnabled(settings.isMissingSchemaWarningEnabled());
  }

  @Override
  public boolean isModified(@NotNull PbTextLanguageSettings settings) {
    return isMissingSchemaWarningEnabled() != settings.isMissingSchemaWarningEnabled();
  }

  @Override
  public void apply(@NotNull PbTextLanguageSettings settings) {
    settings.setMissingSchemaWarningEnabled(isMissingSchemaWarningEnabled());
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return panel;
  }

  private boolean isMissingSchemaWarningEnabled() {
    return missingSchemaWarningCheckbox.isSelected();
  }

  private void setMissingSchemaWarningEnabled(boolean value) {
    missingSchemaWarningCheckbox.setSelected(value);
  }

  private void initComponent() {
    missingSchemaWarningCheckbox =
        new JBCheckBox(PbIdeBundle.message("prototext.settings.missing.schema"));
    panel = new BorderLayoutPanel();
    panel.add(missingSchemaWarningCheckbox, BorderLayout.NORTH);
  }
}
