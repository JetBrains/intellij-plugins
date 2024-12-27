// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.FlexBCTree;
import com.intellij.lang.javascript.flex.build.ValidateFlashConfigurationsPrecompileTask;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureProblemType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import static com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters.*;

public class AirPackageDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private FlexBCTree myTree;

  private JComboBox<DesktopPackageType> myDesktopTypeCombo;

  private JComboBox<AndroidPackageType> myAndroidTypeCombo;
  private JCheckBox myApkCaptiveRuntimeCheckBox;
  private JPanel myApkDebugPortPanel;
  private JTextField myApkDebugPortTextField;
  //private JPanel myApkDebugHostPanel;
  //private JTextField myApkDebugHostTextField;

  private JComboBox<IOSPackageType> myIOSTypeCombo;
  private JCheckBox myIosFastPackagingCheckBox;
  private JLabel myDesktopTypeLabel;
  private JLabel myAndroidTypeLabel;
  private JLabel myIosTypeLabel;

  private final Project myProject;
  //private final String myOwnIpAddress;
  private PasswordStore myPasswords;

  protected AirPackageDialog(final Project project) {
    super(project);
    myProject = project;
    //myOwnIpAddress = FlexUtils.getOwnIpAddress();

    setTitle(FlexBundle.message("package.air.application.title"));
    setOKButtonText("Package");
    setupComboBoxes();

    init();

    loadParameters();
    updateControlsVisibility();
    updateControlsEnabledState();
  }

  private void setupComboBoxes() {
    myDesktopTypeCombo.setModel(new DefaultComboBoxModel<>(DesktopPackageType.values()));
    myAndroidTypeCombo.setModel(new DefaultComboBoxModel<>(AndroidPackageType.values()));
    myIOSTypeCombo.setModel(new DefaultComboBoxModel<>(
      new IOSPackageType[]{IOSPackageType.Test, IOSPackageType.DebugOverNetwork, IOSPackageType.AdHoc, IOSPackageType.AppStore}));

    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControlsVisibility();
      }
    };

    myDesktopTypeCombo.addActionListener(listener);
    myAndroidTypeCombo.addActionListener(listener);
    myIOSTypeCombo.addActionListener(listener);
  }

  private void updateControlsVisibility() {
    final AndroidPackageType androidPackaging = (AndroidPackageType)myAndroidTypeCombo.getSelectedItem();
    myApkCaptiveRuntimeCheckBox.setVisible(androidPackaging == AndroidPackageType.Release);
    myApkDebugPortPanel.setVisible(androidPackaging == AndroidPackageType.DebugOverUSB);
    //myApkDebugHostPanel.setVisible(androidPackaging == AndroidPackageType.DebugOverNetwork);

    final IOSPackageType iosPackaging = (IOSPackageType)myIOSTypeCombo.getSelectedItem();
    myIosFastPackagingCheckBox.setVisible(iosPackaging == IOSPackageType.DebugOverNetwork || iosPackaging == IOSPackageType.Test);
  }

  private void updateControlsEnabledState() {
    boolean desktopPresent = false;
    boolean androidPresent = false;
    boolean iosPresent = false;

    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : getSelectedBCs()) {
      final FlexBuildConfiguration bc = moduleAndBC.second;
      final BuildConfigurationNature nature = bc.getNature();

      if (nature.isDesktopPlatform()) desktopPresent = true;
      if (nature.isMobilePlatform() && bc.getAndroidPackagingOptions().isEnabled()) androidPresent = true;
      if (nature.isMobilePlatform() && bc.getIosPackagingOptions().isEnabled()) iosPresent = true;
      if (desktopPresent && androidPresent && iosPresent) break;
    }

    myDesktopTypeLabel.setEnabled(desktopPresent);
    myDesktopTypeCombo.setEnabled(desktopPresent);

    myAndroidTypeLabel.setEnabled(androidPresent);
    myAndroidTypeCombo.setEnabled(androidPresent);
    myApkCaptiveRuntimeCheckBox.setEnabled(androidPresent);
    UIUtil.setEnabled(myApkDebugPortPanel, androidPresent, true);
    //UIUtil.setEnabled(myApkDebugHostPanel, androidPresent, true);

    myIosTypeLabel.setEnabled(iosPresent);
    myIOSTypeCombo.setEnabled(iosPresent);
    myIosFastPackagingCheckBox.setEnabled(iosPresent);
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  private void createUIComponents() {
    myTree = new FlexBCTree(myProject, bc -> {
      final BuildConfigurationNature nature = bc.getNature();
      return nature.isApp() && !nature.isWebPlatform();
    });

    myTree.addToggleCheckBoxListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        updateControlsEnabledState();
      }
    });
  }

  @Override
  protected String getDimensionServiceKey() {
    return "AirPackageDialog.DimensionServiceKey";
  }

  @Override
  protected String getHelpId() {
    return "reference.flex.package.air.application";
  }

  @Override
  protected ValidationInfo doValidate() {
    final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCs = getSelectedBCs();

    if (modulesAndBCs.isEmpty()) return new ValidationInfo("Please select one or more build configurations");

    if (myApkDebugPortTextField.isVisible() && myApkDebugPortPanel.isEnabled()) {
      try {
        final String portValue = myApkDebugPortTextField.getText().trim();
        final int port = portValue.isEmpty() ? AirPackageUtil.DEBUG_PORT_DEFAULT : Integer.parseInt(portValue);
        if (port <= 0 || port > 65535) return new ValidationInfo("Incorrect port", myApkDebugPortPanel);
      }
      catch (NumberFormatException e) {
        return new ValidationInfo("Incorrect port", myApkDebugPortTextField);
      }
    }

    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCs) {
      final FlexBuildConfiguration bc = moduleAndBC.second;

      if (bc.isSkipCompile() && LocalFileSystem.getInstance().findFileByPath(bc.getActualOutputFilePath()) == null) {
        return new ValidationInfo(
          FlexBundle.message("can.not.package.bc", bc.getName(), FlexBundle.message("compilation.is.switched.off")));
      }

      final BuildConfigurationNature nature = bc.getNature();
      if (nature.isMobilePlatform()) {
        if (!bc.getAndroidPackagingOptions().isEnabled() && !bc.getIosPackagingOptions().isEnabled()) {
          return new ValidationInfo(FlexBundle.message("can.not.package.bc", bc.getName(), "both Android and iOS packaging disabled"));
        }

        if (bc.getAndroidPackagingOptions().isEnabled() && bc.getIosPackagingOptions().isEnabled()) {
          final AndroidPackageType androidPackage = (AndroidPackageType)myAndroidTypeCombo.getSelectedItem();
          final IOSPackageType iosPackage = (IOSPackageType)myIOSTypeCombo.getSelectedItem();
          final boolean androidDebug = androidPackage != AndroidPackageType.Release;
          final boolean iosDebug = iosPackage == IOSPackageType.DebugOverNetwork;

          if (androidDebug != iosDebug) {
            return new ValidationInfo(FlexBundle.message("can.not.package.bc", bc.getName(),
                                                         FlexBundle.message("different.debug.settings", androidDebug ? 1 : 2)));
          }
        }
      }

      final Ref<String> firstErrorRef = new Ref<>();
      ValidateFlashConfigurationsPrecompileTask.checkPackagingOptions(moduleAndBC.first, bc, problem -> {
        if (problem.severity == ProjectStructureProblemType.Severity.ERROR && firstErrorRef.isNull()) {
          firstErrorRef.set(problem.errorMessage);
        }
      });

      // todo better error reporting. May be just mention that errors exist in some BC and provide link to Project Structure
      if (!firstErrorRef.isNull()) {
        return new ValidationInfo(FlexBundle.message("can.not.package.bc", bc.getName(), firstErrorRef.get()));
      }
    }

    return null;
  }

  @Override
  protected void doOKAction() {
    final Collection<Pair<Module, FlexBuildConfiguration>> selectedBCs = getSelectedBCs();
    if (!checkDisabledCompilation(myProject, selectedBCs)) return;
    if (!checkPasswords(selectedBCs)) return;

    saveParameters();
    super.doOKAction();
  }

  private static boolean checkDisabledCompilation(final Project project,
                                                  final Collection<Pair<Module, FlexBuildConfiguration>> selectedBCs) {
    final Collection<FlexBuildConfiguration> bcsWithDisabledCompilation = new ArrayList<>();

    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : selectedBCs) {
      if (moduleAndBC.second.isSkipCompile()) {
        bcsWithDisabledCompilation.add(moduleAndBC.second);
      }
    }

    if (!bcsWithDisabledCompilation.isEmpty()) {
      final StringBuilder bcs = new StringBuilder();
      for (FlexBuildConfiguration bc : bcsWithDisabledCompilation) {
        bcs.append("<b>").append(StringUtil.escapeXmlEntities(bc.getName())).append("</b><br>");
      }
      final String message = FlexBundle.message("package.bc.with.disabled.compilation", bcsWithDisabledCompilation.size(), bcs.toString());
      final int answer =
        Messages.showYesNoDialog(project, message, FlexBundle.message("package.air.application.title"), Messages.getWarningIcon());

      return answer == Messages.YES;
    }

    return true;
  }

  private boolean checkPasswords(final Collection<Pair<Module, FlexBuildConfiguration>> selectedBCs) {
    final Collection<AirPackagingOptions> allPackagingOptions = new ArrayList<>();

    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : selectedBCs) {
      final FlexBuildConfiguration bc = moduleAndBC.second;
      if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
        if (myDesktopTypeCombo.getSelectedItem() != DesktopPackageType.Airi) {
          allPackagingOptions.add(bc.getAirDesktopPackagingOptions());
        }
      }
      else {
        if (bc.getAndroidPackagingOptions().isEnabled()) {
          allPackagingOptions.add(bc.getAndroidPackagingOptions());
        }
        if (bc.getIosPackagingOptions().isEnabled()) {
          allPackagingOptions.add(bc.getIosPackagingOptions());
        }
      }
    }

    myPasswords = AirPackageAction.getPasswords(myProject, allPackagingOptions);
    return myPasswords != null;
  }

  private void loadParameters() {
    final AirPackageProjectParameters params = getInstance(myProject);

    myDesktopTypeCombo.setSelectedItem(params.desktopPackageType);

    myAndroidTypeCombo.setSelectedItem(params.androidPackageType);
    myApkCaptiveRuntimeCheckBox.setSelected(params.apkCaptiveRuntime);
    myApkDebugPortTextField.setText(String.valueOf(params.apkDebugListenPort));
    //myApkDebugHostTextField.setText(params.apkDebugConnectHost.isEmpty() ? myOwnIpAddress : params.apkDebugConnectHost);

    myIOSTypeCombo.setSelectedItem(params.iosPackageType);
    myIosFastPackagingCheckBox.setSelected(params.iosFastPackaging);

    for (String entry : StringUtil.split(params.deselectedBCs, "\n")) {
      final int tabIndex = entry.indexOf("\t");
      if (tabIndex > 0) {
        myTree.setChecked(entry.substring(0, tabIndex), entry.substring(tabIndex + 1), false);
      }
    }
  }

  private void saveParameters() {
    final AirPackageProjectParameters params = getInstance(myProject);

    params.desktopPackageType = (DesktopPackageType)myDesktopTypeCombo.getSelectedItem();

    params.androidPackageType = (AndroidPackageType)myAndroidTypeCombo.getSelectedItem();
    params.apkCaptiveRuntime = myApkCaptiveRuntimeCheckBox.isSelected();

    try {
      final String portValue = myApkDebugPortTextField.getText().trim();
      final int port = portValue.isEmpty() ? AirPackageUtil.DEBUG_PORT_DEFAULT : Integer.parseInt(portValue);
      if (port > 0 && port <= 65535) {
        params.apkDebugListenPort = port;
      }
    }
    catch (NumberFormatException e) {/*ignore*/}

    //final String host = myApkDebugHostTextField.getText().trim();
    //params.apkDebugConnectHost = host.equals(myOwnIpAddress) ? "" : host;

    params.iosPackageType = (IOSPackageType)myIOSTypeCombo.getSelectedItem();
    params.iosFastPackaging = myIosFastPackagingCheckBox.isSelected();

    final Collection<Pair<Module, FlexBuildConfiguration>> deselectedBCs = myTree.getDeselectedBCs();
    final StringBuilder buf = new StringBuilder();
    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : deselectedBCs) {
      if (!buf.isEmpty()) buf.append('\n');
      buf.append(moduleAndBC.first.getName()).append('\t').append(moduleAndBC.second.getName());
    }
    params.deselectedBCs = buf.toString();
  }

  public Collection<Pair<Module, FlexBuildConfiguration>> getSelectedBCs() {
    return myTree.getSelectedBCs();
  }

  public PasswordStore getPasswords() {
    assert isOK() : "ask for passwords only after OK in dialog";

    return myPasswords;
  }
}
