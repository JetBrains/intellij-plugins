// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionBundle;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil;
import com.intellij.lang.javascript.flex.build.InfoFromConfigFile;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AndroidPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.IosPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.*;

public class FlashRunConfigurationForm extends SettingsEditor<FlashRunConfiguration> {

  private static final String LATEST_SELECTED_IOS_SIMULATOR_SDK_PATH_KEY = "LatestSelectedIosSimulatorSdk";

  // Application Main Class must inherit from this class
  public static final String SPRITE_CLASS_NAME = "flash.display.Sprite";
  // The base class for ActionScript-based dynamically-loadable modules
  public static final String MODULE_BASE_CLASS_NAME = "mx.modules.ModuleBase";

  private JPanel myMainPanel;
  private BCCombo myBCCombo;

  private JCheckBox myOverrideMainClassCheckBox;
  private Condition<JSClass> myMainClassFilter;
  private JSReferenceEditor myMainClassComponent;
  private JLabel myOutputFileNameLabel;
  private JTextField myOutputFileNameTextField;

  private JPanel myLaunchPanel;
  private JRadioButton myBCOutputRadioButton;
  private JLabel myBCOutputLabel;
  private JRadioButton myUrlOrFileRadioButton;
  private TextFieldWithBrowseButton myUrlOrFileTextWithBrowse;

  private JPanel myWebOptionsPanel;
  private JLabel myLauncherParametersLabel;
  private TextFieldWithBrowseButton myLauncherParametersTextWithBrowse;
  private JLabel mySdkForDebuggingLabel;
  private FlexSdkComboBoxWithBrowseButton mySdkForDebuggingCombo;
  private JCheckBox myRunTrustedCheckBox;

  private JPanel myDesktopOptionsPanel;
  private RawCommandLineEditor myAdlOptionsEditor;
  private RawCommandLineEditor myAirProgramParametersEditor;

  private JPanel myMobileRunPanel;
  private JRadioButton myOnEmulatorRadioButton;
  private JRadioButton myOnAndroidDeviceRadioButton;
  private JCheckBox myClearAndroidDataCheckBox;
  private JRadioButton myOnIOSSimulatorRadioButton;
  private TextFieldWithBrowseButton myIOSSimulatorSdkTextWithBrowse;
  private JBLabel myIOSSimulatorDeviceLabel;
  private JBTextField myIOSSimulatorDeviceTextField;
  private JRadioButton myOnIOSDeviceRadioButton;
  private JCheckBox myFastPackagingCheckBox;

  private JComboBox<Emulator> myEmulatorCombo;
  private JPanel myEmulatorScreenSizePanel;
  private JTextField myScreenWidth;
  private JTextField myScreenHeight;
  private JTextField myFullScreenWidth;
  private JTextField myFullScreenHeight;
  private JTextField myScreenDpi;

  private JPanel myMobileOptionsPanel;
  private JPanel myDebugTransportPanel;
  private JLabel myDebugOverLabel;
  private JRadioButton myDebugOverNetworkRadioButton;
  private JRadioButton myDebugOverUSBRadioButton;
  private JTextField myUsbDebugPortTextField;
  private JBLabel myEmulatorAdlOptionsLabel;
  private RawCommandLineEditor myEmulatorAdlOptionsEditor;
  private JLabel myAppDescriptorForEmulatorLabel;
  private JComboBox myAppDescriptorForEmulatorCombo;

  private final Project myProject;

  private LauncherParameters myLauncherParameters;

  private boolean myResetting = false;

  public FlashRunConfigurationForm(final Project project) {
    myProject = project;

    initBCCombo();
    initMainClassRelatedControls();
    initRadioButtons();
    initLaunchWithTextWithBrowse();
    initMobileControls();

    mySdkForDebuggingLabel.setLabelFor(mySdkForDebuggingCombo.getChildComponent());
    myEmulatorAdlOptionsLabel.setLabelFor(myEmulatorAdlOptionsEditor.getTextField());
  }

  private void initBCCombo() {
    myBCCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateMainClassField();
        myAppDescriptorForEmulatorCombo.repaint();

        final FlexBuildConfiguration bc = myBCCombo.getBC();
        mySdkForDebuggingCombo.setBCSdk(bc == null ? null : bc.getSdk());

        updateControls();
      }
    });
  }

  private void updateMainClassField() {
    final Module module = myBCCombo.getModule();
    if (module != null) {
      myMainClassComponent.setScope(module.getModuleScope(true));
      myMainClassFilter = BCUtils.getMainClassFilter(module, myBCCombo.getBC(), false, true, true);
      myMainClassComponent.setChooserBlockingMessage(null);
    }
    else {
      myMainClassComponent.setScope(GlobalSearchScope.EMPTY_SCOPE);
      myMainClassFilter = Conditions.alwaysFalse();
      myMainClassComponent.setChooserBlockingMessage("Build configuration not selected");
    }
  }

  private void initMainClassRelatedControls() {
    myOverrideMainClassCheckBox.addActionListener(new ActionListener() {
      @Override
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

    myMainClassComponent.addDocumentListener(new DocumentListener() {
      @Override
      public void documentChanged(@NotNull final DocumentEvent e) {
        final String shortName = StringUtil.getShortName(myMainClassComponent.getText().trim());
        if (!shortName.isEmpty()) {
          myOutputFileNameTextField.setText(shortName + ".swf");
        }
      }
    });

    myOutputFileNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull final javax.swing.event.DocumentEvent e) {
        final FlexBuildConfiguration bc = myBCCombo.getBC();
        if (bc != null && bc.getTargetPlatform() == TargetPlatform.Web) {
          updateBCOutputLabel(bc);
        }
      }
    });
  }

  private void initRadioButtons() {
    myBCOutputRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });

    myUrlOrFileRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();

        if (myUrlOrFileTextWithBrowse.isEnabled()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myUrlOrFileTextWithBrowse.getTextField(), true);
        }
      }
    });

    myUrlOrFileTextWithBrowse
      .addBrowseFolderListener(null, null, myProject, FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
  }

  private void initLaunchWithTextWithBrowse() {
    myLauncherParametersTextWithBrowse.getButton().setMnemonic(myLauncherParametersLabel.getDisplayedMnemonic());
    myLauncherParametersTextWithBrowse.getTextField().setEditable(false);
    myLauncherParametersTextWithBrowse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final FlexLauncherDialog dialog = new FlexLauncherDialog(myProject, myLauncherParameters);
        if (dialog.showAndGet()) {
          myLauncherParameters = dialog.getLauncherParameters();
          updateControls();
        }
      }
    });
  }

  private void initMobileControls() {
    initEmulatorRelatedControls();

    if (!SystemInfo.isMac) {
      myOnIOSSimulatorRadioButton.setEnabled(false);
      myOnIOSSimulatorRadioButton.setText(FlexBundle.message("ios.simulator.on.mac.only.button.text"));
      myIOSSimulatorSdkTextWithBrowse.setVisible(false);
      myIOSSimulatorDeviceLabel.setVisible(false);
      myIOSSimulatorDeviceTextField.setVisible(false);
    }

    myIOSSimulatorSdkTextWithBrowse
      .addBrowseFolderListener(null, null, myProject, FileChooserDescriptorFactory.createSingleFolderDescriptor());

    final ActionListener debugTransportListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateDebugTransportRelatedControls();
      }
    };
    myDebugOverNetworkRadioButton.addActionListener(debugTransportListener);
    myDebugOverUSBRadioButton.addActionListener(debugTransportListener);

    initAppDescriptorForEmulatorCombo(myAppDescriptorForEmulatorCombo,
                                      () -> myBCCombo.getBC());
  }

  public static void initAppDescriptorForEmulatorCombo(final JComboBox<AppDescriptorForEmulator> appDescriptorForEmulatorCombo,
                                                       final NullableComputable<? extends FlexBuildConfiguration> bcComputable) {
    appDescriptorForEmulatorCombo.setModel(new DefaultComboBoxModel<>(AppDescriptorForEmulator.values()));
    appDescriptorForEmulatorCombo.setRenderer(SimpleListCellRenderer.create(
      "", value -> {
        FlexBuildConfiguration bc = bcComputable.compute();
        if (value == AppDescriptorForEmulator.Android) {
          return getDescriptorForEmulatorText("Android", bc == null ? null : bc.getAndroidPackagingOptions());
        }
        else if (value == AppDescriptorForEmulator.IOS) {
          return getDescriptorForEmulatorText("iOS", bc == null ? null : bc.getIosPackagingOptions());
        }
        throw new IllegalArgumentException(String.valueOf(value));
      }));
  }

  private static String getDescriptorForEmulatorText(final String mobilePlatform, final @Nullable AirPackagingOptions packagingOptions) {
    final String prefix = "as set for " + mobilePlatform;

    if (packagingOptions == null) return prefix;

    if (packagingOptions instanceof AndroidPackagingOptions && !((AndroidPackagingOptions)packagingOptions).isEnabled() ||
        packagingOptions instanceof IosPackagingOptions && !((IosPackagingOptions)packagingOptions).isEnabled()) {
      return prefix + ": <" + mobilePlatform + " support is not enabled>";
    }

    if (packagingOptions.isUseGeneratedDescriptor()) return prefix + ": generated";

    final String descriptorPath = packagingOptions.getCustomDescriptorPath();

    if (descriptorPath.isEmpty()) return prefix + ": <custom descriptor is not set>";

    return prefix + ": " + PathUtil.getFileName(descriptorPath);
  }

  private void initEmulatorRelatedControls() {
    myEmulatorCombo.setModel(new DefaultComboBoxModel<>(Emulator.ALL_EMULATORS.toArray(new Emulator[0])));

    myEmulatorCombo.setRenderer(SimpleListCellRenderer.create("", value -> value.name));

    myEmulatorCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateEmulatorRelatedControls();
      }
    });

    final ActionListener targetDeviceListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateControls();

        if (myOnEmulatorRadioButton.isSelected()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myEmulatorCombo, true);
        }
        if (myOnIOSSimulatorRadioButton.isSelected()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myIOSSimulatorSdkTextWithBrowse.getTextField(), true);
        }
      }
    };

    myOnEmulatorRadioButton.addActionListener(targetDeviceListener);
    myOnAndroidDeviceRadioButton.addActionListener(targetDeviceListener);
    myOnIOSSimulatorRadioButton.addActionListener(targetDeviceListener);
    myOnIOSDeviceRadioButton.addActionListener(targetDeviceListener);

    final int preferredWidth = new JLabel("999999").getPreferredSize().width;
    myScreenWidth.setPreferredSize(new Dimension(preferredWidth, myScreenWidth.getPreferredSize().height));
    myScreenHeight.setPreferredSize(new Dimension(preferredWidth, myScreenHeight.getPreferredSize().height));
    myFullScreenWidth.setPreferredSize(new Dimension(preferredWidth, myFullScreenWidth.getPreferredSize().height));
    myFullScreenHeight.setPreferredSize(new Dimension(preferredWidth, myFullScreenHeight.getPreferredSize().height));
    myScreenDpi.setPreferredSize(new Dimension(preferredWidth, myScreenDpi.getPreferredSize().height));
  }

  private void updateControls() {
    final FlexBuildConfiguration bc = myBCCombo.getBC();
    final Module module = myBCCombo.getModule();

    final boolean overrideMainClass = myOverrideMainClassCheckBox.isSelected();
    myMainClassComponent.setEnabled(overrideMainClass);
    myOutputFileNameLabel.setEnabled(overrideMainClass);
    myOutputFileNameTextField.setEnabled(overrideMainClass);

    if (!overrideMainClass && bc != null && module != null) {
      final InfoFromConfigFile info =
        FlexCompilerConfigFileUtil.getInfoFromConfigFile(bc.getCompilerOptions().getAdditionalConfigFilePath());

      myMainClassComponent.setText(StringUtil.notNullize(info.getMainClass(module), bc.getMainClass()));
      myOutputFileNameTextField.setText(StringUtil.notNullize(info.getOutputFileName(), bc.getOutputFileName()));
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

      myUrlOrFileTextWithBrowse.setEnabled(myUrlOrFileRadioButton.isSelected());

      myLauncherParametersTextWithBrowse.getTextField().setText(myLauncherParameters.getPresentableText());
      myRunTrustedCheckBox.setEnabled(!myUrlOrFileRadioButton.isSelected());
    }

    if (mobile) {
      final boolean runOnEmulator = myOnEmulatorRadioButton.isSelected();
      final boolean app = bc.getOutputType() == OutputType.Application;
      myEmulatorCombo.setEnabled(runOnEmulator);
      UIUtil.setEnabled(myEmulatorScreenSizePanel, runOnEmulator, true);

      myClearAndroidDataCheckBox.setEnabled(myOnAndroidDeviceRadioButton.isSelected());
      myFastPackagingCheckBox.setEnabled(myOnIOSDeviceRadioButton.isSelected());

      myEmulatorAdlOptionsLabel.setEnabled(runOnEmulator);
      myEmulatorAdlOptionsEditor.setEnabled(runOnEmulator);
      myAppDescriptorForEmulatorLabel.setEnabled(app && runOnEmulator);
      myAppDescriptorForEmulatorCombo.setEnabled(app && runOnEmulator);

      if (runOnEmulator) {
        updateEmulatorRelatedControls();
      }

      myIOSSimulatorSdkTextWithBrowse.setEnabled(myOnIOSSimulatorRadioButton.isSelected());
      myIOSSimulatorDeviceLabel.setEnabled(myOnIOSSimulatorRadioButton.isSelected());
      myIOSSimulatorDeviceTextField.setEnabled(myOnIOSSimulatorRadioButton.isSelected());

      if (myIOSSimulatorSdkTextWithBrowse.isEnabled() && myIOSSimulatorSdkTextWithBrowse.getText().isEmpty() && SystemInfo.isMac) {
        final String latestSelected = PropertiesComponent.getInstance().getValue(LATEST_SELECTED_IOS_SIMULATOR_SDK_PATH_KEY);
        myIOSSimulatorSdkTextWithBrowse
          .setText(FileUtil.toSystemDependentName(StringUtil.notNullize(latestSelected, guessIosSimulatorSdkPath())));
      }

      updateDebugTransportRelatedControls();
    }
  }

  private void updateBCOutputLabel(final FlexBuildConfiguration bc) {
    if (bc.getOutputType() == OutputType.Application || myOverrideMainClassCheckBox.isSelected()) {
      String outputFileName = myOverrideMainClassCheckBox.isSelected()
                              ? myOutputFileNameTextField.getText().trim()
                              : PathUtil.getFileName(bc.getActualOutputFilePath());

      if (!outputFileName.isEmpty() && bc.isUseHtmlWrapper()) {
        outputFileName += " via HTML wrapper";
      }
      myBCOutputLabel.setText(outputFileName);
    }
    else {
      myBCOutputLabel.setText("");
    }
  }

  private void updateEmulatorRelatedControls() {
    final Emulator emulator = (Emulator)myEmulatorCombo.getSelectedItem();
    if (emulator.adlAlias == null) {
      myScreenWidth.setEditable(true);
      myScreenHeight.setEditable(true);
      myFullScreenWidth.setEditable(true);
      myFullScreenHeight.setEditable(true);

      if (!myResetting && !myScreenDpi.isEditable()) {
        myScreenDpi.setText(""); // User have selected 'Custom...' screen size. Let him select dpi explicitly if needed.
      }

      myScreenDpi.setEditable(true);
    }
    else {
      myScreenWidth.setEditable(false);
      myScreenHeight.setEditable(false);
      myFullScreenWidth.setEditable(false);
      myFullScreenHeight.setEditable(false);
      myScreenDpi.setEditable(false);
      myScreenWidth.setText(String.valueOf(emulator.screenWidth));
      myScreenHeight.setText(String.valueOf(emulator.screenHeight));
      myFullScreenWidth.setText(String.valueOf(emulator.fullScreenWidth));
      myFullScreenHeight.setText(String.valueOf(emulator.fullScreenHeight));
      myScreenDpi.setText(emulator.screenDPI > 0 ? String.valueOf(emulator.screenDPI) : "");
    }
  }

  private void updateDebugTransportRelatedControls() {
    final boolean onDevice = myOnAndroidDeviceRadioButton.isSelected() || myOnIOSDeviceRadioButton.isSelected();
    myDebugOverLabel.setEnabled(onDevice);
    UIUtil.setEnabled(myDebugTransportPanel, onDevice, true);
    if (onDevice) {
      myUsbDebugPortTextField.setEnabled(myDebugOverUSBRadioButton.isSelected());
    }
  }

  private void createUIComponents() {
    myBCCombo = new BCCombo(myProject);
    myMainClassFilter = Conditions.alwaysFalse();
    myMainClassComponent = JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE, null,
                                                          Conditions.alwaysTrue(), // no filtering until IDEA-83046
                                                          ExecutionBundle.message("choose.main.class.dialog.title"));
    mySdkForDebuggingCombo = new FlexSdkComboBoxWithBrowseButton(FlexSdkComboBoxWithBrowseButton.FLEX_OR_FLEXMOJOS_SDK);
    mySdkForDebuggingCombo.showBCSdk(true);
  }

  @Override
  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  @Override
  protected void resetEditorFrom(@NotNull final FlashRunConfiguration configuration) {
    myResetting = true;
    try {
      doResetEditorFrom(configuration);
    }
    finally {
      myResetting = false;
    }
  }

  private void doResetEditorFrom(final FlashRunConfiguration configuration) {
    final FlashRunnerParameters params = configuration.getRunnerParameters();
    myLauncherParameters = params.getLauncherParameters().clone(); // must be before myBCsCombo.setModel()

    myBCCombo.resetFrom(params);

    myOverrideMainClassCheckBox.setSelected(params.isOverrideMainClass());
    if (params.isOverrideMainClass()) {
      myMainClassComponent.setText(params.getOverriddenMainClass());
      myOutputFileNameTextField.setText(params.getOverriddenOutputFileName());
    }

    myBCOutputRadioButton.setSelected(!params.isLaunchUrl());
    myUrlOrFileRadioButton.setSelected(params.isLaunchUrl());
    final String url = params.getUrl();
    final boolean windowsLocalFile = SystemInfo.isWindows && url.length() >= 2 && Character.isLetter(url.charAt(0)) && ':' == url.charAt(1);
    myUrlOrFileTextWithBrowse.setText(windowsLocalFile ? FileUtil.toSystemDependentName(url) : url);

    myRunTrustedCheckBox.setSelected(params.isRunTrusted());

    myAdlOptionsEditor.setText(params.getAdlOptions());
    myAirProgramParametersEditor.setText(params.getAirProgramParameters());

    myOnEmulatorRadioButton.setSelected(params.getMobileRunTarget() == AirMobileRunTarget.Emulator);
    myEmulatorCombo.setSelectedItem(params.getEmulator());
    if (params.getEmulator().adlAlias == null) {
      myScreenWidth.setText(String.valueOf(params.getScreenWidth()));
      myScreenHeight.setText(String.valueOf(params.getScreenHeight()));
      myFullScreenWidth.setText(String.valueOf(params.getFullScreenWidth()));
      myFullScreenHeight.setText(String.valueOf(params.getFullScreenHeight()));
      myScreenDpi.setText(params.getScreenDpi() > 0 ? String.valueOf(params.getScreenDpi()) : "");
    }

    myOnAndroidDeviceRadioButton.setSelected(params.getMobileRunTarget() == AirMobileRunTarget.AndroidDevice);
    myClearAndroidDataCheckBox.setSelected(params.isClearAppDataOnEachLaunch());
    myOnIOSSimulatorRadioButton.setSelected(params.getMobileRunTarget() == AirMobileRunTarget.iOSSimulator);
    myIOSSimulatorSdkTextWithBrowse.setText(FileUtil.toSystemDependentName(params.getIOSSimulatorSdkPath()));
    myIOSSimulatorDeviceTextField.setText(params.getIOSSimulatorDevice());
    myOnIOSDeviceRadioButton.setSelected(params.getMobileRunTarget() == AirMobileRunTarget.iOSDevice);
    myFastPackagingCheckBox.setSelected(params.isFastPackaging());

    myDebugOverNetworkRadioButton.setSelected(params.getDebugTransport() == AirMobileDebugTransport.Network);
    myDebugOverUSBRadioButton.setSelected(params.getDebugTransport() == AirMobileDebugTransport.USB);
    myUsbDebugPortTextField.setText(String.valueOf(params.getUsbDebugPort()));

    myEmulatorAdlOptionsEditor.setText(params.getEmulatorAdlOptions());

    myAppDescriptorForEmulatorCombo.setSelectedItem(params.getAppDescriptorForEmulator());

    final FlexBuildConfiguration bc = myBCCombo.getBC();
    mySdkForDebuggingCombo.setBCSdk(bc == null ? null : bc.getSdk());
    mySdkForDebuggingCombo.setSelectedSdkRaw(params.getDebuggerSdkRaw());

    updateControls();
  }

  private static String guessIosSimulatorSdkPath() {
    final File appDir = new File("/Applications");
    if (appDir.isDirectory()) {
      final String relPath = "/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs";
      final File[] xCodeDirs = appDir.listFiles(file -> {
        final String name = StringUtil.toLowerCase(file.getName());
        return file.isDirectory() && name.startsWith("xcode") && name.endsWith(".app")
               && new File(file.getPath() + relPath).isDirectory();
      });

      if (xCodeDirs.length > 0) {
        final File sdksDir = new File(xCodeDirs[0] + relPath);
        final File[] simulatorSdkDirs = sdksDir.listFiles(file -> {
          final String filename = StringUtil.toLowerCase(file.getName());
          return file.isDirectory() && filename.startsWith("iphonesimulator") && filename.endsWith(".sdk");
        });

        if (simulatorSdkDirs.length > 0) {
          return simulatorSdkDirs[0].getPath();
        }
      }
    }
    return "";
  }

  @Override
  protected void applyEditorTo(@NotNull final FlashRunConfiguration configuration) throws ConfigurationException {
    final FlashRunnerParameters params = configuration.getRunnerParameters();

    myBCCombo.applyTo(params);

    final boolean overrideMainClass = myOverrideMainClassCheckBox.isSelected();
    params.setOverrideMainClass(overrideMainClass);
    params.setOverriddenMainClass(overrideMainClass ? myMainClassComponent.getText().trim() : "");
    params.setOverriddenOutputFileName(overrideMainClass ? myOutputFileNameTextField.getText().trim() : "");

    params.setLaunchUrl(myUrlOrFileRadioButton.isSelected());
    final String url = myUrlOrFileTextWithBrowse.getText().trim();
    final boolean windowsLocalFile = SystemInfo.isWindows && url.length() >= 2 && Character.isLetter(url.charAt(0)) && ':' == url.charAt(1);
    params.setUrl(windowsLocalFile ? FileUtil.toSystemIndependentName(url) : url);

    params.setLauncherParameters(myLauncherParameters);
    params.setRunTrusted(myRunTrustedCheckBox.isSelected());

    params.setAdlOptions(myAdlOptionsEditor.getText().trim());
    params.setAirProgramParameters(myAirProgramParametersEditor.getText().trim());

    final AirMobileRunTarget mobileRunTarget = myOnEmulatorRadioButton.isSelected()
                                               ? AirMobileRunTarget.Emulator
                                               : myOnAndroidDeviceRadioButton.isSelected()
                                                 ? AirMobileRunTarget.AndroidDevice
                                                 : myOnIOSSimulatorRadioButton.isSelected()
                                                   ? AirMobileRunTarget.iOSSimulator
                                                   : AirMobileRunTarget.iOSDevice;
    params.setMobileRunTarget(mobileRunTarget);

    final Emulator emulator = (Emulator)myEmulatorCombo.getSelectedItem();
    params.setEmulator(emulator);

    if (emulator.adlAlias == null) {
      try {
        params.setScreenWidth(Integer.parseInt(myScreenWidth.getText().trim()));
        params.setScreenHeight(Integer.parseInt(myScreenHeight.getText().trim()));
        params.setFullScreenWidth(Integer.parseInt(myFullScreenWidth.getText().trim()));
        params.setFullScreenHeight(Integer.parseInt(myFullScreenHeight.getText().trim()));
      }
      catch (NumberFormatException e) {/**/}

      try {
        params.setScreenDpi(Integer.parseInt(myScreenDpi.getText().trim()));
      }
      catch (NumberFormatException e) {
        params.setScreenDpi(0);
      }
    }

    final FlexBuildConfiguration bc = myBCCombo.getBC();
    if (SystemInfo.isMac && bc != null && bc.getTargetPlatform() == TargetPlatform.Mobile && myOnIOSSimulatorRadioButton.isSelected()) {
      final String path = FileUtil.toSystemIndependentName(myIOSSimulatorSdkTextWithBrowse.getText().trim());
      if (!path.isEmpty()) {
        PropertiesComponent.getInstance().setValue(LATEST_SELECTED_IOS_SIMULATOR_SDK_PATH_KEY, path);
      }
    }

    params.setClearAppDataOnEachLaunch(myClearAndroidDataCheckBox.isSelected());

    params.setIOSSimulatorSdkPath(FileUtil.toSystemIndependentName(myIOSSimulatorSdkTextWithBrowse.getText().trim()));
    params.setIOSSimulatorDevice(myIOSSimulatorDeviceTextField.getText().trim());

    params.setFastPackaging(myFastPackagingCheckBox.isSelected());

    params.setDebugTransport(myDebugOverNetworkRadioButton.isSelected()
                             ? AirMobileDebugTransport.Network
                             : AirMobileDebugTransport.USB);
    try {
      final int port = Integer.parseInt(myUsbDebugPortTextField.getText().trim());
      if (port > 0 && port < 65535) {
        params.setUsbDebugPort(port);
      }
    }
    catch (NumberFormatException ignore) {/*ignore*/}

    params.setEmulatorAdlOptions(myEmulatorAdlOptionsEditor.getText().trim());
    params.setAppDescriptorForEmulator((AppDescriptorForEmulator)myAppDescriptorForEmulatorCombo.getSelectedItem());

    params.setDebuggerSdkRaw(mySdkForDebuggingCombo.getSelectedSdkRaw());
  }

  @Override
  protected void disposeEditor() {
    myBCCombo.dispose();
  }

  public static void updateOutputFileName(final JTextField textField, final boolean isLib) {
    final String outputFileName = textField.getText();
    final String lowercase = StringUtil.toLowerCase(outputFileName);
    final String withoutExtension = lowercase.endsWith(".swf") || lowercase.endsWith(".swc")
                                    ? outputFileName.substring(0, outputFileName.length() - ".sw_".length())
                                    : outputFileName;
    textField.setText(withoutExtension + (isLib ? ".swc" : ".swf"));
  }
}
