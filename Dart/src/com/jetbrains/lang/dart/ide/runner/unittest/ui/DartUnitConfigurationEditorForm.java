package com.jetbrains.lang.dart.ide.runner.unittest.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserFactory;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.RawCommandLineEditor;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunnerParameters;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import static com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunnerParameters.Scope;

public class DartUnitConfigurationEditorForm extends SettingsEditor<DartUnitRunConfiguration> {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton myFileField;
  private RawCommandLineEditor myVMOptions;
  private RawCommandLineEditor myArguments;
  private JComboBox myScopeCombo;
  private JLabel myTestNameLabel;
  private JTextField myTestNameField;
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
          }
        );

        fileChooser.showDialog();

        PsiFile selectedFile = fileChooser.getSelectedFile();
        final VirtualFile virtualFile = selectedFile == null ? null : selectedFile.getVirtualFile();
        if (virtualFile != null) {
          final String path = FileUtil.toSystemDependentName(virtualFile.getPath());
          myFileField.setText(path);
        }
      }
    });

    myWorkingDirectory.addBrowseFolderListener(ExecutionBundle.message("select.working.directory.message"), null, project,
                                               FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myScopeCombo.setModel(new EnumComboBoxModel<Scope>(Scope.class));
    myScopeCombo.setRenderer(new ListCellRendererWrapper<Scope>() {
      @Override
      public void customize(final JList list, final Scope value, final int index, final boolean selected, final boolean hasFocus) {
        setText(StringUtil.capitalize(value.toString().toLowerCase(Locale.US)));
      }
    });

    myScopeCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onScopeChanged();
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
    if (parameters.getScope() != Scope.ALL) {
      myTestNameField.setText(StringUtil.notNullize(parameters.getTestName()));
    }
    else {
      myTestNameField.setText("");
    }
    myScopeCombo.setSelectedItem(parameters.getScope());
    onScopeChanged();
  }

  private void onScopeChanged() {
    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    myTestNameLabel.setVisible(scope == Scope.GROUP || scope == Scope.METHOD);
    myTestNameField.setVisible(scope == Scope.GROUP || scope == Scope.METHOD);
    myTestNameLabel.setText(scope == Scope.GROUP
                            ? DartBundle.message("dart.unit.group.name")
                            : DartBundle.message("dart.unit.method.name"));
  }

  @Override
  protected void applyEditorTo(DartUnitRunConfiguration configuration) throws ConfigurationException {
    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();
    parameters.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(myFileField.getText().trim()), true));
    parameters.setArguments(StringUtil.nullize(myArguments.getText(), true));
    parameters.setVMOptions(StringUtil.nullize(myVMOptions.getText(), true));
    parameters.setWorkingDirectory(StringUtil.nullize(FileUtil.toSystemIndependentName(myWorkingDirectory.getText().trim()), true));
    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    parameters.setScope(scope);
    if (scope != Scope.ALL) {
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
}
