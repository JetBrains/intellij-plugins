/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.intellij.terraform.runtime;

import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TerraformRunConfigurationEditor extends SettingsEditor<TerraformRunConfiguration> implements PanelWithAnchor {
  private CommonProgramParametersPanel myCommonProgramParameters;
  private JPanel myWholePanel;
  private JComponent myAnchor;

  public TerraformRunConfigurationEditor() {
    myCommonProgramParameters.setModuleContext(null);
    myAnchor = UIUtil.mergeComponentsWithAnchor(myCommonProgramParameters);
  }

  @Override
  public void applyEditorTo(@NotNull TerraformRunConfiguration configuration) {
    myCommonProgramParameters.applyTo(configuration);
  }

  @Override
  public void resetEditorFrom(@NotNull TerraformRunConfiguration configuration) {
    myCommonProgramParameters.reset(configuration);
  }

  @Override
  @NotNull
  public JComponent createEditor() {
    return myWholePanel;
  }

  @Override
  public JComponent getAnchor() {
    return myAnchor;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    myAnchor = anchor;
    myCommonProgramParameters.setAnchor(anchor);
  }

}
