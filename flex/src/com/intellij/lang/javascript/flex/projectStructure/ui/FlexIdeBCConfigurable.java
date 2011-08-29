package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration.*;

public class FlexIdeBCConfigurable extends /*ProjectStructureElementConfigurable*/NamedConfigurable<FlexIdeBuildConfiguration> {

  private JPanel myMainPanel;

  private JComboBox myTargetPlatformCombo;
  private JCheckBox myPureActionScriptCheckBox;
  private JComboBox myOutputTypeCombo;
  private JLabel myOptimizeForLabel;
  private JComboBox myOptimizeForCombo;
  private JLabel myTargetPlayerLabel;
  private JComboBox myTargetPlayerCombo;

  private JLabel myMainClassLabel;
  private JTextField myMainClassTextField;
  private JTextField myOutputFileNameTextField;
  private TextFieldWithBrowseButton myOutputFolderField;

  private final Module myModule;
  private final FlexIdeBuildConfiguration myConfiguration;
  private final ModifiableRootModel myModifiableRootModel;
  private String myName;

  private final DependenciesConfigurable myDependenciesConfigurable;
  private final CompilerOptionsConfigurable myCompilerOptionsConfigurable;
  private final HtmlWrapperConfigurable myHtmlWrapperConfigurable;
  private final AirDescriptorConfigurable myAirDescriptorConfigurable;
  private final AirDesktopPackagingConfigurable myAirDesktopPackagingConfigurable;
  private final AndroidPackagingConfigurable myAndroidPackagingConfigurable;
  private final IOSPackagingConfigurable myIOSPackagingConfigurable;
  private final ChangeListener myListener;

  public FlexIdeBCConfigurable(final Module module,
                               final FlexIdeBuildConfiguration configuration,
                               final Runnable treeNodeNameUpdater,
                               ModifiableRootModel modifiableRootModel) {
    super(true, treeNodeNameUpdater);
    myModule = module;
    myConfiguration = configuration;
    myModifiableRootModel = modifiableRootModel;
    myName = configuration.NAME;

    myDependenciesConfigurable =new DependenciesConfigurable(configuration, module.getProject());
    myCompilerOptionsConfigurable = new CompilerOptionsConfigurable(module, configuration.COMPILER_OPTIONS);
    myHtmlWrapperConfigurable = new HtmlWrapperConfigurable(module.getProject(), configuration.HTML_WRAPPER_OPTIONS);
    myAirDescriptorConfigurable = new AirDescriptorConfigurable(configuration.AIR_DESCRIPTOR_OPTIONS);
    myAirDesktopPackagingConfigurable =
      new AirDesktopPackagingConfigurable(module.getProject(), configuration.AIR_DESKTOP_PACKAGING_OPTIONS);
    myAndroidPackagingConfigurable = new AndroidPackagingConfigurable(module.getProject(), configuration.ANDROID_PACKAGING_OPTIONS);
    myIOSPackagingConfigurable = new IOSPackagingConfigurable(module.getProject(), configuration.IOS_PACKAGING_OPTIONS);

    myListener = new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        // TODO
      }
    };
    myDependenciesConfigurable.addFlexSdkListener(myListener);
    initCombos();
  }

  @Nls
  public String getDisplayName() {
    return myName;
  }

  public void setDisplayName(final String name) {
    myName = name;
  }

  public String getBannerSlogan() {
    return "Build Configuration '" + myConfiguration.NAME + "'";
  }

  public String getModuleName() {
    return myModifiableRootModel.getModule().getName();
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

    //myTargetPlayerCombo.setModel(); set in updateControls()
  }

  private void updateControls() {
    final TargetPlatform targetPlatform = (TargetPlatform)myTargetPlatformCombo.getSelectedItem();
    final boolean webPlatform = targetPlatform == TargetPlatform.Web;
    final OutputType outputType = (OutputType)myOutputTypeCombo.getSelectedItem();

    //myOutputTypeCombo.setModel(getSuitableOutputTypes(targetPlatform)); todo implement

    myOptimizeForLabel.setVisible(outputType == OutputType.RuntimeLoadedModule);
    myOptimizeForCombo.setVisible(outputType == OutputType.RuntimeLoadedModule);

    myTargetPlayerLabel.setVisible(webPlatform);
    myTargetPlayerCombo.setVisible(webPlatform);
    if (webPlatform) {
      final Object selectedPlayer = myTargetPlayerCombo.getSelectedItem();
      myTargetPlayerCombo.setModel(new DefaultComboBoxModel(getAvailablePlayersFromSdk()));
      myTargetPlayerCombo.setSelectedItem(selectedPlayer);
    }

    final boolean showMainClass = outputType == OutputType.Application || outputType == OutputType.RuntimeLoadedModule;
    myMainClassLabel.setVisible(showMainClass);
    myMainClassTextField.setVisible(showMainClass);

    final String outputFileName = myOutputFileNameTextField.getText();
    final String lowercase = outputFileName.toLowerCase();
    if (lowercase.endsWith(".swf") || lowercase.endsWith(".swc")) {
      myOutputFileNameTextField.setText(
        outputFileName.substring(0, outputFileName.length() - ".sw_".length()) + (outputType == OutputType.Library ? ".swc" : ".swf"));
    }
  }

  private String[] getAvailablePlayersFromSdk() {
    //String sdkVersion = myDependenciesConfigurable.getSdkChooserPanel().getCurrentSdk();
    return new String[]{"10.1", "10.2"}; // TODO implement
  }

  public ModifiableRootModel getModifiableRootModel() {
    return myModifiableRootModel;
  }

  /**
   * TODO remove this
   * @Deprecated
   */
  @Deprecated
  public boolean isModuleConfiguratorModified() {
    return false;
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
    if (!myConfiguration.TARGET_PLAYER.equals(myTargetPlayerCombo.getSelectedItem())) return true;
    if (!myConfiguration.MAIN_CLASS.equals(myMainClassTextField.getText().trim())) return true;
    if (!myConfiguration.OUTPUT_FILE_NAME.equals(myOutputFileNameTextField.getText().trim())) return true;
    if (!myConfiguration.OUTPUT_FOLDER.equals(myOutputFolderField.getText().trim())) return true;

    if (myDependenciesConfigurable.isModified()) return true;
    if (myCompilerOptionsConfigurable.isModified()) return true;
    if (myHtmlWrapperConfigurable.isModified()) return true;
    if (myAirDescriptorConfigurable.isModified()) return true;
    if (myAirDesktopPackagingConfigurable.isModified()) return true;
    if (myAndroidPackagingConfigurable.isModified()) return true;
    if (myIOSPackagingConfigurable.isModified()) return true;

    return false;
  }

  public void apply() throws ConfigurationException {
    applyOwnTo(myConfiguration);

    myDependenciesConfigurable.apply();
    myCompilerOptionsConfigurable.apply();
    myHtmlWrapperConfigurable.apply();
    myAirDescriptorConfigurable.apply();
    myAirDesktopPackagingConfigurable.apply();
    myAndroidPackagingConfigurable.apply();
    myIOSPackagingConfigurable.apply();
  }

  private void applyTo(final FlexIdeBuildConfiguration configuration) {
    applyOwnTo(configuration);

    myDependenciesConfigurable.applyTo(configuration.DEPENDENCIES);
    myCompilerOptionsConfigurable.applyTo(configuration.COMPILER_OPTIONS);
    myHtmlWrapperConfigurable.applyTo(configuration.HTML_WRAPPER_OPTIONS);
    myAirDescriptorConfigurable.applyTo(configuration.AIR_DESCRIPTOR_OPTIONS);
    myAirDesktopPackagingConfigurable.applyTo(configuration.AIR_DESKTOP_PACKAGING_OPTIONS);
    myAndroidPackagingConfigurable.applyTo(configuration.ANDROID_PACKAGING_OPTIONS);
    myIOSPackagingConfigurable.applyTo(configuration.IOS_PACKAGING_OPTIONS);
  }

  private void applyOwnTo(FlexIdeBuildConfiguration configuration) {
    configuration.NAME = myName;
    configuration.TARGET_PLATFORM = (TargetPlatform)myTargetPlatformCombo.getSelectedItem();
    configuration.PURE_ACTION_SCRIPT = myPureActionScriptCheckBox.isSelected();
    configuration.OUTPUT_TYPE = (OutputType)myOutputTypeCombo.getSelectedItem();
    configuration.OPTIMIZE_FOR = (String)myOptimizeForCombo.getSelectedItem(); // todo myOptimizeForCombo should contain live information
    configuration.TARGET_PLAYER = (String)myTargetPlayerCombo.getSelectedItem();
    configuration.MAIN_CLASS = myMainClassTextField.getText().trim();
    configuration.OUTPUT_FILE_NAME = myOutputFileNameTextField.getText().trim();
    configuration.OUTPUT_FOLDER = myOutputFolderField.getText().trim();
  }

  public void reset() {
    setDisplayName(myConfiguration.NAME);
    myTargetPlatformCombo.setSelectedItem(myConfiguration.TARGET_PLATFORM);
    myPureActionScriptCheckBox.setSelected(myConfiguration.PURE_ACTION_SCRIPT);
    myOutputTypeCombo.setSelectedItem(myConfiguration.OUTPUT_TYPE);
    myOptimizeForCombo.setSelectedItem(myConfiguration.OPTIMIZE_FOR);

    myTargetPlayerCombo.setSelectedItem(myConfiguration.TARGET_PLAYER);
    myMainClassTextField.setText(myConfiguration.MAIN_CLASS);
    myOutputFileNameTextField.setText(myConfiguration.OUTPUT_FILE_NAME);
    myOutputFolderField.setText(myConfiguration.OUTPUT_FOLDER);
    updateControls();

    myDependenciesConfigurable.reset();
    myCompilerOptionsConfigurable.reset();
    myHtmlWrapperConfigurable.reset();
    myAirDescriptorConfigurable.reset();
    myAirDesktopPackagingConfigurable.reset();
    myAndroidPackagingConfigurable.reset();
    myIOSPackagingConfigurable.reset();
  }

  public void disposeUIResources() {
    myDependenciesConfigurable.disposeUIResources();
    myCompilerOptionsConfigurable.disposeUIResources();
    myHtmlWrapperConfigurable.disposeUIResources();
    myAirDescriptorConfigurable.disposeUIResources();
    myAirDesktopPackagingConfigurable.disposeUIResources();
    myAndroidPackagingConfigurable.disposeUIResources();
    myIOSPackagingConfigurable.disposeUIResources();
    myDependenciesConfigurable.removeFlexSdkListener(myListener);
  }

  public DependenciesConfigurable getDependenciesConfigurable() {
    return myDependenciesConfigurable;
  }

  public CompilerOptionsConfigurable getCompilerOptionsConfigurable() {
    return myCompilerOptionsConfigurable;
  }

  public HtmlWrapperConfigurable getHtmlWrapperConfigurable() {
    return myHtmlWrapperConfigurable;
  }

  public AirDescriptorConfigurable getAirDescriptorConfigurable() {
    return myAirDescriptorConfigurable;
  }

  public AirDesktopPackagingConfigurable getAirDesktopPackagingConfigurable() {
    return myAirDesktopPackagingConfigurable;
  }

  public AndroidPackagingConfigurable getAndroidPackagingConfigurable() {
    return myAndroidPackagingConfigurable;
  }

  public IOSPackagingConfigurable getIOSPackagingConfigurable() {
    return myIOSPackagingConfigurable;
  }

  public FlexIdeBuildConfiguration getCurrentConfiguration() {
    final FlexIdeBuildConfiguration configuration = new FlexIdeBuildConfiguration();
    applyTo(configuration);
    return configuration;
  }
}
