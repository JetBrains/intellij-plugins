package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexCompilationUtils;
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
import com.intellij.util.PathUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CreateAirDescriptorTemplateDialog extends DialogWrapper {

  public static final String[] AIR_VERSIONS =
    {"1.0", "1.1", "1.5", "1.5.1", "1.5.2", "1.5.3", "2.0", "2.5", "2.6", "2.7", "3.0", "3.1", "3.2", "3.3"};
  public static final Pattern VERSION_PATTERN = Pattern.compile("[0-9]{1,3}(\\.[0-9]{1,3}){0,2}");

  public static final int ANDROID_PERMISSION_INTERNET = 1;
  public static final int ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
  public static final int ANDROID_PERMISSION_ACCESS_FINE_LOCATION = 4;
  public static final int ANDROID_PERMISSION_CAMERA = 8;

  public static class AirDescriptorOptions {
    private final String AIR_VERSION;
    private final String APP_ID;
    private final String APP_NAME;
    private final String APP_VERSION;
    private final String SWF_NAME;
    private final boolean MOBILE;
    private final boolean AUTO_ORIENTS;
    private final boolean FULL_SCREEN;
    private final boolean ANDROID;
    private final int ANDROID_PERMISSIONS;
    private final boolean IOS;
    private final boolean IPHONE;
    private final boolean IPAD;
    private final boolean IOS_HIGH_RESOLUTION;

    public AirDescriptorOptions(final String airVersion,
                                final String appId,
                                final String appName,
                                final String swfName,
                                final boolean android,
                                final boolean ios) {
      this(airVersion, appId, appName, "0.0.0", swfName, android || ios, android || ios, android || ios,
           android, ANDROID_PERMISSION_INTERNET, ios, ios, ios, ios);
    }

    public AirDescriptorOptions(final String airVersion,
                                final String appId,
                                final String appName,
                                final String appVersion,
                                final String swfName,
                                final boolean mobile,
                                final boolean autoOrients,
                                final boolean fullScreen,
                                final boolean android,
                                final int androidPermissions,
                                final boolean ios,
                                final boolean iPhone,
                                final boolean iPad,
                                final boolean iosHighResolution) {
      AIR_VERSION = airVersion;
      APP_ID = appId;
      APP_NAME = appName;
      APP_VERSION = appVersion;
      SWF_NAME = swfName;
      MOBILE = mobile;
      AUTO_ORIENTS = autoOrients;
      FULL_SCREEN = fullScreen;
      ANDROID = android;
      ANDROID_PERMISSIONS = androidPermissions;
      IOS = ios;
      IPHONE = iPhone;
      IPAD = iPad;
      IOS_HIGH_RESOLUTION = iosHighResolution;
    }
  }

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

  static final String TITLE = FlexBundle.message("create.air.descriptor.template.title");
  private final Project myProject;

  public CreateAirDescriptorTemplateDialog(final Project project,
                                           final String folderPath,
                                           final String mainClass,
                                           final String airVersion,
                                           final boolean androidEnabled,
                                           final boolean iosEnabled) {
    super(project);
    myProject = project;
    setTitle(TITLE);
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

    myAirVersionCombo.setModel(new DefaultComboBoxModel(AIR_VERSIONS));

    final ActionListener listener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };

    myAndroidCheckBox.addActionListener(listener);
    myIOSCheckBox.addActionListener(listener);
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected ValidationInfo doValidate() {
    final String fileName = myDescriptorFileNameTextField.getText().trim();
    if (fileName.isEmpty()) {
      return new ValidationInfo("Descriptor file name not set", myDescriptorFileNameTextField);
    }
    if (!fileName.toLowerCase().endsWith(".xml")) {
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
    if (!appId.equals(FlexCompilationUtils.fixApplicationId(appId))) {
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
      new AirDescriptorOptions(airVersion, appId, appName, appVersion, swfName, mobile, autoOrients, fullScreen, android,
                               androidPermissions, ios, iPhone, iPad, iosHighResolution);

    if (createAirDescriptorTemplate(myProject, true, getDescriptorPath(), options) != null) {
      super.doOKAction();
    }
  }

  @Nullable
  private static VirtualFile createAirDescriptorTemplate(final Project project,
                                                         final boolean interactive,
                                                         final String descriptorPath,
                                                         final AirDescriptorOptions options) {
    final VirtualFile dir = FlexUtils.createDirIfMissing(project, interactive, PathUtil.getParentPath(descriptorPath), TITLE);
    if (dir == null) return null;

    final String fileName = PathUtil.getFileName(descriptorPath);

    final VirtualFile file = dir.findChild(fileName);
    if (file != null) {
      if (file.isDirectory()) {
        if (interactive) {
          Messages.showErrorDialog(project, "Can't create AIR descriptor file.\nFolder with such name exists.", TITLE);
        }
        return null;
      }
      final int choice = interactive
                         ? Messages.showYesNoDialog(project, FlexBundle.message("file.exists.replace.question", fileName),
                                                    TITLE, Messages.getQuestionIcon())
                         : Messages.YES;
      if (choice != Messages.YES) {
        return null;
      }
    }

    try {
      final Ref<VirtualFile> fileRef = new Ref<VirtualFile>();
      final IOException exception = ApplicationManager.getApplication().runWriteAction(new NullableComputable<IOException>() {
        public IOException compute() {
          try {
            fileRef.set(FlexUtils.addFileWithContent(fileName, getAirDescriptorText(options), dir));
          }
          catch (IOException e) {
            return e;
          }
          return null;
        }
      });

      if (exception != null) {
        throw exception;
      }

      return fileRef.get();
    }
    catch (IOException e) {
      if (interactive) {
        Messages.showErrorDialog(project, "Failed to create AIR descriptor file: " + e.getMessage(), TITLE);
      }
      return null;
    }
  }

  public static String getAirDescriptorText(final AirDescriptorOptions options) throws IOException {
    // noinspection IOResourceOpenedButNotSafelyClosed
    final String rawText =
      FileUtil.loadTextAndClose(CreateAirDescriptorTemplateDialog.class.getResourceAsStream("air_descriptor_template.ft"));
    return replaceMacros(rawText, options);
  }

  private static String replaceMacros(final String descriptorText, final AirDescriptorOptions options) {
    final List<String> macros = new ArrayList<String>();
    final List<String> values = new ArrayList<String>();

    addToLists(macros, values, "${air_version}", options.AIR_VERSION);
    addToLists(macros, values, "${app_id}", options.APP_ID);
    addToLists(macros, values, "${app_name}", options.APP_NAME);

    final boolean air25OrLater = StringUtil.compareVersionNumbers(options.AIR_VERSION, "2.5") >= 0;
    addToLists(macros, values, "${app_version}", options.APP_VERSION);
    addToLists(macros, values, "${version_number_comment_start}", air25OrLater ? "" : "<!--");
    addToLists(macros, values, "${version_number_comment_end}", air25OrLater ? "" : "-->");
    addToLists(macros, values, "${version_comment_start}", air25OrLater ? "<!--" : "");
    addToLists(macros, values, "${version_comment_end}", air25OrLater ? "-->" : "");

    addToLists(macros, values, "${swf_name}", options.SWF_NAME);

    addToLists(macros, values, "${auto_orients}", options.MOBILE ? String.valueOf(options.AUTO_ORIENTS) : "");
    addToLists(macros, values, "${auto_orients_comment_start}", options.MOBILE ? "" : "<!--");
    addToLists(macros, values, "${auto_orients_comment_end}", options.MOBILE ? "" : "-->");

    addToLists(macros, values, "${full_screen}", options.MOBILE ? String.valueOf(options.FULL_SCREEN) : "");
    addToLists(macros, values, "${full_screen_comment_start}", options.MOBILE ? "" : "<!--");
    addToLists(macros, values, "${full_screen_comment_end}", options.MOBILE ? "" : "-->");

    addToLists(macros, values, "${iOS_comment_start}", options.MOBILE && options.IOS ? "" : "<!--");
    addToLists(macros, values, "${iOS_comment_end}", options.MOBILE && options.IOS ? "" : "-->");

    addToLists(macros, values, "${iPhone_comment_start}", options.MOBILE && options.IOS && options.IPHONE ? "" : "<!--");
    addToLists(macros, values, "${iPhone_comment_end}", options.MOBILE && options.IOS && options.IPHONE ? "" : "-->");
    addToLists(macros, values, "${iPad_comment_start}", options.MOBILE && options.IOS && options.IPAD ? "" : "<!--");
    addToLists(macros, values, "${iPad_comment_end}", options.MOBILE && options.IOS && options.IPAD ? "" : "-->");
    addToLists(macros, values, "${iOS_high_resolution_comment_start}",
               options.MOBILE && options.IOS && options.IOS_HIGH_RESOLUTION ? "" : "<!--");
    addToLists(macros, values, "${iOS_high_resolution_comment_end}",
               options.MOBILE && options.IOS && options.IOS_HIGH_RESOLUTION ? "" : "-->");

    addToLists(macros, values, "${android_comment_start}", options.MOBILE && options.ANDROID ? "" : "<!--");
    addToLists(macros, values, "${android_comment_end}", options.MOBILE && options.ANDROID ? "" : "-->");

    addToLists(macros, values, "${android_internet_comment_start}",
               options.MOBILE && options.ANDROID && (options.ANDROID_PERMISSIONS & ANDROID_PERMISSION_INTERNET) != 0 ? "" : "<!--");
    addToLists(macros, values, "${android_internet_comment_end}",
               options.MOBILE && options.ANDROID && (options.ANDROID_PERMISSIONS & ANDROID_PERMISSION_INTERNET) != 0 ? "" : "-->");

    addToLists(macros, values, "${android_write_external_storage_comment_start}",
               options.MOBILE && options.ANDROID && (options.ANDROID_PERMISSIONS & ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE) != 0
               ? "" : "<!--");
    addToLists(macros, values, "${android_write_external_storage_comment_end}",
               options.MOBILE && options.ANDROID && (options.ANDROID_PERMISSIONS & ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE) != 0
               ? "" : "-->");

    addToLists(macros, values, "${android_access_fine_location_comment_start}",
               options.MOBILE && options.ANDROID && (options.ANDROID_PERMISSIONS & ANDROID_PERMISSION_ACCESS_FINE_LOCATION) != 0
               ? "" : "<!--");
    addToLists(macros, values, "${android_access_fine_location_comment_end}",
               options.MOBILE && options.ANDROID && (options.ANDROID_PERMISSIONS & ANDROID_PERMISSION_ACCESS_FINE_LOCATION) != 0
               ? "" : "-->");

    addToLists(macros, values, "${android_camera_comment_start}",
               options.MOBILE && options.ANDROID && (options.ANDROID_PERMISSIONS & ANDROID_PERMISSION_CAMERA) != 0 ? "" : "<!--");
    addToLists(macros, values, "${android_camera_comment_end}",
               options.MOBILE && options.ANDROID && (options.ANDROID_PERMISSIONS & ANDROID_PERMISSION_CAMERA) != 0 ? "" : "-->");

    return StringUtil.replace(descriptorText, macros.toArray(new String[macros.size()]), values.toArray(new String[values.size()]));
  }

  private static void addToLists(final List<String> list1, final List<String> list2, final String entry1, final String entry2) {
    list1.add(entry1);
    list2.add(entry2);
  }

  public String getDescriptorPath() {
    return FileUtil.toSystemIndependentName(myDescriptorFolderTextWithBrowse.getText().trim() + "/" +
                                            myDescriptorFileNameTextField.getText().trim());
  }

  public boolean isBothAndroidAndIosSelected() {
    return myAndroidCheckBox.isVisible() && myAndroidCheckBox.isSelected() && myIOSCheckBox.isVisible() && myIOSCheckBox.isSelected();
  }

  protected String getHelpId() {
    return "flex.CreateAirDescriptorTemplateDialog";
  }
}
