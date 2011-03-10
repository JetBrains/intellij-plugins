package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.ModulesComboboxWrapper;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.build.FlexCompilerSettingsEditor;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

public class AirRunConfigurationForm extends SettingsEditor<AirRunConfiguration> {

  private final Project myProject;

  private JPanel myMainPanel;
  private JComboBox myModuleComboBox;
  private JRadioButton myAirDescriptorRadioButton;
  private JRadioButton myMainClassRadioButton;
  private LabeledComponent<JSReferenceEditor> myMainClassComponent;
  private LabeledComponent<ComboboxWithBrowseButton> myApplicationDescriptorComponent;
  private LabeledComponent<TextFieldWithBrowseButton> myRootDirectoryComponent;
  private LabeledComponent<RawCommandLineEditor> myAdlOptionsComponent;
  private LabeledComponent<RawCommandLineEditor> myProgramParametersComponent;
  private FlexSdkComboBoxWithBrowseButton myDebuggerSdkCombo;
  private JSClassChooserDialog.PublicInheritor myMainClassFilter;

  private ModulesComboboxWrapper myModulesComboboxWrapper;

  public AirRunConfigurationForm(final Project project) {
    myProject = project;
    myModulesComboboxWrapper = new ModulesComboboxWrapper(myModuleComboBox);

    myModuleComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetAirRootDirPath();
        final Module module = myModulesComboboxWrapper.getSelectedModule();
        myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module));
        updateMainClassField();
      }
    });

    initRadioButtons();
    initAppDescriptorComponent();

    myRootDirectoryComponent.getComponent().addBrowseFolderListener("AIR Application Root Directory", null, project,
                                                                    new FileChooserDescriptor(false, true, false, false, false, false));

    myAdlOptionsComponent.getComponent()
      .setDialogCaption(StringUtil.capitalizeWords(myAdlOptionsComponent.getRawText(), true));
    myProgramParametersComponent.getComponent()
      .setDialogCaption(StringUtil.capitalizeWords(myProgramParametersComponent.getRawText(), true));

    myDebuggerSdkCombo.showModuleSdk(true);

    updateControls();
    updateMainClassField();
  }

  private void updateMainClassField() {
    try {
      Module module = FlexRunConfiguration.getAndValidateModule(myProject, myModulesComboboxWrapper.getSelectedText());
      myMainClassComponent.getComponent().setScope(GlobalSearchScope.moduleScope(module));
      myMainClassFilter.setModule(module);
      myMainClassComponent.getComponent().setChooserBlockingMessage(null);
    }
    catch (RuntimeConfigurationError error) {
      myMainClassComponent.getComponent().setScope(GlobalSearchScope.EMPTY_SCOPE);
      myMainClassFilter.setModule(null);
      myMainClassComponent.getComponent().setChooserBlockingMessage(error.getMessage());
    }
  }

  private void initRadioButtons() {

    myAirDescriptorRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        IdeFocusManager.getInstance(myProject).requestFocus(myApplicationDescriptorComponent.getComponent().getComboBox(), true);
      }
    });
    myMainClassRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        IdeFocusManager.getInstance(myProject).requestFocus(myMainClassComponent.getComponent().getChildComponent(), true);
        updateControls();
      }
    });
  }

  private void initAppDescriptorComponent() {
    myApplicationDescriptorComponent.getComponent()
      .addBrowseFolderListener(myProject, new FileChooserDescriptor(true, false, false, false, false, false) {
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
          return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || "xml".equalsIgnoreCase(file.getExtension()));
        }
      });

    final JComboBox comboBox = myApplicationDescriptorComponent.getComponent().getComboBox();
    comboBox.setEditable(true);

    final String[] descriptorPaths = FlexUtils.collectAirDescriptorsForProject(myProject);
    comboBox.setModel(new DefaultComboBoxModel(descriptorPaths));

    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (myRootDirectoryComponent.getComponent().getText().length() == 0) {
          resetAirRootDirPath();
        }
      }
    });
  }

  private void resetAirRootDirPath() {
    final Module module = myModulesComboboxWrapper.getSelectedModule();
    if (module != null) {
      final Collection<FlexBuildConfiguration> configs = FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module);
      if (!configs.isEmpty()) {
        myRootDirectoryComponent.getComponent().setText(FileUtil.toSystemDependentName(configs.iterator().next().getCompileOutputPath()));
      }
    }
  }

  private void updateControls() {
    myApplicationDescriptorComponent.setVisible(myAirDescriptorRadioButton.isSelected());
    myRootDirectoryComponent.setVisible(myAirDescriptorRadioButton.isSelected());
    myMainClassComponent.setVisible(myMainClassRadioButton.isSelected());
  }

  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  protected void resetEditorFrom(final AirRunConfiguration airRunConfiguration) {
    final AirRunnerParameters runnerParameters = airRunConfiguration.getRunnerParameters();

    myModulesComboboxWrapper.configure(myProject, runnerParameters.getModuleName());
    myAirDescriptorRadioButton.setSelected(runnerParameters.getAirRunMode() == AirRunnerParameters.AirRunMode.AppDescriptor);
    myMainClassRadioButton.setSelected(runnerParameters.getAirRunMode() == AirRunnerParameters.AirRunMode.MainClass);
    myApplicationDescriptorComponent.getComponent().getComboBox().getEditor()
      .setItem(FileUtil.toSystemDependentName(runnerParameters.getAirDescriptorPath()));
    myRootDirectoryComponent.getComponent().setText(FileUtil.toSystemDependentName(runnerParameters.getAirRootDirPath()));
    myMainClassComponent.getComponent().setText(runnerParameters.getMainClassName());
    myAdlOptionsComponent.getComponent().setText(runnerParameters.getAdlOptions());
    myAdlOptionsComponent.getComponent().setText(runnerParameters.getAdlOptions());
    myProgramParametersComponent.getComponent().setText(runnerParameters.getAirProgramParameters());

    final Module module = myModulesComboboxWrapper.getSelectedModule();
    myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module));
    myDebuggerSdkCombo.setSelectedSdkRaw(runnerParameters.getDebuggerSdkRaw());

    updateControls();
  }

  protected void applyEditorTo(final AirRunConfiguration airRunConfiguration) throws ConfigurationException {
    final AirRunnerParameters params = airRunConfiguration.getRunnerParameters();

    params.setModuleName(myModulesComboboxWrapper.getSelectedText());
    final AirRunnerParameters.AirRunMode airRunMode =
      myAirDescriptorRadioButton.isSelected() ? AirRunnerParameters.AirRunMode.AppDescriptor : AirRunnerParameters.AirRunMode.MainClass;
    params.setAirRunMode(airRunMode);
    params.setAirDescriptorPath(
      FileUtil.toSystemIndependentName((String)myApplicationDescriptorComponent.getComponent().getComboBox().getEditor().getItem()).trim());
    params.setAirRootDirPath(FileUtil.toSystemIndependentName(myRootDirectoryComponent.getComponent().getText().trim()));
    params.setMainClassName(myMainClassComponent.getComponent().getText().trim());
    params.setAdlOptions(myAdlOptionsComponent.getComponent().getText().trim());
    params.setAirProgramParameters(myProgramParametersComponent.getComponent().getText());
    params.setDebuggerSdkRaw(myDebuggerSdkCombo.getSelectedSdkRaw());
  }

  protected void disposeEditor() {
  }

  private void createUIComponents() {
    myDebuggerSdkCombo = new FlexSdkComboBoxWithBrowseButton(FlexSdkComboBoxWithBrowseButton.FLEX_RELATED_SDK);
    myMainClassFilter = new JSClassChooserDialog.PublicInheritor(myProject, FlexCompilerSettingsEditor.SPRITE_CLASS_NAME, null, true);
    myMainClassComponent = LabeledComponent.create(JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE, null,
                                                                                  myMainClassFilter, ExecutionBundle
      .message("choose.main.class.dialog.title")), "");
  }
}
