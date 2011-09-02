package com.intellij.lang.javascript.flex.run;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.PlatformIcons;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class FlexIdeRunConfigurationForm extends SettingsEditor<FlexIdeRunConfiguration> {

  private JPanel myMainPanel;
  private JComboBox myBCsCombo;
  
  private JPanel myLaunchPanel;
  private JRadioButton myBCOutputRadioButton;
  private JLabel myBCOutputLabel;
  private JRadioButton myURLRadioButton;
  private JTextField myURLTextField;

  private JPanel myWebOptionsPanel;
  private TextFieldWithBrowseButton myLauncherParametersTextWithBrowse;
  private JCheckBox myRunTrustedCheckBox;

  private JPanel myDesktopOptionsPanel;
  private RawCommandLineEditor myAdlOptionsEditor;
  private RawCommandLineEditor myAirProgramParametersEditor;

  private final Project myProject;
  private FlexIdeBuildConfiguration[] myAllConfigs;
  private boolean mySingleModuleProject;
  private Map<FlexIdeBuildConfiguration, Module> myBCToModuleMap = new THashMap<FlexIdeBuildConfiguration, Module>();

  private LauncherParameters myLauncherParameters;

  public FlexIdeRunConfigurationForm(final Project project) {
    myProject = project;

    initBCCombo();
    initRadioButtons();
    initLaunchWithTextWithBrowse();
  }

  private void initBCCombo() {
    final Collection<FlexIdeBuildConfiguration> allConfigs = new ArrayList<FlexIdeBuildConfiguration>();

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    mySingleModuleProject = modules.length == 1;
    for (final Module module : modules) {
      if (ModuleType.get(module) instanceof FlexModuleType) {
        for (final FlexIdeBuildConfiguration config : FlexIdeBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
          if (config.OUTPUT_TYPE == FlexIdeBuildConfiguration.OutputType.Application) {
            allConfigs.add(config);
            myBCToModuleMap.put(config, module);
          }
        }
      }
    }
    myAllConfigs = allConfigs.toArray(new FlexIdeBuildConfiguration[allConfigs.size()]);

    myBCsCombo.setRenderer(new ListCellRendererWrapper(myBCsCombo.getRenderer()) {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof Pair) {
          final String moduleName = (String)((Pair)value).first;
          final String configName = (String)((Pair)value).second;
          setIcon(PlatformIcons.ERROR_INTRODUCTION_ICON);
          setText("<html><font color='red'>" + getPresentableText(moduleName, configName, mySingleModuleProject) + "</font></html>");
        }
        else {
          assert value instanceof FlexIdeBuildConfiguration : value;
          final FlexIdeBuildConfiguration config = (FlexIdeBuildConfiguration)value;
          setIcon(config.getIcon());
          setText(getPresentableText(myBCToModuleMap.get(config).getName(), config.NAME, mySingleModuleProject));
        }
      }
    });

    myBCsCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        // remove invalid entry
        final Object selectedItem = myBCsCombo.getSelectedItem();
        final Object firstItem = myBCsCombo.getItemAt(0);
        if (selectedItem instanceof FlexIdeBuildConfiguration && !(firstItem instanceof FlexIdeBuildConfiguration)) {
          myBCsCombo.setModel(new DefaultComboBoxModel(myAllConfigs));
          myBCsCombo.setSelectedItem(selectedItem);
        }

        updateControls();
      }
    });
  }

  private void initRadioButtons() {
    myBCOutputRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });
    
    myURLRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        IdeFocusManager.getInstance(myProject).requestFocus(myURLTextField, true);
      }
    });
  }

  private void initLaunchWithTextWithBrowse() {
    myLauncherParametersTextWithBrowse.getTextField().setEditable(false);
    myLauncherParametersTextWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final FlexLauncherDialog dialog = new FlexLauncherDialog(myProject, myLauncherParameters);
        dialog.show();
        if (dialog.isOK()) {
          myLauncherParameters = dialog.getLauncherParameters();
          updateControls();
        }
      }
    });
  }

  private void updateControls() {
    final Object item = myBCsCombo.getSelectedItem();
    final FlexIdeBuildConfiguration config = item instanceof FlexIdeBuildConfiguration ? (FlexIdeBuildConfiguration)item : null;

    final boolean web = config != null && config.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Web;
    final boolean desktop = config != null && config.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Desktop;
    
    myLaunchPanel.setVisible(web);
    myWebOptionsPanel.setVisible(web);
    myDesktopOptionsPanel.setVisible(desktop);

    if (web) {
      String bcOutput = config.OUTPUT_FILE_NAME;
      if (!bcOutput.isEmpty() && config.USE_HTML_WRAPPER) {
        bcOutput += " via HTML wrapper";
      }
      myBCOutputLabel.setText(bcOutput);

      myURLTextField.setEnabled(myURLRadioButton.isSelected());

      myLauncherParametersTextWithBrowse.getTextField().setText(myLauncherParameters.getPresentableText());
      myRunTrustedCheckBox.setEnabled(!myURLRadioButton.isSelected());
    }

  }

  private static String getPresentableText(String moduleName, String configName, final boolean singleModuleProject) {
    moduleName = moduleName.isEmpty() ? "[no module]" : moduleName;
    configName = configName.isEmpty() ? "[no configuration]" : configName;
    return singleModuleProject ? configName : configName + " (" + moduleName + ")";
  }

  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  protected void resetEditorFrom(final FlexIdeRunConfiguration configuration) {
    final FlexIdeRunnerParameters params = configuration.getRunnerParameters();
    myLauncherParameters = params.getLauncherParameters().clone(); // must be before myBCsCombo.setModel()

    final Module module = ModuleManager.getInstance(myProject).findModuleByName(params.getModuleName());
    final FlexIdeBuildConfiguration config =
      module != null && (ModuleType.get(module) instanceof FlexModuleType)
      ? FlexIdeBuildConfigurationManager.getInstance(module).findConfigurationByName(params.getBCName())
      : null;

    if (config == null) {
      final Object[] model = new Object[myAllConfigs.length + 1];
      model[0] = Pair.create(params.getModuleName(), params.getBCName());
      System.arraycopy(myAllConfigs, 0, model, 1, myAllConfigs.length);
      myBCsCombo.setModel(new DefaultComboBoxModel(model));
      myBCsCombo.setSelectedIndex(0);
    }
    else {
      myBCsCombo.setModel(new DefaultComboBoxModel(myAllConfigs));
      myBCsCombo.setSelectedItem(config);
    }

    myBCOutputRadioButton.setSelected(!params.isLaunchUrl());
    myURLRadioButton.setSelected(params.isLaunchUrl());
    myURLTextField.setText(params.getUrl());

    myRunTrustedCheckBox.setSelected(params.isRunTrusted());

    myAdlOptionsEditor.setText(params.getAdlOptions());
    myAirProgramParametersEditor.setText(params.getAirProgramParameters());

    updateControls();
  }

  protected void applyEditorTo(final FlexIdeRunConfiguration configuration) throws ConfigurationException {
    final FlexIdeRunnerParameters params = configuration.getRunnerParameters();

    final Object selectedItem = myBCsCombo.getSelectedItem();

    if (selectedItem instanceof Pair) {
      params.setModuleName((String)((Pair)selectedItem).first);
      params.setBCName((String)((Pair)selectedItem).second);
    }
    else {
      assert selectedItem instanceof FlexIdeBuildConfiguration : selectedItem;
      params.setModuleName(myBCToModuleMap.get(((FlexIdeBuildConfiguration)selectedItem)).getName());
      params.setBCName(((FlexIdeBuildConfiguration)selectedItem).NAME);
    }

    params.setLaunchUrl(myURLRadioButton.isSelected());
    params.setUrl(myURLTextField.getText().trim());

    params.setLauncherParameters(myLauncherParameters);
    params.setRunTrusted(myRunTrustedCheckBox.isSelected());

    params.setAdlOptions(myAdlOptionsEditor.getText().trim());
    params.setAirProgramParameters(myAirProgramParametersEditor.getText().trim());
  }

  protected void disposeEditor() {
    myAllConfigs = null;
    myBCToModuleMap = null;
  }
}
