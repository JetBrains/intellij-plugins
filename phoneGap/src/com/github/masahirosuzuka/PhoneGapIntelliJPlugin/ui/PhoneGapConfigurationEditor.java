package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUIUtil;
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

public class PhoneGapConfigurationEditor extends SettingsEditor<PhoneGapRunConfiguration> {

  private TextFieldWithHistoryWithBrowseButton myExecutablePathField;
  private ComboBox myPlatformField;
  private final Project myProject;

  public PhoneGapConfigurationEditor(Project project) {
    myProject = project;
  }

  @Override
  protected void resetEditorFrom(PhoneGapRunConfiguration s) {

    String executable = s.getExecutable();
    PhoneGapUIUtil.setExecutablePath(myExecutablePathField,
                                     !StringUtil.isEmpty(executable) ? executable : PhoneGapSettings.getInstance().getExecutablePath());
    String item = getCommandsMap().get(s.getPlatform());
    if (item != null) {
      myPlatformField.setSelectedItem(item);
    }
  }

  @Override
  protected void applyEditorTo(PhoneGapRunConfiguration s) throws ConfigurationException {
    s.setExecutable(myExecutablePathField.getText());
    String item = (String)myPlatformField.getSelectedItem();
    s.setPlatform(ContainerUtil.getFirstItem(getCommandsMap().getKeysByValue(item)));
    s.setCommand("run");
  }


  @NotNull
  @Override
  protected JComponent createEditor() {
    myExecutablePathField = PhoneGapUIUtil.createPhoneGapExecutableTextField(myProject);
    myPlatformField = new ComboBox();
    addItems(myPlatformField);

    return FormBuilder.createFormBuilder()
      .addLabeledComponent("Phonegap/Cordova executable path:", myExecutablePathField)
      .addLabeledComponent("Platform:", myPlatformField)
      .getPanel();
  }

  @SuppressWarnings("unchecked")
  private static void addItems(ComboBox box) {
    ArrayList<String> list = ContainerUtil.newArrayList(getCommandsMap().values());
    ContainerUtil.sort(list);
    for (String s : list) {
      box.addItem(s);
    }

    box.setSelectedIndex(0);
  }

  private static BidirectionalMap<String, String> getCommandsMap() {
    BidirectionalMap<String, String> map = new BidirectionalMap<String, String>();
    map.put("android", "Android");
    map.put("ios", "iOS");
    map.put("Setup http server", "Ripple");

    return map;
  }
}
