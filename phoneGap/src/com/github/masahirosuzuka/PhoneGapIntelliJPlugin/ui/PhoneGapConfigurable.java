package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUIUtil;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * PhoneGapSettingDialog.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/05/05.
 */
public class PhoneGapConfigurable implements Configurable {

  private TextFieldWithHistoryWithBrowseButton myExecutablePath;

  private final PhoneGapSettings mySettings = PhoneGapSettings.getInstance();
  private UIController myUIController;
  private Project myProject;

  public PhoneGapConfigurable(Project project) {
    myProject = project;
  }

  private class UIController {

    public void reset(PhoneGapSettings.State state) {
      PhoneGapUIUtil.setExecutablePath(myExecutablePath, state.getExecutablePath());
    }

    public boolean isModified() {
      return !getState().equals(mySettings.getState());
    }

    private PhoneGapSettings.State getState() {
      PhoneGapSettings.State state = new PhoneGapSettings.State();
      state.executablePath = myExecutablePath.getText();
      return state;
    }
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "PhoneGap/Cordova";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return "phoneGap/cordova";
  }

  @Nullable
  @Override
  public JComponent createComponent() {

    if (myUIController == null) {
      myExecutablePath = PhoneGapUIUtil.createPhoneGapExecutableTextField(myProject);
      myUIController = new UIController();
      myUIController.reset(mySettings.getState());
    }

    JPanel panel = FormBuilder.createFormBuilder().addLabeledComponent("PhoneGap/Cordova executable path:", myExecutablePath).getPanel();
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(panel, BorderLayout.NORTH);
    return wrapper;
  }

  @Override
  public boolean isModified() {
    return myUIController.isModified();
  }

  @Override
  public void apply() throws ConfigurationException {
    mySettings.loadState(myUIController.getState());
  }

  @Override
  public void reset() {
    myUIController.reset(mySettings.getState());
  }

  @Override
  public void disposeUIResources() {
  }
}
