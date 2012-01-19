package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexCompilerSettingsEditor;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.javascript.flex.sdk.FlexSdkUtils.*;

public class FlexIdeBCConfigurable extends /*ProjectStructureElementConfigurable*/NamedConfigurable<ModifiableFlexIdeBuildConfiguration>
  implements CompositeConfigurable.Item {

  private JPanel myMainPanel;

  private JTextField myNameField;

  private JComboBox myTargetPlatformCombo;
  private JCheckBox myPureActionScriptCheckBox;
  private JComboBox myOutputTypeCombo;
  private JPanel myOptimizeForPanel;
  private JComboBox myOptimizeForCombo;

  private JLabel myMainClassLabel;
  private JSReferenceEditor myMainClassComponent;
  private JLabel myMainClassWarning;
  private JTextField myOutputFileNameTextField;
  private JLabel myOutputFileNameWarning;
  private TextFieldWithBrowseButton myOutputFolderField;
  private JLabel myOutputFolderWarning;

  private JPanel myHtmlWrapperPanel;
  private JCheckBox myUseHTMLWrapperCheckBox;
  private JLabel myWrapperFolderLabel;
  private TextFieldWithBrowseButton myWrapperTemplateTextWithBrowse;
  private JButton myCreateHtmlWrapperTemplateButton;

  private JCheckBox mySkipCompilationCheckBox;
  private JLabel myWarning;

  private final Module myModule;
  private final ModifiableFlexIdeBuildConfiguration myConfiguration;
  private final Runnable myTreeNodeNameUpdater;
  private String myName;

  private final DependenciesConfigurable myDependenciesConfigurable;
  private final CompilerOptionsConfigurable myCompilerOptionsConfigurable;
  private final @Nullable AirDesktopPackagingConfigurable myAirDesktopPackagingConfigurable;
  private final @Nullable AndroidPackagingConfigurable myAndroidPackagingConfigurable;
  private final @Nullable IOSPackagingConfigurable myIOSPackagingConfigurable;

  public FlexIdeBCConfigurable(final Module module,
                               final ModifiableFlexIdeBuildConfiguration bc,
                               final Runnable treeNodeNameUpdater,
                               final @NotNull FlexProjectConfigurationEditor configEditor,
                               final ProjectSdksModel sdksModel) {
    super(false, treeNodeNameUpdater);
    myModule = module;
    myConfiguration = bc;
    myTreeNodeNameUpdater = treeNodeNameUpdater;
    myName = bc.getName();

    final BuildConfigurationNature nature = bc.getNature();

    myDependenciesConfigurable = new DependenciesConfigurable(bc, module.getProject(), configEditor, sdksModel);
    myCompilerOptionsConfigurable =
      new CompilerOptionsConfigurable(module, bc.getNature(), myDependenciesConfigurable, bc.getCompilerOptions());

    myCompilerOptionsConfigurable.addAdditionalOptionsListener(new CompilerOptionsConfigurable.OptionsListener() {
      public void configFileChanged(final String additionalConfigFilePath) {
        checkIfConfigFileOverridesOptions(additionalConfigFilePath);
      }

      public void additionalOptionsChanged(final String additionalOptions) {
        // may be parse additionalOptions in the same way as config file
      }
    });

    final Computable<String> mainClassComputable = new Computable<String>() {
      public String compute() {
        return myMainClassComponent.getText().trim();
      }
    };
    final Computable<String> airVersionComputable = new Computable<String>() {
      public String compute() {
        final Sdk sdk = myDependenciesConfigurable.getCurrentSdk();
        return sdk == null ? "" : FlexSdkUtils.getAirVersion(sdk.getVersionString());
      }
    };
    final Computable<Boolean> androidEnabledComputable = new Computable<Boolean>() {
      public Boolean compute() {
        return myAndroidPackagingConfigurable != null && myAndroidPackagingConfigurable.isPackagingEnabled();
      }
    };
    final Computable<Boolean> iosEnabledComputable = new Computable<Boolean>() {
      public Boolean compute() {
        return myIOSPackagingConfigurable != null && myIOSPackagingConfigurable.isPackagingEnabled();
      }
    };
    final Consumer<String> createdDescriptorConsumer = new Consumer<String>() {
      // called only for mobile projects if generated descriptor contains both Android and iOS 
      public void consume(final String descriptorPath) {
        assert myAndroidPackagingConfigurable != null && myIOSPackagingConfigurable != null;
        myAndroidPackagingConfigurable.setUseCustomDescriptor(descriptorPath);
        myIOSPackagingConfigurable.setUseCustomDescriptor(descriptorPath);
      }
    };

    myAirDesktopPackagingConfigurable = nature.isDesktopPlatform() && nature.isApp()
                                        ? new AirDesktopPackagingConfigurable(module, bc.getAirDesktopPackagingOptions(),
                                                                              mainClassComputable, airVersionComputable,
                                                                              androidEnabledComputable, iosEnabledComputable,
                                                                              createdDescriptorConsumer)
                                        : null;
    myAndroidPackagingConfigurable = nature.isMobilePlatform() && nature.isApp()
                                     ? new AndroidPackagingConfigurable(module, bc.getAndroidPackagingOptions(),
                                                                        mainClassComputable, airVersionComputable, androidEnabledComputable,
                                                                        iosEnabledComputable, createdDescriptorConsumer)
                                     : null;
    myIOSPackagingConfigurable = nature.isMobilePlatform() && nature.isApp()
                                 ? new IOSPackagingConfigurable(module, bc.getIosPackagingOptions(), mainClassComputable,
                                                                airVersionComputable, androidEnabledComputable, iosEnabledComputable,
                                                                createdDescriptorConsumer)
                                 : null;

    myNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        setDisplayName(myNameField.getText());
        if (treeNodeNameUpdater != null) {
          treeNodeNameUpdater.run();
        }
      }
    });

    TargetPlatform.initCombo(myTargetPlatformCombo);
    OutputType.initCombo(myOutputTypeCombo);

    myOutputFolderField.addBrowseFolderListener(null, null, module.getProject(),
                                                FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myUseHTMLWrapperCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        IdeFocusManager.getInstance(module.getProject()).requestFocus(myWrapperTemplateTextWithBrowse.getTextField(), true);
      }
    });

    final String title = "Select folder with HTML wrapper template";
    final String description = "Folder must contain 'index.template.html' file which must contain '${swf}' macro.";
    myWrapperTemplateTextWithBrowse.addBrowseFolderListener(title, description, module.getProject(),
                                                            FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myCreateHtmlWrapperTemplateButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final Sdk sdk = myDependenciesConfigurable.getCurrentSdk();
        if (sdk == null) {
          Messages.showInfoMessage(myModule.getProject(), FlexBundle.message("sdk.needed.to.create.wrapper"),
                                   CreateHtmlWrapperTemplateDialog.TITLE);
        }
        else {
          String path = myWrapperTemplateTextWithBrowse.getText().trim();
          if (path.isEmpty()) {
            path = FlexUtils.getContentOrModuleFolderPath(module) + "/" + CreateHtmlWrapperTemplateDialog.HTML_TEMPLATE_FOLDER_NAME;
          }
          final CreateHtmlWrapperTemplateDialog dialog = new CreateHtmlWrapperTemplateDialog(module, sdk, path);
          dialog.show();
          if (dialog.isOK()) {
            myWrapperTemplateTextWithBrowse.setText(FileUtil.toSystemDependentName(dialog.getWrapperFolderPath()));
          }
        }
      }
    });

    myOptimizeForCombo.setModel(new CollectionComboBoxModel(Arrays.asList(""), ""));
    myOptimizeForCombo.setRenderer(new ListCellRendererWrapper(myOptimizeForCombo.getRenderer()) {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if ("".equals(value)) {
          setText("<no optimization>");
        }
      }
    });


    myMainClassWarning.setIcon(IconLoader.getIcon("smallWarning.png"));
    myOutputFileNameWarning.setIcon(IconLoader.getIcon("smallWarning.png"));
    myOutputFolderWarning.setIcon(IconLoader.getIcon("smallWarning.png"));

    myWarning.setIcon(UIUtil.getBalloonWarningIcon());
  }

  private void checkIfConfigFileOverridesOptions(final String configFilePath) {
    String mainClass = null;
    String outputPath = null;
    String targetPlayer = null;

    final VirtualFile configFile = configFilePath.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(configFilePath);
    if (configFile != null) {
      final FileDocumentManager manager = FileDocumentManager.getInstance();
      if (manager.isFileModified(configFile)) {
        final Document document = manager.getCachedDocument(configFile);
        if (document != null) {
          manager.saveDocument(document);
        }
      }

      final List<String> xmlElements = Arrays.asList(FILE_SPEC_ELEMENT, OUTPUT_ELEMENT, TARGET_PLAYER_ELEMENT);
      try {
        final Map<String, List<String>> map = FlexUtils.findXMLElements(configFile.getInputStream(), xmlElements);

        final List<String> fileSpecList = map.get(FILE_SPEC_ELEMENT);
        if (!fileSpecList.isEmpty()) {
          mainClass = getClassForOutputTagValue(myModule.getProject(), fileSpecList.get(0), configFile.getParent());
        }

        final List<String> outputList = map.get(OUTPUT_ELEMENT);
        if (!outputList.isEmpty()) {
          outputPath = outputList.get(0);
          if (!FileUtil.isAbsolute(outputPath)) {
            outputPath = configFile.getParent().getPath() + "/" + outputPath;
          }
        }

        final List<String> targetPlayerList = map.get(TARGET_PLAYER_ELEMENT);
        if (!targetPlayerList.isEmpty()) {
          targetPlayer = targetPlayerList.get(0);
        }
      }
      catch (IOException ignore) {/*ignore*/ }
    }

    overriddenValuesChanged(mainClass, outputPath);
    myDependenciesConfigurable.overriddenTargetPlayerChanged(targetPlayer);
  }

  private static String getClassForOutputTagValue(final Project project, final String outputTagValue, final VirtualFile baseDir) {
    if (outputTagValue.isEmpty()) return "unknown";

    final VirtualFile file = VfsUtil.findRelativeFile(outputTagValue, baseDir);
    if (file == null) return FileUtil.getNameWithoutExtension(PathUtil.getFileName(outputTagValue));

    final VirtualFile sourceRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(file);
    if (sourceRoot == null) return file.getNameWithoutExtension();

    final String relativePath = VfsUtilCore.getRelativePath(file, sourceRoot, '/');
    return relativePath == null ? file.getNameWithoutExtension() : FileUtil.getNameWithoutExtension(relativePath).replace("/", ".");
  }

  /**
   * Called when {@link CompilerOptionsConfigurable} is initialized and when path to additional config file is changed
   * <code>null</code> parameter value means that the value is not overridden in additional config file
   */
  public void overriddenValuesChanged(final @Nullable String mainClass, final @Nullable String outputPath) {
    final String outputFileName = outputPath == null ? null : PathUtil.getFileName(outputPath);
    final String outputFolderPath = outputPath == null ? null : PathUtil.getParentPath(outputPath);

    myMainClassWarning.setToolTipText(FlexBundle.message("actual.value.from.config.file.0", mainClass));
    myMainClassWarning.setVisible(myMainClassComponent.isVisible() && mainClass != null);

    myOutputFileNameWarning.setToolTipText(FlexBundle.message("actual.value.from.config.file.0", outputFileName));
    myOutputFileNameWarning.setVisible(outputFileName != null);

    myOutputFolderWarning.setToolTipText(
      FlexBundle.message("actual.value.from.config.file.0", FileUtil.toSystemDependentName(StringUtil.notNullize(outputFolderPath))));
    myOutputFolderWarning.setVisible(outputFolderPath != null);

    final String warning = myMainClassWarning.isVisible() && outputPath == null
                           ? FlexBundle.message("overridden.in.config.file", "Main class", mainClass)
                           : !myMainClassWarning.isVisible() && outputPath != null
                             ? FlexBundle.message("overridden.in.config.file", "Output path", FileUtil.toSystemDependentName(outputPath))
                             : FlexBundle.message("main.class.and.output.overridden.in.config.file");
    myWarning.setText(warning);

    myWarning.setVisible(myMainClassWarning.isVisible() || myOutputFileNameWarning.isVisible() || myOutputFolderWarning.isVisible());
  }

  @Nls
  public String getDisplayName() {
    return myName;
  }

  @Override
  public void updateName() {
    myNameField.setText(getDisplayName());
  }

  public void setDisplayName(final String name) {
    myName = name;
  }

  public String getBannerSlogan() {
    return "Build Configuration '" + myName + "'";
  }

  public Module getModule() {
    return myModule;
  }

  public String getModuleName() {
    final ModuleEditor moduleEditor = getModulesConfigurator().getModuleEditor(myModule);
    assert moduleEditor != null : myModule;
    return moduleEditor.getName();
  }

  private ModulesConfigurator getModulesConfigurator() {
    return ModuleStructureConfigurable.getInstance(myModule.getProject()).getContext().getModulesConfigurator();
  }

  public Icon getIcon() {
    return myConfiguration.getIcon();
  }

  public ModifiableFlexIdeBuildConfiguration getEditableObject() {
    return myConfiguration;
  }

  public String getHelpTopic() {
    return "Build_Configuration_page";
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  private void updateControls() {
    final OutputType outputType = (OutputType)myOutputTypeCombo.getSelectedItem();

    myOptimizeForPanel.setVisible(outputType == OutputType.RuntimeLoadedModule);

    final boolean showMainClass = outputType == OutputType.Application || outputType == OutputType.RuntimeLoadedModule;
    myMainClassLabel.setVisible(showMainClass);
    myMainClassComponent.setVisible(showMainClass);

    myHtmlWrapperPanel.setVisible(
      myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Web && myOutputTypeCombo.getSelectedItem() == OutputType.Application);
    myWrapperFolderLabel.setEnabled(myUseHTMLWrapperCheckBox.isSelected());
    myWrapperTemplateTextWithBrowse.setEnabled(myUseHTMLWrapperCheckBox.isSelected());
    myCreateHtmlWrapperTemplateButton.setEnabled(myUseHTMLWrapperCheckBox.isSelected());
  }

  public String getTreeNodeText() {
    StringBuilder b = new StringBuilder();
    if (myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Mobile) {
      b.append("Mobile");
    }
    else if (myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Desktop) {
      b.append("AIR");
    }
    else {
      if (myPureActionScriptCheckBox.isSelected()) {
        b.append("AS");
      }
      else {
        b.append("Flex");
      }
    }
    b.append(" ");
    if (myOutputTypeCombo.getSelectedItem() == OutputType.Application) {
      b.append("App");
    }
    else if (myOutputTypeCombo.getSelectedItem() == OutputType.RuntimeLoadedModule) {
      b.append("Runtime module");
    }
    else {
      b.append("Lib");
    }
    b.append(": ").append(myName);
    return b.toString();
  }

  public OutputType getOutputType() {
    // immutable field
    return myConfiguration.getOutputType();
  }

  public boolean isModified() {
    if (!myConfiguration.getName().equals(myName)) return true;
    if (myConfiguration.getTargetPlatform() != myTargetPlatformCombo.getSelectedItem()) return true;
    if (myConfiguration.isPureAs() != myPureActionScriptCheckBox.isSelected()) return true;
    if (myConfiguration.getOutputType() != myOutputTypeCombo.getSelectedItem()) return true;
    if (!myConfiguration.getOptimizeFor().equals(myOptimizeForCombo.getSelectedItem())) return true;
    if (!myConfiguration.getMainClass().equals(myMainClassComponent.getText().trim())) return true;
    if (!myConfiguration.getOutputFileName().equals(myOutputFileNameTextField.getText().trim())) return true;
    if (!myConfiguration.getOutputFolder().equals(FileUtil.toSystemIndependentName(myOutputFolderField.getText().trim()))) return true;
    if (myConfiguration.isUseHtmlWrapper() != myUseHTMLWrapperCheckBox.isSelected()) return true;
    if (!myConfiguration.getWrapperTemplatePath()
      .equals(FileUtil.toSystemIndependentName(myWrapperTemplateTextWithBrowse.getText().trim()))) {
      return true;
    }
    if (myConfiguration.isSkipCompile() != mySkipCompilationCheckBox.isSelected()) return true;

    if (myDependenciesConfigurable.isModified()) return true;
    if (myCompilerOptionsConfigurable.isModified()) return true;
    if (myAirDesktopPackagingConfigurable != null && myAirDesktopPackagingConfigurable.isModified()) return true;
    if (myAndroidPackagingConfigurable != null && myAndroidPackagingConfigurable.isModified()) return true;
    if (myIOSPackagingConfigurable != null && myIOSPackagingConfigurable.isModified()) return true;

    return false;
  }

  public void apply() throws ConfigurationException {
    applyOwnTo(myConfiguration, true);

    myDependenciesConfigurable.apply();
    myCompilerOptionsConfigurable.apply();
    if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.apply();
    if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.apply();
    if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.apply();
  }

  private void applyTo(final ModifiableFlexIdeBuildConfiguration configuration, boolean validate) throws ConfigurationException {
    applyOwnTo(configuration, validate);

    myDependenciesConfigurable.applyTo(configuration.getDependencies());
    myCompilerOptionsConfigurable.applyTo(configuration.getCompilerOptions());
    if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.applyTo(configuration.getAirDesktopPackagingOptions());
    if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.applyTo(configuration.getAndroidPackagingOptions());
    if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.applyTo(configuration.getIosPackagingOptions());
  }

  private void applyOwnTo(ModifiableFlexIdeBuildConfiguration configuration, boolean validate) throws ConfigurationException {
    if (validate && StringUtil.isEmptyOrSpaces(myName)) {
      throw new ConfigurationException("Module '" + getModuleName() + "': build configuration name is empty");
    }
    configuration.setName(myName);
    configuration.setTargetPlatform((TargetPlatform)myTargetPlatformCombo.getSelectedItem());
    configuration.setPureAs(myPureActionScriptCheckBox.isSelected());
    configuration.setOutputType((OutputType)myOutputTypeCombo.getSelectedItem());
    configuration.setOptimizeFor((String)myOptimizeForCombo.getSelectedItem()); // todo myOptimizeForCombo should contain live information
    configuration.setMainClass(myMainClassComponent.getText().trim());
    configuration.setOutputFileName(myOutputFileNameTextField.getText().trim());
    configuration.setOutputFolder(FileUtil.toSystemIndependentName(myOutputFolderField.getText().trim()));
    configuration.setUseHtmlWrapper(myUseHTMLWrapperCheckBox.isSelected());
    configuration.setWrapperTemplatePath(FileUtil.toSystemIndependentName(myWrapperTemplateTextWithBrowse.getText().trim()));
    configuration.setSkipCompile(mySkipCompilationCheckBox.isSelected());
  }

  public void reset() {
    setDisplayName(myConfiguration.getName());
    myTargetPlatformCombo.setSelectedItem(myConfiguration.getTargetPlatform());
    myPureActionScriptCheckBox.setSelected(myConfiguration.isPureAs());
    myOutputTypeCombo.setSelectedItem(myConfiguration.getOutputType());
    myOptimizeForCombo.setSelectedItem(myConfiguration.getOptimizeFor());

    myMainClassComponent.setText(myConfiguration.getMainClass());
    myOutputFileNameTextField.setText(myConfiguration.getOutputFileName());
    myOutputFolderField.setText(FileUtil.toSystemDependentName(myConfiguration.getOutputFolder()));
    myUseHTMLWrapperCheckBox.setSelected(myConfiguration.isUseHtmlWrapper());
    myWrapperTemplateTextWithBrowse.setText(FileUtil.toSystemDependentName(myConfiguration.getWrapperTemplatePath()));
    mySkipCompilationCheckBox.setSelected(myConfiguration.isSkipCompile());

    updateControls();
    overriddenValuesChanged(null, null); // no warnings initially

    myDependenciesConfigurable.reset();
    myCompilerOptionsConfigurable.reset();
    if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.reset();
    if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.reset();
    if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.reset();
  }

  public void disposeUIResources() {
    myDependenciesConfigurable.disposeUIResources();
    myCompilerOptionsConfigurable.disposeUIResources();
    if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.disposeUIResources();
    if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.disposeUIResources();
    if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.disposeUIResources();
  }

  //public ModifiableFlexIdeBuildConfiguration getCurrentConfiguration() {
  //  final ModifiableFlexIdeBuildConfiguration configuration = Factory.createBuildConfiguration();
  //  try {
  //    applyTo(configuration, false);
  //  }
  //  catch (ConfigurationException ignored) {
  //    // no validation
  //  }
  //  return configuration;
  //}

  public List<NamedConfigurable> getChildren() {
    final List<NamedConfigurable> children = new ArrayList<NamedConfigurable>();

    children.add(myDependenciesConfigurable);
    children.add(myCompilerOptionsConfigurable);
    ContainerUtil.addIfNotNull(myAirDesktopPackagingConfigurable, children);
    ContainerUtil.addIfNotNull(myAndroidPackagingConfigurable, children);
    ContainerUtil.addIfNotNull(myIOSPackagingConfigurable, children);

    return children;
  }

  public CompositeConfigurable wrapInTabs() {
    List<NamedConfigurable> tabs = new ArrayList<NamedConfigurable>();
    tabs.add(this);
    tabs.addAll(getChildren());
    return new CompositeConfigurable(tabs, myTreeNodeNameUpdater);
  }

  public boolean isParentFor(final DependenciesConfigurable dependenciesConfigurable) {
    return myDependenciesConfigurable == dependenciesConfigurable;
  }

  private void createUIComponents() {
    final String baseClass = myConfiguration.getOutputType() == OutputType.RuntimeLoadedModule
                             ? FlexCompilerSettingsEditor.MODULE_BASE_CLASS_NAME
                             : FlexCompilerSettingsEditor.SPRITE_CLASS_NAME;
    final Condition<JSClass> filter = new JSClassChooserDialog.PublicInheritor(myModule, baseClass, true);
    myMainClassComponent = JSReferenceEditor.forClassName("", myModule.getProject(), null, GlobalSearchScope.moduleScope(myModule), null,
                                                          filter, ExecutionBundle.message("choose.main.class.dialog.title"));
  }

  public static FlexIdeBCConfigurable unwrap(CompositeConfigurable c) {
    return (FlexIdeBCConfigurable)c.getMainChild();
  }

  @Override
  public String getTabTitle() {
    return "General";
  }
}
