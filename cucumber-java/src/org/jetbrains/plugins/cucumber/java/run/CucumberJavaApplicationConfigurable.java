package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.application.options.ModuleDescriptionsComboBox;
import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.execution.ui.*;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
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

public class CucumberJavaApplicationConfigurable extends SettingsEditor<CucumberJavaRunConfiguration> implements PanelWithAnchor {
  private final Project myProject;
  private JComponent myAnchor;
  private final ConfigurationModuleSelector myModuleSelector;

  private LabeledComponent<EditorTextFieldWithBrowseButton> myMainClass;
  private JPanel myWholePanel;
  private LabeledComponent<ModuleDescriptionsComboBox> myModule;
  private LabeledComponent<RawCommandLineEditor> myGlue;
  private LabeledComponent<TextFieldWithBrowseButton> myFeatureOrFolder;
  private CommonJavaParametersPanel myCommonProgramParameters;
  private LabeledComponent<ShortenCommandLineModeCombo> myShortenClasspathModeCombo;
  private JrePathEditor myJrePathEditor;

  private final Module myModuleContext;

  public CucumberJavaApplicationConfigurable(Project project) {
    myProject = project;
    ModuleDescriptionsComboBox moduleComponent = myModule.getComponent();
    myModuleSelector = new ConfigurationModuleSelector(project, moduleComponent);
    myJrePathEditor.setDefaultJreSelector(DefaultJreSelector.fromModuleDependencies(moduleComponent, false));

    ClassBrowser.createApplicationClassBrowser(project, myModuleSelector).setField(myMainClass.getComponent());
    myModuleContext = myModuleSelector.getModule();


    final ActionListener fileToRunActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor();
        fileChooserDescriptor.setTitle(CucumberJavaBundle.message("run.configuration.form.choose.file.or.folder.title"));
        fileChooserDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, myModuleContext);
        VirtualFile file = FileChooser.chooseFile(fileChooserDescriptor, myProject, null);
        if (file != null) {
          setFeatureOrFolder(file.getPresentableUrl());
        }
      }
    };
    myFeatureOrFolder.getComponent().getButton().addActionListener(fileToRunActionListener);

    myAnchor = UIUtil.mergeComponentsWithAnchor(myMainClass, myGlue, myFeatureOrFolder, myModule, myCommonProgramParameters);

    myJrePathEditor.setAnchor(myModule.getLabel());
    myShortenClasspathModeCombo.setAnchor(myModule.getLabel());
    myShortenClasspathModeCombo.setComponent(new ShortenCommandLineModeCombo(myProject, myJrePathEditor, moduleComponent));

    myGlue.getComponent().setDialogCaption(CucumberJavaBundle.message("run.configuration.form.glue.title"));
  }

  public void setFeatureOrFolder(String path) {
    myFeatureOrFolder.getComponent().setText(path);
  }

  private void createUIComponents() {
    myMainClass = new LabeledComponent<>();
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
    myFeatureOrFolder.setAnchor(anchor);
    myModule.setAnchor(anchor);
    myCommonProgramParameters.setAnchor(anchor);
  }

  @Override
  protected void resetEditorFrom(@NotNull CucumberJavaRunConfiguration configuration) {
    myModuleSelector.reset(configuration);
    myCommonProgramParameters.reset(configuration);

    myMainClass.getComponent().setText(configuration.getMainClassName());
    myGlue.getComponent().setText(configuration.getGlue());
    myFeatureOrFolder.getComponent().setText(configuration.getFilePath());
    myJrePathEditor.setPathOrName(configuration.getAlternativeJrePath(), configuration.isAlternativeJrePathEnabled());
    myShortenClasspathModeCombo.getComponent().setSelectedItem(configuration.getShortenCommandLine());
  }

  @Override
  protected void applyEditorTo(@NotNull CucumberJavaRunConfiguration configuration) {
    myCommonProgramParameters.applyTo(configuration);
    myModuleSelector.applyTo(configuration);

    configuration.setMainClassName(myMainClass.getComponent().getText());
    configuration.setGlue(myGlue.getComponent().getText());
    configuration.setFilePath(myFeatureOrFolder.getComponent().getText());
    configuration.setAlternativeJrePath(myJrePathEditor.getJrePathOrName());
    configuration.setAlternativeJrePathEnabled(myJrePathEditor.isAlternativeJreSelected());
    configuration.setShortenCommandLine(myShortenClasspathModeCombo.getComponent().getSelectedItem());
    configuration.setModule(myModuleSelector.getModule());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myWholePanel;
  }
}
