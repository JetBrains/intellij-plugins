package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.execution.ui.ClassBrowser;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaCodeFragment;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.ui.EditorTextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: Andrey.Vokin
 * Date: 10/23/12
 */
public class CucumberJavaApplicationConfigurable extends SettingsEditor<CucumberJavaRunConfiguration> implements PanelWithAnchor {
  private final Project myProject;
  private JComponent myAnchor;
  private final ConfigurationModuleSelector myModuleSelector;

  private LabeledComponent<EditorTextFieldWithBrowseButton> myMainClass;
  private JPanel myWholePanel;
  private LabeledComponent<JComboBox> myModule;
  private LabeledComponent<JTextField> myGlue;
  private LabeledComponent<RawCommandLineEditor> myVmOptions;
  private LabeledComponent<RawCommandLineEditor> myProgramArguments;
  private LabeledComponent<TextFieldWithBrowseButton> myWorkingDirectory;

  private Module myModuleContext;

  public CucumberJavaApplicationConfigurable(Project project) {
    myProject = project;
    myModuleSelector = new ConfigurationModuleSelector(project, myModule.getComponent());

    ClassBrowser.createApplicationClassBrowser(project, myModuleSelector).setField(myMainClass.getComponent());
    myModuleContext = myModuleSelector.getModule();

    ActionListener workingDirectoryActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        fileChooserDescriptor.setTitle(ExecutionBundle.message("select.working.directory.message"));
        fileChooserDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, myModuleContext);
        Project project = myProject;
        VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, myWorkingDirectory, project, null);
        if (file != null) {
          setWorkingDirectory(file.getPresentableUrl());
        }
      }
    };
    myWorkingDirectory.getComponent().getButton().addActionListener(workingDirectoryActionListener);

    myModule.getComponent().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {

      }
    });

    myAnchor = UIUtil.mergeComponentsWithAnchor(myMainClass, myGlue, myModule, myVmOptions, myProgramArguments, myWorkingDirectory);

    myProgramArguments.getComponent().setDialogCaption(CucumberJavaBundle.message("run.configuration.form.program.parameters.title"));
    myVmOptions.getComponent().setDialogCaption(CucumberJavaBundle.message("run.configuration.form.vm.options.title"));
  }

  public void setWorkingDirectory(String dir) {
    myWorkingDirectory.getComponent().setText(dir);
  }

  private void createUIComponents() {
    myMainClass = new LabeledComponent<EditorTextFieldWithBrowseButton>();
    myMainClass.setComponent(new EditorTextFieldWithBrowseButton(myProject, true, new JavaCodeFragment.VisibilityChecker() {
      @Override
      public Visibility isDeclarationVisible(PsiElement declaration, PsiElement place) {
        if (declaration instanceof PsiClass) {
          final PsiClass aClass = (PsiClass)declaration;
          if (ConfigurationUtil.MAIN_CLASS.value(aClass) && PsiMethodUtil.findMainMethod(aClass) != null) {
            return Visibility.VISIBLE;
          }
        }
        return Visibility.NOT_VISIBLE;
      }
    }));
  }

  @Override
  public JComponent getAnchor() {
    return myAnchor;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    myAnchor = anchor;
    myMainClass.setAnchor(anchor);
    myGlue.setAnchor(anchor);
    myModule.setAnchor(anchor);
    myVmOptions.setAnchor(anchor);
    myProgramArguments.setAnchor(anchor);
    myWorkingDirectory.setAnchor(anchor);
  }

  @Override
  protected void resetEditorFrom(CucumberJavaRunConfiguration configuration) {
    myModuleSelector.reset(configuration);
    myMainClass.getComponent().setText(configuration.MAIN_CLASS_NAME);
    myVmOptions.getComponent().setText(configuration.getVMParameters());
    myProgramArguments.getComponent().setText(configuration.getProgramParameters());
    setWorkingDirectory(configuration.getWorkingDirectory());

    myWorkingDirectory.getComponent().setText(configuration.getWorkingDirectory());
    myGlue.getComponent().setText(configuration.getGlue());
  }

  @Override
  protected void applyEditorTo(CucumberJavaRunConfiguration configuration) throws ConfigurationException {
    myModuleSelector.applyTo(configuration);

    configuration.MAIN_CLASS_NAME = myMainClass.getComponent().getText();
    configuration.setVMParameters(myVmOptions.getComponent().getText());
    configuration.setProgramParameters(myProgramArguments.getComponent().getText());
    configuration.setWorkingDirectory(myWorkingDirectory.getComponent().getText());
    configuration.setGlue(myGlue.getComponent().getText());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myWholePanel;
  }

  @Override
  protected void disposeEditor() {
  }
}
