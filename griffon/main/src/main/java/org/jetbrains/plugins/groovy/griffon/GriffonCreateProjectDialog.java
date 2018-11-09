// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.griffon;

import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.MvcCommand;

import javax.swing.*;

/**
 * @author peter
 */
public class GriffonCreateProjectDialog extends DialogWrapper {
  private JTextField myOptionField;
  private JPanel myComponent;
  private JRadioButton myCreateApp;
  private JRadioButton myCreatePlugin;
  private JRadioButton myCreateAddon;
  private JRadioButton myCreateArchetype;
  private JLabel myCreateLabel;

  public GriffonCreateProjectDialog(@NotNull Module module) {
    super(module.getProject());
    setTitle("Create Griffon Structure");
    myCreateLabel.setText("Create Griffon structure in module '" + module.getName() + "':");
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myComponent;
  }

  MvcCommand getCommand() {
    String cmd;

    if (myCreateAddon.isSelected()) {
      cmd = "create-addon";
    }
    else if (myCreateApp.isSelected()) {
      cmd = "create-app";
    }
    else if (myCreateArchetype.isSelected()) {
      cmd = "create-archetype";
    }
    else if (myCreatePlugin.isSelected()) {
      cmd = "create-plugin";
    }
    else {
      throw new AssertionError("No selection");
    }

    String text = myOptionField.getText();
    if (text == null) text = "";

    return new MvcCommand(cmd, ParametersList.parse(text));
  }

}
