package com.jetbrains.plugins.meteor.runner;


import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.ide.browsers.StartBrowserPanel;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.PathUtil;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorUIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MeteorRunConfigurationEditor extends SettingsEditor<MeteorRunConfiguration> {

  private StartBrowserPanel myStartBrowserPanel;
  private MeteorDebuggableProgramParametersPanel myMeteorDebuggablePanel;
  private final Project myProject;

  public MeteorRunConfigurationEditor(Project project) {
    myProject = project;
  }

  @Override
  protected void resetEditorFrom(@NotNull MeteorRunConfiguration s) {

    myStartBrowserPanel.setFromSettings(s.getStartBrowserSettings());
    myMeteorDebuggablePanel.reset(s);
  }

  @Override
  protected void applyEditorTo(@NotNull MeteorRunConfiguration s) throws ConfigurationException {

    s.setStartBrowserSettings(myStartBrowserPanel.createSettings());
    myMeteorDebuggablePanel.applyTo(s);
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    myStartBrowserPanel = new StartBrowserPanel();
    myMeteorDebuggablePanel = new MeteorDebuggableProgramParametersPanel(myProject);
    JBTabbedPane mainPane = new JBTabbedPane();
    mainPane.addTab(MeteorBundle.message("configuration"), myMeteorDebuggablePanel);
    mainPane.addTab(MeteorBundle.message("browser.live.edit"), myStartBrowserPanel.getComponent());
    mainPane.setSelectedIndex(0);

    return mainPane;
  }

  protected static class MeteorDebuggableProgramParametersPanel extends CommonProgramParametersPanel {

    private LabeledComponent<? extends JComponent> exePathComponent;
    private TextFieldWithHistoryWithBrowseButton exePathTextField;
    private final Project myProject;


    public MeteorDebuggableProgramParametersPanel(@NotNull Project project) {
      super(false);
      myProject = project;
      init();
    }

    @NotNull
    @Override
    protected Project getProject() {
      return myProject;
    }

    @Override
    protected void addComponents() {
      exePathTextField = MeteorUIUtil.createTextField(myProject);
      exePathComponent = LabeledComponent.create(exePathTextField, MeteorBundle.message("settings.meteor.configurable.executable"));
      exePathComponent.setLabelLocation(BorderLayout.WEST);
      add(exePathComponent);

      super.addComponents();
    }

    @Override
    public void setAnchor(JComponent anchor) {
      super.setAnchor(anchor);
      exePathComponent.setAnchor(anchor);
    }

    @Override
    public void applyTo(CommonProgramRunConfigurationParameters c) {
      super.applyTo(c);

      MeteorRunConfiguration configuration = (MeteorRunConfiguration)c;
      configuration.setExePath(exePathTextField.getText());
    }

    @Override
    public void reset(CommonProgramRunConfigurationParameters c) {
      super.reset(c);
      MeteorRunConfiguration configuration = (MeteorRunConfiguration)c;
      MeteorUIUtil.setValue(configuration.getExePath(), exePathTextField);
      setWorkingDirectory(PathUtil.toSystemDependentName(configuration.getEffectiveWorkingDirectory()));
    }
  }
}
