package com.jetbrains.lang.dart.ide.runner.test.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartCommandLineConfigurationEditorForm;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunnerParameters;
import com.jetbrains.lang.dart.ide.runner.util.Scope;
import com.jetbrains.lang.dart.ide.runner.util.TestModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DartTestConfigurationEditorForm extends SettingsEditor<DartTestRunConfiguration> {

  private JPanel myMainPanel;
  private JComboBox myScopeCombo;
  private JLabel myTestFileLabel;
  private TextFieldWithBrowseButton myFileField;
  private JLabel myTestNameLabel;
  private JTextField myTestNameField;
  private RawCommandLineEditor myVMOptions;
  private JBCheckBox myCheckedModeCheckBox;
  private RawCommandLineEditor myArguments;
  private TextFieldWithBrowseButton myWorkingDirectory;
  private EnvironmentVariablesComponent myEnvironmentVariables;
  private JCheckBox myVMCheckBox;
  private JCheckBox myDartiumCheckBox;
  private JCheckBox myChromeCheckBox;
  private JCheckBox myFirefoxCheckBox;

  private final Project myProject;
  private TestModel myCachedModel;

  public DartTestConfigurationEditorForm(@NotNull final Project project) {
    myProject = project;

    DartCommandLineConfigurationEditorForm.initDartFileTextWithBrowse(project, myFileField);

    myWorkingDirectory.addBrowseFolderListener(ExecutionBundle.message("select.working.directory.message"), null, project,
                                               FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myScopeCombo.setModel(new EnumComboBoxModel<Scope>(Scope.class));
    myScopeCombo.setRenderer(new ListCellRendererWrapper<Scope>() {
      @Override
      public void customize(final JList list, final Scope value, final int index, final boolean selected, final boolean hasFocus) {
        setText(value.getPresentableName());
      }
    });

    myScopeCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onScopeChanged();
        onTestNameChanged(); // Scope changes can invalidate test label
      }
    });

    final DocumentAdapter documentListener = new DocumentAdapter() {
      @Override
      protected void textChanged(final DocumentEvent e) {
        onTestNameChanged();
      }
    };

    myFileField.getTextField().getDocument().addDocumentListener(documentListener);
    myTestNameField.getDocument().addDocumentListener(documentListener);

    myVMOptions.setDialogCaption(DartBundle.message("config.vmoptions.caption"));
    myArguments.setDialogCaption(DartBundle.message("config.progargs.caption"));

    // 'Environment variables' is the widest label, anchored by myTestFileLabel
    myTestFileLabel.setPreferredSize(myEnvironmentVariables.getLabel().getPreferredSize());
    myEnvironmentVariables.setAnchor(myTestFileLabel);
  }

  @Override
  protected void resetEditorFrom(DartTestRunConfiguration configuration) {
    final DartTestRunnerParameters parameters = configuration.getRunnerParameters();

    myScopeCombo.setSelectedItem(parameters.getScope());
    myFileField.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getFilePath())));
    myTestNameField.setText(parameters.getScope() == Scope.ALL ? "" : StringUtil.notNullize(parameters.getTestName()));
    myArguments.setText(StringUtil.notNullize(parameters.getArguments()));
    myVMOptions.setText(StringUtil.notNullize(parameters.getVMOptions()));
    myCheckedModeCheckBox.setSelected(parameters.isCheckedMode());
    myWorkingDirectory.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getWorkingDirectory())));
    myEnvironmentVariables.setEnvs(parameters.getEnvs());
    myEnvironmentVariables.setPassParentEnvs(parameters.isIncludeParentEnvs());

    onScopeChanged();
  }

  @Override
  protected void applyEditorTo(DartTestRunConfiguration configuration) throws ConfigurationException {
    final DartTestRunnerParameters parameters = configuration.getRunnerParameters();

    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    parameters.setScope(scope);
    parameters.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(myFileField.getText().trim()), true));
    parameters.setTestName(scope == Scope.ALL ? null : StringUtil.nullize(myTestNameField.getText().trim()));
    parameters.setArguments(StringUtil.nullize(myArguments.getText().trim(), true));
    parameters.setVMOptions(StringUtil.nullize(myVMOptions.getText().trim(), true));
    parameters.setCheckedMode(myCheckedModeCheckBox.isSelected());
    parameters.setWorkingDirectory(StringUtil.nullize(FileUtil.toSystemIndependentName(myWorkingDirectory.getText().trim()), true));
    parameters.setEnvs(myEnvironmentVariables.getEnvs());
    parameters.setIncludeParentEnvs(myEnvironmentVariables.isPassParentEnvs());
  }

  private void onScopeChanged() {
    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    myTestNameLabel.setVisible(scope == Scope.GROUP || scope == Scope.METHOD);
    myTestNameField.setVisible(scope == Scope.GROUP || scope == Scope.METHOD);
    myTestNameLabel.setText(scope == Scope.GROUP ? DartBundle.message("dart.unit.group.name") : DartBundle.message("dart.unit.test.name"));
  }

  private void onTestNameChanged() {
    final String filePath = FileUtil.toSystemIndependentName(myFileField.getText().trim());
    if (filePath.isEmpty()) {
      return;
    }

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (file == null || file.isDirectory()) {
      return;
    }

    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    if (scope != Scope.METHOD && scope != Scope.GROUP) {
      return;
    }

    final String testLabel = myTestNameField.getText().trim();

    if (myCachedModel == null || !myCachedModel.appliesTo(file)) {
      myCachedModel = new TestModel(myProject, file);
    }

    if (!myCachedModel.includes(scope, testLabel)) {
      myTestNameField.setForeground(JBColor.RED);
      final String message = scope == Scope.METHOD
                             ? DartBundle.message("test.label.not.found", testLabel)
                             : DartBundle.message("test.group.not.found", testLabel);
      myTestNameField.setToolTipText(message);
    }
    else {
      myTestNameField.setForeground(UIUtil.getFieldForegroundColor());
      myTestNameField.setToolTipText(null);
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }
}
