package com.jetbrains.actionscript.profiler.ui;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.actionscript.profiler.model.ActionScriptProfileSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: Maxim
 * Date: 27.09.2010
 * Time: 17:32:09
 */
public class ActionScriptProfileSettingsForm {
  private JPanel myPanel;
  private JTextField myHostField;
  private JTextField myPortField;
  private TextFieldWithBrowseButton myPathToMmCfgTextField;
  private JCheckBox myCustomPathCheckBox;

  private String customPathToMmCfg = "";

  public ActionScriptProfileSettingsForm() {
    myCustomPathCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!myCustomPathCheckBox.isSelected()) {
          customPathToMmCfg = myPathToMmCfgTextField.getText();
        }
        updateMmCfg();
      }
    });

    myPathToMmCfgTextField.getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final VirtualFile mmCfg = FileChooser.chooseFile(FlexUtils.createFileChooserDescriptor("cfg"), getPanel(), null, null);
        if (mmCfg != null) {
          customPathToMmCfg = mmCfg.getPath();
          updateMmCfg();
        }
      }
    });
  }

  public void resetEditorFrom(ActionScriptProfileSettings profileSettings) {
    customPathToMmCfg = profileSettings.getPathToMmCfg();

    myHostField.setText(profileSettings.getHost());
    myPortField.setText(String.valueOf(profileSettings.getPort()));
    myCustomPathCheckBox.setSelected(profileSettings.isUseCustomPathToMmCfg());
    setUseCustomDirectory();
  }

  public void applyEditorTo(ActionScriptProfileSettings profileSettings) {
    profileSettings.setHostFromString(myHostField.getText());
    profileSettings.setPortFromString(myPortField.getText());
    profileSettings.setUseCustomPathToMmCfg(myCustomPathCheckBox.isSelected());
    if (myCustomPathCheckBox.isSelected()) {
      profileSettings.setPathToMmCfg(FileUtil.toSystemIndependentName(myPathToMmCfgTextField.getText()));
    }
  }

  private void setUseCustomDirectory() {
    myPathToMmCfgTextField.setEnabled(myCustomPathCheckBox.isSelected());
    updateMmCfg();
  }

  private void updateMmCfg() {
    final String defaultMmCfgPath = FileUtil.toSystemDependentName(ActionScriptProfileSettings.getDefaultMmCfgPath());
    myPathToMmCfgTextField
      .setText(myCustomPathCheckBox.isSelected() ? FileUtil.toSystemDependentName(customPathToMmCfg) : defaultMmCfgPath);
    myPathToMmCfgTextField.setEnabled(myCustomPathCheckBox.isSelected());
  }

  public JPanel getPanel() {
    return myPanel;
  }

  public boolean isModified(ActionScriptProfileSettings settings) {
    final boolean result =
      !Comparing.equal(settings.getHost(), myHostField.getText()) || !Comparing.equal(Integer.toString(settings.getPort()), myPortField.getText())
      || !Comparing.equal(settings.isUseCustomPathToMmCfg(), myCustomPathCheckBox.isSelected());
    final boolean flag = myCustomPathCheckBox.isSelected() &&
                         Comparing.equal(settings.getPathToMmCfg(), FileUtil.toSystemIndependentName(myPathToMmCfgTextField.getText()));
    if (!result && flag) {
      return true;
    }
    return result;
  }
}
