package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.PhoneGapRunConfiguration;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.COMMAND_EMULATE;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.COMMAND_RUN;

public class PhoneGapRunConfigurationEditor extends SettingsEditor<PhoneGapRunConfiguration> {

  public static final ArrayList<String> COMMANDS_LIST = ContainerUtil.newArrayList(COMMAND_EMULATE, COMMAND_RUN);
  private TextFieldWithHistoryWithBrowseButton myExecutablePathField;
  private ComboBox myPlatformField;
  private ComboBox myCommand;
  private final Project myProject;

  public PhoneGapRunConfigurationEditor(Project project) {
    myProject = project;
  }

  @Override
  protected void resetEditorFrom(PhoneGapRunConfiguration s) {

    String executable = s.getExecutable();
    PhoneGapUtil.setExecutablePath(myExecutablePathField,
                                   !StringUtil.isEmpty(executable) ? executable : PhoneGapSettings.getInstance().getExecutablePath());
    String item = getPlatformsMap().get(s.getPlatform());
    if (item != null) {
      myPlatformField.setSelectedItem(item);
    }
    String command = s.getCommand();
    if (command != null && COMMANDS_LIST.contains(command)) {
      myCommand.setSelectedItem(command);
    }

  }

  @Override
  protected void applyEditorTo(PhoneGapRunConfiguration s) throws ConfigurationException {
    s.setExecutable(myExecutablePathField.getText());
    String item = (String)myPlatformField.getSelectedItem();
    s.setPlatform(ContainerUtil.getFirstItem(getPlatformsMap().getKeysByValue(item)));
    s.setCommand((String)myCommand.getSelectedItem());
  }


  @NotNull
  @Override
  protected JComponent createEditor() {
    myExecutablePathField = PhoneGapUtil.createPhoneGapExecutableTextField(myProject);
    myPlatformField = new ComboBox();
    myCommand = new ComboBox();

    addPlatformItems(myPlatformField);
    addCommandItems(myCommand);

    return FormBuilder.createFormBuilder()
      .addLabeledComponent("PhoneGap/Cordova executable path:", myExecutablePathField)
      .addLabeledComponent("Command:", myCommand)
      .addLabeledComponent("Platform:", myPlatformField)
      .getPanel();
  }

  private static void addCommandItems(ComboBox box) {
    addItems(box, COMMANDS_LIST);
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

  private static BidirectionalMap<String, String> getPlatformsMap() {
    BidirectionalMap<String, String> map = new BidirectionalMap<String, String>();
    map.put("android", "Android");
    map.put("ios", "iOS");

    return map;
  }
}
