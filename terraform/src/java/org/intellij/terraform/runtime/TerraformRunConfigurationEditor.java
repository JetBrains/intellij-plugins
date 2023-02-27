// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
