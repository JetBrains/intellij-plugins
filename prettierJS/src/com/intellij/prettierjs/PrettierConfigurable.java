// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageField;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PrettierConfigurable implements SearchableConfigurable {
  @NotNull
  private final Project myProject;
  private final NodeJsInterpreterField myNodeInterpreterField;
  private final NodePackageField myPackageField;
  private final JBCheckBox myRunOnSaveCheckbox;

  public PrettierConfigurable(@NotNull Project project) {
    myProject = project;
    myNodeInterpreterField = new NodeJsInterpreterField(myProject, false);
    myPackageField = new NodePackageField(myNodeInterpreterField, PrettierUtil.PACKAGE_NAME);
    myRunOnSaveCheckbox = new JBCheckBox(PrettierBundle.message("run.on.save"));
  }

  @NotNull
  @Override
  public String getId() {
    return "settings.javascript.prettier";
  }

  @Nls
  @Override
  public String getDisplayName() {
    return PrettierBundle.message("configurable.PrettierConfigurable.display.name");
  }

  public void showEditDialog() {
    ShowSettingsUtil.getInstance().editConfigurable(myProject, this);
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    JPanel mainPanel = FormBuilder.createFormBuilder()
      .setAlignLabelOnRight(true)
      .addLabeledComponent("&Node interpreter:", myNodeInterpreterField)
      .addLabeledComponent("&Prettier package:", myPackageField)
      .addComponent(myRunOnSaveCheckbox, IdeBorderFactory.TITLED_BORDER_TOP_INSET)
      .getPanel();
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(mainPanel, BorderLayout.NORTH);
    wrapper.setPreferredSize(new Dimension(400, 200));
    return wrapper;
  }

  @Override
  public boolean isModified() {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(myProject);
    return !configuration.getInterpreterRef().equals(myNodeInterpreterField.getInterpreterRef()) ||
           !myPackageField.getSelected().equals(configuration.getPackage()) ||
           myRunOnSaveCheckbox.isSelected() != configuration.isRunOnSave();
  }

  @Override
  public void reset() {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(myProject);
    myNodeInterpreterField.setInterpreterRef(configuration.getInterpreterRef());
    myPackageField.setSelected(configuration.getPackage());
    myRunOnSaveCheckbox.setSelected(configuration.isRunOnSave());
  }

  @Override
  public void apply() {
    NodePackage selectedPackage = myPackageField.getSelected();
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(myProject);
    configuration.update(myNodeInterpreterField.getInterpreterRef(), selectedPackage);
    configuration.setRunOnSave(myRunOnSaveCheckbox.isSelected());
    PrettierLanguageService.getInstance(myProject).terminateStartedProcess(false);
  }
}
