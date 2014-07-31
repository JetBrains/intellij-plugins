package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUIUtil;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.plugins.PhoneGapPluginsView;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
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
  private PhoneGapPluginsView phoneGapPluginsView;
  private Project myProject;
  private JPanel myWrapper;
  private JBLabel myVersion;

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
    return "settings.phonegap";
  }

  @Nullable
  @Override
  public JComponent createComponent() {

    if (myWrapper == null) {
      myExecutablePath = PhoneGapUIUtil.createPhoneGapExecutableTextField(myProject);
      myVersion = new JBLabel();
      myUIController = new UIController();
      myUIController.reset(mySettings.getState());
      phoneGapPluginsView = new PhoneGapPluginsView(myProject);
      JPanel panel = FormBuilder.createFormBuilder()
        .addLabeledComponent("PhoneGap/Cordova executable path:", myExecutablePath)
        .addLabeledComponent("Phonegap/Cordova version:", myVersion)
        .addComponent(phoneGapPluginsView.getPanel()).getPanel();
      myWrapper = new JPanel(new BorderLayout());
      myWrapper.add(panel, BorderLayout.NORTH);
      setUpListener();
      if (!StringUtil.isEmpty(myExecutablePath.getText())) {
        PhoneGapCommandLine line = getCommandLine();
        phoneGapPluginsView.setupService(line);
        setVersion(line);
      }
    }

    return myWrapper;
  }

  private PhoneGapCommandLine getCommandLine() {
    return new PhoneGapCommandLine(myExecutablePath.getText(), myProject.getBasePath());
  }

  private void setVersion(PhoneGapCommandLine line) {
    myVersion.setText(line.isCorrectExecutable() ? line.version() : "");
  }

  public void setUpListener() {
    final JTextField textField = myExecutablePath.getChildComponent().getTextEditor();
    final Ref<String> prevExecutablePathRef = Ref.create(StringUtil.notNullize(textField.getText()));
    textField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        String executablePath = StringUtil.notNullize(textField.getText());
        String prevExecutablePath = prevExecutablePathRef.get();
        if (!prevExecutablePath.equals(executablePath)) {
          PhoneGapCommandLine line = getCommandLine();
          phoneGapPluginsView.setupService(line);
          setVersion(line);
          prevExecutablePathRef.set(executablePath);
        }
      }
    });
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
