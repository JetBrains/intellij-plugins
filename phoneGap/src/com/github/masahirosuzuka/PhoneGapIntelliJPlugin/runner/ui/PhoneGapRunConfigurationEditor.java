package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapTargets;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.PhoneGapRunConfiguration;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.*;

public class PhoneGapRunConfigurationEditor extends SettingsEditor<PhoneGapRunConfiguration> {

  public static final ArrayList<String> COMMANDS_LIST = ContainerUtil.newArrayList(COMMAND_EMULATE, COMMAND_RUN, COMMAND_SERVE);
  public static final ArrayList<String> COMMANDS_PHONEGAP_LIST =
    ContainerUtil.newArrayList(COMMAND_EMULATE, COMMAND_RUN, COMMAND_SERVE, COMMAND_REMOTE_BUILD, COMMAND_REMOTE_RUN);

  public static final String PLATFORM_ANDROID = "android";
  public static final String PLATFORM_IOS = "ios";
  private final PhoneGapTargets myTargetsProvider;
  private TextFieldWithHistoryWithBrowseButton myExecutablePathField;
  private TextFieldWithHistoryWithBrowseButton myWorkDirField;
  private ComboBox myPlatformField;
  private ComboBox myCommand;
  private final Project myProject;
  private JBCheckBox myHasTarget;
  private TextFieldWithHistory myTarget;
  private JBTextField myExtraArgsTextField;
  public PhoneGapRunConfigurationEditor(Project project) {
    myProject = project;
    myTargetsProvider = new PhoneGapTargets(project);
  }

  @Override
  protected void resetEditorFrom(PhoneGapRunConfiguration s) {

    String executable = s.getExecutable();
    PhoneGapUtil.setFieldWithHistoryWithBrowseButtonPath(myExecutablePathField,
                                                         !StringUtil.isEmpty(executable)
                                                         ? executable
                                                         : PhoneGapSettings.getInstance().getExecutablePath());
    String item = getPlatformsMap().get(s.getPlatform());
    if (item != null) {
      myPlatformField.setSelectedItem(item);
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
    PhoneGapUtil.setTextFieldWithHistory(myTarget, s.getTarget());
    fillTargetValuesAndSetVisible();
  }

  private void fillTargetValuesAndSetVisible() {
    setTargetFieldsEnable();

    if (myTarget.isEnabled()) {
      final String platform = getPlatformAsCodeFromField();
      final String command = (String)myCommand.getSelectedItem();
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          final String currentText = myTarget.getText();

          final List<String> targets = ContainerUtil.newArrayList(myTargetsProvider.getTargets(platform, command));
          if (!StringUtil.isEmpty(currentText) && !targets.contains(currentText)) {
            targets.add(currentText);
          }

          UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
              if (platform == getPlatformAsCodeFromField()) {
                myTarget.setHistory(targets);
                PhoneGapUtil.setTextFieldWithHistory(myTarget, currentText);
              }
            }
          });
        }
      });
    }
  }

  private String getPlatformAsCodeFromField() {
    return ContainerUtil.getFirstItem(getPlatformsMap().getKeysByValue((String)myPlatformField.getSelectedItem()));
  }

  @Override
  protected void applyEditorTo(PhoneGapRunConfiguration s) throws ConfigurationException {
    s.setExecutable(myExecutablePathField.getText());
    String item = (String)myPlatformField.getSelectedItem();
    s.setPlatform(ContainerUtil.getFirstItem(getPlatformsMap().getKeysByValue(item)));
    s.setCommand((String)myCommand.getSelectedItem());
    s.setWorkDir(myWorkDirField.getText());
    s.setTarget(myTarget.getText());
    s.setHasTarget(myHasTarget.isSelected());
    s.setExtraArgs(myExtraArgsTextField.getText());
  }


  @NotNull
  @Override
  protected JComponent createEditor() {

    myExecutablePathField = PhoneGapUtil.createPhoneGapExecutableTextField(myProject);
    myWorkDirField = PhoneGapUtil.createPhoneGapWorkingDirectoryField(myProject);
    myPlatformField = new ComboBox();
    myCommand = new ComboBox();
    myHasTarget = new JBCheckBox("Specify target");
    myTarget = new TextFieldWithHistory();
    myExtraArgsTextField = new JBTextField(15);
    myCommand.setMinimumAndPreferredWidth(200);
    myPlatformField.setMinimumAndPreferredWidth(200);
    addPlatformItems(myPlatformField);
    myTarget.setMinimumAndPreferredWidth(myPlatformField.getPreferredSize().width);

    setListenerForPlatforms();
    setListenerForCommand();
    setListenerForHasTarget();
    setListenerForExecutablePath();
    setCommandList();

    return FormBuilder.createFormBuilder()
      .addLabeledComponent(PhoneGapBundle.message("phonegap.conf.executable.name"), myExecutablePathField)
      .addLabeledComponent(PhoneGapBundle.message("phonegap.conf.work.dir.name"), myWorkDirField)
      .addLabeledComponent("Command:", myCommand)
      .addLabeledComponent("Platform:", myPlatformField)
      .addLabeledComponent(PhoneGapBundle.message("phonegap.conf.extra.args.name"), myExtraArgsTextField)
      .addLabeledComponent(myHasTarget, myTarget)
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
        fillTargetValuesAndSetVisible();
      }
    });
  }

  private void setListenerForExecutablePath() {
    final Ref<String> prevExecutablePathRef = Ref.create(StringUtil.notNullize(myExecutablePathField.getText()));
    final JTextField textEditor = myExecutablePathField.getChildComponent().getTextEditor();
    textEditor.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        String executablePath = StringUtil.notNullize(textEditor.getText());
        if (StringUtil.isEmpty(executablePath)) return;

        String prevExecutablePath = prevExecutablePathRef.get();
        if (!prevExecutablePath.equals(executablePath)) {
          setCommandList();
          setTargetFieldsEnable();
          prevExecutablePathRef.set(executablePath);
        }
      }
    });
  }

  private static void addPlatformItems(ComboBox box) {
    addItems(box, ContainerUtil.newArrayList(getPlatformsMap().values()));
  }

  @SuppressWarnings("unchecked")
  private static void addItems(ComboBox box, List<String> list) {
    ContainerUtil.sort(list);
    for (String s : list) {
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

  private void replaceCommandList(String selectedItem, List<String> commandList) {
    if (myCommand.getItemCount() == commandList.size()) return;

    myCommand.removeAllItems();
    addItems(myCommand, commandList);

    if (commandList.contains(selectedItem)) {
      myCommand.setSelectedItem(selectedItem);
    }
  }

  private static BidirectionalMap<String, String> getPlatformsMap() {
    BidirectionalMap<String, String> map = new BidirectionalMap<String, String>();
    map.put(PLATFORM_ANDROID, "Android");
    map.put(PLATFORM_IOS, "iOS");

    return map;
  }

  private void setTargetFieldsEnable() {
    boolean isNotServe = !COMMAND_SERVE.equals(myCommand.getSelectedItem());

    myHasTarget.setEnabled(isNotServe && !isPhoneGap());
    myTarget.setEnabled(isNotServe && myHasTarget.isSelected() && !isPhoneGap());
  }

  private boolean isPhoneGap() {
    return Boolean.TRUE.equals(isPhoneGapExecutableByPath(myExecutablePathField.getText()));
  }
}
