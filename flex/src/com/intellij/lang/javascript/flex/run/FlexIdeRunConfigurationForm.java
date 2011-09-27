package com.intellij.lang.javascript.flex.run;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.UIUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class FlexIdeRunConfigurationForm extends SettingsEditor<FlexIdeRunConfiguration> {

  private JPanel myMainPanel;
  private JComboBox myBCsCombo;

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
  private FlexIdeBuildConfiguration[] myAllConfigs;
  private boolean mySingleModuleProject;
  private Map<FlexIdeBuildConfiguration, Module> myBCToModuleMap = new THashMap<FlexIdeBuildConfiguration, Module>();

  private LauncherParameters myLauncherParameters;

  public FlexIdeRunConfigurationForm(final Project project) {
    myProject = project;

    initBCCombo();
    initRadioButtons();
    initLaunchWithTextWithBrowse();
    initMobileControls();
  }

  private void initBCCombo() {
    final Collection<FlexIdeBuildConfiguration> allConfigs = new ArrayList<FlexIdeBuildConfiguration>();

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    mySingleModuleProject = modules.length == 1;
    for (final Module module : modules) {
      if (ModuleType.get(module) instanceof FlexModuleType) {
        for (final FlexIdeBuildConfiguration config : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
          if (config.getOutputType() == OutputType.Application) {
            allConfigs.add(config);
            myBCToModuleMap.put(config, module);
          }
        }
      }
    }
    myAllConfigs = allConfigs.toArray(new FlexIdeBuildConfiguration[allConfigs.size()]);

    myBCsCombo.setRenderer(new ListCellRendererWrapper(myBCsCombo.getRenderer()) {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof Pair) {
          final String moduleName = (String)((Pair)value).first;
          final String configName = (String)((Pair)value).second;
          setIcon(PlatformIcons.ERROR_INTRODUCTION_ICON);
          setText("<html><font color='red'>" + getPresentableText(moduleName, configName, mySingleModuleProject) + "</font></html>");
        }
        else {
          assert value instanceof FlexIdeBuildConfiguration : value;
          final FlexIdeBuildConfiguration config = (FlexIdeBuildConfiguration)value;
          setIcon(config.getIcon());
          setText(getPresentableText(myBCToModuleMap.get(config).getName(), config.getName(), mySingleModuleProject));
        }
      }
    });

    myBCsCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        // remove invalid entry
        final Object selectedItem = myBCsCombo.getSelectedItem();
        final Object firstItem = myBCsCombo.getItemAt(0);
        if (selectedItem instanceof FlexIdeBuildConfiguration && !(firstItem instanceof FlexIdeBuildConfiguration)) {
          myBCsCombo.setModel(new DefaultComboBoxModel(myAllConfigs));
          myBCsCombo.setSelectedItem(selectedItem);
        }

        updateControls();
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
        IdeFocusManager.getInstance(myProject).requestFocus(myURLTextField, true);
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

    myEmulatorAdlOptionsEditor.setDialogCaption("AIR Debug Launcher Options");
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
    final Object item = myBCsCombo.getSelectedItem();
    final FlexIdeBuildConfiguration config = item instanceof FlexIdeBuildConfiguration ? (FlexIdeBuildConfiguration)item : null;

    final boolean web = config != null && config.getTargetPlatform() == TargetPlatform.Web;
    final boolean desktop = config != null && config.getTargetPlatform() == TargetPlatform.Desktop;
    final boolean mobile = config != null && config.getTargetPlatform() == TargetPlatform.Mobile;

    myLaunchPanel.setVisible(web);
    myWebOptionsPanel.setVisible(web);
    myDesktopOptionsPanel.setVisible(desktop);
    myMobileRunPanel.setVisible(mobile);
    myMobileOptionsPanel.setVisible(mobile);

    if (web) {
      String bcOutput = config.getOutputFileName();
      if (!bcOutput.isEmpty() && config.isUseHtmlWrapper()) {
        // todo support
        //bcOutput += " via HTML wrapper";
      }
      myBCOutputLabel.setText(bcOutput);

      myURLTextField.setEnabled(myURLRadioButton.isSelected());

      myLauncherParametersTextWithBrowse.getTextField().setText(myLauncherParameters.getPresentableText());
      myRunTrustedCheckBox.setEnabled(!myURLRadioButton.isSelected());
    }

    if (mobile) {
      myOnAndroidDeviceRadioButton.setEnabled(config.getAndroidPackagingOptions().isEnabled());
      if (myOnAndroidDeviceRadioButton.isSelected() && !myOnAndroidDeviceRadioButton.isEnabled()) {
        myOnEmulatorRadioButton.setSelected(true);
      }

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


  private static String getPresentableText(String moduleName, String configName, final boolean singleModuleProject) {
    moduleName = moduleName.isEmpty() ? "[no module]" : moduleName;
    configName = configName.isEmpty() ? "[no configuration]" : configName;
    return singleModuleProject ? configName : configName + " (" + moduleName + ")";
  }

  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  protected void resetEditorFrom(final FlexIdeRunConfiguration configuration) {
    final FlexIdeRunnerParameters params = configuration.getRunnerParameters();
    myLauncherParameters = params.getLauncherParameters().clone(); // must be before myBCsCombo.setModel()

    final Module module = ModuleManager.getInstance(myProject).findModuleByName(params.getModuleName());
    final FlexIdeBuildConfiguration config =
      module != null && (ModuleType.get(module) instanceof FlexModuleType)
      ? FlexBuildConfigurationManager.getInstance(module).findConfigurationByName(params.getBCName())
      : null;

    if (config == null) {
      final Object[] model = new Object[myAllConfigs.length + 1];
      model[0] = Pair.create(params.getModuleName(), params.getBCName());
      System.arraycopy(myAllConfigs, 0, model, 1, myAllConfigs.length);
      myBCsCombo.setModel(new DefaultComboBoxModel(model));
      myBCsCombo.setSelectedIndex(0);
    }
    else {
      myBCsCombo.setModel(new DefaultComboBoxModel(myAllConfigs));
      myBCsCombo.setSelectedItem(config);
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

  protected void applyEditorTo(final FlexIdeRunConfiguration configuration) throws ConfigurationException {
    final FlexIdeRunnerParameters params = configuration.getRunnerParameters();

    final Object selectedItem = myBCsCombo.getSelectedItem();

    if (selectedItem instanceof Pair) {
      params.setModuleName((String)((Pair)selectedItem).first);
      params.setBCName((String)((Pair)selectedItem).second);
    }
    else {
      assert selectedItem instanceof FlexIdeBuildConfiguration : selectedItem;
      params.setModuleName(myBCToModuleMap.get(((FlexIdeBuildConfiguration)selectedItem)).getName());
      params.setBCName(((FlexIdeBuildConfiguration)selectedItem).getName());
    }

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
    myAllConfigs = null;
    myBCToModuleMap = null;
  }
}
