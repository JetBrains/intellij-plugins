package com.jetbrains.lang.dart.ide.runner.unittest.ui;

import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserFactory;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunnerParameters;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author: Fedor.Korotkov
 */
public class DartUnitConfigurationEditorForm extends SettingsEditor<DartUnitRunConfiguration> {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton myFileField;
  private RawCommandLineEditor myVMOptions;
  private RawCommandLineEditor myArguments;
  private JComboBox myScopeCombo;
  private JLabel myTestNameLabel;
  private JTextField myTestNameField;
  private JPanel myTestNamePanel;
  private JPanel myAdditionalPanel;
  private TextFieldWithBrowseButton myWorkingDirectory;

  public DartUnitConfigurationEditorForm(final Project project) {
    myFileField.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TreeFileChooser fileChooser = TreeFileChooserFactory.getInstance(project).createFileChooser(
          DartBundle.message("choose.dart.main.file"),
          null,
          DartFileType.INSTANCE,
          new TreeFileChooser.PsiFileFilter() {
            public boolean accept(PsiFile file) {
              return true;
            }
          });

        fileChooser.showDialog();

        PsiFile selectedFile = fileChooser.getSelectedFile();
        final VirtualFile virtualFile = selectedFile == null ? null : selectedFile.getVirtualFile();
        if (virtualFile != null) {
          final String path = FileUtil.toSystemDependentName(virtualFile.getPath());
          myFileField.setText(path);
        }
      }
    });

    myWorkingDirectory.getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event) {
        final VirtualFile virtualFile =
          FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, null);
        if (virtualFile != null) {
          final String path = FileUtil.toSystemDependentName(virtualFile.getPath());
          myWorkingDirectory.setText(path);
        }
      }
    });

    for (DartUnitRunnerParameters.Scope scope : DartUnitRunnerParameters.Scope.values()) {
      myScopeCombo.addItem(StringUtil.capitalize(scope.toString().toLowerCase()));
    }

    myScopeCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateScope(getScope());
      }
    });

    myVMOptions.setDialogCaption(DartBundle.message("config.vmoptions.caption"));
    myArguments.setDialogCaption(DartBundle.message("config.progargs.caption"));
  }

  @Override
  protected void resetEditorFrom(DartUnitRunConfiguration configuration) {
    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();
    myFileField.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getFilePath())));
    myArguments.setText(StringUtil.notNullize(parameters.getArguments()));
    myVMOptions.setText(StringUtil.notNullize(parameters.getVMOptions()));
    myWorkingDirectory.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getWorkingDirectory())));
    if (parameters.getScope() != DartUnitRunnerParameters.Scope.ALL) {
      myTestNameField.setText(StringUtil.notNullize(parameters.getTestName()));
    }
    else {
      myTestNameField.setText("");
    }
    setScope(parameters.getScope());
  }

  private void setScope(DartUnitRunnerParameters.Scope scope) {
    myScopeCombo.setSelectedItem(StringUtil.capitalize(scope.toString().toLowerCase()));
    updateScope(scope);
  }

  private void updateScope(DartUnitRunnerParameters.Scope scope) {
    boolean contains = false;
    Component[] components = myAdditionalPanel.getComponents();
    for (Component component : components) {
      if (component == myTestNamePanel) {
        contains = true;
        break;
      }
    }
    if (scope == DartUnitRunnerParameters.Scope.ALL && contains) {
      myAdditionalPanel.remove(myTestNamePanel);
    }
    else if (scope != DartUnitRunnerParameters.Scope.ALL && !contains) {
      final GridConstraints constraints = new GridConstraints();
      constraints.setRow(0);
      constraints.setFill(GridConstraints.FILL_HORIZONTAL);
      myAdditionalPanel.add(myTestNamePanel, constraints);

      myTestNameLabel.setText(scope == DartUnitRunnerParameters.Scope.GROUP
                              ? DartBundle.message("dart.unit.group.name")
                              : DartBundle.message("dart.unit.method.name"));
    }
  }

  @Override
  protected void applyEditorTo(DartUnitRunConfiguration configuration) throws ConfigurationException {
    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();
    parameters.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(myFileField.getText().trim()), true));
    parameters.setArguments(StringUtil.nullize(myArguments.getText(), true));
    parameters.setVMOptions(StringUtil.nullize(myVMOptions.getText(), true));
    parameters.setWorkingDirectory(StringUtil.nullize(FileUtil.toSystemIndependentName(myWorkingDirectory.getText().trim()), true));
    parameters.setScope(getScope());
    if (getScope() != DartUnitRunnerParameters.Scope.ALL) {
      parameters.setTestName(StringUtil.nullize(myTestNameField.getText()));
    }
    else {
      parameters.setTestName(null);
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }

  public DartUnitRunnerParameters.Scope getScope() {
    final String selectedScope = myScopeCombo.getSelectedItem().toString();
    return DartUnitRunnerParameters.Scope.valueOf(selectedScope.toUpperCase());
  }
}
