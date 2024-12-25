package com.jetbrains.plugins.meteor.settings.ui;


import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.MeteorUIUtil;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public final class MeteorConfigurable implements Configurable, Configurable.NoScroll {

  public static final String ID = "settings.javascript.meteor";
  private TextFieldWithHistoryWithBrowseButton myExecutablePathField;
  private JBCheckBox myExcludeMeteorLocalFolder;
  private JBCheckBox myStartOnce;
  private MeteorSettings mySettings;
  private final Project myProject;
  private JBCheckBox myWeakSearchTemplate;
  private JBCheckBox myAutoImportPackage;

  public MeteorConfigurable(Project project) {
    myProject = project;
  }

  @Override
  public @Nls String getDisplayName() {
    return MeteorBundle.message("settings.meteor.name");
  }

  @Override
  public @Nullable String getHelpTopic() {
    return ID;
  }

  @Override
  public @Nullable JComponent createComponent() {
    mySettings = MeteorSettings.getInstance();
    myExecutablePathField = MeteorUIUtil.createTextField(myProject);
    myExcludeMeteorLocalFolder = new JBCheckBox(MeteorBundle.message("settings.meteor.configurable.exclude-local"));
    myStartOnce = new JBCheckBox(MeteorBundle.message("settings.meteor.configurable.disable.hot.push"));
    myAutoImportPackage = new JBCheckBox(MeteorBundle.message("settings.meteor.configurable.auto.import.packages"));
    myWeakSearchTemplate = new JBCheckBox(MeteorBundle.message("settings.meteor.configurable.weak.search.template"));
    FormBuilder builder = FormBuilder.createFormBuilder();
    builder.addLabeledComponent(MeteorBundle.message("settings.meteor.configurable.executable"), myExecutablePathField);
    builder.addComponent(myExcludeMeteorLocalFolder);
    builder.addComponent(myStartOnce);
    builder.addComponent(myAutoImportPackage);
    builder.addComponent(myWeakSearchTemplate);
    if (!MeteorFacade.getInstance().isMeteorProject(myProject)) {
      JLabel viewer = new JLabel(MeteorBundle.message("settings.meteor.configurable.not.meteor.warning"), UIUtil.getBalloonWarningIcon(), SwingConstants.LEFT);
      UIUtil.applyStyle(UIUtil.ComponentStyle.SMALL, viewer);
      viewer.setBackground(MessageType.WARNING.getPopupBackground());
      builder.addComponent(viewer);
    }

    JPanel panel = builder.getPanel();
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(panel, BorderLayout.NORTH);
    return wrapper;
  }

  @Override
  public boolean isModified() {
    return !StringUtil.equals(myExecutablePathField.getText(), mySettings.getExecutablePath()) ||
           (myExcludeMeteorLocalFolder.isSelected() != mySettings.isExcludeMeteorLocalFolder()) ||
           (myWeakSearchTemplate.isSelected() != mySettings.isWeakSearch()) ||
           (myAutoImportPackage.isSelected() != mySettings.isAutoImport()) ||
           (myStartOnce.isSelected() == mySettings.isStartOnce());
  }

  @Override
  public void apply() throws ConfigurationException {
    if (mySettings != null) {
      mySettings.setExecutablePath(myExecutablePathField.getText());
      mySettings.setExcludeMeteorLocalFolder(myExcludeMeteorLocalFolder.isSelected());
      mySettings.setStartOnce(!myStartOnce.isSelected());
      mySettings.setIsWeakSearch(myWeakSearchTemplate.isSelected());
      mySettings.setAutoImport(myAutoImportPackage.isSelected());
    }

  }

  @Override
  public void reset() {
    if (mySettings != null) {
      myExecutablePathField.getChildComponent().setText(mySettings.getExecutablePath());
      myExecutablePathField.getChildComponent().addCurrentTextToHistory();
      myExcludeMeteorLocalFolder.setSelected(mySettings.isExcludeMeteorLocalFolder());
      myStartOnce.setSelected(!mySettings.isStartOnce());
      myWeakSearchTemplate.setSelected(mySettings.isWeakSearch());
      myAutoImportPackage.setSelected(mySettings.isAutoImport());
    }
  }
}
