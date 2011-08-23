package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.options.HtmlWrapperOptions;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HtmlWrapperConfigurable extends NamedConfigurable<HtmlWrapperOptions> {

  private JPanel myMainPanel;

  private JRadioButton myWrapperFromSdkRadioButton;
  private JCheckBox myEnableHistoryCheckBox;
  private JCheckBox myCheckPlayerVersionCheckBox;
  private JCheckBox myExpressInstallCheckBox;

  private JRadioButton myWrapperFromFolderRadioButton;
  private TextFieldWithBrowseButton myTemplateFolderTextWithBrowse;

  private JRadioButton myNoWrapperRadioButton;

  private final HtmlWrapperOptions myHtmlWrapperOptions;

  public HtmlWrapperConfigurable(final Project project, final HtmlWrapperOptions htmlWrapperOptions) {
    myHtmlWrapperOptions = htmlWrapperOptions;
    initListeners(project);
  }

  private void initListeners(final Project project) {
    final ActionListener listener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };

    myWrapperFromSdkRadioButton.addActionListener(listener);
    myCheckPlayerVersionCheckBox.addActionListener(listener);

    myWrapperFromFolderRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        IdeFocusManager.getInstance(project).requestFocus(myTemplateFolderTextWithBrowse.getTextField(), true);
      }
    });

    myTemplateFolderTextWithBrowse
      .addBrowseFolderListener(null, null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myNoWrapperRadioButton.addActionListener(listener);

  }

  @Nls
  public String getDisplayName() {
    return "HTML Wrapper";
  }

  public void setDisplayName(final String name) {
    assert false;
  }

  public String getBannerSlogan() {
    return "HTML Wrapper";
  }

  public Icon getIcon() {
    return null;
  }

  public HtmlWrapperOptions getEditableObject() {
    return myHtmlWrapperOptions;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    final HtmlWrapperOptions.WrapperType type = getWrapperType();
    if (type != myHtmlWrapperOptions.WRAPPER_TYPE) return true;
    if (myEnableHistoryCheckBox.isSelected() != myHtmlWrapperOptions.ENABLE_HISTORY) return true;
    if (myCheckPlayerVersionCheckBox.isSelected() != myHtmlWrapperOptions.CHECK_PLAYER) return true;
    if (myExpressInstallCheckBox.isSelected() != myHtmlWrapperOptions.EXPRESS_INSTALL) return true;
    if (!getTemplateFolderPath().equals(myHtmlWrapperOptions.TEMPLATE_FOLDER_PATH)) return true;

    return false;
  }

  private String getTemplateFolderPath() {
    return FileUtil.toSystemIndependentName(myTemplateFolderTextWithBrowse.getText());
  }

  private HtmlWrapperOptions.WrapperType getWrapperType() {
    return myWrapperFromSdkRadioButton.isSelected()
           ? HtmlWrapperOptions.WrapperType.FromSdk
           : myWrapperFromFolderRadioButton.isSelected()
             ? HtmlWrapperOptions.WrapperType.FromFolder
             : HtmlWrapperOptions.WrapperType.NoWrapper;
  }

  public void apply() throws ConfigurationException {
    applyTo(myHtmlWrapperOptions);
  }

  public void applyTo(final HtmlWrapperOptions htmlWrapperOptions) {
    htmlWrapperOptions.WRAPPER_TYPE = getWrapperType();
    htmlWrapperOptions.ENABLE_HISTORY = myEnableHistoryCheckBox.isSelected();
    htmlWrapperOptions.CHECK_PLAYER = myCheckPlayerVersionCheckBox.isSelected();
    htmlWrapperOptions.EXPRESS_INSTALL = myExpressInstallCheckBox.isSelected();
    htmlWrapperOptions.TEMPLATE_FOLDER_PATH = getTemplateFolderPath();
  }

  public void reset() {
    myWrapperFromSdkRadioButton.setSelected(myHtmlWrapperOptions.WRAPPER_TYPE == HtmlWrapperOptions.WrapperType.FromSdk);
    myEnableHistoryCheckBox.setSelected(myHtmlWrapperOptions.ENABLE_HISTORY);
    myCheckPlayerVersionCheckBox.setSelected(myHtmlWrapperOptions.CHECK_PLAYER);
    myExpressInstallCheckBox.setSelected(myHtmlWrapperOptions.EXPRESS_INSTALL);

    myWrapperFromFolderRadioButton.setSelected(myHtmlWrapperOptions.WRAPPER_TYPE == HtmlWrapperOptions.WrapperType.FromFolder);
    myTemplateFolderTextWithBrowse.setText(FileUtil.toSystemDependentName(myHtmlWrapperOptions.TEMPLATE_FOLDER_PATH));

    myNoWrapperRadioButton.setSelected(myHtmlWrapperOptions.WRAPPER_TYPE == HtmlWrapperOptions.WrapperType.NoWrapper);

    updateControls();
  }

  private void updateControls() {
    myEnableHistoryCheckBox.setEnabled(myWrapperFromSdkRadioButton.isSelected());
    myCheckPlayerVersionCheckBox.setEnabled(myWrapperFromSdkRadioButton.isSelected());
    myExpressInstallCheckBox.setEnabled(myWrapperFromSdkRadioButton.isSelected() && myCheckPlayerVersionCheckBox.isSelected());
    myTemplateFolderTextWithBrowse.setEnabled(myWrapperFromFolderRadioButton.isSelected());
  }

  public void disposeUIResources() {
  }
}
