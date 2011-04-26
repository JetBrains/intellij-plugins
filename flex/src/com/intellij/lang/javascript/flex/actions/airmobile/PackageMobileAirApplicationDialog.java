package com.intellij.lang.javascript.flex.actions.airmobile;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.actions.airinstaller.CertificateParameters;
import com.intellij.lang.javascript.flex.actions.airinstaller.CreateCertificateDialog;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.CellEditorComponentWithBrowseButton;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParameters.FilePathAndPathInPackage;
import static com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters.*;

public class PackageMobileAirApplicationDialog extends DialogWrapper {

  private JPanel myMainPanel;

  private JComboBox myTargetPlatformCombo;
  private JLabel myPackageTypeLabel;
  private JComboBox myAndroidPackageTypeCombo;
  private JComboBox myIOSPackageTypeCombo;

  private LabeledComponent<FlexSdkComboBoxWithBrowseButton> myFlexSdkComponent;
  private LabeledComponent<ComboboxWithBrowseButton> myAirDescriptorComponent;
  private LabeledComponent<JTextField> myInstallerFileNameComponent;
  private LabeledComponent<TextFieldWithBrowseButton> myInstallerLocationComponent;

  private JTable myFilesToPackageTable;
  private JButton myAddButton;
  private JButton myRemoveButton;

  private JLabel myProvisioningProfileLabel;
  private TextFieldWithBrowseButton myProvisioningProfileTextWithBrowse;
  private JButton myCreateCertButton;
  private JComboBox myKeystoreTypeCombo;
  private JPasswordField myKeystorePasswordField;
  private TextFieldWithBrowseButton myKeystoreFileTextWithBrowse;
  private JLabel myKeyAliasLabel;
  private JTextField myKeyAliasTextField;
  private JLabel myKeyPasswordLabel;
  private JPasswordField myKeyPasswordField;
  private JLabel myProviderClassNameLabel;
  private JTextField myProviderClassNameTextField;
  private JLabel myTsaUrlLabel;
  private JTextField myTsaUrlTextField;
  private HoverHyperlinkLabel myMoreOptionsHyperlinkLabel;

  private final Project myProject;

  private final List<FilePathAndPathInPackage> myFilesToPackage = new ArrayList<FilePathAndPathInPackage>();
  private static final String TITLE = "Package Mobile AIR Application";

  private static final String MORE_OPTIONS = "More options";
  private static final String LESS_OPTIONS = "Less options";

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
    initTable();
    initTableButtons();
    initCreateCertButton();
    initMoreOptionsHyperlinkLabel();

    myProvisioningProfileTextWithBrowse
      .addBrowseFolderListener(null, null, myProject, new FileChooserDescriptor(true, false, false, false, false, false));
    myKeystoreFileTextWithBrowse.addBrowseFolderListener(null, null, myProject,
                                                         new FileChooserDescriptor(true, false, false, false, false, false));

    loadDefaultParameters();
    updateTargetSpecificControls();
    updateRemoveButtonState();
    updateMoreOptions();

    init();
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

    myProvisioningProfileLabel.setVisible(isIOS);
    myProvisioningProfileTextWithBrowse.setVisible(isIOS);
    myCreateCertButton.setVisible(isAndroid);
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

      final Sdk flexSdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
      if (flexSdk != null) {
        myFlexSdkComponent.getComponent().getComboBox().setSelectedItem(flexSdk);
      }
    }

    try {
      final String contentPath = FlexUtils.findXMLElement(airDescriptor.getInputStream(), "<application><initialWindow><content>");
      if (contentPath != null && !StringUtil.isEmptyOrSpaces(contentPath)) {
        if (myFilesToPackage.isEmpty() || !myFilesToPackage.get(0).PATH_IN_PACKAGE.equals(contentPath)) {
          boolean tableRowAdded = false;
          if (module != null) {
            final String fileName = FileUtil.toSystemIndependentName(contentPath).substring(contentPath.lastIndexOf("/") + 1);

            for (String path : getOutputFilePaths(module)) {
              if (path.endsWith("/" + fileName)) {
                myFilesToPackage.add(0, new FilePathAndPathInPackage(FileUtil.toSystemDependentName(path), contentPath));
                tableRowAdded = true;
                break;
              }
            }
          }

          if (!tableRowAdded) {
            myFilesToPackage.add(0, new FilePathAndPathInPackage("", contentPath));
          }

          ((AbstractTableModel)myFilesToPackageTable.getModel()).fireTableDataChanged();
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

  private void initTable() {
    myFilesToPackageTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // otherwise model is not in sync with view
    myFilesToPackageTable.setRowHeight(20);

    myFilesToPackageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateRemoveButtonState();
      }
    });

    myFilesToPackageTable.setModel(new DefaultTableModel() {

      public int getColumnCount() {
        return Column.values().length;
      }

      public int getRowCount() {
        return myFilesToPackage.size();
      }

      public String getColumnName(int column) {
        return Column.values()[column].getColumnName();
      }

      public Class<?> getColumnClass(int column) {
        return Column.values()[column].getColumnClass();
      }

      public Object getValueAt(int row, int column) {
        return Column.values()[column].getValue(myFilesToPackage.get(row));
      }

      public void setValueAt(Object aValue, int row, int column) {
        Column.values()[column].setValue(myFilesToPackage, row, aValue);
      }
    });

    myFilesToPackageTable.getColumnModel().getColumn(0).setCellEditor(new AbstractTableCellEditor() {
      private CellEditorComponentWithBrowseButton<JTextField> myComponent;

      public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        final ActionListener listener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            FileChooserDescriptor d = new FileChooserDescriptor(true, true, false, true, false, false);

            VirtualFile initialFile = LocalFileSystem.getInstance().findFileByPath((String)getCellEditorValue());
            VirtualFile[] files = FileChooser.chooseFiles(myProject, d, initialFile);
            if (files.length == 1 && files[0] != null) {
              myComponent.getChildComponent().setText(files[0].getPresentableUrl());
            }
          }
        };

        myComponent = new CellEditorComponentWithBrowseButton<JTextField>(new TextFieldWithBrowseButton(listener), this);
        myComponent.getChildComponent().setText((String)value);
        return myComponent;
      }

      public Object getCellEditorValue() {
        return myComponent.getChildComponent().getText();
      }
    });
  }

  private void initTableButtons() {
    myAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VirtualFile[] files = FileChooser.chooseFiles(myProject, new FileChooserDescriptor(true, true, false, true, false, false));
        if (files.length == 1 && files[0] != null) {
          myFilesToPackage.add(new FilePathAndPathInPackage(files[0].getPresentableUrl(), files[0].getName()));
          ((AbstractTableModel)myFilesToPackageTable.getModel()).fireTableDataChanged();
        }
      }
    });

    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myFilesToPackageTable.isEditing()) {
          myFilesToPackageTable.getCellEditor().stopCellEditing();
        }
        final int[] selectedRows = myFilesToPackageTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
          myFilesToPackage.remove(selectedRows[i]);
        }
        ((AbstractTableModel)myFilesToPackageTable.getModel()).fireTableDataChanged();
      }
    });
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
  }

  private String getDotExtension() {
    final boolean isAndroid = myTargetPlatformCombo.getSelectedItem() == MobilePlatform.Android;
    final boolean isIOS = myTargetPlatformCombo.getSelectedItem() == MobilePlatform.iOS;
    return isAndroid ? ".apk" : isIOS ? ".ipa" : "";
  }

  private void initCreateCertButton() {
    myCreateCertButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final Sdk flexSdk = myFlexSdkComponent.getComponent().getSelectedSdk();
        if (flexSdk == null) {
          Messages.showErrorDialog(myProject, "Flex or AIR SDK is required to create certificate", CreateCertificateDialog.TITLE);
        }
        else {
          final CreateCertificateDialog dialog = new CreateCertificateDialog(myProject, flexSdk, suggestKeystoreFileLocation());
          dialog.show();
          if (dialog.isOK()) {
            final CertificateParameters parameters = dialog.getCertificateParameters();
            myKeystoreFileTextWithBrowse.setText(parameters.getKeystoreFilePath());
            myKeystoreTypeCombo.setSelectedIndex(0);
            myKeystorePasswordField.setText(parameters.getKeystorePassword());
          }
        }
      }
    });
  }

  private void initMoreOptionsHyperlinkLabel() {
    myMoreOptionsHyperlinkLabel.setText(MORE_OPTIONS);
    myMoreOptionsHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          showMoreOptions(!isShowingMoreOptions());

          int preferredHeight = getContentPane().getPreferredSize().height;
          getPeer().setSize(getPeer().getSize().width, preferredHeight);
        }
      }
    });
  }

  private String suggestKeystoreFileLocation() {
    final String airDescriptorPath = ((String)myAirDescriptorComponent.getComponent().getComboBox().getEditor().getItem()).trim();
    if (airDescriptorPath.length() > 0) {
      final VirtualFile airDescriptor = LocalFileSystem.getInstance().findFileByPath(airDescriptorPath);
      if (airDescriptor != null) {
        final Module module = ModuleUtil.findModuleForFile(airDescriptor, myProject);
        if (module != null) {
          final VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
          if (contentRoots.length > 0) {
            return contentRoots[0].getPath();
          }
        }
      }
    }
    final VirtualFile baseDir = myProject.getBaseDir();
    return baseDir == null ? "" : baseDir.getPath();
  }


  private void showMoreOptions(final boolean show) {
    myMoreOptionsHyperlinkLabel.setText(show ? LESS_OPTIONS : MORE_OPTIONS);
    updateMoreOptions();
  }

  private boolean isShowingMoreOptions() {
    return myMoreOptionsHyperlinkLabel.getText().contains(LESS_OPTIONS);
  }

  private void updateRemoveButtonState() {
    myRemoveButton.setEnabled(myFilesToPackageTable.getSelectedRowCount() > 0);
  }

  private void updateMoreOptions() {
    final boolean showingMoreOption = isShowingMoreOptions();
    myKeyAliasLabel.setVisible(showingMoreOption);
    myKeyAliasTextField.setVisible(showingMoreOption);
    myKeyPasswordLabel.setVisible(showingMoreOption);
    myKeyPasswordField.setVisible(showingMoreOption);
    myProviderClassNameLabel.setVisible(showingMoreOption);
    myProviderClassNameTextField.setVisible(showingMoreOption);
    myTsaUrlLabel.setVisible(showingMoreOption);
    myTsaUrlTextField.setVisible(showingMoreOption);
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected void doOKAction() {
    final TableCellEditor cellEditor = myFilesToPackageTable.getCellEditor();
    if (cellEditor != null) {
      // apply currently edited value if any
      cellEditor.stopCellEditing();
    }

    if (validateInput() && packageMobileApplication()) {
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

    if (myFilesToPackage.isEmpty()) {
      return "No files to package";
    }

    for (FilePathAndPathInPackage path : myFilesToPackage) {
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

    if (myTargetPlatformCombo.getSelectedItem() == MobilePlatform.iOS) {
      final String provisioningProfilePath = myProvisioningProfileTextWithBrowse.getText().trim();
      if (provisioningProfilePath.length() == 0) {
        return "Provisioning profile file path is empty";
      }

      final VirtualFile provisioningProfile = LocalFileSystem.getInstance().findFileByPath(provisioningProfilePath);
      if (provisioningProfile == null || provisioningProfile.isDirectory()) {
        return FlexBundle.message("file.not.found", provisioningProfilePath);
      }
    }

    final String keystorePath = myKeystoreFileTextWithBrowse.getText().trim();
    if (keystorePath.length() == 0) {
      return "Keystore file path is empty";
    }

    final VirtualFile keystore = LocalFileSystem.getInstance().findFileByPath(keystorePath);
    if (keystore == null || keystore.isDirectory()) {
      return FlexBundle.message("file.not.found", keystorePath);
    }

    if (myKeystorePasswordField.getPassword().length == 0) {
      return "Keystore password is empty";
    }
    return null;
  }

  private MobileAirPackageParameters getPackageParameters() {
    final MobilePlatform mobilePlatform = (MobilePlatform)myTargetPlatformCombo.getSelectedItem();
    final AndroidPackageType androidPackageType = (AndroidPackageType)myAndroidPackageTypeCombo.getSelectedItem();
    final IOSPackageType iOSPackageType = (IOSPackageType)myIOSPackageTypeCombo.getSelectedItem();
    final Sdk flexSdk = myFlexSdkComponent.getComponent().getSelectedSdk();
    final String airDescriptorPath = ((String)myAirDescriptorComponent.getComponent().getComboBox().getEditor().getItem()).trim();
    final String installerFileName = myInstallerFileNameComponent.getComponent().getText().trim();
    final String installerFileLocation = myInstallerLocationComponent.getComponent().getText().trim();
    final String provisioningProfilePath = myProvisioningProfileTextWithBrowse.getText().trim();
    final String keystorePath = myKeystoreFileTextWithBrowse.getText().trim();
    final String keystoreType = (String)myKeystoreTypeCombo.getSelectedItem();
    final String keystorePassword = new String(myKeystorePasswordField.getPassword());
    final boolean showingMore = isShowingMoreOptions();
    final String keyAlias = showingMore ? myKeyAliasTextField.getText().trim() : "";
    final String keyPassword = showingMore ? new String(myKeyPasswordField.getPassword()) : "";
    final String provider = showingMore ? myProviderClassNameTextField.getText().trim() : "";
    final String tsa = showingMore ? myTsaUrlTextField.getText().trim() : "";

    return new MobileAirPackageParameters(mobilePlatform, androidPackageType, iOSPackageType, flexSdk, airDescriptorPath, installerFileName,
                                          installerFileLocation, myFilesToPackage, MobileAirUtil.getLocalHostAddress(),
                                          MobileAirUtil.DEBUG_PORT_DEFAULT, "", provisioningProfilePath, keystorePath, keystoreType,
                                          keystorePassword, keyAlias, keyPassword, provider, tsa);
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

    myMoreOptionsHyperlinkLabel = new HoverHyperlinkLabel(MORE_OPTIONS);
  }

  private boolean packageMobileApplication() {
    final MobileAirPackageParameters parameters = getPackageParameters();
    saveAsDefaultParameters(parameters);

    final boolean ok = ExternalTask.runWithProgress(MobileAirUtil.createMobileAirPackageTask(myProject, parameters),
                                                    FlexBundle.message("packaging.application", parameters.MOBILE_PLATFORM), TITLE);

    if (ok) {
      final String message = FlexBundle.message("application.created", parameters.MOBILE_PLATFORM, parameters.INSTALLER_FILE_NAME);
      ToolWindowManager.getInstance(myProject).notifyByBalloon(ToolWindowId.PROJECT_VIEW, MessageType.INFO, message);
    }

    return ok;
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
      myFilesToPackage.clear();
      myFilesToPackage.addAll(parameters.FILES_TO_PACKAGE);

      myProvisioningProfileTextWithBrowse.setText(parameters.PROVISIONING_PROFILE_PATH);
      myKeystoreFileTextWithBrowse.setText(parameters.KEYSTORE_PATH);
      myKeystoreTypeCombo.setSelectedItem(parameters.KEYSTORE_TYPE);
      myKeyAliasTextField.setText(parameters.KEY_ALIAS);
      myProviderClassNameTextField.setText(parameters.PROVIDER_CLASS);
      myTsaUrlTextField.setText(parameters.TSA);

      try {
        final PasswordSafe passwordSafe = PasswordSafe.getInstance();
        myKeystorePasswordField.setText(passwordSafe.getPassword(myProject, getClass(), MOBILE_AIR_PACKAGE_KEYSTORE_PASSWORD_KEY));
        myKeyPasswordField.setText(passwordSafe.getPassword(myProject, getClass(), MOBILE_AIR_PACKAGE_KEY_PASSWORD_KEY));
      }
      catch (PasswordSafeException ignored) {/*ignore*/}

      if (StringUtil.isNotEmpty(parameters.KEY_ALIAS) ||
          StringUtil.isNotEmpty(parameters.PROVIDER_CLASS) ||
          StringUtil.isNotEmpty(parameters.TSA)) {
        showMoreOptions(true);
      }
    }
  }

  private enum Column {
    Path("Path to file or folder", String.class) {
      Object getValue(final FilePathAndPathInPackage row) {
        return row.FILE_PATH;
      }

      void setValue(final List<FilePathAndPathInPackage> myFilesToPackage, final int row, final Object value) {
        myFilesToPackage.get(row).FILE_PATH = (String)value;
      }
    },

    RelativePath("Its relative path in installation package", String.class) {
      Object getValue(final FilePathAndPathInPackage row) {
        return row.PATH_IN_PACKAGE;
      }

      void setValue(final List<FilePathAndPathInPackage> myFilePathsToPackage, final int row, final Object value) {
        myFilePathsToPackage.get(row).PATH_IN_PACKAGE = (String)value;
      }
    };

    private final String myColumnName;
    private final Class myColumnClass;

    private Column(final String columnName, final Class columnClass) {
      myColumnName = columnName;
      myColumnClass = columnClass;
    }

    public String getColumnName() {
      return myColumnName;
    }

    private Class getColumnClass() {
      return myColumnClass;
    }

    abstract Object getValue(FilePathAndPathInPackage row);

    abstract void setValue(List<FilePathAndPathInPackage> myFilesToPackage, int row, Object value);
  }

  protected String getHelpId() {
    return "reference.flex.package.air.installation.file";
  }
}

