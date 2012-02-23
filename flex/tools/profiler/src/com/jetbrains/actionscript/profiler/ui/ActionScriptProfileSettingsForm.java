package com.jetbrains.actionscript.profiler.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
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
  private JRadioButton myUseLocalUserDirectoryRadioButton;
  private JRadioButton myUseCustomDirectoryRadioButton;
  private TextFieldWithBrowseButton myPathToMmCfgTextField;
  private JPanel myPathPanel;
  private JLabel myFolderPathLabel;

  public ActionScriptProfileSettingsForm() {
    final ActionListener radioButtonListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setUseCustomDirectory(myUseCustomDirectoryRadioButton.isSelected());
      }
    };

    final ButtonGroup group = new ButtonGroup();
    group.add(myUseCustomDirectoryRadioButton);
    group.add(myUseLocalUserDirectoryRadioButton);

    myUseCustomDirectoryRadioButton.addActionListener(radioButtonListener);
    myUseLocalUserDirectoryRadioButton.addActionListener(radioButtonListener);

    myPathToMmCfgTextField.getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final VirtualFile folder = FileChooser.chooseFile(getPanel(), FileChooserDescriptorFactory.createSingleFolderDescriptor());
        if (folder != null) {
          myPathToMmCfgTextField.setText(folder.getPresentableUrl());
        }
      }
    });

    myFolderPathLabel.setLabelFor(myPathToMmCfgTextField.getTextField());
  }

  public void resetEditorFrom(ActionScriptProfileSettings profileSettings) {
    myHostField.setText(profileSettings.getHost());
    myPortField.setText(String.valueOf(profileSettings.getPort()));
    myPathToMmCfgTextField.setText(profileSettings.getPathToMmCfg());
    setUseCustomDirectory(profileSettings.isUseCustomPathToMmCfg());
  }

  public void applyEditorTo(ActionScriptProfileSettings profileSettings) {
    profileSettings.setHostFromString(myHostField.getText());
    profileSettings.setPortFromString(myPortField.getText());
    profileSettings.setPathToMmCfg(FileUtil.toSystemIndependentName(myPathToMmCfgTextField.getText()));
    profileSettings.setUseCustomPathToMmCfg(myUseCustomDirectoryRadioButton.isSelected());
  }

  private void setUseCustomDirectory(boolean useCustomPathToMmCfg) {
    myUseCustomDirectoryRadioButton.setSelected(useCustomPathToMmCfg);
    myUseLocalUserDirectoryRadioButton.setSelected(!useCustomPathToMmCfg);
    myPathToMmCfgTextField.setEnabled(useCustomPathToMmCfg);
  }

  public JPanel getPanel() {
    return myPanel;
  }

  public boolean isModified(ActionScriptProfileSettings settings) {
    return !Comparing.equal(settings.getHost(), myHostField.getText()) || !Comparing.equal(settings.getPort(), myPortField.getText())
           || !Comparing.equal(settings.getPathToMmCfg(), FileUtil.toSystemIndependentName(myPathToMmCfgTextField.getText()))
           || !Comparing.equal(settings.isUseCustomPathToMmCfg(), myUseCustomDirectoryRadioButton.isSelected());
  }
}
