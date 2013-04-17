package com.intellij.lang.javascript.flex.run;

import com.intellij.ide.browsers.BrowserSelector;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.JBCheckBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlexLauncherDialog extends DialogWrapper {
  private JPanel myMainPanel;

  private JRadioButton myDefaultOSApplicationRadioButton;

  private JRadioButton myBrowserRadioButton;
  private JPanel myBrowserSelectorPanel;
  private BrowserSelector myBrowserSelector;

  private JRadioButton myPlayerRadioButton;
  private TextFieldWithBrowseButton myPlayerTextWithBrowse;
  private JBCheckBox myNewPlayerInstanceCheckBox;

  private final Project myProject;

  public FlexLauncherDialog(final Project project, final LauncherParameters launcherParameters) {
    super(project);
    myProject = project;
    setTitle(FlexBundle.message("launch.with.title"));

    myBrowserSelector = new BrowserSelector(false);
    myBrowserSelectorPanel.add(BorderLayout.CENTER, myBrowserSelector.getMainComponent());
    initRadioButtons();
    initControls(launcherParameters);
    updateControls();

    init();
  }

  private void initRadioButtons() {
    myDefaultOSApplicationRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateControls();
      }
    });
    myBrowserRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateControls();
        IdeFocusManager.getInstance(myProject).requestFocus(myBrowserSelector.getMainComponent(), true);
      }
    });
    myPlayerRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateControls();
        IdeFocusManager.getInstance(myProject).requestFocus(myPlayerTextWithBrowse.getTextField(), true);
      }
    });
  }

  private void initControls(final LauncherParameters launcherParameters) {
    final LauncherParameters.LauncherType launcherType = launcherParameters.getLauncherType();
    myDefaultOSApplicationRadioButton.setSelected(launcherType == LauncherParameters.LauncherType.OSDefault);
    myBrowserRadioButton.setSelected(launcherType == LauncherParameters.LauncherType.Browser);
    myPlayerRadioButton.setSelected(launcherType == LauncherParameters.LauncherType.Player);

    myBrowserSelector.setSelectedBrowser(launcherParameters.getBrowserFamily());

    myPlayerTextWithBrowse.setText(FileUtil.toSystemDependentName(launcherParameters.getPlayerPath()));
    myPlayerTextWithBrowse
      .addBrowseFolderListener(null, null, myProject,
                               new FileChooserDescriptor(true, true, false, false, false, false) {
                                 @Override
                                 public boolean isFileSelectable(final VirtualFile file) {
                                   return SystemInfo.isMac && file.isDirectory() && "app".equalsIgnoreCase(file.getExtension())
                                          || !file.isDirectory();
                                 }
                               });
    myNewPlayerInstanceCheckBox.setVisible(SystemInfo.isMac);
  }

  private void updateControls() {
    myBrowserSelector.getMainComponent().setEnabled(myBrowserRadioButton.isSelected());

    myPlayerTextWithBrowse.setEnabled(myPlayerRadioButton.isSelected());
    myNewPlayerInstanceCheckBox.setEnabled(myPlayerRadioButton.isSelected());
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public LauncherParameters getLauncherParameters() {
    final LauncherParameters.LauncherType launcherType = myPlayerRadioButton.isSelected()
                                                         ? LauncherParameters.LauncherType.Player
                                                         : myBrowserRadioButton.isSelected()
                                                           ? LauncherParameters.LauncherType.Browser
                                                           : LauncherParameters.LauncherType.OSDefault;
    final BrowsersConfiguration.BrowserFamily browser = myBrowserSelector.getSelectedBrowser();
    final BrowsersConfiguration.BrowserFamily notNullBrowser = browser == null ? BrowsersConfiguration.BrowserFamily.FIREFOX : browser;
    final String playerPath = FileUtil.toSystemIndependentName(myPlayerTextWithBrowse.getText().trim());
    final boolean isNewPlayerInstance = myNewPlayerInstanceCheckBox.isSelected();
    return new LauncherParameters(launcherType, notNullBrowser, playerPath, isNewPlayerInstance);
  }
}
