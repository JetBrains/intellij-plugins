package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
  private JTextField myMainClassTextField;
  private JTextField myOutputFileNameTextField;
  private TextFieldWithBrowseButton myOutputFolderField;

  private JPanel myHtmlWrapperPanel;
  private JCheckBox myUseHTMLWrapperCheckBox;
  private JLabel myWrapperFolderLabel;
  private TextFieldWithBrowseButton myWrapperTemplateTextWithBrowse;
  private JButton myCreateHtmlTemplateButton;

  private JCheckBox mySkipCompilationCheckBox;

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
                               final ModifiableFlexIdeBuildConfiguration configuration,
                               final Runnable treeNodeNameUpdater,
                               @NotNull FlexProjectConfigurationEditor configEditor) {
    super(false, treeNodeNameUpdater);
    myModule = module;
    myConfiguration = configuration;
    myTreeNodeNameUpdater = treeNodeNameUpdater;
    myName = configuration.getName();

    final BuildConfigurationNature nature = configuration.getNature();

    myDependenciesConfigurable = new DependenciesConfigurable(configuration, module.getProject(), configEditor);
    myCompilerOptionsConfigurable = new CompilerOptionsConfigurable(module, configuration.getCompilerOptions());
    myAirDesktopPackagingConfigurable = nature.isDesktopPlatform() && nature.isApp()
                                        ? new AirDesktopPackagingConfigurable(module, configuration.getAirDesktopPackagingOptions())
                                        : null;
    myAndroidPackagingConfigurable = nature.isMobilePlatform() && nature.isApp()
                                     ? new AndroidPackagingConfigurable(module, configuration.getAndroidPackagingOptions())
                                     : null;
    myIOSPackagingConfigurable = nature.isMobilePlatform() && nature.isApp()
                                 ? new IOSPackagingConfigurable(module, configuration.getIosPackagingOptions())
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

    myOptimizeForCombo.setModel(new CollectionComboBoxModel(Arrays.asList(""), ""));
    myOptimizeForCombo.setRenderer(new ListCellRendererWrapper(myOptimizeForCombo.getRenderer()) {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if ("".equals(value)) {
          setText("<no optimization>");
        }
      }
    });
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
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  private void initCombos() {
    myTargetPlatformCombo.setModel(new DefaultComboBoxModel(TargetPlatform.values()));
    myTargetPlatformCombo.setRenderer(new ListCellRendererWrapper<TargetPlatform>(myTargetPlatformCombo.getRenderer()) {
      public void customize(JList list, TargetPlatform value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableText());
      }
    });

    myOutputTypeCombo.setModel(new DefaultComboBoxModel(OutputType.values()));
    myOutputTypeCombo.setRenderer(new ListCellRendererWrapper<OutputType>(myOutputTypeCombo.getRenderer()) {
      public void customize(JList list, OutputType value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableText());
      }
    });
  }

  private void updateControls() {
    final OutputType outputType = (OutputType)myOutputTypeCombo.getSelectedItem();

    myOptimizeForPanel.setVisible(outputType == OutputType.RuntimeLoadedModule);

    final boolean showMainClass = outputType == OutputType.Application || outputType == OutputType.RuntimeLoadedModule;
    myMainClassLabel.setVisible(showMainClass);
    myMainClassTextField.setVisible(showMainClass);

    myHtmlWrapperPanel.setVisible(
      myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Web && myOutputTypeCombo.getSelectedItem() == OutputType.Application);
    myWrapperFolderLabel.setEnabled(myUseHTMLWrapperCheckBox.isSelected());
    myWrapperTemplateTextWithBrowse.setEnabled(myUseHTMLWrapperCheckBox.isSelected());
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
    if (!myConfiguration.getMainClass().equals(myMainClassTextField.getText().trim())) return true;
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
    configuration.setMainClass(myMainClassTextField.getText().trim());
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

    myMainClassTextField.setText(myConfiguration.getMainClass());
    myOutputFileNameTextField.setText(myConfiguration.getOutputFileName());
    myOutputFolderField.setText(FileUtil.toSystemDependentName(myConfiguration.getOutputFolder()));
    myUseHTMLWrapperCheckBox.setSelected(myConfiguration.isUseHtmlWrapper());
    myWrapperTemplateTextWithBrowse.setText(FileUtil.toSystemDependentName(myConfiguration.getWrapperTemplatePath()));
    mySkipCompilationCheckBox.setSelected(myConfiguration.isSkipCompile());

    updateControls();

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

  public static FlexIdeBCConfigurable unwrap(CompositeConfigurable c) {
    return (FlexIdeBCConfigurable)c.getMainChild();
  }

  @Override
  public String getTabTitle() {
    return "General";
  }
}
