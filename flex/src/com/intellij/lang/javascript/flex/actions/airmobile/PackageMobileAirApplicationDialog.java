package com.intellij.lang.javascript.flex.actions.airmobile;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.FilesToPackageForm;
import com.intellij.lang.javascript.flex.actions.SigningOptionsForm;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParameters.FilePathAndPathInPackage;
import static com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters.*;

public class PackageMobileAirApplicationDialog extends DialogWrapper implements PanelWithAnchor {

  private JPanel myMainPanel;

  private JComboBox myTargetPlatformCombo;
  private JLabel myPackageTypeLabel;
  private JComboBox myAndroidPackageTypeCombo;
  private JComboBox myIOSPackageTypeCombo;
  private JCheckBox myFastPackagingCheckBox;

  private LabeledComponent<FlexSdkComboBoxWithBrowseButton> myFlexSdkComponent;
  private LabeledComponent<ComboboxWithBrowseButton> myAirDescriptorComponent;
  private LabeledComponent<JTextField> myInstallerFileNameComponent;
  private LabeledComponent<TextFieldWithBrowseButton> myInstallerLocationComponent;
  private JComponent anchor;

  private FilesToPackageForm myFilesToPackageForm;
  private SigningOptionsForm mySigningOptionsForm;

  private final Project myProject;
  private MobileAirPackageParameters myPackageParameters;

  public static final String TITLE = "Package Mobile AIR Application";

  private static final String MOBILE_AIR_PACKAGE_KEYSTORE_PASSWORD_KEY = "MOBILE_AIR_PACKAGE_KEYSTORE_PASSWORD_KEY";
  private static final String MOBILE_AIR_PACKAGE_KEY_PASSWORD_KEY = "MOBILE_AIR_PACKAGE_KEY_PASSWORD_KEY";

  public PackageMobileAirApplicationDialog(final Project project) {
    super(project, true);
    myProject = project;
    setTitle(TITLE);
    setOKButtonText("Package");
    initTargetSpecificControls();
    initAirDescriptorComponent();
    initInstallerLocationComponent();

    loadDefaultParameters();
    updateTargetSpecificControls();

    init();

    setAnchor(myFlexSdkComponent.getLabel());
  }

  public JComponent getPreferredFocusedComponent() {
    return myAirDescriptorComponent.getComponent().getComboBox();
  }

  private void updateTargetSpecificControls() {
    final boolean isAndroid = myTargetPlatformCombo.getSelectedItem() == MobilePlatform.Android;
    final boolean isIOS = myTargetPlatformCombo.getSelectedItem() == MobilePlatform.iOS;

    myAndroidPackageTypeCombo.setVisible(isAndroid);
    myIOSPackageTypeCombo.setVisible(isIOS);
    myPackageTypeLabel.setLabelFor(isAndroid ? myAndroidPackageTypeCombo : myIOSPackageTypeCombo);
    updateFastPackagingCheckBox();

    mySigningOptionsForm.setProvisioningProfileApplicable(isIOS);
    mySigningOptionsForm.setCreateCertificateButtonApplicable(isAndroid);
  }

  private void updateFastPackagingCheckBox() {
    final IOSPackageType type = (IOSPackageType)myIOSPackageTypeCombo.getSelectedItem();
    myFastPackagingCheckBox.setVisible(myTargetPlatformCombo.getSelectedItem() == MobilePlatform.iOS &&
                                       (type == IOSPackageType.DebugOverNetwork || type == IOSPackageType.Test));
  }

  private void initAirDescriptorComponent() {
    final ComboboxWithBrowseButton comboWithBrowse = myAirDescriptorComponent.getComponent();
    comboWithBrowse.addBrowseFolderListener(myProject, new FileChooserDescriptor(true, false, false, false, false, false) {
      public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
        return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || "xml".equalsIgnoreCase(file.getExtension()));
      }
    });

    final JComboBox comboBox = comboWithBrowse.getComboBox();
    comboBox.setEditable(true);
    final String[] descriptorPaths = FlexUtils.collectAirDescriptorsForProject(myProject);
    if (descriptorPaths.length > 0) {
      comboBox.setModel(new DefaultComboBoxModel(descriptorPaths));
      onAirDescriptorSelected(descriptorPaths[0]);
    }

    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onAirDescriptorSelected((String)comboBox.getEditor().getItem());
      }
    });
  }

  private void onAirDescriptorSelected(final String airDescriptorPath) {
    if (StringUtil.isEmpty(airDescriptorPath)) return;

    final VirtualFile airDescriptor = LocalFileSystem.getInstance().findFileByPath(airDescriptorPath);
    if (airDescriptor == null) return;

    try {
      final String fileName = FlexUtils.findXMLElement(airDescriptor.getInputStream(), "<application><filename>");
      if (!StringUtil.isEmpty(fileName)) {
        myInstallerFileNameComponent.getComponent().setText(fileName + getDotExtension());
      }
    }
    catch (IOException e) {/*ignore*/}

    final Module module = ModuleUtil.findModuleForFile(airDescriptor, myProject);
    if (module != null) {
      final CompilerModuleExtension compilerExtension = CompilerModuleExtension.getInstance(module);
      if (compilerExtension != null) {
        myInstallerLocationComponent.getComponent()
          .setText(FileUtil.toSystemDependentName(VfsUtil.urlToPath(compilerExtension.getCompilerOutputUrl())));
      }

      final Sdk flexSdk = FlexUtils.getSdkForActiveBC(module);
      if (flexSdk != null) {
        myFlexSdkComponent.getComponent().getComboBox().setSelectedItem(flexSdk);
      }
    }

    try {
      final String contentPath = FlexUtils.findXMLElement(airDescriptor.getInputStream(), "<application><initialWindow><content>");
      if (contentPath != null && !StringUtil.isEmptyOrSpaces(contentPath)) {
        final List<FilePathAndPathInPackage> filesToPackage = myFilesToPackageForm.getFilesToPackage();
        if (filesToPackage.isEmpty() || !filesToPackage.get(0).PATH_IN_PACKAGE.equals(contentPath)) {
          boolean tableRowAdded = false;
          if (module != null) {
            final String fileName = FileUtil.toSystemIndependentName(contentPath).substring(contentPath.lastIndexOf("/") + 1);

            for (String path : getOutputFilePaths(module)) {
              if (path.endsWith("/" + fileName)) {
                filesToPackage.add(0, new FilePathAndPathInPackage(FileUtil.toSystemDependentName(path), contentPath));
                tableRowAdded = true;
                break;
              }
            }
          }

          if (!tableRowAdded) {
            filesToPackage.add(0, new FilePathAndPathInPackage("", contentPath));
          }

          myFilesToPackageForm.fireDataChanged();
        }
      }
    }
    catch (IOException e) {/*ignore*/}
  }

  private void initInstallerLocationComponent() {
    myInstallerLocationComponent.getComponent()
      .addBrowseFolderListener(null, null, myProject, new FileChooserDescriptor(false, true, false, false, false, false) {
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
          return super.isFileVisible(file, showHiddenFiles) && file.isDirectory();
        }
      }, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
  }

  private void initTargetSpecificControls() {
    myTargetPlatformCombo.setModel(new DefaultComboBoxModel(MobilePlatform.values()));
    myTargetPlatformCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateTargetSpecificControls();

        final String fileName = myInstallerFileNameComponent.getComponent().getText().trim();
        String newName = fileName.endsWith(".apk")
                         ? fileName.substring(0, fileName.length() - ".apk".length())
                         : fileName.endsWith(".ipa") ? fileName.substring(0, fileName.length() - ".ipa".length()) : fileName;
        newName += getDotExtension();
        myInstallerFileNameComponent.getComponent().setText(newName);
      }
    });

    myAndroidPackageTypeCombo.setModel(new DefaultComboBoxModel(AndroidPackageType.values()));
    myIOSPackageTypeCombo.setModel(new DefaultComboBoxModel(IOSPackageType.values()));

    myIOSPackageTypeCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateFastPackagingCheckBox();
      }
    });
  }

  private String getDotExtension() {
    final boolean isAndroid = myTargetPlatformCombo.getSelectedItem() == MobilePlatform.Android;
    final boolean isIOS = myTargetPlatformCombo.getSelectedItem() == MobilePlatform.iOS;
    return isAndroid ? ".apk" : isIOS ? ".ipa" : "";
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected void doOKAction() {
    myFilesToPackageForm.stopEditing();
    myPackageParameters = createPackageParameters();
    saveAsDefaultParameters(myPackageParameters);

    final String adtVersion = MobileAirUtil.getAdtVersion(myProject, myPackageParameters.getFlexSdk());
    if (MobileAirUtil.checkAdtVersionForPackaging(myProject, adtVersion, myPackageParameters)) {
      super.doOKAction();
    }
  }

  public MobileAirPackageParameters getPackageParameters() {
    return myPackageParameters;
  }

  protected ValidationInfo doValidate() {
    final String airDescriptorPath = ((String)myAirDescriptorComponent.getComponent().getComboBox().getEditor().getItem()).trim();
    if (airDescriptorPath.length() == 0) {
      return new ValidationInfo("AIR application descriptor path is empty", myAirDescriptorComponent.getComponent());
    }

    final VirtualFile descriptor = LocalFileSystem.getInstance().findFileByPath(airDescriptorPath);
    if (descriptor == null || descriptor.isDirectory()) {
      return new ValidationInfo(FlexBundle.message("file.not.found", airDescriptorPath), myAirDescriptorComponent.getComponent());
    }

    final String installerFileName = myInstallerFileNameComponent.getComponent().getText().trim();
    if (installerFileName.length() == 0) {
      return new ValidationInfo("Package file name is empty", myInstallerFileNameComponent.getComponent());
    }

    final String installerLocation = FileUtil.toSystemDependentName(myInstallerLocationComponent.getComponent().getText().trim());
    if (installerLocation.length() == 0) {
      return new ValidationInfo("Package file location is empty", myInstallerLocationComponent.getComponent());
    }

    final VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(installerLocation);
    if (dir == null || !dir.isDirectory()) {
      return new ValidationInfo(FlexBundle.message("folder.does.not.exist", installerLocation),
                                myInstallerLocationComponent.getComponent());
    }

    if (myFilesToPackageForm.getFilesToPackage().isEmpty()) {
      return new ValidationInfo("No files to package", myFilesToPackageForm.getMainPanel());
    }

    for (FilePathAndPathInPackage path : myFilesToPackageForm.getFilesToPackage()) {
      final String fullPath = FileUtil.toSystemIndependentName(path.FILE_PATH.trim());
      String relPathInPackage = FileUtil.toSystemIndependentName(path.PATH_IN_PACKAGE.trim());
      if (relPathInPackage.startsWith("/")) {
        relPathInPackage = relPathInPackage.substring(1);
      }

      if (fullPath.length() == 0) {
        return new ValidationInfo("Empty file path to package", myFilesToPackageForm.getMainPanel());
      }

      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(fullPath);
      if (file == null) {
        return new ValidationInfo(FlexBundle.message("file.not.found", fullPath), myFilesToPackageForm.getMainPanel());
      }

      if (relPathInPackage.length() == 0) {
        return new ValidationInfo("Empty relative file path in installation package", myFilesToPackageForm.getMainPanel());
      }

      if (file.isDirectory() && !fullPath.endsWith("/" + relPathInPackage)) {
        return new ValidationInfo(MessageFormat.format("Relative folder path doesn''t match its full path: {0}", relPathInPackage),
                                  myFilesToPackageForm.getMainPanel());
      }
    }

    return mySigningOptionsForm.validate();
  }

  private MobileAirPackageParameters createPackageParameters() {
    final MobilePlatform mobilePlatform = (MobilePlatform)myTargetPlatformCombo.getSelectedItem();
    final AndroidPackageType androidPackageType = (AndroidPackageType)myAndroidPackageTypeCombo.getSelectedItem();
    final IOSPackageType iOSPackageType = (IOSPackageType)myIOSPackageTypeCombo.getSelectedItem();
    final Sdk flexSdk = myFlexSdkComponent.getComponent().getSelectedSdk();
    final String airDescriptorPath = ((String)myAirDescriptorComponent.getComponent().getComboBox().getEditor().getItem()).trim();
    final String installerFileName = myInstallerFileNameComponent.getComponent().getText().trim();
    final String installerFileLocation = myInstallerLocationComponent.getComponent().getText().trim();
    final String provisioningProfilePath = mySigningOptionsForm.getProvisioningProfilePath();
    final String keystorePath = mySigningOptionsForm.getKeystorePath();
    final String keystoreType = mySigningOptionsForm.getKeystoreType();
    final String keystorePassword = mySigningOptionsForm.getKeystorePassword();
    final String keyAlias = mySigningOptionsForm.getKeyAlias();
    final String keyPassword = mySigningOptionsForm.getKeyPassword();
    final String provider = mySigningOptionsForm.getProviderClassName();
    final String tsa = mySigningOptionsForm.getTsaUrl();

    return new MobileAirPackageParameters(mobilePlatform, androidPackageType, iOSPackageType, myFastPackagingCheckBox.isSelected(),
                                          flexSdk, airDescriptorPath, installerFileName,
                                          installerFileLocation, myFilesToPackageForm.getFilesToPackage(),
                                          MobileAirUtil.getLocalHostAddress(), MobileAirUtil.DEBUG_PORT_DEFAULT, "",
                                          provisioningProfilePath, keystorePath, keystoreType, keystorePassword, keyAlias, keyPassword,
                                          provider, tsa);
  }

  @Override
  public JComponent getAnchor() {
    return anchor;
  }

  @Override
  public void setAnchor(JComponent anchor) {
    this.anchor = anchor;
    myAirDescriptorComponent.setAnchor(anchor);
    myInstallerFileNameComponent.setAnchor(anchor);
    myInstallerLocationComponent.setAnchor(anchor);
    myFlexSdkComponent.setAnchor(anchor);
  }

  private static List<String> getOutputFilePaths(final Module module) {
    final List<String> result = new ArrayList<String>();
    for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
      if (config.USE_CUSTOM_CONFIG_FILE) {
        final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(config.CUSTOM_CONFIG_FILE);
        if (configFile != null) {
          try {
            final String outputPath = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><output>");
            if (outputPath != null) {
              result.add(FileUtil.toSystemIndependentName(outputPath));
            }
          }
          catch (IOException e) {/*ignore*/}
        }
      }
      else {
        result.add(config.getOutputFileFullPath());
      }
    }
    return result;
  }

  private void createUIComponents() {
    myFlexSdkComponent = new LabeledComponent<FlexSdkComboBoxWithBrowseButton>();
    myFlexSdkComponent.setComponent(new FlexSdkComboBoxWithBrowseButton());
    myFilesToPackageForm = new FilesToPackageForm(myProject);

    final Computable<Module> moduleComputable = new Computable<Module>() {
      public Module compute() {
        final String airDescriptorPath = ((String)myAirDescriptorComponent.getComponent().getComboBox().getEditor().getItem()).trim();
        if (airDescriptorPath.length() > 0) {
          final VirtualFile airDescriptor = LocalFileSystem.getInstance().findFileByPath(airDescriptorPath);
          if (airDescriptor != null) {
            return ModuleUtil.findModuleForFile(airDescriptor, myProject);
          }
        }
        return null;
      }
    };

    final Computable<Sdk> sdkComputable = new Computable<Sdk>() {
      public Sdk compute() {
        return myFlexSdkComponent.getComponent().getSelectedSdk();
      }
    };

    final Runnable resizeHandler = new Runnable() {
      public void run() {
        getPeer().setSize(getPeer().getSize().width, getPeer().getPreferredSize().height);
      }
    };

    mySigningOptionsForm = new SigningOptionsForm(myProject, moduleComputable, sdkComputable, resizeHandler);
    mySigningOptionsForm.setUseTempCertificateCheckBoxVisible(false); // todo make true for Android
  }

  private void saveAsDefaultParameters(final MobileAirPackageParameters parameters) {
    MobileAirPackageParameters.getInstance(myProject).loadState(parameters);

    final PasswordSafe passwordSafe = PasswordSafe.getInstance();
    try {
      passwordSafe.storePassword(myProject, getClass(), MOBILE_AIR_PACKAGE_KEYSTORE_PASSWORD_KEY, parameters.getKeystorePassword());
      passwordSafe.storePassword(myProject, getClass(), MOBILE_AIR_PACKAGE_KEY_PASSWORD_KEY, parameters.getKeyPassword());
    }
    catch (PasswordSafeException ignore) {
    }
  }

  private void loadDefaultParameters() {
    final MobileAirPackageParameters parameters = MobileAirPackageParameters.getInstance(myProject).getState();
    if (!StringUtil.isEmpty(parameters.AIR_DESCRIPTOR_PATH)) {
      myTargetPlatformCombo.setSelectedItem(parameters.MOBILE_PLATFORM);
      myAndroidPackageTypeCombo.setSelectedItem(parameters.ANDROID_PACKAGE_TYPE);
      myIOSPackageTypeCombo.setSelectedItem(parameters.IOS_PACKAGE_TYPE);
      myFastPackagingCheckBox.setSelected(parameters.FAST_PACKAGING);

      if (!StringUtil.isEmpty(parameters.SDK_NAME)) {
        final JComboBox sdkCombo = myFlexSdkComponent.getComponent().getComboBox();
        for (int i = 0; i < sdkCombo.getItemCount(); i++) {
          final Object item = sdkCombo.getItemAt(i);
          if (item instanceof Sdk && ((Sdk)item).getName().equals(parameters.SDK_NAME)) {
            sdkCombo.setSelectedItem(item);
            break;
          }
        }
      }

      myAirDescriptorComponent.getComponent().getComboBox().setSelectedItem(parameters.AIR_DESCRIPTOR_PATH);
      myInstallerFileNameComponent.getComponent().setText(parameters.INSTALLER_FILE_NAME);
      myInstallerLocationComponent.getComponent().setText(parameters.INSTALLER_FILE_LOCATION);
      myFilesToPackageForm.resetFrom(parameters.FILES_TO_PACKAGE);

      mySigningOptionsForm.setProvisioningProfilePath(parameters.PROVISIONING_PROFILE_PATH);
      mySigningOptionsForm.setKeystorePath(parameters.KEYSTORE_PATH);
      mySigningOptionsForm.setKeystoreType(parameters.KEYSTORE_TYPE);
      mySigningOptionsForm.setKeyAlias(parameters.KEY_ALIAS);
      mySigningOptionsForm.setProviderClassName(parameters.PROVIDER_CLASS);
      mySigningOptionsForm.setTsaUrl(parameters.TSA);

      try {
        final PasswordSafe passwordSafe = PasswordSafe.getInstance();
        mySigningOptionsForm.setKeystorePassword(passwordSafe.getPassword(myProject, getClass(), MOBILE_AIR_PACKAGE_KEYSTORE_PASSWORD_KEY));
        mySigningOptionsForm.setKeyPassword(passwordSafe.getPassword(myProject, getClass(), MOBILE_AIR_PACKAGE_KEY_PASSWORD_KEY));
      }
      catch (PasswordSafeException ignored) {/*ignore*/}
    }
  }

  protected String getHelpId() {
    return "reference.flex.package.mobile.air.application";
  }
}

