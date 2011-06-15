package com.intellij.lang.javascript.flex.actions.airdescriptor;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CreateAirDescriptorDialog extends DialogWrapper {
  private Project myProject;

  private JPanel myMainPanel;
  private JTextField myDescriptorFileNameTextField;
  private TextFieldWithBrowseButton myDescriptorFileLocationTextWithBrowse;
  private JComboBox myAirVersionComboBox;
  private JTextField myApplicationIdTextField;
  private JTextField myApplicationFileNameTextField;
  private JTextField myApplicationNameTextField;
  private JTextField myApplicationVersionTextField;
  private ComboboxWithBrowseButton myApplicationContentComboWithBrowse;
  private JTextField myApplicationTitleTextField;
  private JTextField myApplicationWidthTextField;
  private JTextField myApplicationHeightTextField;

  private static final String[] AIR_VERSIONS = new String[]{"1.0", "1.1", "1.5", "1.5.1", "1.5.2", "1.5.3", "2.0", "2.5", "2.6", "2.7"};
  private static final Pattern VERSION_PATTERN = Pattern.compile("[0-9]{1,3}(\\.[0-9]{1,3}){0,2}");

  public CreateAirDescriptorDialog(final Project project) {
    super(project, true);
    myProject = project;
    setTitle("Create AIR Application Descriptor");
    setOKButtonText("Create");

    initAirVersionComboBox();
    initDescriptorFileLocationTextWithBrowse();
    initApplicationContentTextWithBrowse();

    init();
  }

  public JComponent getPreferredFocusedComponent() {
    return myApplicationContentComboWithBrowse.getComboBox();
  }

  private void initAirVersionComboBox() {
    myAirVersionComboBox.setModel(new DefaultComboBoxModel(AIR_VERSIONS));
    myAirVersionComboBox.setSelectedIndex(AIR_VERSIONS.length - 1);
  }

  private void initDescriptorFileLocationTextWithBrowse() {
    myDescriptorFileLocationTextWithBrowse.setText(suggestDirPathToCreateDescriptorIn());
    myDescriptorFileLocationTextWithBrowse.addBrowseFolderListener("Choose location for AIR application descriptor", "", myProject,
                                                                   new FileChooserDescriptor(false, true, false, false, false, false));
  }

  private String suggestDirPathToCreateDescriptorIn() {
    final Module airModule = getAirModule();
    if (airModule != null) {
      final ModuleRootManager rootManager = ModuleRootManager.getInstance(airModule);
      final VirtualFile[] sourceRoots = rootManager.getSourceRoots();
      if (sourceRoots.length > 0) {
        return FileUtil.toSystemDependentName(sourceRoots[0].getPath());
      }
    }
    final VirtualFile baseDir = myProject.getBaseDir();
    return FileUtil.toSystemDependentName(baseDir == null ? "" : baseDir.getPath());
  }

  @Nullable
  private Module getAirModule() {
    final ModuleManager moduleManager = ModuleManager.getInstance(myProject);
    final Module[] modules = moduleManager.getModules();
    if (modules.length > 0) {
      for (final Module module : modules) {
        if (FlexSdkUtils.hasDependencyOnAir(module)) {
          return module;
        }
      }

      for (final Module module : modules) {
        if (FlexUtils.isFlexModuleOrContainsFlexFacet(module)) {
          return module;
        }
      }
      return modules[0];
    }
    return null;
  }

  private void initApplicationContentTextWithBrowse() {
    myApplicationContentComboWithBrowse.getComboBox().setEditable(true);
    myApplicationContentComboWithBrowse.getComboBox().setModel(new DefaultComboBoxModel(collectPotentialAirSwfFileNames(myProject)));

    final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
      public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
        return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || isAppropriateExtension(file.getExtension()));
      }

      private boolean isAppropriateExtension(String extension) {
        return "swf".equalsIgnoreCase(extension);
      }
    };

    final TextComponentAccessor<JComboBox> accessor = new TextComponentAccessor<JComboBox>() {
      public String getText(JComboBox comboBox) {
        Object item = comboBox.getEditor().getItem();
        return item.toString();
      }

      public void setText(JComboBox comboBox, String text) {
        comboBox.getEditor().setItem(text.substring(FileUtil.toSystemIndependentName(text).lastIndexOf("/") + 1));
      }
    };

    myApplicationContentComboWithBrowse
      .addBrowseFolderListener("Choose the main SWF file of the AIR application", "", myProject, descriptor, accessor);
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected void doOKAction() {
    if (validateInput()) {
      super.doOKAction();
    }
  }

  private boolean validateInput() {
    final String descriptorFileName = myDescriptorFileNameTextField.getText().trim();
    if (!descriptorFileName.endsWith(".xml")) {
      Messages.showErrorDialog(myProject, "AIR application descriptor file must have .xml extension", "Error");
      return false;
    }

    final String descriptorFileLocation = myDescriptorFileLocationTextWithBrowse.getText().trim();
    if (descriptorFileLocation.length() == 0) {
      Messages.showErrorDialog(myProject, "AIR descriptor file location not specified" + descriptorFileLocation, "Error");
      return false;
    }

    final String appId = myApplicationIdTextField.getText().trim();
    if (!appId.matches("[A-Za-z0-9\\-\\.]{1,212}")) {
      Messages.showErrorDialog(myProject, "Application ID is invalid", "Error");
      return false;
    }

    final String appFileName = myApplicationFileNameTextField.getText().trim();
    if (appFileName.length() == 0) {
      Messages.showErrorDialog(myProject, "Application file name is invalid", "Error");
      return false;
    }

    final String appVersion = myApplicationVersionTextField.getText().trim();
    if (appVersion.length() == 0) {
      Messages.showErrorDialog(myProject, "Application version is required", "Error");
      return false;
    }

    if (StringUtil.compareVersionNumbers(myAirVersionComboBox.getSelectedItem().toString(), "2.5") >= 0) {
      if (!VERSION_PATTERN.matcher(appVersion).matches()) {
        Messages.showErrorDialog(myProject, "Application version must have following format: <0-999>.<0-999>.<0-999>", "Error");
        return false;
      }
    }

    final String appContent = myApplicationContentComboWithBrowse.getComboBox().getEditor().getItem().toString().trim();
    if (appContent.length() == 0) {
      Messages.showErrorDialog(myProject, "Application content not specified", "Error");
      return false;
    }

    int appWidth = parseUnsignedInt(myApplicationWidthTextField.getText());
    if (appWidth < 0) {
      Messages.showErrorDialog(myProject, "Application width is incorrect", "Error");
      return false;
    }

    int appHeight = parseUnsignedInt(myApplicationHeightTextField.getText());
    if (appHeight < 0) {
      Messages.showErrorDialog(myProject, "Application height is incorrect", "Error");
      return false;
    }
    return true;
  }

  public AirDescriptorParameters getAirDescriptorParameters() {
    final String descriptorFileName = myDescriptorFileNameTextField.getText().trim();
    final String descriptorFileLocation = myDescriptorFileLocationTextWithBrowse.getText().trim();
    final String airVersion = myAirVersionComboBox.getSelectedItem().toString();
    final String appId = myApplicationIdTextField.getText().trim();
    final String appFileName = myApplicationFileNameTextField.getText().trim();
    final String appName = myApplicationNameTextField.getText().trim();
    final String appVersion = myApplicationVersionTextField.getText().trim();
    final String appContent = myApplicationContentComboWithBrowse.getComboBox().getEditor().getItem().toString().trim();
    final String appTitle = myApplicationTitleTextField.getText().trim();
    int appWidth = parseUnsignedInt(myApplicationWidthTextField.getText());
    int appHeight = parseUnsignedInt(myApplicationHeightTextField.getText());

    return new AirDescriptorParameters(descriptorFileName, descriptorFileLocation, airVersion, appId, appFileName, appName, appVersion,
                                       appContent, appTitle, appWidth, appHeight, false);
  }

  private static int parseUnsignedInt(final String text) {
    int i = -1;
    if (text != null) {
      try {
        i = Integer.parseInt(text.trim());
      }
      catch (NumberFormatException e) {
        // ignore
      }
    }
    return i;
  }

  private static String[] collectPotentialAirSwfFileNames(final Project project) {
    final List<String> result = new ArrayList<String>();
    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      if (FlexUtils.isFlexModuleOrContainsFlexFacet(module)) {
        final Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
        if (sdk != null && sdk.getSdkType() instanceof AirSdkType) {
          for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
            if (config.DO_BUILD && config.OUTPUT_TYPE.equals(FlexBuildConfiguration.APPLICATION)) {
              if (config.USE_CUSTOM_CONFIG_FILE) {
                final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(config.CUSTOM_CONFIG_FILE);
                if (configFile != null) {
                  try {
                    final String outputPath = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><output>");
                    if (outputPath != null) {
                      result.add(outputPath.substring(FileUtil.toSystemIndependentName(outputPath).lastIndexOf("/") + 1));
                    }
                  }
                  catch (IOException e) {/*ignore*/}
                }
              }
              else {
                if (config.OUTPUT_FILE_NAME.length() > 0) {
                  result.add(config.OUTPUT_FILE_NAME);
                }
              }
            }
          }
        }
      }
    }
    return ArrayUtil.toStringArray(result);
  }

  protected String getHelpId() {
    return "reference.flex.create.air.application.descriptor";
  }
}
