package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.actions.FilesToPackageForm;
import com.intellij.lang.javascript.flex.actions.SigningOptionsForm;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.ComboboxWithBrowseButton;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParameters.FilePathAndPathInPackage;

public class PackageAirInstallerDialog extends DialogWrapper {

  private JPanel myMainPanel;

  private LabeledComponent<FlexSdkComboBoxWithBrowseButton> myFlexSdkComponent;
  private LabeledComponent<ComboboxWithBrowseButton> myAirDescriptorComponent;
  private LabeledComponent<JTextField> myInstallerFileNameComponent;
  private LabeledComponent<TextFieldWithBrowseButton> myInstallerLocationComponent;

  private FilesToPackageForm myFilesToPackageForm;
  private JCheckBox myDoNotSignCheckBox;
  private SigningOptionsForm mySigningOptionsForm;

  private final Project myProject;

  private static final String TITLE = "Package AIR Installation File";

  private static final String AIR_INSTALLER_KEYSTORE_PASSWORD_KEY = "AIR_INSTALLER_KEYSTORE_PASSWORD_KEY";
  private static final String AIR_INSTALLER_KEY_PASSWORD_KEY = "AIR_INSTALLER_KEY_PASSWORD_KEY";

  public PackageAirInstallerDialog(final Project project) {
    super(project, true);
    myProject = project;
    setTitle(TITLE);
    setOKButtonText("Package");
    initAirDescriptorComponent();
    initInstallerLocationComponent();
    initDoNotSignCheckBox();

    loadDefaultParameters();
    updateSigningOptionsPanel();
    init();
  }

  public JComponent getPreferredFocusedComponent() {
    return myAirDescriptorComponent.getComponent().getComboBox();
  }

  private void updateSigningOptionsPanel() {
    mySigningOptionsForm.setEnabled(!myDoNotSignCheckBox.isSelected());
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
        myInstallerFileNameComponent.getComponent().setText(fileName + ".air");
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

      final Sdk flexSdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
      if (flexSdk != null) {
        myFlexSdkComponent.getComponent().getComboBox().setSelectedItem(flexSdk);
      }
    }

    try {
      final String contentPath = FlexUtils.findXMLElement(airDescriptor.getInputStream(), "<application><initialWindow><content>");
      if (!StringUtil.isEmptyOrSpaces(contentPath)) {
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

  private void initDoNotSignCheckBox() {
    myDoNotSignCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateSigningOptionsPanel();

        final String fileName = myInstallerFileNameComponent.getComponent().getText().trim();
        String newName = fileName.endsWith(".air")
                         ? fileName.substring(0, fileName.length() - ".air".length())
                         : fileName.endsWith(".airi") ? fileName.substring(0, fileName.length() - ".airi".length()) : fileName;
        newName += myDoNotSignCheckBox.isSelected() ? ".airi" : ".air";
        myInstallerFileNameComponent.getComponent().setText(newName);
      }
    });
  }


  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected void doOKAction() {
    myFilesToPackageForm.stopEditing();

    if (validateInput() && packageAirInstaller()) {
      super.doOKAction();
    }
  }

  private boolean validateInput() {
    final String errorMessage = getErrorMessage();
    if (errorMessage != null) {
      Messages.showErrorDialog(myProject, errorMessage, TITLE);
      return false;
    }
    return true;
  }

  @Nullable
  private String getErrorMessage() {
    final String airDescriptorPath = ((String)myAirDescriptorComponent.getComponent().getComboBox().getEditor().getItem()).trim();
    if (airDescriptorPath.length() == 0) {
      return "AIR application descriptor path is empty";
    }

    final VirtualFile descriptor = LocalFileSystem.getInstance().findFileByPath(airDescriptorPath);
    if (descriptor == null || descriptor.isDirectory()) {
      return FlexBundle.message("file.not.found", airDescriptorPath);
    }

    final String installerFileName = myInstallerFileNameComponent.getComponent().getText().trim();
    if (installerFileName.length() == 0) {
      return "AIR installer file name is empty";
    }

    final String installerLocation = FileUtil.toSystemDependentName(myInstallerLocationComponent.getComponent().getText().trim());
    if (installerLocation.length() == 0) {
      return "AIR installer location is empty";
    }

    final VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(installerLocation);
    if (dir == null || !dir.isDirectory()) {
      return FlexBundle.message("folder.does.not.exist", installerLocation);
    }

    if (myFilesToPackageForm.getFilesToPackage().isEmpty()) {
      return "No files to package";
    }

    for (FilePathAndPathInPackage path : myFilesToPackageForm.getFilesToPackage()) {
      final String fullPath = FileUtil.toSystemIndependentName(path.FILE_PATH.trim());
      String relPathInPackage = FileUtil.toSystemIndependentName(path.PATH_IN_PACKAGE.trim());
      if (relPathInPackage.startsWith("/")) {
        relPathInPackage = relPathInPackage.substring(1);
      }

      if (fullPath.length() == 0) {
        return "Empty file path to package";
      }

      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(fullPath);
      if (file == null) {
        return FlexBundle.message("file.not.found", fullPath);
      }

      if (relPathInPackage.length() == 0) {
        return "Empty relative file path in installation package";
      }

      if (file.isDirectory() && !fullPath.endsWith("/" + relPathInPackage)) {
        return MessageFormat.format("Relative folder path doesn''t match its full path: {0}", relPathInPackage);
      }
    }

    if (!myDoNotSignCheckBox.isSelected()) {
      final String keystorePath = mySigningOptionsForm.getKeystorePath();
      if (keystorePath.length() == 0) {
        return "Keystore file path is empty";
      }

      final VirtualFile keystore = LocalFileSystem.getInstance().findFileByPath(keystorePath);
      if (keystore == null || keystore.isDirectory()) {
        return FlexBundle.message("file.not.found", keystorePath);
      }

      if (mySigningOptionsForm.getKeystorePassword().length() == 0) {
        return "Keystore password is empty";
      }
    }
    return null;
  }

  private AirInstallerParameters getAirInstallerParameters() {
    final Sdk flexSdk = myFlexSdkComponent.getComponent().getSelectedSdk();
    final String airDescriptorPath = ((String)myAirDescriptorComponent.getComponent().getComboBox().getEditor().getItem()).trim();
    final String installerFileName = myInstallerFileNameComponent.getComponent().getText().trim();
    final String installerFileLocation = myInstallerLocationComponent.getComponent().getText().trim();
    final boolean doNotSign = myDoNotSignCheckBox.isSelected();
    final String keystorePath = mySigningOptionsForm.getKeystorePath();
    final String keystoreType = mySigningOptionsForm.getKeystoreType();
    final String keystorePassword = mySigningOptionsForm.getKeystorePassword();
    final String keyAlias = mySigningOptionsForm.getKeyAlias();
    final String keyPassword = mySigningOptionsForm.getKeyPassword();
    final String provider = mySigningOptionsForm.getProviderClassName();
    final String tsa = mySigningOptionsForm.getTsaUrl();

    return new AirInstallerParameters(flexSdk, airDescriptorPath, installerFileName, installerFileLocation,
                                      myFilesToPackageForm.getFilesToPackage(), doNotSign, keystorePath, keystoreType, keystorePassword,
                                      keyAlias, keyPassword, provider, tsa);
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
    mySigningOptionsForm.setProvisioningProfileApplicable(false);
  }

  private boolean packageAirInstaller() {
    final AirInstallerParameters parameters = getAirInstallerParameters();
    saveAsDefaultParameters(parameters);

    FileDocumentManager.getInstance().saveAllDocuments();
    final boolean ok = ExternalTask.runWithProgress(createAirInstallerTask(myProject, parameters), "Packaging AIR installer", TITLE);

    if (ok) {
      final String message = parameters.DO_NOT_SIGN
                             ? MessageFormat.format("Unsigned AIR package created: {0}", parameters.INSTALLER_FILE_NAME)
                             : MessageFormat.format("AIR installation file created: {0}", parameters.INSTALLER_FILE_NAME);
      ToolWindowManager.getInstance(myProject).notifyByBalloon(ToolWindowId.PROJECT_VIEW, MessageType.INFO, message);
    }

    return ok;
  }

  private void saveAsDefaultParameters(final AirInstallerParameters parameters) {
    AirInstallerParameters.getInstance(myProject).loadState(parameters);

    if (!parameters.DO_NOT_SIGN) {
      final PasswordSafe passwordSafe = PasswordSafe.getInstance();
      try {
        passwordSafe.storePassword(myProject, getClass(), AIR_INSTALLER_KEYSTORE_PASSWORD_KEY, parameters.getKeystorePassword());
        passwordSafe.storePassword(myProject, getClass(), AIR_INSTALLER_KEY_PASSWORD_KEY, parameters.getKeyPassword());
      }
      catch (PasswordSafeException ignore) {
      }
    }
  }

  private void loadDefaultParameters() {
    final AirInstallerParameters parameters = AirInstallerParameters.getInstance(myProject).getState();
    if (parameters != null) {
      if (!StringUtil.isEmpty(parameters.AIR_DESCRIPTOR_PATH)) {
        if (!StringUtil.isEmpty(parameters.SDK_NAME)) {
          final JComboBox combo = myFlexSdkComponent.getComponent().getComboBox();
          for (int i = 0; i < combo.getItemCount(); i++) {
            final Object item = combo.getItemAt(i);
            if (item instanceof Sdk && ((Sdk)item).getName().equals(parameters.SDK_NAME)) {
              combo.setSelectedItem(item);
              break;
            }
          }
        }

        myAirDescriptorComponent.getComponent().getComboBox().setSelectedItem(parameters.AIR_DESCRIPTOR_PATH);
        myInstallerFileNameComponent.getComponent().setText(parameters.INSTALLER_FILE_NAME);
        myInstallerLocationComponent.getComponent().setText(parameters.INSTALLER_FILE_LOCATION);
        myFilesToPackageForm.getFilesToPackage().clear();
        myFilesToPackageForm.getFilesToPackage().addAll(parameters.FILES_TO_PACKAGE);
        myDoNotSignCheckBox.setSelected(parameters.DO_NOT_SIGN);
        mySigningOptionsForm.setKeystorePath(parameters.KEYSTORE_PATH);
        mySigningOptionsForm.setKeystoreType(parameters.KEYSTORE_TYPE);
        mySigningOptionsForm.setKeyAlias(parameters.KEY_ALIAS);
        mySigningOptionsForm.setProviderClassName(parameters.PROVIDER_CLASS);
        mySigningOptionsForm.setTsaUrl(parameters.TSA);

        if (!parameters.DO_NOT_SIGN) {
          try {
            final PasswordSafe passwordSafe = PasswordSafe.getInstance();
            mySigningOptionsForm.setKeystorePassword(passwordSafe.getPassword(myProject, getClass(), AIR_INSTALLER_KEYSTORE_PASSWORD_KEY));
            mySigningOptionsForm.setKeyPassword(passwordSafe.getPassword(myProject, getClass(), AIR_INSTALLER_KEY_PASSWORD_KEY));
          }
          catch (PasswordSafeException ignored) {
          }
        }
      }
    }
  }

  private static ExternalTask createAirInstallerTask(final Project project, final AirInstallerParameters parameters) {
    return new AdtTask(project, parameters.getFlexSdk()) {
      protected void appendAdtOptions(List<String> command) {
        command.add(parameters.DO_NOT_SIGN ? "-prepare" : "-package");
        if (!parameters.DO_NOT_SIGN) {
          appendSigningOptions(command, parameters);
        }
        appendPaths(command, parameters);
      }
    };
  }

  protected String getHelpId() {
    return "reference.flex.package.air.installation.file";
  }
}

