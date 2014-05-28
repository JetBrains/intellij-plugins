package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.FlexBCTree;
import com.intellij.lang.javascript.flex.build.FlashProjectStructureProblem;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.ui.AirPackagingConfigurableBase;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureProblemType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters.*;

public class AirPackageDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private FlexBCTree myTree;

  private JComboBox myDesktopTypeCombo;

  private JComboBox myAndroidTypeCombo;
  private JCheckBox myApkCaptiveRuntimeCheckBox;
  private JPanel myApkDebugPortPanel;
  private JTextField myApkDebugPortTextField;
  //private JPanel myApkDebugHostPanel;
  //private JTextField myApkDebugHostTextField;

  private JComboBox myIOSTypeCombo;
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
    myDesktopTypeCombo.setModel(new DefaultComboBoxModel(DesktopPackageType.values()));
    myAndroidTypeCombo.setModel(new DefaultComboBoxModel(AndroidPackageType.values()));
    myIOSTypeCombo.setModel(new DefaultComboBoxModel(
      new IOSPackageType[]{IOSPackageType.Test, IOSPackageType.DebugOverNetwork, IOSPackageType.AdHoc, IOSPackageType.AppStore}));

    final ActionListener listener = new ActionListener() {
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

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  private void createUIComponents() {
    myTree = new FlexBCTree(myProject, new Condition<FlexBuildConfiguration>() {
      public boolean value(final FlexBuildConfiguration bc) {
        final BuildConfigurationNature nature = bc.getNature();
        return nature.isApp() && !nature.isWebPlatform();
      }
    });

    myTree.addToggleCheckBoxListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        updateControlsEnabledState();
      }
    });
  }

  protected String getDimensionServiceKey() {
    return "AirPackageDialog.DimensionServiceKey";
  }

  protected String getHelpId() {
    return "reference.flex.package.air.application";
  }

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

      final Ref<String> firstErrorRef = new Ref<String>();
      checkPackagingOptions(moduleAndBC.first, bc, new Consumer<FlashProjectStructureProblem>() {
        public void consume(final FlashProjectStructureProblem problem) {
          if (problem.severity == ProjectStructureProblemType.Severity.ERROR && firstErrorRef.isNull()) {
            firstErrorRef.set(problem.errorMessage);
          }
        }
      });

      // todo better error reporting. May be just mention that errors exist in some BC and provide link to Project Structure
      if (!firstErrorRef.isNull()) {
        return new ValidationInfo(FlexBundle.message("can.not.package.bc", bc.getName(), firstErrorRef.get()));
      }
    }

    return null;
  }

  public static void checkPackagingOptions(final Module module,
                                           final FlexBuildConfiguration bc,
                                           final Consumer<FlashProjectStructureProblem> errorConsumer) {
    if (bc.getOutputType() != OutputType.Application) return;

    if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
      checkPackagingOptions(module, bc.getSdk(), bc.getAirDesktopPackagingOptions(), false,
                            PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
    }
    else if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      if (bc.getAndroidPackagingOptions().isEnabled()) {
        checkPackagingOptions(module, bc.getSdk(), bc.getAndroidPackagingOptions(), false,
                              PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
      }
      if (bc.getIosPackagingOptions().isEnabled()) {
        checkPackagingOptions(module, bc.getSdk(), bc.getIosPackagingOptions(), false,
                              PathUtil.getParentPath(bc.getActualOutputFilePath()), errorConsumer);
      }
    }
  }
  
  private static void checkPackagingOptions(final Module module,
                                            final @Nullable Sdk sdk,
                                            final AirPackagingOptions packagingOptions,
                                            final boolean isForIosSimulator,
                                            final String outputFolderPath,
                                            final Consumer<FlashProjectStructureProblem> errorConsumer) {
    final String device = packagingOptions instanceof AndroidPackagingOptions
                          ? "Android"
                          : packagingOptions instanceof IosPackagingOptions
                            ? "iOS"
                            : "";
    if (!packagingOptions.isUseGeneratedDescriptor()) {
      if (packagingOptions.getCustomDescriptorPath().isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem
                                .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions,
                                                               FlexBundle.message("custom.descriptor.not.set", device),
                                                               AirPackagingConfigurableBase.Location.CustomDescriptor));
      }
      else {
        final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(packagingOptions.getCustomDescriptorPath());
        if (descriptorFile == null || descriptorFile.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                    .message("custom.descriptor.not.found", device,
                                             FileUtil.toSystemDependentName(packagingOptions.getCustomDescriptorPath())),
                                                                 AirPackagingConfigurableBase.Location.CustomDescriptor));
        }
        else if (sdk != null && sdk.getSdkType() != FlexmojosSdkType.getInstance()) {
          checkAirVersionIfCustomDescriptor(module, sdk, packagingOptions, errorConsumer, false, "does not matter");
        }
      }
    }

    if (packagingOptions.getPackageFileName().isEmpty()) {
      errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
        .message("package.file.name.not.set", device), AirPackagingConfigurableBase.Location.PackageFileName));
    }

    for (AirPackagingOptions.FilePathAndPathInPackage entry : packagingOptions.getFilesToPackage()) {
      final String fullPath = entry.FILE_PATH;
      String relPathInPackage = entry.PATH_IN_PACKAGE;
      if (relPathInPackage.startsWith("/")) {
        relPathInPackage = relPathInPackage.substring(1);
      }

      if (fullPath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
          .message("packaging.options.empty.file.name", device), AirPackagingConfigurableBase.Location.FilesToPackage));
      }
      else {
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(fullPath);
        if (file == null) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                    .message("packaging.options.file.not.found", device, FileUtil.toSystemDependentName(fullPath)),
                                                                 AirPackagingConfigurableBase.Location.FilesToPackage));
        }

        if (relPathInPackage.isEmpty()) {
          errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
            .message("packaging.options.empty.relative.path", device), AirPackagingConfigurableBase.Location.FilesToPackage));
        }

        if (file != null && file.isDirectory()) {
          if (FileUtil.isAncestor(file.getPath(), outputFolderPath, false)) {
            errorConsumer.consume(FlashProjectStructureProblem
                                    .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                      .message("folder.to.package.includes.output", device, file.getPresentableUrl()),
                                                                   AirPackagingConfigurableBase.Location.FilesToPackage));
          }
          else if (!relPathInPackage.isEmpty() && !".".equals(relPathInPackage) && !fullPath.endsWith("/" + relPathInPackage)) {
            errorConsumer.consume(
              FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                .message("packaging.options.relative.path.not.matches", device, FileUtil.toSystemDependentName(relPathInPackage)),
                                                                         AirPackagingConfigurableBase.Location.FilesToPackage));
          }
        }
      }
    }

    if (packagingOptions instanceof IosPackagingOptions) {
      final String path = packagingOptions.getSigningOptions().getIOSSdkPath();
      if (!path.isEmpty() && !new File(path).isDirectory()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
          .message("packaging.options.bad.ios.sdk.path", device, FileUtil.toSystemDependentName(path)),
                                                                                         AirPackagingConfigurableBase.Location.IosSdkPath));
      }
    }

    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();
    if (packagingOptions instanceof IosPackagingOptions && !isForIosSimulator) {
      final String provisioningProfilePath = signingOptions.getProvisioningProfilePath();
      if (provisioningProfilePath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
          .message("ios.provisioning.profile.not.set"), AirPackagingConfigurableBase.Location.ProvisioningProfile));
      }
      else {
        final VirtualFile provisioningProfile = LocalFileSystem.getInstance().findFileByPath(provisioningProfilePath);
        if (provisioningProfile == null || provisioningProfile.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                    .message("ios.provisioning.profile.not.found", FileUtil.toSystemDependentName(provisioningProfilePath)),
                                                                 AirPackagingConfigurableBase.Location.ProvisioningProfile));
        }
      }
    }

    final boolean tempCertificate = packagingOptions instanceof IosPackagingOptions ? isForIosSimulator
                                                                                    : signingOptions.isUseTempCertificate();
    if (!tempCertificate) {
      final String keystorePath = signingOptions.getKeystorePath();
      if (keystorePath.isEmpty()) {
        errorConsumer.consume(FlashProjectStructureProblem.createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions,
                                                                                         FlexBundle.message("keystore.not.set", device),
                                                                                         AirPackagingConfigurableBase.Location.Keystore));
      }
      else {
        final VirtualFile keystore = LocalFileSystem.getInstance().findFileByPath(keystorePath);
        if (keystore == null || keystore.isDirectory()) {
          errorConsumer.consume(FlashProjectStructureProblem
                                  .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.ERROR, packagingOptions, FlexBundle
                                    .message("keystore.not.found", device, FileUtil.toSystemDependentName(keystorePath)),
                                                                 AirPackagingConfigurableBase.Location.Keystore));
        }
      }
    }
  }

  public static void checkAirVersionIfCustomDescriptor(final Module module,
                                                       final Sdk sdk,
                                                       final AirPackagingOptions packagingOptions,
                                                       final Consumer<FlashProjectStructureProblem> errorConsumer,
                                                       final boolean errorMessageForRunConfigValidation,
                                                       final String bcName) {
    if (packagingOptions.isUseGeneratedDescriptor()) return;
    if (packagingOptions.getCustomDescriptorPath().isEmpty()) return;
    final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(packagingOptions.getCustomDescriptorPath());
    if (descriptorFile == null || descriptorFile.isDirectory()) return;
    if (sdk.getSdkType() == FlexmojosSdkType.getInstance()) return;


    final PsiFile file = PsiManager.getInstance(module.getProject()).findFile(descriptorFile);
    final XmlTag rootTag = file instanceof XmlFile ? ((XmlFile)file).getRootTag() : null;
    final String ns = rootTag == null ? null : rootTag.getNamespace();
    final String nsVersion = ns != null && ns.startsWith(FlexCommonUtils.AIR_NAMESPACE_BASE)
                             ? ns.substring(FlexCommonUtils.AIR_NAMESPACE_BASE.length())
                             : null;
    final String airSdkVersion = FlexCommonUtils.getAirVersion(sdk.getHomePath(), sdk.getVersionString());

    if (nsVersion != null && airSdkVersion != null && !nsVersion.equals(airSdkVersion)) {
      final String message;
      if (errorMessageForRunConfigValidation) {
        message = FlexBundle.message("bc.0.module.1.air.version.mismatch.warning", bcName, module.getName(), nsVersion, airSdkVersion,
                                     FileUtil.toSystemDependentName(descriptorFile.getPath()));
      }
      else {
        final String device = packagingOptions instanceof AndroidPackagingOptions
                              ? "Android"
                              : packagingOptions instanceof IosPackagingOptions
                                ? "iOS"
                                : "";
        message = FlexBundle.message("air.version.mismatch.warning", device, descriptorFile.getName(), nsVersion, airSdkVersion);
      }
      errorConsumer.consume(FlashProjectStructureProblem
                              .createPackagingOptionsProblem(ProjectStructureProblemType.Severity.WARNING, packagingOptions, message,
                                                             AirPackagingConfigurableBase.Location.CustomDescriptor));
    }
  }

  protected void doOKAction() {
    final Collection<Pair<Module, FlexBuildConfiguration>> selectedBCs = getSelectedBCs();
    if (!checkDisabledCompilation(myProject, selectedBCs)) return;
    if (!checkPasswords(selectedBCs)) return;

    saveParameters();
    super.doOKAction();
  }

  private static boolean checkDisabledCompilation(final Project project,
                                                  final Collection<Pair<Module, FlexBuildConfiguration>> selectedBCs) {
    final Collection<FlexBuildConfiguration> bcsWithDisabledCompilation = new ArrayList<FlexBuildConfiguration>();

    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : selectedBCs) {
      if (moduleAndBC.second.isSkipCompile()) {
        bcsWithDisabledCompilation.add(moduleAndBC.second);
      }
    }

    if (!bcsWithDisabledCompilation.isEmpty()) {
      final StringBuilder bcs = new StringBuilder();
      for (FlexBuildConfiguration bc : bcsWithDisabledCompilation) {
        bcs.append("<b>").append(StringUtil.escapeXml(bc.getName())).append("</b><br>");
      }
      final String message = FlexBundle.message("package.bc.with.disabled.compilation", bcsWithDisabledCompilation.size(), bcs.toString());
      final int answer =
        Messages.showYesNoDialog(project, message, FlexBundle.message("package.air.application.title"), Messages.getWarningIcon());

      return answer == Messages.YES;
    }

    return true;
  }

  private boolean checkPasswords(final Collection<Pair<Module, FlexBuildConfiguration>> selectedBCs) {
    final Collection<AirPackagingOptions> allPackagingOptions = new ArrayList<AirPackagingOptions>();

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
    final AirPackageProjectParameters params = AirPackageProjectParameters.getInstance(myProject);

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
    final AirPackageProjectParameters params = AirPackageProjectParameters.getInstance(myProject);

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
      if (buf.length() > 0) buf.append('\n');
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
