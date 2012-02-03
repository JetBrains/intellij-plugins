package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.options.CompilerUIConfigurable;
import com.intellij.execution.ExecutionBundle;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ex.MultiLineLabel;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

/**
 * @author Maxim.Mossienko
 *         Date: Sep 2, 2008
 *         Time: 3:49:07 PM
 */
public class FlexCompilerSettingsEditor implements ModuleConfigurationEditor {
  // Application Main Class must inherit from this class
  public static final String SPRITE_CLASS_NAME = "flash.display.Sprite";
  // The base class for ActionScript-based dynamically-loadable modules
  public static final String MODULE_BASE_CLASS_NAME = "mx.modules.ModuleBase";

  private JPanel myMainPanel;
  private JCheckBox myUseIDEBuilderCheckBox;
  private JTabbedPane mySettingsTabbedPane;
  private JPanel myBasicTabPanel;
  private JPanel myAdvancedTabPanel;
  private JRadioButton myApplicationOutputTypeRadioButton;
  private JRadioButton myLibraryOutputTypeRadioButton;
  private JCheckBox myUseDefaultSdkConfigFileCheckBox;
  private JLabel myFlexSdkConfigXmlLabel;
  private JCheckBox myUseCustomConfigFileCheckBox;
  private TextFieldWithBrowseButton myCustomConfigFileTextField;
  private JCheckBox myCustomConfigFileForTestsCheckBox;
  private TextFieldWithBrowseButton myCustomConfigFileForTestsTextWithBrowse;
  private JPanel myMainClassAndOutputPanel;
  private JPanel myMainClassPanel;
  private JSReferenceEditor myMainClassTextWithBrowse;
  private JTextField myOutputFileNameTextField;
  private JPanel myModuleOutputPathPanel;
  private JRadioButton myInheritProjectCompileOutputRadioButton;
  private JRadioButton myUseModuleCompileOutputRadioButton;
  private JPanel myModuleSpecificOutputPathPanel;
  private TextFieldWithBrowseButton myModuleSpecificOutputPathTextField;
  private TextFieldWithBrowseButton myModuleSpecificOutputPathForTestsTextField;
  private JCheckBox myExcludeOutputPathsCheckBox;
  private JPanel myFacetOutputPathPanel;
  private JPanel myFacetSpecificOutputPathPanel;
  private JCheckBox myUseFacetCompileOutputPathCheckBox;
  private TextFieldWithBrowseButton myFacetSpecificOutputPathTextField;
  private TextFieldWithBrowseButton myFacetSpecificOutputPathForTestsTextField;
  private JCheckBox myUseFrameworkAsRsl;
  private FlashPlayerVersionForm myTargetPlayerVersionForm;
  private JCheckBox myLocaleCheckBox;
  private TextFieldWithBrowseButton.NoPathCompletion myLocaleTextFieldWithBrowse;
  private TextFieldWithBrowseButton.NoPathCompletion myCustomNamespacesTextWithBrowse;
  private JPanel myCssFilesToCompilePanel;
  private TextFieldWithBrowseButton.NoPathCompletion myCssFilesTextWithBrowse;
  private List<FlexBuildConfiguration.NamespaceAndManifestFileInfo> myNamespaceAndManifestFileInfoList;
  private List<String> myCssFilesList;
  private ServerTechnologyForm myServerTechnologyForm;
  private JTextField myAdditionalCompilerOptionsTextField;
  private JCheckBox myIncludeResourceFilesInSwcCheckBox;
  private JButton myConfigureResourcePatternsButton;
  private TextFieldWithBrowseButton.NoPathCompletion myConditionalCompilationDefinitionsTextWithBrowse;
  private MultiLineLabel myIdeBuilderOffLabel;
  private List<FlexBuildConfiguration.ConditionalCompilationDefinition> myConditionalCompilationDefinitionList;
  private final Module myModule;
  private @Nullable FlexFacet myFlexFacet;
  private final FlexBuildConfiguration config;
  private CompilerModuleExtension myCompilerExtension;
  private static final Icon ourIcon = IconLoader.getIcon("flex_compiler_settings.png", FlexCompilerSettingsEditor.class);

  /**
   * @param module must have type of <code>FlexModuleType</code>
   */
  public FlexCompilerSettingsEditor(final Module module, final CompilerModuleExtension compilerExtension) {
    this(module, null, compilerExtension);
  }

  /**
   * Either <code>module</code> has type of <code>FlexModuleType</code> and <code>flexFacet</code> is <code>null</code>,
   * or <code>module</code> has type other than <code>FlexModuleType</code> and <code>flexFacet</code> is not <code>null</code>.
   */
  public FlexCompilerSettingsEditor(final Module module,
                                    final @Nullable FlexFacet flexFacet,
                                    final CompilerModuleExtension compilerExtension) {
    // initialize modude before createUIComponents() call
    myModule = module;

    assert (ModuleType.get(module) instanceof FlexModuleType && flexFacet == null) ||
           (!(ModuleType.get(module) instanceof FlexModuleType) && flexFacet != null) : "incorrect method usage";

    myFlexFacet = flexFacet;
    myCompilerExtension = compilerExtension;
    config = flexFacet == null ? FlexBuildConfiguration.getInstance(module) : FlexBuildConfiguration.getInstance(flexFacet);

    if (flexFacet == null) {
      myFacetOutputPathPanel.setVisible(false);
    }
    else {
      myModuleOutputPathPanel.setVisible(false);
    }

    myUseIDEBuilderCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateAllControls();
      }
    });

    myUseCustomConfigFileCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateSettingsTabbedPane();
      }
    });

    myCustomConfigFileForTestsCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateConfigFileForTestsSpecificControls();
      }
    });

    final ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateOutputPathSpecificControls();
      }
    };

    myInheritProjectCompileOutputRadioButton.addActionListener(listener);
    myUseModuleCompileOutputRadioButton.addActionListener(listener);
    myUseFacetCompileOutputPathCheckBox.addActionListener(listener);

    final FileChooserDescriptor configFileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
      public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
        return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || file.getFileType() == StdFileTypes.XML);
      }
    };
    myCustomConfigFileTextField.addBrowseFolderListener(FlexBundle.message("flex.choose.configuration.file"), null, myModule.getProject(),
                                                        configFileChooserDescriptor);
    myCustomConfigFileForTestsTextWithBrowse
      .addBrowseFolderListener(FlexBundle.message("flex.choose.configuration.file"), null, myModule.getProject(),
                               configFileChooserDescriptor);

    final ActionListener outputTypeListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateOutputFileName(myOutputFileNameTextField, myLibraryOutputTypeRadioButton.isSelected());
        updateMainClassSpecificControls();
        updateResourceFilesSpecificControls();
      }
    };

    myApplicationOutputTypeRadioButton.addActionListener(outputTypeListener);
    myLibraryOutputTypeRadioButton.addActionListener(outputTypeListener);

    myModuleSpecificOutputPathTextField
      .addBrowseFolderListener(FlexBundle.message("flex.choose.output.directory"), null, myModule.getProject(),
                               FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myModuleSpecificOutputPathForTestsTextField
      .addBrowseFolderListener(FlexBundle.message("flex.choose.test.output.directory"), null, myModule.getProject(),
                               FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myConfigureResourcePatternsButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        ShowSettingsUtil.getInstance().editConfigurable(module.getProject(), new CompilerUIConfigurable(module.getProject()));
      }
    });

    myConditionalCompilationDefinitionsTextWithBrowse.getTextField().setEditable(false);
    myConditionalCompilationDefinitionsTextWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final ConditionalCompilationDefinitionsDialog dialog =
          new ConditionalCompilationDefinitionsDialog(myModule.getProject(), myConditionalCompilationDefinitionList);
        dialog.show();
        if (dialog.isOK()) {
          myConditionalCompilationDefinitionList = dialog.getCurrentList();
          setConditionalCompilationDefinitionsText();
        }
      }
    });

    myFacetSpecificOutputPathTextField
      .addBrowseFolderListener(FlexBundle.message("flex.choose.output.directory"), null, myModule.getProject(),
                               FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myFacetSpecificOutputPathForTestsTextField
      .addBrowseFolderListener(FlexBundle.message("flex.choose.test.output.directory"), null, myModule.getProject(),
                               FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myLocaleCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateLocaleSpecificControls();
      }
    });

    myLocaleTextFieldWithBrowse.getTextField().setToolTipText("Comma separated list of locales");

    myLocaleTextFieldWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final Sdk flexSdk = FlexUtils.getSdkForActiveBC(myModule);
        if (flexSdk == null) {
          Messages.showErrorDialog(myModule.getProject(),
                                   FlexBundle.message("flex.sdk.not.set.for", myModule.getName()),
                                   "Flex SDK not found");
        }
        else {
          final VirtualFile sdkRoot = flexSdk.getHomeDirectory();
          if (sdkRoot == null) {
            Messages.showErrorDialog(myModule.getProject(), FlexBundle.message("sdk.home.directory.not.found.for", flexSdk.getName()),
                                     FlexBundle.message("sdk.home.directory.not.found"));
          }
          else {
            final LocalesDialog dialog =
              new LocalesDialog(myModule.getProject(), sdkRoot, myLocaleTextFieldWithBrowse.getText());
            dialog.show();
            if (dialog.isOK()) {
              myLocaleTextFieldWithBrowse.setText(dialog.getSelectedLocalesCommaSeparated());
            }
          }
        }
      }
    });

    myCustomNamespacesTextWithBrowse.getTextField().setEditable(false);
    myCustomNamespacesTextWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final NamespacesAndManifestFilesDialog dialog =
          new NamespacesAndManifestFilesDialog(myModule.getProject(), myNamespaceAndManifestFileInfoList,
                                               myLibraryOutputTypeRadioButton.isSelected());
        dialog.show();
        if (dialog.isOK()) {
          myNamespaceAndManifestFileInfoList = dialog.getCurrentList();
          setCustomNamespacesText();
        }
      }
    });

    myCssFilesTextWithBrowse.getTextField().setEditable(false);
    myCssFilesTextWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final CssFilesDialog dialog = new CssFilesDialog(module.getProject(), myCssFilesList);
        dialog.show();
        if (dialog.isOK()) {
          myCssFilesList = dialog.getCurrentList();
          setCssFilesText();
        }
      }
    });

    myLocaleTextFieldWithBrowse.setButtonIcon(PlatformIcons.OPEN_EDIT_DIALOG_ICON);
    myConditionalCompilationDefinitionsTextWithBrowse.setButtonIcon(PlatformIcons.OPEN_EDIT_DIALOG_ICON);
    myCustomNamespacesTextWithBrowse.setButtonIcon(PlatformIcons.OPEN_EDIT_DIALOG_ICON);
    myCssFilesTextWithBrowse.setButtonIcon(PlatformIcons.OPEN_EDIT_DIALOG_ICON);

    myIdeBuilderOffLabel.setIcon(UIUtil.getBalloonInformationIcon());
  }

  private void updateAllControls() {
    myIdeBuilderOffLabel.setVisible(!myUseIDEBuilderCheckBox.isSelected());
    updateSettingsTabbedPane();
  }

  private void updateSettingsTabbedPane() {
    final Sdk sdk = FlexUtils.getSdkForActiveBC(myModule);
    final boolean flexmojosSdk = sdk != null && sdk.getSdkType() instanceof FlexmojosSdkType;
    myUseDefaultSdkConfigFileCheckBox.setEnabled(!flexmojosSdk);
    myFlexSdkConfigXmlLabel.setEnabled(!flexmojosSdk);
    boolean useCustomConfigFile = myUseCustomConfigFileCheckBox.isSelected();
    myCustomConfigFileTextField.setEnabled(useCustomConfigFile);
    updateConfigFileForTestsSpecificControls();

    UIUtil.setEnabled(myMainClassAndOutputPanel, !useCustomConfigFile, true);
    UIUtil.setEnabled(myAdvancedTabPanel, !useCustomConfigFile, true);
    UIUtil.setEnabled(myCssFilesToCompilePanel, true, true);

    if (!useCustomConfigFile) {
      updateMainClassSpecificControls();
      updateOutputPathSpecificControls();
      updateResourceFilesSpecificControls();
      updateTargetPlayerSpecificControls();
      updateLocaleSpecificControls();
    }
  }

  private void updateConfigFileForTestsSpecificControls() {
    myCustomConfigFileForTestsCheckBox.setEnabled(myUseCustomConfigFileCheckBox.isSelected());
    myCustomConfigFileForTestsTextWithBrowse
      .setEnabled(myCustomConfigFileForTestsCheckBox.isEnabled() && myCustomConfigFileForTestsCheckBox.isSelected());
  }

  private void updateMainClassSpecificControls() {
    if (!myUseCustomConfigFileCheckBox.isSelected()) {
      UIUtil.setEnabled(myMainClassPanel, myApplicationOutputTypeRadioButton.isSelected(), true);
    }
  }

  private void updateResourceFilesSpecificControls() {
    if (!myUseCustomConfigFileCheckBox.isSelected()) {
      myIncludeResourceFilesInSwcCheckBox.setEnabled(myLibraryOutputTypeRadioButton.isSelected());
      myConfigureResourcePatternsButton.setEnabled(myLibraryOutputTypeRadioButton.isSelected());
    }
  }

  private void updateOutputPathSpecificControls() {
    UIUtil.setEnabled(myModuleSpecificOutputPathPanel, myUseModuleCompileOutputRadioButton.isSelected(), true);
    UIUtil.setEnabled(myFacetSpecificOutputPathPanel, myUseFacetCompileOutputPathCheckBox.isSelected(), true);
  }

  private void updateTargetPlayerSpecificControls() {
    final Sdk sdk = FlexUtils.getSdkForActiveBC(myModule);
    final boolean applicable = sdk != null && TargetPlayerUtils.isTargetPlayerApplicable(sdk);
    UIUtil.setEnabled(myTargetPlayerVersionForm.getMainPanel(), applicable, true);
  }

  private void updateLocaleSpecificControls() {
    myLocaleTextFieldWithBrowse.setEnabled(myLocaleCheckBox.isEnabled() && myLocaleCheckBox.isSelected());
  }

  public static void updateOutputFileName(final JTextField textField, final boolean isLib) {
    final String outputFileName = textField.getText();
    final String lowercase = outputFileName.toLowerCase();
    final String withoutExtension = lowercase.endsWith(".swf") || lowercase.endsWith(".swc")
                                    ? outputFileName.substring(0, outputFileName.length() - ".sw_".length())
                                    : outputFileName;
    textField.setText(withoutExtension + (isLib ? ".swc" : ".swf"));
  }

  public void saveData() {
    apply();
  }

  public void moduleStateChanged() {
    reset();
  }

  @Nls
  public String getDisplayName() {
    return FlexBundle.message("flex.compiler.settings");
  }

  public Icon getIcon() {
    return ourIcon;
  }

  public String getHelpTopic() {
    return "reference.settings.modules.facet.flex.settings";
  }

  public JComponent createComponent() {
    return myMainPanel;
  }

  public boolean isModified() {
    if (config.DO_BUILD != myUseIDEBuilderCheckBox.isSelected()) return true;
    if (!config.OUTPUT_TYPE.equals(getOutputType())) return true;
    if (config.USE_DEFAULT_SDK_CONFIG_FILE != myUseDefaultSdkConfigFileCheckBox.isSelected()) return true;
    if (config.USE_CUSTOM_CONFIG_FILE != myUseCustomConfigFileCheckBox.isSelected()) return true;
    if (!config.CUSTOM_CONFIG_FILE.equals(FileUtil.toSystemIndependentName(myCustomConfigFileTextField.getText().trim()))) return true;
    if (config.USE_CUSTOM_CONFIG_FILE_FOR_TESTS != myCustomConfigFileForTestsCheckBox.isSelected()) return true;
    if (!config.CUSTOM_CONFIG_FILE_FOR_TESTS
      .equals(FileUtil.toSystemIndependentName(myCustomConfigFileForTestsTextWithBrowse.getText().trim()))) {
      return true;
    }
    if (!config.MAIN_CLASS.equals(myMainClassTextWithBrowse.getText().trim())) return true;
    if (!config.OUTPUT_FILE_NAME.equals(myOutputFileNameTextField.getText().trim())) return true;
    if (myFlexFacet == null) {
      final String moduleSpecificOutputDir = myCompilerExtension.getCompilerOutputPointer() == null ? "" :
                                             VfsUtil.urlToPath(myCompilerExtension.getCompilerOutputPointer().getUrl());
      final String moduleSpecificTestOutputDir = myCompilerExtension.getCompilerOutputForTestsPointer() == null ? "" :
                                                 VfsUtil.urlToPath(myCompilerExtension.getCompilerOutputForTestsPointer().getUrl());

      if (myCompilerExtension.isCompilerOutputPathInherited() != myInheritProjectCompileOutputRadioButton.isSelected()) return true;
      if (!myCompilerExtension.isCompilerOutputPathInherited() &&
          (!moduleSpecificOutputDir.equals(FileUtil.toSystemIndependentName(myModuleSpecificOutputPathTextField.getText().trim())) ||
           !moduleSpecificTestOutputDir
             .equals(FileUtil.toSystemIndependentName(myModuleSpecificOutputPathForTestsTextField.getText().trim())) ||
           myCompilerExtension.isExcludeOutput() != myExcludeOutputPathsCheckBox.isSelected())) {
        return true;
      }
    }
    else {
      if (config.USE_FACET_COMPILE_OUTPUT_PATH != myUseFacetCompileOutputPathCheckBox.isSelected()) return true;
      if (config.USE_FACET_COMPILE_OUTPUT_PATH &&
          (!config.FACET_COMPILE_OUTPUT_PATH
            .equals(FileUtil.toSystemIndependentName(myFacetSpecificOutputPathTextField.getText().trim())) ||
           !config.FACET_COMPILE_OUTPUT_PATH_FOR_TESTS
             .equals(FileUtil.toSystemIndependentName(myFacetSpecificOutputPathForTestsTextField.getText().trim())))) {
        return true;
      }
    }
    if (config.INCLUDE_RESOURCE_FILES_IN_SWC != myIncludeResourceFilesInSwcCheckBox.isSelected()) return true;
    if (config.STATIC_LINK_RUNTIME_SHARED_LIBRARIES == myUseFrameworkAsRsl.isSelected()) return true;
    if (!TargetPlayerUtils.isEqual(config.TARGET_PLAYER_VERSION, myTargetPlayerVersionForm.getPlayerVersion())) return true;
    if (config.USE_LOCALE_SETTINGS != myLocaleCheckBox.isSelected()) return true;
    if (!config.LOCALE.equals(myLocaleTextFieldWithBrowse.getText())) return true;
    if (!config.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST.equals(myNamespaceAndManifestFileInfoList)) return true;
    if (!config.CSS_FILES_LIST.equals(myCssFilesList)) return true;
    if (!config.CONDITIONAL_COMPILATION_DEFINITION_LIST.equals(myConditionalCompilationDefinitionList)) return true;
    if (!config.PATH_TO_SERVICES_CONFIG_XML.equals(FileUtil.toSystemIndependentName(myServerTechnologyForm.getPathToServicesConfigXml()))) {
      return true;
    }
    if (!config.CONTEXT_ROOT.equals(myServerTechnologyForm.getContextRoot())) return true;
    if (!config.ADDITIONAL_COMPILER_OPTIONS.equals(myAdditionalCompilerOptionsTextField.getText())) return true;

    return false;
  }

  public void apply() {
    if (!isModified()) {
      return;
    }

    final FlexCompilerHandler flexCompilerHandler = FlexCompilerHandler.getInstance(myModule.getProject());
    flexCompilerHandler.quitCompilerShell();
    flexCompilerHandler.getCompilerDependenciesCache().markModuleAndDependentModulesDirty(myModule);

    config.DO_BUILD = myUseIDEBuilderCheckBox.isSelected();
    config.OUTPUT_TYPE = getOutputType();
    config.USE_DEFAULT_SDK_CONFIG_FILE = myUseDefaultSdkConfigFileCheckBox.isSelected();
    config.USE_CUSTOM_CONFIG_FILE = myUseCustomConfigFileCheckBox.isSelected();
    config.CUSTOM_CONFIG_FILE = FileUtil.toSystemIndependentName(myCustomConfigFileTextField.getText().trim());
    config.USE_CUSTOM_CONFIG_FILE_FOR_TESTS = myCustomConfigFileForTestsCheckBox.isSelected();
    config.CUSTOM_CONFIG_FILE_FOR_TESTS = FileUtil.toSystemIndependentName(myCustomConfigFileForTestsTextWithBrowse.getText().trim());
    config.MAIN_CLASS = myMainClassTextWithBrowse.getText().trim();
    config.OUTPUT_FILE_NAME = myOutputFileNameTextField.getText().trim();
    if (myFlexFacet == null) {
      final boolean inherit = myInheritProjectCompileOutputRadioButton.isSelected();
      myCompilerExtension.inheritCompilerOutputPath(inherit);
      if (!inherit) {
        myCompilerExtension.setCompilerOutputPath(getCompilerOutputUrl(myModuleSpecificOutputPathTextField.getText().trim()));
        myCompilerExtension
          .setCompilerOutputPathForTests(getCompilerOutputUrl(myModuleSpecificOutputPathForTestsTextField.getText().trim()));
        myCompilerExtension.setExcludeOutput(myExcludeOutputPathsCheckBox.isSelected());
      }
      myCompilerExtension.commit();
    }
    else {
      config.USE_FACET_COMPILE_OUTPUT_PATH = myUseFacetCompileOutputPathCheckBox.isSelected();
      config.FACET_COMPILE_OUTPUT_PATH = FileUtil.toSystemIndependentName(myFacetSpecificOutputPathTextField.getText().trim());
      config.FACET_COMPILE_OUTPUT_PATH_FOR_TESTS =
        FileUtil.toSystemIndependentName(myFacetSpecificOutputPathForTestsTextField.getText().trim());
    }
    config.INCLUDE_RESOURCE_FILES_IN_SWC = myIncludeResourceFilesInSwcCheckBox.isSelected();
    config.STATIC_LINK_RUNTIME_SHARED_LIBRARIES = !myUseFrameworkAsRsl.isSelected();

    final boolean targetPlayerMajorOrMinorVersionChanged =
      !TargetPlayerUtils.majorAndMinorVersionEqual(config.TARGET_PLAYER_VERSION, myTargetPlayerVersionForm.getPlayerVersion());
    config.TARGET_PLAYER_VERSION = myTargetPlayerVersionForm.getPlayerVersion();
    if (targetPlayerMajorOrMinorVersionChanged && myFlexFacet == null) {
      // in case of Flex facet Sdk will be managed in FlexFacetConfigurationImpl.FlexFacetEditorTab.onFacetInitialized()
      TargetPlayerUtils.changeFlexSdkIfNeeded(myModule, config.TARGET_PLAYER_VERSION);
    }

    config.USE_LOCALE_SETTINGS = myLocaleCheckBox.isSelected();
    config.LOCALE = myLocaleTextFieldWithBrowse.getText();
    config.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST = myNamespaceAndManifestFileInfoList;
    config.CONDITIONAL_COMPILATION_DEFINITION_LIST = myConditionalCompilationDefinitionList;
    config.CSS_FILES_LIST = myCssFilesList;

    config.PATH_TO_SERVICES_CONFIG_XML = myServerTechnologyForm.getPathToServicesConfigXml();
    config.CONTEXT_ROOT = myServerTechnologyForm.getContextRoot();

    config.ADDITIONAL_COMPILER_OPTIONS = myAdditionalCompilerOptionsTextField.getText();
  }

  private void createUIComponents() {
    Condition<JSClass> filter = createMainClassFilter(myModule);
    myMainClassTextWithBrowse =
      JSReferenceEditor.forClassName("", myModule.getProject(), null, GlobalSearchScope.moduleScope(myModule), null, filter,
                                     ExecutionBundle.message("choose.main.class.dialog.title"));
  }

  public static Condition<JSClass> createMainClassFilter(Module module) {
    return Conditions.or(new JSClassChooserDialog.PublicInheritor(module, SPRITE_CLASS_NAME, true),
                         new JSClassChooserDialog.PublicInheritor(module, MODULE_BASE_CLASS_NAME, true));
  }

  @Nullable
  private static String getCompilerOutputUrl(final String path) {
    // copy/paste from BuildElementsEditor.createOutputPathPanel
    if (path.length() == 0) {
      return null;
    }
    else {
      // should set only absolute paths
      String canonicalPath;
      try {
        canonicalPath = FileUtil.resolveShortWindowsName(path);
      }
      catch (IOException e) {
        canonicalPath = path;
      }
      return VfsUtil.pathToUrl(FileUtil.toSystemIndependentName(canonicalPath));
    }
  }

  public void reset() {
    myUseIDEBuilderCheckBox.setSelected(config.DO_BUILD);
    myApplicationOutputTypeRadioButton.setSelected(FlexBuildConfiguration.APPLICATION.equals(config.OUTPUT_TYPE));
    myLibraryOutputTypeRadioButton.setSelected(FlexBuildConfiguration.LIBRARY.equals(config.OUTPUT_TYPE));
    myUseDefaultSdkConfigFileCheckBox.setSelected(config.USE_DEFAULT_SDK_CONFIG_FILE);
    myUseCustomConfigFileCheckBox.setSelected(config.USE_CUSTOM_CONFIG_FILE);
    myCustomConfigFileTextField.setText(FileUtil.toSystemDependentName(config.CUSTOM_CONFIG_FILE));
    myCustomConfigFileForTestsCheckBox.setSelected(config.USE_CUSTOM_CONFIG_FILE_FOR_TESTS);
    myCustomConfigFileForTestsTextWithBrowse.setText(FileUtil.toSystemDependentName(config.CUSTOM_CONFIG_FILE_FOR_TESTS));
    myMainClassTextWithBrowse.setText(config.MAIN_CLASS);
    myOutputFileNameTextField.setText(config.OUTPUT_FILE_NAME);

    final String moduleOutputPath = FileUtil.toSystemDependentName(VfsUtil.urlToPath(myCompilerExtension.getCompilerOutputUrl()));
    final String moduleOutputPathForTests =
      FileUtil.toSystemDependentName(VfsUtil.urlToPath(myCompilerExtension.getCompilerOutputUrlForTests()));

    if (myFlexFacet == null) {
      myInheritProjectCompileOutputRadioButton.setSelected(myCompilerExtension.isCompilerOutputPathInherited());
      myUseModuleCompileOutputRadioButton.setSelected(!myCompilerExtension.isCompilerOutputPathInherited());
      myModuleSpecificOutputPathTextField.setText(moduleOutputPath);
      myModuleSpecificOutputPathForTestsTextField.setText(moduleOutputPathForTests);
      myExcludeOutputPathsCheckBox.setSelected(myCompilerExtension.isExcludeOutput());
    }
    else {
      myUseFacetCompileOutputPathCheckBox.setSelected(config.USE_FACET_COMPILE_OUTPUT_PATH);
      myFacetSpecificOutputPathTextField.setText(
        config.USE_FACET_COMPILE_OUTPUT_PATH ? FileUtil.toSystemDependentName(config.FACET_COMPILE_OUTPUT_PATH) : moduleOutputPath);
      myFacetSpecificOutputPathForTestsTextField.setText(config.USE_FACET_COMPILE_OUTPUT_PATH
                                                         ? FileUtil.toSystemDependentName(config.FACET_COMPILE_OUTPUT_PATH_FOR_TESTS)
                                                         : moduleOutputPathForTests);
    }

    myAdditionalCompilerOptionsTextField.setText(config.ADDITIONAL_COMPILER_OPTIONS);

    myTargetPlayerVersionForm.setPlayerVersion(config.TARGET_PLAYER_VERSION);

    myIncludeResourceFilesInSwcCheckBox.setSelected(config.INCLUDE_RESOURCE_FILES_IN_SWC);
    myUseFrameworkAsRsl.setSelected(!config.STATIC_LINK_RUNTIME_SHARED_LIBRARIES);
    myLocaleCheckBox.setSelected(config.USE_LOCALE_SETTINGS);
    myLocaleTextFieldWithBrowse.setText(config.LOCALE);
    myNamespaceAndManifestFileInfoList = config.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST;
    setCustomNamespacesText();
    myCssFilesList = config.CSS_FILES_LIST;
    setCssFilesText();
    myConditionalCompilationDefinitionList = config.CONDITIONAL_COMPILATION_DEFINITION_LIST;
    setConditionalCompilationDefinitionsText();

    final Sdk flexSdk = FlexUtils.getSdkForActiveBC(myModule);
    if (flexSdk != null && flexSdk.getSdkType() instanceof IFlexSdkType) {
      final String baseConfigFileName = FlexSdkUtils.getBaseConfigFileName((IFlexSdkType)flexSdk.getSdkType());
      myFlexSdkConfigXmlLabel.setText(baseConfigFileName);
      myFlexSdkConfigXmlLabel.setIcon(flexSdk.getSdkType().getIcon());

      if (flexSdk.getSdkType() instanceof FlexmojosSdkType) {
        myFlexSdkConfigXmlLabel.setText("(not applicable for Flexmojos SDK)");
        myFlexSdkConfigXmlLabel.setIcon(null);
      }
    }
    else {
      myFlexSdkConfigXmlLabel.setText("(not applicable)");
      myFlexSdkConfigXmlLabel.setIcon(null);
    }

    myServerTechnologyForm.setPathToServicesConfigXml(FileUtil.toSystemDependentName(config.PATH_TO_SERVICES_CONFIG_XML));
    myServerTechnologyForm.setContextRoot(config.CONTEXT_ROOT);
    updateAllControls();
  }

  private void setCustomNamespacesText() {
    final String s = StringUtil
      .join(myNamespaceAndManifestFileInfoList, new Function<FlexBuildConfiguration.NamespaceAndManifestFileInfo, String>() {
        public String fun(final FlexBuildConfiguration.NamespaceAndManifestFileInfo info) {
          return info.NAMESPACE;
        }
      }, ", ");
    myCustomNamespacesTextWithBrowse.setText(s);
  }

  private void setCssFilesText() {
    final String s = StringUtil.join(myCssFilesList, new Function<String, String>() {
      public String fun(final String path) {
        return path.substring(FileUtil.toSystemIndependentName(path).lastIndexOf("/") + 1);
      }
    }, ", ");
    myCssFilesTextWithBrowse.setText(s);
  }

  private void setConditionalCompilationDefinitionsText() {
    final String s = StringUtil
      .join(myConditionalCompilationDefinitionList, new Function<FlexBuildConfiguration.ConditionalCompilationDefinition, String>() {
        public String fun(final FlexBuildConfiguration.ConditionalCompilationDefinition conditionalCompilationDefinition) {
          return conditionalCompilationDefinition.NAME + "=" + conditionalCompilationDefinition.VALUE;
        }
      }, ", ");
    myConditionalCompilationDefinitionsTextWithBrowse.setText(s);
  }

  public String getOutputType() {
    return myApplicationOutputTypeRadioButton.isSelected() ? FlexBuildConfiguration.APPLICATION : FlexBuildConfiguration.LIBRARY;
  }

  public void disposeUIResources() {
    myMainPanel = null;
    myUseIDEBuilderCheckBox = null;
    myUseDefaultSdkConfigFileCheckBox = null;
    myUseCustomConfigFileCheckBox = null;
    myCustomConfigFileTextField = null;
    myCustomConfigFileForTestsCheckBox = null;
    myCustomConfigFileForTestsTextWithBrowse = null;
    myOutputFileNameTextField = null;
    myInheritProjectCompileOutputRadioButton = null;
    myUseModuleCompileOutputRadioButton = null;
    myModuleSpecificOutputPathPanel = null;
    myModuleSpecificOutputPathTextField = null;
    myModuleSpecificOutputPathForTestsTextField = null;
    myFacetOutputPathPanel = null;
    myFacetSpecificOutputPathPanel = null;
    myUseFacetCompileOutputPathCheckBox = null;
    myFacetSpecificOutputPathTextField = null;
    myFacetSpecificOutputPathForTestsTextField = null;
    myUseFrameworkAsRsl = null;
    myLocaleCheckBox = null;
    myLocaleTextFieldWithBrowse = null;
    myServerTechnologyForm = null;
    myAdditionalCompilerOptionsTextField = null;
  }
}
