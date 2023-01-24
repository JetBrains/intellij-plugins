// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapTargets;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.PhoneGapRunConfiguration;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ReloadableComboBoxPanel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.*;

public class PhoneGapRunConfigurationEditor extends SettingsEditor<PhoneGapRunConfiguration> {

  public static final List<String> COMMANDS_LIST =
    List.of(COMMAND_EMULATE, COMMAND_RUN, COMMAND_PREPARE, COMMAND_SERVE);
  public static final List<String> COMMANDS_PHONEGAP_LIST =
    List.of(COMMAND_EMULATE, COMMAND_RUN, COMMAND_PREPARE, COMMAND_SERVE, COMMAND_REMOTE_BUILD, COMMAND_REMOTE_RUN);

  public static final String PLATFORM_ANDROID = "android";
  public static final String PLATFORM_IOS = "ios";
  public static final String PLATFORM_AMAZON_FIREOS = "amazon-fireos";
  public static final String PLATFORM_BLACKBERRY_10 = "blackberry10";
  public static final String PLATFORM_UBUNTU = "ubuntu";
  public static final String PLATFORM_WP_8 = "wp8";
  public static final String PLATFORM_WINDOWS_8 = "windows8";
  public static final String PLATFORM_WINDOWS = "windows";
  public static final String PLATFORM_FIREFOXOS = "firefoxos";
  public static final String PLATFORM_BROWSER = "browser";


  private TextFieldWithHistoryWithBrowseButton myExecutablePathField;
  private TextFieldWithHistoryWithBrowseButton myWorkDirField;
  private ComboBoxWithMoreOption myPlatformField;
  private ComboBox<String> myCommand;
  private final Project myProject;
  private JBCheckBox myHasTarget;
  private PhoneGapTargetsPanel myTarget;
  private JBTextField myExtraArgsTextField;
  private EnvironmentVariablesTextFieldWithBrowseButton myEnvComponent;

  public PhoneGapRunConfigurationEditor(Project project) {
    myProject = project;
  }

  @Override
  protected void resetEditorFrom(@NotNull PhoneGapRunConfiguration s) {

    String executable = s.getExecutable();
    PhoneGapUtil.setFieldWithHistoryWithBrowseButtonPath(myExecutablePathField,
                                                         !StringUtil.isEmpty(executable)
                                                         ? executable
                                                         : PhoneGapSettings.getInstance().getExecutablePath());
    String item = s.getNormalizedPlatform();
    if (item != null) {
      //call to lower for back compatibility (old settings store 'iOS' and 'Android')
      myPlatformField.setSelectedWithExtend(item);
    }
    String command = s.getCommand();
    if (command != null) {
      myCommand.setSelectedItem(command);
    }

    String workDir = s.getWorkDir();
    PhoneGapUtil.setFieldWithHistoryWithBrowseButtonPath(myWorkDirField,
                                                         !StringUtil.isEmpty(workDir)
                                                         ? workDir
                                                         : PhoneGapSettings.getInstance().getWorkingDirectory(myProject));

    boolean hasTarget = s.hasTarget();

    myHasTarget.setSelected(hasTarget);
    myExtraArgsTextField.setText(s.getExtraArgs());
    myEnvComponent.setEnvs(s.getEnvs());
    myEnvComponent.setPassParentEnvs(s.isPassParent());
    PhoneGapUtil.setTextFieldWithHistory(myTarget.getTargetsField(), s.getTarget());
    fillTargetValuesAndSetVisible(false);
  }

  private void fillTargetValuesAndSetVisible() {
    fillTargetValuesAndSetVisible(true);
  }

  private void fillTargetValuesAndSetVisible(boolean resetField) {
    setTargetFieldsEnable();

    if (resetField) {
      myTarget.getTargetsField().setHistory(ContainerUtil.emptyList());
      myTarget.getTargetsField().setText("");
    }

    myTarget.reloadValuesInBackground();
  }

  private String getPlatformAsCodeFromField() {
    return (String)myPlatformField.getSelectedItem();
  }

  @Override
  protected void applyEditorTo(@NotNull PhoneGapRunConfiguration persistentSettings) throws ConfigurationException {
    persistentSettings.setExecutable(myExecutablePathField.getText());
    String item = (String)myPlatformField.getSelectedItem();
    persistentSettings.setPlatform(item);
    persistentSettings.setCommand((String)myCommand.getSelectedItem());
    persistentSettings.setWorkDir(myWorkDirField.getText());
    persistentSettings.setTarget(myTarget.getSelectedValue());
    persistentSettings.setHasTarget(myHasTarget.isSelected());
    persistentSettings.setExtraArgs(myExtraArgsTextField.getText());
    persistentSettings.setEnvs(myEnvComponent.getEnvs());
    persistentSettings.setPassParent(myEnvComponent.isPassParentEnvs());
  }


  @NotNull
  @Override
  protected JComponent createEditor() {

    myExecutablePathField = PhoneGapUtil.createPhoneGapExecutableTextField(myProject);
    myWorkDirField = PhoneGapUtil.createPhoneGapWorkingDirectoryField(myProject);
    myPlatformField = new ComboBoxWithMoreOption(getDefaultPlatforms(), getNonDefaultPlatforms());
    myCommand = new ComboBox();

    myEnvComponent = new EnvironmentVariablesTextFieldWithBrowseButton();
    myEnvComponent.setPassParentEnvs(true);

    myHasTarget = new JBCheckBox(PhoneGapBundle.message("checkbox.specify.target"));
    myTarget = new PhoneGapTargetsPanel();
    myExtraArgsTextField = new JBTextField(15);
    myCommand.setMinimumAndPreferredWidth(200);
    myPlatformField.setMinimumAndPreferredWidth(200);
    myTarget.getTargetsField().setMinimumAndPreferredWidth(myPlatformField.getPreferredSize().width);
    myTarget.setDataProvider(new ReloadableComboBoxPanel.DataProvider<>() {

      @NotNull
      @Override
      public Set<String> getCachedValues() {
        return Collections.emptySet();
      }

      @Override
      public void updateValuesAsynchronously() {
        if (!myTarget.isEnabled()) {
          processEmpty();
          return;
        }

        final String platform = getPlatformAsCodeFromField();
        final String command = (String)myCommand.getSelectedItem();
        final PhoneGapTargets targetsProvider = PhoneGapTargets.createTargetsList(myProject, platform);
        if (targetsProvider == null) {
          processEmpty();
          return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
          final String currentText = myTarget.getTargetsField().getText();
          final Set<String> targets = new LinkedHashSet<>(PhoneGapTargets.listTargets(targetsProvider, command));
          if (!StringUtil.isEmpty(currentText)) {
            targets.add(currentText);
          }

          UIUtil.invokeLaterIfNeeded(() -> myTarget.onUpdateValues(targets));
        });
      }

      private void processEmpty() {
        myTarget.onUpdateValues(Collections.emptySet());
      }
    });

    setListenerForPlatforms();
    setListenerForCommand();
    setListenerForHasTarget();
    setListenerForExecutablePath();
    setCommandList();

    return FormBuilder.createFormBuilder()
      .addLabeledComponent(PhoneGapBundle.message("phonegap.conf.executable.name"), myExecutablePathField)
      .addLabeledComponent(PhoneGapBundle.message("phonegap.conf.work.dir.name"), myWorkDirField)
      .addLabeledComponent(ExecutionBundle.message("environment.variables.component.title"), myEnvComponent)
      .addLabeledComponent(PhoneGapBundle.message("phonegap.run.label.command"), myCommand)
      .addLabeledComponent(PhoneGapBundle.message("phonegap.run.label.platform"), myPlatformField)
      .addLabeledComponent(PhoneGapBundle.message("phonegap.conf.extra.args.name"), myExtraArgsTextField)
      .addLabeledComponent(myHasTarget, myTarget.getMainPanel())
      .getPanel();
  }

  private void setListenerForPlatforms() {
    myPlatformField.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        fillTargetValuesAndSetVisible();
      }
    });
  }

  private void setListenerForHasTarget() {
    myHasTarget.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        fillTargetValuesAndSetVisible();
      }
    });
  }

  private void setListenerForCommand() {
    myCommand.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          fillTargetValuesAndSetVisible();
        }
      }
    });
  }

  private void setListenerForExecutablePath() {
    final Ref<String> prevExecutablePathRef = Ref.create(StringUtil.notNullize(myExecutablePathField.getText()));
    final JTextField textEditor = myExecutablePathField.getChildComponent().getTextEditor();
    textEditor.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        String executablePath = StringUtil.notNullize(textEditor.getText());
        if (StringUtil.isEmpty(executablePath)) return;

        String prevExecutablePath = prevExecutablePathRef.get();
        if (!prevExecutablePath.equals(executablePath)) {
          setCommandList();
          prevExecutablePathRef.set(executablePath);
        }
      }
    });
  }


  private static void addItems(@NotNull ComboBox<String> box, @NotNull List<@NlsSafe String> list) {
    for (@NlsSafe String s : list) {
      box.addItem(s);
    }

    box.setSelectedIndex(0);
  }

  private void setCommandList() {

    String selectedItem = (String)myCommand.getSelectedItem();
    if (isPhoneGap()) {
      replaceCommandList(selectedItem, COMMANDS_PHONEGAP_LIST);
    }
    else {
      replaceCommandList(selectedItem, COMMANDS_LIST);
    }
  }

  private void replaceCommandList(@NlsSafe String selectedItem, List<@NlsSafe String> commandList) {
    if (myCommand.getItemCount() == commandList.size()) return;

    myCommand.removeAllItems();
    addItems(myCommand, commandList);

    if (selectedItem != null && commandList.contains(selectedItem)) {
      myCommand.setSelectedItem(selectedItem);
    }
  }


  private static Set<String> getDefaultPlatforms() {
    LinkedHashSet<String> set = new LinkedHashSet<>();
    set.add(PLATFORM_ANDROID);
    set.add(PLATFORM_IOS);
    set.add(PLATFORM_BROWSER);
    return set;
  }

  private static Set<String> getNonDefaultPlatforms() {
    LinkedHashSet<String> set = new LinkedHashSet<>();
    set.add(PLATFORM_AMAZON_FIREOS);
    set.add(PLATFORM_FIREFOXOS);
    set.add(PLATFORM_BLACKBERRY_10);
    set.add(PLATFORM_UBUNTU);
    set.add(PLATFORM_WP_8);
    set.add(PLATFORM_WINDOWS);
    set.add(PLATFORM_WINDOWS_8);
    return set;
  }

  private void setTargetFieldsEnable() {
    boolean isNotServe = !COMMAND_SERVE.equals(myCommand.getSelectedItem());

    myHasTarget.setEnabled(isNotServe);
    myTarget.setEnabled(isNotServe && myHasTarget.isSelected());
  }

  private boolean isPhoneGap() {
    return Boolean.TRUE.equals(isPhoneGapExecutableByPath(myExecutablePathField.getText()));
  }
}
