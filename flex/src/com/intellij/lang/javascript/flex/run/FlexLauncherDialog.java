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
                            final LauncherParameters.LauncherType launcherType,
                            final BrowsersConfiguration.BrowserFamily browserFamily,
                            final String playerPath) {
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

  public FlexLauncherDialog(final Project project, final LauncherParameters launcherParameters) {
    this(project, launcherParameters.getLauncherType(), launcherParameters.getBrowserFamily(), launcherParameters.getPlayerPath());
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
      .addBrowseFolderListener(null, null, myProject,
                               new FileChooserDescriptor(true, true, false, false, false, false) {
                                 @Override
                                 public boolean isFileSelectable(final VirtualFile file) {
                                   return SystemInfo.isMac && file.isDirectory() && "app".equalsIgnoreCase(file.getExtension())
                                          || !file.isDirectory();
                                 }
                               });
  }

  private void initControls(final LauncherParameters.LauncherType launcherType,
                            final BrowsersConfiguration.BrowserFamily browserFamily,
                            final String playerPath) {
    myDefaultOSApplicationRadioButton.setSelected(launcherType == LauncherParameters.LauncherType.OSDefault);
    myBrowserRadioButton.setSelected(launcherType == LauncherParameters.LauncherType.Browser);
    myPlayerRadioButton.setSelected(launcherType == LauncherParameters.LauncherType.Player);
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

  public LauncherParameters.LauncherType getLauncherType() {
    return myPlayerRadioButton.isSelected()
           ? LauncherParameters.LauncherType.Player
           : myBrowserRadioButton.isSelected() ? LauncherParameters.LauncherType.Browser : LauncherParameters.LauncherType.OSDefault;
  }

  @Nullable
  public BrowsersConfiguration.BrowserFamily getBrowserFamily() {
    return myBrowserSelector.getSelectedBrowser();
  }

  public String getPlayerPath() {
    return FileUtil.toSystemIndependentName(myPlayerTextWithBrowse.getText().trim());
  }

  public LauncherParameters getLauncherParameters() {
    final BrowsersConfiguration.BrowserFamily browserFamily = getBrowserFamily();
    return new LauncherParameters(getLauncherType(), browserFamily == null ? BrowsersConfiguration.BrowserFamily.FIREFOX : browserFamily,
                                  getPlayerPath());
  }
}
