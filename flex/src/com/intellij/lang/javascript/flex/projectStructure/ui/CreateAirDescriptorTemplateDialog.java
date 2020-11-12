package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.AirDescriptorOptions;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.intellij.flex.build.AirDescriptorOptions.*;

public class CreateAirDescriptorTemplateDialog extends DialogWrapper {

  public static final Pattern VERSION_PATTERN = Pattern.compile("[0-9]{1,3}(\\.[0-9]{1,3}){0,2}");

  private JPanel myMainPanel;

  private JTextField myDescriptorFileNameTextField;
  private TextFieldWithBrowseButton myDescriptorFolderTextWithBrowse;
  private JComboBox myAirVersionCombo;

  private JTextField myAppIdTextField;
  private JTextField myAppNameTextField;
  private JTextField myAppVersionTextField;

  private JPanel myMobileOptionsPanel;

  private JCheckBox myAndroidCheckBox;
  private JCheckBox myIOSCheckBox;
  private JCheckBox myAutoOrientCheckBox;
  private JCheckBox myFullScreenCheckBox;

  private JBTabbedPane myMobilePlatformsTabs;

  private JPanel myAndroidPanel;
  private JCheckBox myAndroidInternetCheckBox;
  private JCheckBox myAndroidWriteExternalStorageCheckBox;
  private JCheckBox myAndroidAccessFineLocationCheckBox;
  private JCheckBox myAndroidCameraCheckBox;

  private JPanel myIOSPanel;
  private JRadioButton myIOSAllRadioButton;
  private JRadioButton myIPhoneRadioButton;
  private JRadioButton myIPadRadioButton;
  private JCheckBox myIOSHighResolutionCheckBox;

  private final Project myProject;
  private final String[] myExtensions;

  public CreateAirDescriptorTemplateDialog(final Project project,
                                           final String folderPath,
                                           final String mainClass,
                                           final String airVersion,
                                           final String[] extensions,
                                           final boolean androidEnabled,
                                           final boolean iosEnabled) {
    super(project);
    myProject = project;
    myExtensions = extensions;
    setTitle(getTitleText());
    setOKButtonText("Create");
    initControls();

    init();

    final String appName = StringUtil.getShortName(mainClass);
    myDescriptorFileNameTextField.setText(appName + "-app.xml");
    myDescriptorFolderTextWithBrowse.setText(FileUtil.toSystemDependentName(folderPath));
    myAirVersionCombo.setSelectedItem(airVersion);
    myAppIdTextField.setText(mainClass);
    myAppNameTextField.setText(appName);
    myAppVersionTextField.setText("0.0.0");
    myAndroidCheckBox.setSelected(androidEnabled);
    UIUtil.applyStyle(UIUtil.ComponentStyle.MINI, myAndroidInternetCheckBox);
    UIUtil.applyStyle(UIUtil.ComponentStyle.MINI, myAndroidWriteExternalStorageCheckBox);
    UIUtil.applyStyle(UIUtil.ComponentStyle.MINI, myAndroidAccessFineLocationCheckBox);
    UIUtil.applyStyle(UIUtil.ComponentStyle.MINI, myAndroidCameraCheckBox);
    myAndroidInternetCheckBox.setSelected(true);
    myAndroidWriteExternalStorageCheckBox.setSelected(false);
    myAndroidAccessFineLocationCheckBox.setSelected(false);
    myAndroidCameraCheckBox.setSelected(false);
    myIOSCheckBox.setSelected(iosEnabled);
    myIOSAllRadioButton.setSelected(true);

    myMobileOptionsPanel.setVisible(androidEnabled || iosEnabled);
    updateControls();
  }

  private void initControls() {
    myDescriptorFolderTextWithBrowse
      .addBrowseFolderListener(null, null, myProject, FileChooserDescriptorFactory.createSingleFolderDescriptor());

    final String[] items = Arrays.copyOf(ArrayUtil.reverseArray(FlexApplicationComponent.AIR_VERSIONS), 8);
    myAirVersionCombo.setModel(new DefaultComboBoxModel(items));

    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };

    myAndroidCheckBox.addActionListener(listener);
    myIOSCheckBox.addActionListener(listener);
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  protected ValidationInfo doValidate() {
    final String fileName = myDescriptorFileNameTextField.getText().trim();
    if (fileName.isEmpty()) {
      return new ValidationInfo("Descriptor file name not set", myDescriptorFileNameTextField);
    }
    if (!StringUtil.toLowerCase(fileName).endsWith(".xml")) {
      return new ValidationInfo("Descriptor file name must have xml extension", myDescriptorFileNameTextField);
    }

    final String folderPath = myDescriptorFolderTextWithBrowse.getText().trim();
    if (folderPath.isEmpty()) {
      return new ValidationInfo("Folder is not set", myDescriptorFolderTextWithBrowse);
    }
    final VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(folderPath);
    if (dir != null && !dir.isDirectory()) {
      return new ValidationInfo("Folder for AIR descriptor must be specified", myDescriptorFolderTextWithBrowse);
    }

    final String airVersion = ((String)myAirVersionCombo.getSelectedItem()).trim();
    if (airVersion.isEmpty()) {
      return new ValidationInfo("AIR version is not set", myAirVersionCombo);
    }
    if (!VERSION_PATTERN.matcher(airVersion).matches()) {
      return new ValidationInfo("Incorrect AIR version", myAirVersionCombo);
    }

    final String appId = myAppIdTextField.getText().trim();
    if (appId.isEmpty()) {
      return new ValidationInfo("Application ID is required", myAppIdTextField);
    }
    if (!appId.equals(FlexCommonUtils.fixApplicationId(appId))) {
      return new ValidationInfo("Application ID must contain only following symbols: 0-9, a-z, A-Z, '.', '-'", myAppIdTextField);
    }

    if (myAppNameTextField.getText().trim().isEmpty()) {
      return new ValidationInfo("Application name is required", myAppNameTextField);
    }

    final String appVersion = myAppVersionTextField.getText().trim();
    if (appVersion.isEmpty()) {
      return new ValidationInfo("Application version is not set", myAppVersionTextField);
    }
    if (StringUtil.compareVersionNumbers(airVersion, "2.5") >= 0) {
      if (!VERSION_PATTERN.matcher(appVersion).matches()) {
        return new ValidationInfo("Application version must have following format: [0-999].[0-999].[0-999]", myAppVersionTextField);
      }
    }

    return null;
  }

  private void updateControls() {
    UIUtil.setEnabled(myMobilePlatformsTabs.getTabComponentAt(0), myAndroidCheckBox.isSelected(), true);
    UIUtil.setEnabled(myAndroidPanel, myAndroidCheckBox.isSelected(), true);
    UIUtil.setEnabled(myMobilePlatformsTabs.getTabComponentAt(1), myIOSCheckBox.isSelected(), true);
    UIUtil.setEnabled(myIOSPanel, myIOSCheckBox.isSelected(), true);
  }

  @Override
  protected void doOKAction() {
    final String airVersion = ((String)myAirVersionCombo.getSelectedItem()).trim();
    final String appId = myAppIdTextField.getText().trim();
    final String appName = myAppNameTextField.getText().trim();
    final String appVersion = myAppVersionTextField.getText().trim();
    final String swfName = "SWF file name is set automatically at compile time";
    final boolean mobile = myMobileOptionsPanel.isVisible();
    final boolean autoOrients = mobile && myAutoOrientCheckBox.isSelected();
    final boolean fullScreen = mobile && myFullScreenCheckBox.isSelected();
    final boolean android = mobile && myAndroidCheckBox.isSelected();
    final int androidPermissions = !mobile || !android ? 0 :
                                   (myAndroidInternetCheckBox.isSelected() ? ANDROID_PERMISSION_INTERNET : 0)
                                   | (myAndroidWriteExternalStorageCheckBox.isSelected() ? ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE : 0)
                                   | (myAndroidAccessFineLocationCheckBox.isSelected() ? ANDROID_PERMISSION_ACCESS_FINE_LOCATION : 0)
                                   | (myAndroidCameraCheckBox.isSelected() ? ANDROID_PERMISSION_CAMERA : 0);
    final boolean ios = mobile && myIOSCheckBox.isSelected();
    final boolean iPhone = mobile && ios && (myIOSAllRadioButton.isSelected() || myIPhoneRadioButton.isSelected());
    final boolean iPad = mobile && ios && (myIOSAllRadioButton.isSelected() || myIPadRadioButton.isSelected());
    final boolean iosHighResolution = mobile && ios && myIOSHighResolutionCheckBox.isSelected();

    final AirDescriptorOptions options =
      new AirDescriptorOptions(airVersion, appId, appName, appVersion, swfName, myExtensions,
                               mobile, autoOrients, fullScreen,
                               android, androidPermissions,
                               ios, iPhone, iPad, iosHighResolution);

    if (createAirDescriptorTemplate(myProject, true, getDescriptorPath(), options) != null) {
      super.doOKAction();
    }
  }

  @Nullable
  private static VirtualFile createAirDescriptorTemplate(final Project project,
                                                         final boolean interactive,
                                                         final String descriptorPath,
                                                         final AirDescriptorOptions options) {
    final VirtualFile dir = FlexUtils.createDirIfMissing(project, interactive, PathUtil.getParentPath(descriptorPath), getTitleText());
    if (dir == null) return null;

    final String fileName = PathUtil.getFileName(descriptorPath);

    final VirtualFile file = dir.findChild(fileName);
    if (file != null) {
      if (file.isDirectory()) {
        if (interactive) {
          Messages.showErrorDialog(project, "Can't create AIR descriptor file.\nFolder with such name exists.", getTitleText());
        }
        return null;
      }
      final int choice = interactive
                         ? Messages.showYesNoDialog(project, FlexBundle.message("file.exists.replace.question", fileName),
                                                    getTitleText(), Messages.getQuestionIcon())
                         : Messages.YES;
      if (choice != Messages.YES) {
        return null;
      }
    }

    try {
      final Ref<VirtualFile> fileRef = new Ref<>();
      final IOException exception = ApplicationManager.getApplication().runWriteAction((NullableComputable<IOException>)() -> {
        try {
          fileRef.set(FlexUtils.addFileWithContent(fileName, options.getAirDescriptorText(), dir));
        }
        catch (IOException e) {
          return e;
        }
        return null;
      });

      if (exception != null) {
        throw exception;
      }

      return fileRef.get();
    }
    catch (IOException e) {
      if (interactive) {
        Messages.showErrorDialog(project, "Failed to create AIR descriptor file: " + e.getMessage(), getTitleText());
      }
      return null;
    }
  }

  public String getDescriptorPath() {
    return FileUtil.toSystemIndependentName(myDescriptorFolderTextWithBrowse.getText().trim() + "/" +
                                            myDescriptorFileNameTextField.getText().trim());
  }

  public boolean isBothAndroidAndIosSelected() {
    return myAndroidCheckBox.isVisible() && myAndroidCheckBox.isSelected() && myIOSCheckBox.isVisible() && myIOSCheckBox.isSelected();
  }

  @Override
  protected String getHelpId() {
    return "flex.CreateAirDescriptorTemplateDialog";
  }

  static String getTitleText() {
    return FlexBundle.message("create.air.descriptor.template.title");
  }
}
