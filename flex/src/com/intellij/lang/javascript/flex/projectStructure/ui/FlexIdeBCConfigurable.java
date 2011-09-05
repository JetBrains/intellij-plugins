package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration.OutputType;
import static com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration.TargetPlatform;

public class FlexIdeBCConfigurable extends /*ProjectStructureElementConfigurable*/NamedConfigurable<FlexIdeBuildConfiguration>
  implements CompositeConfigurable.Item {

  private JPanel myMainPanel;

  private JTextField myNameField;

  private JComboBox myTargetPlatformCombo;
  private JCheckBox myPureActionScriptCheckBox;
  private JComboBox myOutputTypeCombo;
  private JLabel myOptimizeForLabel;
  private JComboBox myOptimizeForCombo;

  private JLabel myMainClassLabel;
  private JTextField myMainClassTextField;
  private JTextField myOutputFileNameTextField;
  private TextFieldWithBrowseButton myOutputFolderField;

  private JCheckBox myUseHTMLWrapperCheckBox;
  private JLabel myWrapperFolderLabel;
  private TextFieldWithBrowseButton myWrapperTemplateTextWithBrowse;

  private final Module myModule;
  private final FlexIdeBuildConfiguration myConfiguration;
  private final Runnable myTreeNodeNameUpdater;
  private String myName;

  private final DependenciesConfigurable myDependenciesConfigurable;
  private final CompilerOptionsConfigurable myCompilerOptionsConfigurable;
  private final @Nullable AirDesktopPackagingConfigurable myAirDesktopPackagingConfigurable;
  private final @Nullable AndroidPackagingConfigurable myAndroidPackagingConfigurable;
  private final @Nullable IOSPackagingConfigurable myIOSPackagingConfigurable;

  public FlexIdeBCConfigurable(final Module module,
                               final FlexIdeBuildConfiguration configuration,
                               final FlexSdksModifiableModel sdksModel,
                               final Runnable treeNodeNameUpdater) {
    super(false, treeNodeNameUpdater);
    myModule = module;
    myConfiguration = configuration;
    myTreeNodeNameUpdater = treeNodeNameUpdater;
    myName = configuration.NAME;

    final BuildConfigurationNature nature = configuration.getNature();

    ModifiableRootModel modifiableRootModel = getModulesConfigurator().getOrCreateModuleEditor(myModule).getModifiableRootModelProxy();
    myDependenciesConfigurable = new DependenciesConfigurable(configuration, module.getProject(), sdksModel, modifiableRootModel);
    myCompilerOptionsConfigurable = new CompilerOptionsConfigurable(module, configuration.COMPILER_OPTIONS);
    myAirDesktopPackagingConfigurable = nature.isDesktopPlatform() && nature.isApp()
                                        ? new AirDesktopPackagingConfigurable(module, configuration.AIR_DESKTOP_PACKAGING_OPTIONS)
                                        : null;
    myAndroidPackagingConfigurable = nature.isMobilePlatform() && nature.isApp()
                                     ? new AndroidPackagingConfigurable(module.getProject(), configuration.ANDROID_PACKAGING_OPTIONS)
                                     : null;
    myIOSPackagingConfigurable = nature.isMobilePlatform() && nature.isApp()
                                 ? new IOSPackagingConfigurable(module.getProject(), configuration.IOS_PACKAGING_OPTIONS)
                                 : null;

    myNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        setDisplayName(myNameField.getText());
        if (treeNodeNameUpdater != null) {
          treeNodeNameUpdater.run();
        }
      }
    });

    initCombos();
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

  public FlexIdeBuildConfiguration getEditableObject() {
    return myConfiguration;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  private void initCombos() {
    myTargetPlatformCombo.setModel(new DefaultComboBoxModel(TargetPlatform.values()));
    myTargetPlatformCombo.setRenderer(new ListCellRendererWrapper<TargetPlatform>(myTargetPlatformCombo.getRenderer()) {
      public void customize(JList list, TargetPlatform value, int index, boolean selected, boolean hasFocus) {
        setText(value.PRESENTABLE_TEXT);
      }
    });

    myOutputTypeCombo.setModel(new DefaultComboBoxModel(OutputType.values()));
    myOutputTypeCombo.setRenderer(new ListCellRendererWrapper<OutputType>(myOutputTypeCombo.getRenderer()) {
      public void customize(JList list, OutputType value, int index, boolean selected, boolean hasFocus) {
        setText(value.PRESENTABLE_TEXT);
      }
    });
  }

  private void updateControls() {
    final OutputType outputType = (OutputType)myOutputTypeCombo.getSelectedItem();

    myOptimizeForLabel.setVisible(outputType == OutputType.RuntimeLoadedModule);
    myOptimizeForCombo.setVisible(outputType == OutputType.RuntimeLoadedModule);

    final boolean showMainClass = outputType == OutputType.Application || outputType == OutputType.RuntimeLoadedModule;
    myMainClassLabel.setVisible(showMainClass);
    myMainClassTextField.setVisible(showMainClass);

    final boolean wrapperApplicable =
      myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Web && myOutputTypeCombo.getSelectedItem() == OutputType.Application;
    final boolean enabled = wrapperApplicable && myUseHTMLWrapperCheckBox.isSelected();
    myUseHTMLWrapperCheckBox.setVisible(wrapperApplicable);
    myWrapperFolderLabel.setVisible(wrapperApplicable);
    myWrapperFolderLabel.setEnabled(enabled);
    myWrapperTemplateTextWithBrowse.setVisible(wrapperApplicable);
    myWrapperTemplateTextWithBrowse.setEnabled(enabled);
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
    return myConfiguration.OUTPUT_TYPE;
  }

  public boolean isModified() {
    if (!myConfiguration.NAME.equals(myName)) return true;
    if (myConfiguration.TARGET_PLATFORM != myTargetPlatformCombo.getSelectedItem()) return true;
    if (myConfiguration.PURE_ACTION_SCRIPT != myPureActionScriptCheckBox.isSelected()) return true;
    if (myConfiguration.OUTPUT_TYPE != myOutputTypeCombo.getSelectedItem()) return true;
    if (!myConfiguration.OPTIMIZE_FOR.equals(myOptimizeForCombo.getSelectedItem())) return true;
    if (!myConfiguration.MAIN_CLASS.equals(myMainClassTextField.getText().trim())) return true;
    if (!myConfiguration.OUTPUT_FILE_NAME.equals(myOutputFileNameTextField.getText().trim())) return true;
    if (!myConfiguration.OUTPUT_FOLDER.equals(FileUtil.toSystemIndependentName(myOutputFolderField.getText().trim()))) return true;
    if (myConfiguration.USE_HTML_WRAPPER != myUseHTMLWrapperCheckBox.isSelected()) return true;
    if (!myConfiguration.WRAPPER_TEMPLATE_PATH.equals(FileUtil.toSystemIndependentName(myWrapperTemplateTextWithBrowse.getText().trim()))) {
      return true;
    }

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

  private void applyTo(final FlexIdeBuildConfiguration configuration, boolean validate) throws ConfigurationException {
    applyOwnTo(configuration, validate);

    myDependenciesConfigurable.applyTo(configuration.DEPENDENCIES);
    myCompilerOptionsConfigurable.applyTo(configuration.COMPILER_OPTIONS);
    if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.applyTo(configuration.AIR_DESKTOP_PACKAGING_OPTIONS);
    if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.applyTo(configuration.ANDROID_PACKAGING_OPTIONS);
    if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.applyTo(configuration.IOS_PACKAGING_OPTIONS);
  }

  private void applyOwnTo(FlexIdeBuildConfiguration configuration, boolean validate) throws ConfigurationException {
    if (validate && StringUtil.isEmptyOrSpaces(myName)) {
      throw new ConfigurationException("Module '" + getModuleName() + "': build configuration name is empty");
    }
    configuration.NAME = myName;
    configuration.TARGET_PLATFORM = (TargetPlatform)myTargetPlatformCombo.getSelectedItem();
    configuration.PURE_ACTION_SCRIPT = myPureActionScriptCheckBox.isSelected();
    configuration.OUTPUT_TYPE = (OutputType)myOutputTypeCombo.getSelectedItem();
    configuration.OPTIMIZE_FOR = (String)myOptimizeForCombo.getSelectedItem(); // todo myOptimizeForCombo should contain live information
    configuration.MAIN_CLASS = myMainClassTextField.getText().trim();
    configuration.OUTPUT_FILE_NAME = myOutputFileNameTextField.getText().trim();
    configuration.OUTPUT_FOLDER = FileUtil.toSystemIndependentName(myOutputFolderField.getText().trim());
    configuration.USE_HTML_WRAPPER = myUseHTMLWrapperCheckBox.isSelected();
    configuration.WRAPPER_TEMPLATE_PATH = FileUtil.toSystemIndependentName(myWrapperTemplateTextWithBrowse.getText().trim());
  }

  public void reset() {
    setDisplayName(myConfiguration.NAME);
    myTargetPlatformCombo.setSelectedItem(myConfiguration.TARGET_PLATFORM);
    myPureActionScriptCheckBox.setSelected(myConfiguration.PURE_ACTION_SCRIPT);
    myOutputTypeCombo.setSelectedItem(myConfiguration.OUTPUT_TYPE);
    myOptimizeForCombo.setSelectedItem(myConfiguration.OPTIMIZE_FOR);

    myMainClassTextField.setText(myConfiguration.MAIN_CLASS);
    myOutputFileNameTextField.setText(myConfiguration.OUTPUT_FILE_NAME);
    myOutputFolderField.setText(FileUtil.toSystemDependentName(myConfiguration.OUTPUT_FOLDER));
    myUseHTMLWrapperCheckBox.setSelected(myConfiguration.USE_HTML_WRAPPER);
    myWrapperTemplateTextWithBrowse.setText(FileUtil.toSystemDependentName(myConfiguration.WRAPPER_TEMPLATE_PATH));

    updateControls();

    myDependenciesConfigurable.reset();
    myCompilerOptionsConfigurable.reset();
    if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.reset();
    if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.reset();
    if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.reset();
  }

  public void disposeUIResources() {
    if (FlexIdeUtils.isFlatUi()) {
      myDependenciesConfigurable.disposeUIResources();
      myCompilerOptionsConfigurable.disposeUIResources();
      if (myAirDesktopPackagingConfigurable != null) myAirDesktopPackagingConfigurable.disposeUIResources();
      if (myAndroidPackagingConfigurable != null) myAndroidPackagingConfigurable.disposeUIResources();
      if (myIOSPackagingConfigurable != null) myIOSPackagingConfigurable.disposeUIResources();
    }
  }

  public FlexIdeBuildConfiguration getCurrentConfiguration() {
    final FlexIdeBuildConfiguration configuration = new FlexIdeBuildConfiguration();
    try {
      applyTo(configuration, false);
    }
    catch (ConfigurationException ignored) {
      // no validation
    }
    return configuration;
  }

  public List<NamedConfigurable> getChildren() {
    final List<NamedConfigurable> children = new ArrayList<NamedConfigurable>();

    children.add(myDependenciesConfigurable);
    children.add(myCompilerOptionsConfigurable);
    ContainerUtil.addIfNotNull(myAirDesktopPackagingConfigurable, children);
    ContainerUtil.addIfNotNull(myAndroidPackagingConfigurable, children);
    ContainerUtil.addIfNotNull(myIOSPackagingConfigurable, children);

    return children;
  }

  public NamedConfigurable<FlexIdeBuildConfiguration> wrapInTabsIfNeeded() {
    if (!FlexIdeUtils.isFlatUi()) return this;

    List<NamedConfigurable> tabs = new ArrayList<NamedConfigurable>();
    tabs.add(this);
    tabs.addAll(getChildren());
    return new CompositeConfigurable(tabs, myTreeNodeNameUpdater);
  }

  public boolean isParentFor(final DependenciesConfigurable dependenciesConfigurable) {
    return myDependenciesConfigurable == dependenciesConfigurable;
  }

  public static FlexIdeBCConfigurable unwrapIfNeeded(NamedConfigurable c) {
    if (!FlexIdeUtils.isFlatUi()) {
      return (FlexIdeBCConfigurable)c;
    }

    return (FlexIdeBCConfigurable)((CompositeConfigurable)c).getMainChild();
  }

  @Override
  public String getTabTitle() {
    return "General";
  }
}
