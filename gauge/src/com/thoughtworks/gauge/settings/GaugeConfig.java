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

package com.thoughtworks.gauge.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.thoughtworks.gauge.GaugeBundle;

import javax.swing.*;

public final class GaugeConfig {
  private JPanel configWindow;
  private TextFieldWithBrowseButton gaugePath;
  private TextFieldWithBrowseButton homePath;
  private JCheckBox useIntelliJTestRunner;

  JComponent createEditor() {
    FileChooserDescriptor homeFileDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
    homePath.addBrowseFolderListener(
      "",
      GaugeBundle.message("form.gauge.home"),
      null,
      homeFileDescriptor,
      TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);

    FileChooserDescriptor gaugePathFileDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
    gaugePath.addBrowseFolderListener(
      "",
      GaugeBundle.message("form.gauge.binary.path"),
      null,
      gaugePathFileDescriptor,
      TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);

    return configWindow;
  }

  void setValues(GaugeSettingsModel model) {
    this.homePath.setText(model.homePath);
    this.gaugePath.setText(model.gaugePath);
    this.useIntelliJTestRunner.setSelected(model.useIntelliJTestRunner);
  }

  GaugeSettingsModel getValues() {
    return new GaugeSettingsModel(this.gaugePath.getText(), this.homePath.getText(), this.useIntelliJTestRunner.isSelected());
  }
}
