// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import com.intellij.openapi.util.io.OSAgnosticPathUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.dsl.listCellRenderer.BuilderKt;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileRunTarget;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AppDescriptorForEmulator;
import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.Emulator;

public class FlashRunConfigurationForm extends SettingsEditor<FlashRunConfiguration> {

  private static final String LATEST_SELECTED_IOS_SIMULATOR_SDK_PATH_KEY = "LatestSelectedIosSimulatorSdk";

  // Application Main Class must inherit from this class
  public static final String SPRITE_CLASS_NAME = "flash.display.Sprite";
  // The base class for ActionScript-based dynamically-loadable modules
  public static final String MODULE_BASE_CLASS_NAME = "mx.modules.ModuleBase";

  private final JPanel myMainPanel;
  private final BCCombo myBCCombo;

  private final JCheckBox myOverrideMainClassCheckBox;
  private Condition<JSClass> myMainClassFilter;
  private final JSReferenceEditor myMainClassComponent;
  private final JLabel myOutputFileNameLabel;
  private final JTextField myOutputFileNameTextField;

  private final JPanel myLaunchPanel;
  private final JRadioButton myBCOutputRadioButton;
  private final JLabel myBCOutputLabel;
  private final JRadioButton myUrlOrFileRadioButton;
  private final TextFieldWithBrowseButton myUrlOrFileTextWithBrowse;

  private final JPanel myWebOptionsPanel;
  private final JLabel myLauncherParametersLabel;
  private final TextFieldWithBrowseButton myLauncherParametersTextWithBrowse;
  private final JLabel mySdkForDebuggingLabel;
  private final FlexSdkComboBoxWithBrowseButton mySdkForDebuggingCombo;
  private final JCheckBox myRunTrustedCheckBox;

  private final JPanel myDesktopOptionsPanel;
  private final RawCommandLineEditor myAdlOptionsEditor;
  private final RawCommandLineEditor myAirProgramParametersEditor;

  private final JPanel myMobileRunPanel;
  private final JRadioButton myOnEmulatorRadioButton;
  private final JRadioButton myOnAndroidDeviceRadioButton;
  private final JCheckBox myClearAndroidDataCheckBox;
  private final JRadioButton myOnIOSSimulatorRadioButton;
  private final TextFieldWithBrowseButton myIOSSimulatorSdkTextWithBrowse;
  private final JBLabel myIOSSimulatorDeviceLabel;
  private final JBTextField myIOSSimulatorDeviceTextField;
  private final JRadioButton myOnIOSDeviceRadioButton;
  private final JCheckBox myFastPackagingCheckBox;

  private final JComboBox<Emulator> myEmulatorCombo;
  private final JPanel myEmulatorScreenSizePanel;
  private final JTextField myScreenWidth;
  private final JTextField myScreenHeight;
  private final JTextField myFullScreenWidth;
  private final JTextField myFullScreenHeight;
  private final JTextField myScreenDpi;

  private final JPanel myMobileOptionsPanel;
  private final JPanel myDebugTransportPanel;
  private final JLabel myDebugOverLabel;
  private final JRadioButton myDebugOverNetworkRadioButton;
  private final JRadioButton myDebugOverUSBRadioButton;
  private final JTextField myUsbDebugPortTextField;
  private final JBLabel myEmulatorAdlOptionsLabel;
  private final RawCommandLineEditor myEmulatorAdlOptionsEditor;
  private final JLabel myAppDescriptorForEmulatorLabel;
  private final JComboBox myAppDescriptorForEmulatorCombo;

  private final Project myProject;

  private LauncherParameters myLauncherParameters;

  private boolean myResetting = false;

  public FlashRunConfigurationForm(final Project project) {
    myProject = project;
    {
      myBCCombo = new BCCombo(myProject);
      myMainClassFilter = Conditions.alwaysFalse();
      myMainClassComponent = JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE, null,
                                                            Conditions.alwaysTrue(), // no filtering until IDEA-83046
                                                            ExecutionBundle.message("choose.main.class.dialog.title"));
      mySdkForDebuggingCombo = new FlexSdkComboBoxWithBrowseButton(FlexSdkComboBoxWithBrowseButton.FLEX_OR_FLEXMOJOS_SDK);
      mySdkForDebuggingCombo.showBCSdk(true);
    }
    {
      // GUI initializer generated by IntelliJ IDEA GUI Designer
      // >>> IMPORTANT!! <<<
      // DO NOT EDIT OR ADD ANY CODE HERE!
      myMainPanel = new JPanel();
      myMainPanel.setLayout(new GridLayoutManager(9, 3, new Insets(0, 0, 0, 0), -1, -1));
      final Spacer spacer1 = new Spacer();
      myMainPanel.add(spacer1, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                                                   GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
      final Spacer spacer2 = new Spacer();
      myMainPanel.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                   GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
      myLaunchPanel = new JPanel();
      myLaunchPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
      myLaunchPanel.putClientProperty("BorderFactoryClass", "com.intellij.ui.IdeBorderFactory$PlainSmallWithIndent");
      myMainPanel.add(myLaunchPanel, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                         GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                         GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                         null, null, 0, false));
      myLaunchPanel.setBorder(IdeBorderFactory.PlainSmallWithIndent.createTitledBorder(BorderFactory.createEtchedBorder(), "What to Launch",
                                                                                       TitledBorder.DEFAULT_JUSTIFICATION,
                                                                                       TitledBorder.DEFAULT_POSITION, null, null));
      myBCOutputRadioButton = new JRadioButton();
      myBCOutputRadioButton.setText("Build output:");
      myBCOutputRadioButton.setMnemonic('O');
      myBCOutputRadioButton.setDisplayedMnemonicIndex(6);
      myLaunchPanel.add(myBCOutputRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                   GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                                                   null, null, null, 0, false));
      myBCOutputLabel = new JLabel();
      myBCOutputLabel.setText("SomeFile.swf via HTML wrapper");
      myLaunchPanel.add(myBCOutputLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                             GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                             null, 0, false));
      myUrlOrFileRadioButton = new JRadioButton();
      myUrlOrFileRadioButton.setText("URL or local file:");
      myUrlOrFileRadioButton.setMnemonic('R');
      myUrlOrFileRadioButton.setDisplayedMnemonicIndex(1);
      myLaunchPanel.add(myUrlOrFileRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                    GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                                                    null, null, null, 0, false));
      myUrlOrFileTextWithBrowse = new TextFieldWithBrowseButton();
      myLaunchPanel.add(myUrlOrFileTextWithBrowse,
                        new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                            GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                            new Dimension(150, -1), null, 0, false));
      myWebOptionsPanel = new JPanel();
      myWebOptionsPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
      myWebOptionsPanel.putClientProperty("BorderFactoryClass", "com.intellij.ui.IdeBorderFactory$PlainSmallWithIndent");
      myMainPanel.add(myWebOptionsPanel, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                             null, null, null, 0, false));
      myWebOptionsPanel.setBorder(IdeBorderFactory.PlainSmallWithIndent.createTitledBorder(BorderFactory.createEtchedBorder(), "Options",
                                                                                           TitledBorder.DEFAULT_JUSTIFICATION,
                                                                                           TitledBorder.DEFAULT_POSITION, null, null));
      final JPanel panel1 = new JPanel();
      panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 5, 0, 0), -1, -1));
      myWebOptionsPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                        null, null, 0, false));
      myLauncherParametersLabel = new JLabel();
      myLauncherParametersLabel.setText("Launch with:");
      myLauncherParametersLabel.setDisplayedMnemonic('W');
      myLauncherParametersLabel.setDisplayedMnemonicIndex(7);
      panel1.add(myLauncherParametersLabel,
                 new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myLauncherParametersTextWithBrowse = new TextFieldWithBrowseButton();
      myWebOptionsPanel.add(myLauncherParametersTextWithBrowse,
                            new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                false));
      myRunTrustedCheckBox = new JCheckBox();
      this.$$$loadButtonText$$$(myRunTrustedCheckBox, this.$$$getMessageFromBundle$$$("messages/FlexBundle", "run.trusted"));
      myWebOptionsPanel.add(myRunTrustedCheckBox, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                      GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                      GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                                                      null, null, null, 0, false));
      final JPanel panel2 = new JPanel();
      panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 5, 0, 0), -1, -1));
      myWebOptionsPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                        null, null, 0, false));
      mySdkForDebuggingLabel = new JLabel();
      mySdkForDebuggingLabel.setText("Use debugger from SDK:");
      mySdkForDebuggingLabel.setDisplayedMnemonic('D');
      mySdkForDebuggingLabel.setDisplayedMnemonicIndex(4);
      panel2.add(mySdkForDebuggingLabel,
                 new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myWebOptionsPanel.add(mySdkForDebuggingCombo,
                            new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                null, 0, false));
      myDesktopOptionsPanel = new JPanel();
      myDesktopOptionsPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
      myDesktopOptionsPanel.putClientProperty("BorderFactoryClass", "com.intellij.ui.IdeBorderFactory$PlainSmallWithIndent");
      myMainPanel.add(myDesktopOptionsPanel, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                 GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                 GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
      myDesktopOptionsPanel.setBorder(
        IdeBorderFactory.PlainSmallWithIndent.createTitledBorder(BorderFactory.createEtchedBorder(), "Options",
                                                                 TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null,
                                                                 null));
      final JLabel label1 = new JLabel();
      label1.setText("AIR Debug Launcher options:");
      label1.setDisplayedMnemonic('O');
      label1.setDisplayedMnemonicIndex(19);
      myDesktopOptionsPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                            GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                            null, 0, false));
      final JLabel label2 = new JLabel();
      label2.setText("Program parameters:");
      label2.setDisplayedMnemonic('P');
      label2.setDisplayedMnemonicIndex(0);
      myDesktopOptionsPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                            GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                            null, 0, false));
      myAirProgramParametersEditor = new RawCommandLineEditor();
      myAirProgramParametersEditor.setDialogCaption("Program Parameters");
      myDesktopOptionsPanel.add(myAirProgramParametersEditor,
                                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                    GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myAdlOptionsEditor = new RawCommandLineEditor();
      myAdlOptionsEditor.setDialogCaption("ADL Options");
      myDesktopOptionsPanel.add(myAdlOptionsEditor,
                                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                    GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myMobileRunPanel = new JPanel();
      myMobileRunPanel.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
      myMobileRunPanel.putClientProperty("BorderFactoryClass", "com.intellij.ui.IdeBorderFactory$PlainSmallWithIndent");
      myMainPanel.add(myMobileRunPanel, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                            null, null, null, 0, false));
      myMobileRunPanel.setBorder(IdeBorderFactory.PlainSmallWithIndent.createTitledBorder(BorderFactory.createEtchedBorder(), "Run on",
                                                                                          TitledBorder.DEFAULT_JUSTIFICATION,
                                                                                          TitledBorder.DEFAULT_POSITION, null, null));
      myOnEmulatorRadioButton = new JRadioButton();
      myOnEmulatorRadioButton.setText("Emulator:");
      myOnEmulatorRadioButton.setMnemonic('E');
      myOnEmulatorRadioButton.setDisplayedMnemonicIndex(0);
      myMobileRunPanel.add(myOnEmulatorRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                        GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myEmulatorCombo = new JComboBox();
      myMobileRunPanel.add(myEmulatorCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                null, null, 0, false));
      myEmulatorScreenSizePanel = new JPanel();
      myEmulatorScreenSizePanel.setLayout(new GridLayoutManager(1, 10, new Insets(0, 0, 0, 0), 0, -1));
      myMobileRunPanel.add(myEmulatorScreenSizePanel,
                           new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                               null, 0, false));
      final JLabel label3 = new JLabel();
      label3.setText("Screen:");
      label3.setDisplayedMnemonic('R');
      label3.setDisplayedMnemonicIndex(2);
      myEmulatorScreenSizePanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                null, null, 0, false));
      myScreenWidth = new JTextField();
      myEmulatorScreenSizePanel.add(myScreenWidth,
                                    new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                        null, 0, false));
      final JLabel label4 = new JLabel();
      label4.setHorizontalAlignment(0);
      label4.setHorizontalTextPosition(0);
      label4.setText("x");
      myEmulatorScreenSizePanel.add(label4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                                                                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                null, null, 0, false));
      myScreenHeight = new JTextField();
      myEmulatorScreenSizePanel.add(myScreenHeight,
                                    new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                        null, 0, false));
      final JLabel label5 = new JLabel();
      label5.setText("  Full:");
      label5.setDisplayedMnemonic('F');
      label5.setDisplayedMnemonicIndex(2);
      myEmulatorScreenSizePanel.add(label5, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                null, null, 0, false));
      myFullScreenWidth = new JTextField();
      myEmulatorScreenSizePanel.add(myFullScreenWidth,
                                    new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                        null, 0, false));
      final JLabel label6 = new JLabel();
      label6.setHorizontalAlignment(0);
      label6.setHorizontalTextPosition(0);
      label6.setText("x");
      myEmulatorScreenSizePanel.add(label6, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                                                                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                null, null, 0, false));
      myFullScreenHeight = new JTextField();
      myEmulatorScreenSizePanel.add(myFullScreenHeight,
                                    new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                        null, 0, false));
      final JLabel label7 = new JLabel();
      label7.setText("ppi:");
      label7.setDisplayedMnemonic('P');
      label7.setDisplayedMnemonicIndex(0);
      myEmulatorScreenSizePanel.add(label7, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                null, null, 0, false));
      myScreenDpi = new JTextField();
      myEmulatorScreenSizePanel.add(myScreenDpi,
                                    new GridConstraints(0, 9, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                        null, 0, false));
      final Spacer spacer3 = new Spacer();
      myMobileRunPanel.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
      final JPanel panel3 = new JPanel();
      panel3.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
      myMobileRunPanel.add(panel3, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                       null, null, 0, false));
      myOnAndroidDeviceRadioButton = new JRadioButton();
      myOnAndroidDeviceRadioButton.setText("Android device");
      myOnAndroidDeviceRadioButton.setMnemonic('V');
      myOnAndroidDeviceRadioButton.setDisplayedMnemonicIndex(10);
      panel3.add(myOnAndroidDeviceRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                   GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                                                   null, null, null, 0, false));
      myOnIOSDeviceRadioButton = new JRadioButton();
      myOnIOSDeviceRadioButton.setText("iOS device");
      myOnIOSDeviceRadioButton.setMnemonic('D');
      myOnIOSDeviceRadioButton.setDisplayedMnemonicIndex(4);
      panel3.add(myOnIOSDeviceRadioButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                               GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myFastPackagingCheckBox = new JCheckBox();
      myFastPackagingCheckBox.setText("Fast packaging");
      myFastPackagingCheckBox.setMnemonic('K');
      myFastPackagingCheckBox.setDisplayedMnemonicIndex(8);
      panel3.add(myFastPackagingCheckBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                              GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                              GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myClearAndroidDataCheckBox = new JCheckBox();
      myClearAndroidDataCheckBox.setText("Clear application data on each launch");
      myClearAndroidDataCheckBox.setMnemonic('I');
      myClearAndroidDataCheckBox.setDisplayedMnemonicIndex(10);
      panel3.add(myClearAndroidDataCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                 GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                                                 null, null, null, 0, false));
      final Spacer spacer4 = new Spacer();
      panel3.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
      final Spacer spacer5 = new Spacer();
      panel3.add(spacer5, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
      final JPanel panel4 = new JPanel();
      panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
      panel3.add(panel4, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                                             0, false));
      myOnIOSSimulatorRadioButton = new JRadioButton();
      myOnIOSSimulatorRadioButton.setText("iOS Simulator, SDK:");
      myOnIOSSimulatorRadioButton.setMnemonic('L');
      myOnIOSSimulatorRadioButton.setDisplayedMnemonicIndex(8);
      panel4.add(myOnIOSSimulatorRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                  GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                                                  null, null, null, 0, false));
      myIOSSimulatorSdkTextWithBrowse = new TextFieldWithBrowseButton();
      panel4.add(myIOSSimulatorSdkTextWithBrowse,
                 new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                     GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myIOSSimulatorDeviceLabel = new JBLabel();
      myIOSSimulatorDeviceLabel.setText("Simulator device:");
      panel4.add(myIOSSimulatorDeviceLabel,
                 new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                     GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myIOSSimulatorDeviceTextField = new JBTextField();
      panel4.add(myIOSSimulatorDeviceTextField,
                 new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                                     false));
      myMobileOptionsPanel = new JPanel();
      myMobileOptionsPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
      myMobileOptionsPanel.putClientProperty("BorderFactoryClass", "com.intellij.ui.IdeBorderFactory$PlainSmallWithIndent");
      myMainPanel.add(myMobileOptionsPanel, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                null, null, null, 0, false));
      myMobileOptionsPanel.setBorder(IdeBorderFactory.PlainSmallWithIndent.createTitledBorder(BorderFactory.createEtchedBorder(), "Options",
                                                                                              TitledBorder.DEFAULT_JUSTIFICATION,
                                                                                              TitledBorder.DEFAULT_POSITION, null, null));
      final JPanel panel5 = new JPanel();
      panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
      myMobileOptionsPanel.add(panel5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                           GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                           GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                           null, null, null, 0, false));
      myDebugTransportPanel = new JPanel();
      myDebugTransportPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
      panel5.add(myDebugTransportPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                            null, null, null, 0, false));
      myDebugOverNetworkRadioButton = new JRadioButton();
      myDebugOverNetworkRadioButton.setText("Network");
      myDebugOverNetworkRadioButton.setMnemonic('W');
      myDebugOverNetworkRadioButton.setDisplayedMnemonicIndex(3);
      myDebugTransportPanel.add(myDebugOverNetworkRadioButton,
                                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                    GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myDebugOverUSBRadioButton = new JRadioButton();
      myDebugOverUSBRadioButton.setText("USB, port:");
      myDebugOverUSBRadioButton.setMnemonic('U');
      myDebugOverUSBRadioButton.setDisplayedMnemonicIndex(0);
      myDebugTransportPanel.add(myDebugOverUSBRadioButton,
                                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                    GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myUsbDebugPortTextField = new JTextField();
      myUsbDebugPortTextField.setColumns(4);
      myDebugTransportPanel.add(myUsbDebugPortTextField,
                                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                    GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                    null, 0, false));
      final Spacer spacer6 = new Spacer();
      panel5.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                              GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
      myDebugOverLabel = new JLabel();
      myDebugOverLabel.setText("Debug on device over: ");
      myMobileOptionsPanel.add(myDebugOverLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                     GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                                     null, null, null, 0, false));
      myEmulatorAdlOptionsLabel = new JBLabel();
      myEmulatorAdlOptionsLabel.setText("ADL options (emulator):");
      myEmulatorAdlOptionsLabel.setDisplayedMnemonic('L');
      myEmulatorAdlOptionsLabel.setDisplayedMnemonicIndex(2);
      myMobileOptionsPanel.add(myEmulatorAdlOptionsLabel,
                               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                   GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                   false));
      myEmulatorAdlOptionsEditor = new RawCommandLineEditor();
      myEmulatorAdlOptionsEditor.setDialogCaption("ADL Options");
      myMobileOptionsPanel.add(myEmulatorAdlOptionsEditor,
                               new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                   GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                   new Dimension(150, -1), null, 0, false));
      myAppDescriptorForEmulatorLabel = new JLabel();
      myAppDescriptorForEmulatorLabel.setText("App descriptor (emulator):");
      myAppDescriptorForEmulatorLabel.setDisplayedMnemonic('O');
      myAppDescriptorForEmulatorLabel.setDisplayedMnemonicIndex(12);
      myMobileOptionsPanel.add(myAppDescriptorForEmulatorLabel,
                               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                   GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                   false));
      myAppDescriptorForEmulatorCombo = new JComboBox();
      myMobileOptionsPanel.add(myAppDescriptorForEmulatorCombo,
                               new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                   GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null,
                                                   0, false));
      myOverrideMainClassCheckBox = new JCheckBox();
      myOverrideMainClassCheckBox.setText("Override main class:");
      myOverrideMainClassCheckBox.setMnemonic('M');
      myOverrideMainClassCheckBox.setDisplayedMnemonicIndex(9);
      myMainPanel.add(myOverrideMainClassCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                       GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
      myOutputFileNameLabel = new JLabel();
      myOutputFileNameLabel.setText("Output file name:");
      myOutputFileNameLabel.setDisplayedMnemonic('T');
      myOutputFileNameLabel.setDisplayedMnemonicIndex(2);
      myMainPanel.add(myOutputFileNameLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                 null, null, 0, false));
      myOutputFileNameTextField = new JTextField();
      myMainPanel.add(myOutputFileNameTextField,
                      new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                          GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                          new Dimension(150, -1), null, 0, false));
      myMainClassComponent.setText("");
      myMainPanel.add(myMainClassComponent, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                                                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                new Dimension(150, -1), null, 0, false));
      final JLabel label8 = new JLabel();
      Font label8Font = this.$$$getFont$$$(null, Font.BOLD, -1, label8.getFont());
      if (label8Font != null) label8.setFont(label8Font);
      label8.setText("Build configuration:");
      label8.setDisplayedMnemonic('C');
      label8.setDisplayedMnemonicIndex(6);
      myMainPanel.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
      myMainPanel.add(myBCCombo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                     null, null, 0, false));
      myLauncherParametersLabel.setLabelFor(myLauncherParametersTextWithBrowse);
      label1.setLabelFor(myAdlOptionsEditor);
      label2.setLabelFor(myAirProgramParametersEditor);
      label3.setLabelFor(myScreenWidth);
      label5.setLabelFor(myFullScreenWidth);
      label7.setLabelFor(myScreenDpi);
      myIOSSimulatorDeviceLabel.setLabelFor(myIOSSimulatorDeviceTextField);
      myAppDescriptorForEmulatorLabel.setLabelFor(myAppDescriptorForEmulatorCombo);
      myOutputFileNameLabel.setLabelFor(myOutputFileNameTextField);
      label8.setLabelFor(myBCCombo);
      ButtonGroup buttonGroup;
      buttonGroup = new ButtonGroup();
      buttonGroup.add(myBCOutputRadioButton);
      buttonGroup.add(myUrlOrFileRadioButton);
      buttonGroup = new ButtonGroup();
      buttonGroup.add(myDebugOverNetworkRadioButton);
      buttonGroup.add(myDebugOverUSBRadioButton);
      buttonGroup = new ButtonGroup();
      buttonGroup.add(myOnEmulatorRadioButton);
      buttonGroup.add(myOnAndroidDeviceRadioButton);
      buttonGroup.add(myOnIOSDeviceRadioButton);
      buttonGroup.add(myOnIOSSimulatorRadioButton);
    }

    initBCCombo();
    initMainClassRelatedControls();
    initRadioButtons();
    initLaunchWithTextWithBrowse();
    initMobileControls();

    mySdkForDebuggingLabel.setLabelFor(mySdkForDebuggingCombo.getChildComponent());
    myEmulatorAdlOptionsLabel.setLabelFor(myEmulatorAdlOptionsEditor.getTextField());
  }

  /** @noinspection ALL */
  private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
    if (currentFont == null) return null;
    String resultName;
    if (fontName == null) {
      resultName = currentFont.getName();
    }
    else {
      Font testFont = new Font(fontName, Font.PLAIN, 10);
      if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
        resultName = fontName;
      }
      else {
        resultName = currentFont.getName();
      }
    }
    Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
    Font fontWithFallback = isMac
                            ? new Font(font.getFamily(), font.getStyle(), font.getSize())
                            : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
    return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
  }

  private static Method $$$cachedGetBundleMethod$$$ = null;

  /** @noinspection ALL */
  private String $$$getMessageFromBundle$$$(String path, String key) {
    ResourceBundle bundle;
    try {
      Class<?> thisClass = this.getClass();
      if ($$$cachedGetBundleMethod$$$ == null) {
        Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
        $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
      }
      bundle = (ResourceBundle)$$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
    }
    catch (Exception e) {
      bundle = ResourceBundle.getBundle(path);
    }
    return bundle.getString(key);
  }

  /** @noinspection ALL */
  private void $$$loadButtonText$$$(AbstractButton component, String text) {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '&') {
        i++;
        if (i == text.length()) break;
        if (!haveMnemonic && text.charAt(i) != '&') {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic) {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /** @noinspection ALL */
  public JComponent $$$getRootComponent$$$() { return myMainPanel; }

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
      public void documentChanged(final @NotNull DocumentEvent e) {
        final String shortName = StringUtil.getShortName(myMainClassComponent.getText().trim());
        if (!shortName.isEmpty()) {
          myOutputFileNameTextField.setText(shortName + ".swf");
        }
      }
    });

    myOutputFileNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final @NotNull javax.swing.event.DocumentEvent e) {
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

    myUrlOrFileTextWithBrowse.addBrowseFolderListener(myProject, FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
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

    myIOSSimulatorSdkTextWithBrowse.addBrowseFolderListener(myProject, FileChooserDescriptorFactory.createSingleFolderDescriptor());

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
    appDescriptorForEmulatorCombo.setRenderer(BuilderKt.textListCellRenderer(
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

    myEmulatorCombo.setRenderer(BuilderKt.textListCellRenderer("", value -> value.name));

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

  @Override
  protected @NotNull JComponent createEditor() {
    return myMainPanel;
  }

  @Override
  protected void resetEditorFrom(final @NotNull FlashRunConfiguration configuration) {
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
    final boolean windowsLocalFile = SystemInfo.isWindows && OSAgnosticPathUtil.startsWithWindowsDrive(url);
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
  protected void applyEditorTo(final @NotNull FlashRunConfiguration configuration) throws ConfigurationException {
    final FlashRunnerParameters params = configuration.getRunnerParameters();

    myBCCombo.applyTo(params);

    final boolean overrideMainClass = myOverrideMainClassCheckBox.isSelected();
    params.setOverrideMainClass(overrideMainClass);
    params.setOverriddenMainClass(overrideMainClass ? myMainClassComponent.getText().trim() : "");
    params.setOverriddenOutputFileName(overrideMainClass ? myOutputFileNameTextField.getText().trim() : "");

    params.setLaunchUrl(myUrlOrFileRadioButton.isSelected());
    final String url = myUrlOrFileTextWithBrowse.getText().trim();
    final boolean windowsLocalFile = SystemInfo.isWindows && OSAgnosticPathUtil.startsWithWindowsDrive(url);
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
