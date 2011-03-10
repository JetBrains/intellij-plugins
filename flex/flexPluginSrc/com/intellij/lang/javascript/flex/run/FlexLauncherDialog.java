package com.intellij.lang.javascript.flex.run;

import com.intellij.ide.browsers.BrowserSelector;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlexLauncherDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JRadioButton myDefaultOSApplicationRadioButton;
  private JRadioButton myBrowserRadioButton;
  private JRadioButton myPlayerRadioButton;
  private TextFieldWithBrowseButton myPlayerTextWithBrowse;
  private JPanel myBrowserSelectorPanel;
  private BrowserSelector myBrowserSelector;
  private final Project myProject;

  public FlexLauncherDialog(final Project project,
                               final FlexRunnerParameters.LauncherType launcherType,
                               final BrowsersConfiguration.BrowserFamily browserFamily, final String playerPath) {
    super(project);
    myProject = project;
    setTitle(FlexBundle.message("launch.with.title"));

    myBrowserSelector = new BrowserSelector(false);
    myBrowserSelectorPanel.add(BorderLayout.CENTER, myBrowserSelector.getMainComponent());
    initRadioButtons();
    initPlayerTextWithBrowse();
    initControls(launcherType, browserFamily, playerPath);
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

  private void initPlayerTextWithBrowse() {
    myPlayerTextWithBrowse
      .addBrowseFolderListener(null, null, myProject, new FileChooserDescriptor(true, false, false, false, false, false));
  }

  private void initControls(final FlexRunnerParameters.LauncherType launcherType,
                            final BrowsersConfiguration.BrowserFamily browserFamily,
                            final String playerPath) {
    myDefaultOSApplicationRadioButton.setSelected(launcherType == FlexRunnerParameters.LauncherType.OSDefault);
    myBrowserRadioButton.setSelected(launcherType == FlexRunnerParameters.LauncherType.Browser);
    myPlayerRadioButton.setSelected(launcherType == FlexRunnerParameters.LauncherType.Player);
    myBrowserSelector.setSelectedBrowser(browserFamily);
    myPlayerTextWithBrowse.setText(FileUtil.toSystemDependentName(playerPath));
  }

  private void updateControls() {
    myBrowserSelector.getMainComponent().setEnabled(myBrowserRadioButton.isSelected());
    myPlayerTextWithBrowse.setEnabled(myPlayerRadioButton.isSelected());
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public FlexRunnerParameters.LauncherType getLauncherType() {
    return myPlayerRadioButton.isSelected()
           ? FlexRunnerParameters.LauncherType.Player
           : myBrowserRadioButton.isSelected() ? FlexRunnerParameters.LauncherType.Browser : FlexRunnerParameters.LauncherType.OSDefault;
  }

  @Nullable
  public BrowsersConfiguration.BrowserFamily getBrowserFamily() {
    return myBrowserSelector.getSelectedBrowser();
  }

  public String getPlayerPath() {
    return FileUtil.toSystemIndependentName(myPlayerTextWithBrowse.getText().trim());
  }
}
