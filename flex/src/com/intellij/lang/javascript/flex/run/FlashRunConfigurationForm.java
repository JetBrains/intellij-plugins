package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionBundle;
import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.InfoFromConfigFile;

public class FlashRunConfigurationForm extends SettingsEditor<FlashRunConfiguration> {

  // Application Main Class must inherit from this class
  public static final String SPRITE_CLASS_NAME = "flash.display.Sprite";
  // The base class for ActionScript-based dynamically-loadable modules
  public static final String MODULE_BASE_CLASS_NAME = "mx.modules.ModuleBase";
  
  private JPanel myMainPanel;
  private BCCombo myBCCombo;

  private JCheckBox myOverrideMainClassCheckBox;
  private JSClassChooserDialog.PublicInheritor myMainClassFilter;
  private JSReferenceEditor myMainClassComponent;
  private JLabel myOutputFileNameLabel;
  private JTextField myOutputFileNameTextField;

  private JPanel myLaunchPanel;
  private JRadioButton myBCOutputRadioButton;
  private JLabel myBCOutputLabel;
  private JRadioButton myURLRadioButton;
  private JTextField myURLTextField;

  private JPanel myWebOptionsPanel;
  private TextFieldWithBrowseButton myLauncherParametersTextWithBrowse;
  private JCheckBox myRunTrustedCheckBox;

  private JPanel myDesktopOptionsPanel;
  private RawCommandLineEditor myAdlOptionsEditor;
  private RawCommandLineEditor myAirProgramParametersEditor;

  private JPanel myMobileRunPanel;
  private JRadioButton myOnEmulatorRadioButton;
  private JRadioButton myOnAndroidDeviceRadioButton;
  private JRadioButton myOnIOSDeviceRadioButton;

  private JComboBox myEmulatorCombo;
  private JPanel myEmulatorScreenSizePanel;
  private JTextField myScreenWidth;
  private JTextField myScreenHeight;
  private JTextField myFullScreenWidth;
  private JTextField myFullScreenHeight;

  private JPanel myMobileOptionsPanel;
  private JPanel myDebugTransportPanel;
  private JLabel myDebugOverLabel;
  private JRadioButton myDebugOverNetworkRadioButton;
  private JRadioButton myDebugOverUSBRadioButton;
  private JTextField myUsbDebugPortTextField;
  private JBLabel myAdlOptionsLabel;
  private RawCommandLineEditor myEmulatorAdlOptionsEditor;

  private final Project myProject;

  private LauncherParameters myLauncherParameters;

  public FlashRunConfigurationForm(final Project project) {
    myProject = project;

    initBCCombo();
    initMainClassRelatedControls();
    initRadioButtons();
    initLaunchWithTextWithBrowse();
    initMobileControls();
  }

  private void initBCCombo() {
    myBCCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateMainClassField();
        updateControls();
      }
    });
  }

  private void updateMainClassField() {
    final Module module = myBCCombo.getModule();
    if (module != null) {
      myMainClassComponent.setScope(GlobalSearchScope.moduleScope(module));
      myMainClassFilter.setModule(module);
      myMainClassComponent.setChooserBlockingMessage(null);
    }
    else {
      myMainClassComponent.setScope(GlobalSearchScope.EMPTY_SCOPE);
      myMainClassFilter.setModule(null);
      myMainClassComponent.setChooserBlockingMessage("Build configuration not selected");
    }
  }

  private void initMainClassRelatedControls() {
    myOverrideMainClassCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (myOverrideMainClassCheckBox.isSelected()) {
          updateOutputFileName(myOutputFileNameTextField, false);
        }

        updateControls();

        if (myMainClassComponent.isEnabled()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myMainClassComponent.getChildComponent(), true);
        }
      }
    });

    myMainClassComponent.addDocumentListener(new DocumentAdapter() {
      public void documentChanged(final DocumentEvent e) {
        final String shortName = StringUtil.getShortName(myMainClassComponent.getText().trim());
        if (!shortName.isEmpty()) {
          myOutputFileNameTextField.setText(shortName + ".swf");
        }
      }
    });

    myOutputFileNameTextField.getDocument().addDocumentListener(new com.intellij.ui.DocumentAdapter() {
      protected void textChanged(final javax.swing.event.DocumentEvent e) {
        final FlexIdeBuildConfiguration bc = myBCCombo.getBC();
        if (bc != null && bc.getTargetPlatform() == TargetPlatform.Web) {
          updateBCOutputLabel(bc);
        }
      }
    });
  }

  private void initRadioButtons() {
    myBCOutputRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });

    myURLRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();

        if (myURLTextField.isEnabled()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myURLTextField, true);
        }
      }
    });
  }

  private void initLaunchWithTextWithBrowse() {
    myLauncherParametersTextWithBrowse.getTextField().setEditable(false);
    myLauncherParametersTextWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final FlexLauncherDialog dialog = new FlexLauncherDialog(myProject, myLauncherParameters);
        dialog.show();
        if (dialog.isOK()) {
          myLauncherParameters = dialog.getLauncherParameters();
          updateControls();
        }
      }
    });
  }

  private void initMobileControls() {
    initEmulatorRelatedControls();
    myOnIOSDeviceRadioButton.setVisible(false); // until supported

    final ActionListener debugTransportListener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateDebugTransportRelatedControls();
      }
    };
    myDebugOverNetworkRadioButton.addActionListener(debugTransportListener);
    myDebugOverUSBRadioButton.addActionListener(debugTransportListener);
  }

  private void initEmulatorRelatedControls() {
    myEmulatorCombo.setModel(new DefaultComboBoxModel(AirMobileRunnerParameters.Emulator.values()));

    myEmulatorCombo.setRenderer(new ListCellRendererWrapper<AirMobileRunnerParameters.Emulator>(myEmulatorCombo.getRenderer()) {
      @Override
      public void customize(JList list, AirMobileRunnerParameters.Emulator value, int index, boolean selected, boolean hasFocus) {
        setText(value.name);
      }
    });

    myEmulatorCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateEmulatorRelatedControls();
      }
    });

    final ActionListener targetDeviceListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateControls();

        if (myOnEmulatorRadioButton.isSelected()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myEmulatorCombo, true);
        }
      }
    };

    myOnEmulatorRadioButton.addActionListener(targetDeviceListener);
    myOnAndroidDeviceRadioButton.addActionListener(targetDeviceListener);
    myOnIOSDeviceRadioButton.addActionListener(targetDeviceListener);
  }

  private void updateControls() {
    final FlexIdeBuildConfiguration bc = myBCCombo.getBC();
    final Module module = myBCCombo.getModule();

    final boolean overrideMainClass = myOverrideMainClassCheckBox.isSelected();
    myMainClassComponent.setEnabled(overrideMainClass);
    myOutputFileNameLabel.setEnabled(overrideMainClass);
    myOutputFileNameTextField.setEnabled(overrideMainClass);

    if (!overrideMainClass && bc != null && module != null) {
      InfoFromConfigFile info = InfoFromConfigFile.DEFAULT;

      final String configFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
      if (!configFilePath.isEmpty()) {
        info = FlexCompilerConfigFileUtil.getInfoFromConfigFile(module, configFilePath);
      }

      myMainClassComponent.setText(StringUtil.notNullize(info.mainClass, bc.getMainClass()));
      myOutputFileNameTextField.setText(StringUtil.notNullize(info.outputFileName, bc.getOutputFileName()));
    }

    final boolean web = bc != null && bc.getTargetPlatform() == TargetPlatform.Web;
    final boolean desktop = bc != null && bc.getTargetPlatform() == TargetPlatform.Desktop;
    final boolean mobile = bc != null && bc.getTargetPlatform() == TargetPlatform.Mobile;

    myLaunchPanel.setVisible(web);
    myWebOptionsPanel.setVisible(web);
    myDesktopOptionsPanel.setVisible(desktop);
    myMobileRunPanel.setVisible(mobile);
    myMobileOptionsPanel.setVisible(mobile);

    if (web) {
      updateBCOutputLabel(bc);

      myURLTextField.setEnabled(myURLRadioButton.isSelected());

      myLauncherParametersTextWithBrowse.getTextField().setText(myLauncherParameters.getPresentableText());
      myRunTrustedCheckBox.setEnabled(!myURLRadioButton.isSelected());
    }

    if (mobile) {
      final boolean runOnEmulator = myOnEmulatorRadioButton.isSelected();
      myEmulatorCombo.setEnabled(runOnEmulator);
      UIUtil.setEnabled(myEmulatorScreenSizePanel, runOnEmulator, true);
      myAdlOptionsLabel.setEnabled(runOnEmulator);
      myEmulatorAdlOptionsEditor.setEnabled(runOnEmulator);

      if (runOnEmulator) {
        updateEmulatorRelatedControls();
      }

      updateDebugTransportRelatedControls();
    }
  }

  private void updateBCOutputLabel(final FlexIdeBuildConfiguration bc) {
    if (bc.getOutputType() == OutputType.Application || myOverrideMainClassCheckBox.isSelected()) {
      String bcOutput = myOverrideMainClassCheckBox.isSelected() ? myOutputFileNameTextField.getText().trim() : bc.getOutputFileName();
      if (!bcOutput.isEmpty() && bc.isUseHtmlWrapper()) {
        bcOutput += " via HTML wrapper";
      }
      myBCOutputLabel.setText(bcOutput);
    }
    else {
      myBCOutputLabel.setText("");
    }
  }

  private void updateEmulatorRelatedControls() {
    final AirMobileRunnerParameters.Emulator emulator = (AirMobileRunnerParameters.Emulator)myEmulatorCombo.getSelectedItem();
    if (emulator.adlAlias == null) {
      myScreenWidth.setEditable(true);
      myScreenHeight.setEditable(true);
      myFullScreenWidth.setEditable(true);
      myFullScreenHeight.setEditable(true);
    }
    else {
      myScreenWidth.setEditable(false);
      myScreenHeight.setEditable(false);
      myFullScreenWidth.setEditable(false);
      myFullScreenHeight.setEditable(false);
      myScreenWidth.setText(String.valueOf(emulator.screenWidth));
      myScreenHeight.setText(String.valueOf(emulator.screenHeight));
      myFullScreenWidth.setText(String.valueOf(emulator.fullScreenWidth));
      myFullScreenHeight.setText(String.valueOf(emulator.fullScreenHeight));
    }
  }

  private void updateDebugTransportRelatedControls() {
    final boolean enabled = !myOnEmulatorRadioButton.isSelected();
    myDebugOverLabel.setEnabled(enabled);
    UIUtil.setEnabled(myDebugTransportPanel, enabled, true);
    if (enabled) {
      myUsbDebugPortTextField.setEnabled(myDebugOverUSBRadioButton.isSelected());
    }
  }

  private void createUIComponents() {
    myBCCombo = new BCCombo(myProject);
    myMainClassFilter = new JSClassChooserDialog.PublicInheritor(myProject, SPRITE_CLASS_NAME, null, true);
    myMainClassComponent = JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE, null,
                                                          myMainClassFilter, ExecutionBundle.message("choose.main.class.dialog.title"));
  }

  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  protected void resetEditorFrom(final FlashRunConfiguration configuration) {
    final FlashRunnerParameters params = configuration.getRunnerParameters();
    myLauncherParameters = params.getLauncherParameters().clone(); // must be before myBCsCombo.setModel()

    myBCCombo.resetFrom(params);

    myOverrideMainClassCheckBox.setSelected(params.isOverrideMainClass());
    if (params.isOverrideMainClass()) {
      myMainClassComponent.setText(params.getOverriddenMainClass());
      myOutputFileNameTextField.setText(params.getOverriddenOutputFileName());
    }

    myBCOutputRadioButton.setSelected(!params.isLaunchUrl());
    myURLRadioButton.setSelected(params.isLaunchUrl());
    myURLTextField.setText(params.getUrl());

    myRunTrustedCheckBox.setSelected(params.isRunTrusted());

    myAdlOptionsEditor.setText(params.getAdlOptions());
    myAirProgramParametersEditor.setText(params.getAirProgramParameters());

    myOnEmulatorRadioButton.setSelected(params.getMobileRunTarget() == AirMobileRunnerParameters.AirMobileRunTarget.Emulator);
    myEmulatorCombo.setSelectedItem(params.getEmulator());
    if (params.getEmulator().adlAlias == null) {
      myScreenWidth.setText(String.valueOf(params.getScreenWidth()));
      myScreenHeight.setText(String.valueOf(params.getScreenHeight()));
      myFullScreenWidth.setText(String.valueOf(params.getFullScreenWidth()));
      myFullScreenHeight.setText(String.valueOf(params.getFullScreenHeight()));
    }

    myOnAndroidDeviceRadioButton.setSelected(params.getMobileRunTarget() == AirMobileRunnerParameters.AirMobileRunTarget.AndroidDevice);
    myOnIOSDeviceRadioButton.setSelected(params.getMobileRunTarget() == AirMobileRunnerParameters.AirMobileRunTarget.iOSDevice);

    myDebugOverNetworkRadioButton.setSelected(params.getDebugTransport() == AirMobileRunnerParameters.AirMobileDebugTransport.Network);
    myDebugOverUSBRadioButton.setSelected(params.getDebugTransport() == AirMobileRunnerParameters.AirMobileDebugTransport.USB);
    myUsbDebugPortTextField.setText(String.valueOf(params.getUsbDebugPort()));

    myEmulatorAdlOptionsEditor.setText(params.getEmulatorAdlOptions());

    updateControls();
  }

  protected void applyEditorTo(final FlashRunConfiguration configuration) throws ConfigurationException {
    final FlashRunnerParameters params = configuration.getRunnerParameters();

    myBCCombo.applyTo(params);

    final boolean overrideMainClass = myOverrideMainClassCheckBox.isSelected();
    params.setOverrideMainClass(overrideMainClass);
    params.setOverriddenMainClass(overrideMainClass ? myMainClassComponent.getText().trim() : "");
    params.setOverriddenOutputFileName(overrideMainClass ? myOutputFileNameTextField.getText().trim() : "");

    params.setLaunchUrl(myURLRadioButton.isSelected());
    params.setUrl(myURLTextField.getText().trim());

    params.setLauncherParameters(myLauncherParameters);
    params.setRunTrusted(myRunTrustedCheckBox.isSelected());

    params.setAdlOptions(myAdlOptionsEditor.getText().trim());
    params.setAirProgramParameters(myAirProgramParametersEditor.getText().trim());

    final AirMobileRunnerParameters.AirMobileRunTarget mobileRunTarget = myOnEmulatorRadioButton.isSelected()
                                                                         ? AirMobileRunnerParameters.AirMobileRunTarget.Emulator
                                                                         : myOnAndroidDeviceRadioButton.isSelected()
                                                                           ? AirMobileRunnerParameters.AirMobileRunTarget.AndroidDevice
                                                                           : AirMobileRunnerParameters.AirMobileRunTarget.iOSDevice;
    params.setMobileRunTarget(mobileRunTarget);

    final AirMobileRunnerParameters.Emulator emulator = (AirMobileRunnerParameters.Emulator)myEmulatorCombo.getSelectedItem();
    params.setEmulator(emulator);

    if (emulator.adlAlias == null) {
      try {
        params.setScreenWidth(Integer.parseInt(myScreenWidth.getText()));
        params.setScreenHeight(Integer.parseInt(myScreenHeight.getText()));
        params.setFullScreenWidth(Integer.parseInt(myFullScreenWidth.getText()));
        params.setFullScreenHeight(Integer.parseInt(myFullScreenHeight.getText()));
      }
      catch (NumberFormatException e) {/**/}
    }

    params.setDebugTransport(myDebugOverNetworkRadioButton.isSelected()
                             ? AirMobileRunnerParameters.AirMobileDebugTransport.Network
                             : AirMobileRunnerParameters.AirMobileDebugTransport.USB);
    try {
      final int port = Integer.parseInt(myUsbDebugPortTextField.getText().trim());
      if (port > 0 && port < 65535) {
        params.setUsbDebugPort(port);
      }
    }
    catch (NumberFormatException ignore) {/*ignore*/}

    params.setEmulatorAdlOptions(myEmulatorAdlOptionsEditor.getText().trim());
  }

  protected void disposeEditor() {
    myBCCombo.dispose();
  }

  public static void updateOutputFileName(final JTextField textField, final boolean isLib) {
    final String outputFileName = textField.getText();
    final String lowercase = outputFileName.toLowerCase();
    final String withoutExtension = lowercase.endsWith(".swf") || lowercase.endsWith(".swc")
                                    ? outputFileName.substring(0, outputFileName.length() - ".sw_".length())
                                    : outputFileName;
    textField.setText(withoutExtension + (isLib ? ".swc" : ".swf"));
  }
}
