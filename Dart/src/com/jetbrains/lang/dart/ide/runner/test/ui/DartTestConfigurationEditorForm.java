package com.jetbrains.lang.dart.ide.runner.test.ui;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.JBColor;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartCommandLineConfigurationEditorForm;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunnerParameters;
import com.jetbrains.lang.dart.ide.runner.util.Scope;
import com.jetbrains.lang.dart.ide.runner.util.TestModel;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
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
  private JLabel myDirLabel;
  private TextFieldWithBrowseButton myDirField;
  private JLabel myTestNameLabel;
  private JTextField myTestNameField;
  private JLabel myTargetNameLabel;
  private JTextField myTargetNameField;
  private JTextField myTestRunnerOptionsField;
  private EnvironmentVariablesComponent myEnvironmentVariables;

  private final Project myProject;
  private TestModel myCachedModel;

  public DartTestConfigurationEditorForm(@NotNull final Project project) {
    myProject = project;

    DartCommandLineConfigurationEditorForm.initDartFileTextWithBrowse(project, myFileField);
    myDirField.addBrowseFolderListener(DartBundle.message("choose.dart.directory"), null, project,
                                       // Unfortunately, withFileFilter() only works for files, not directories.
                                       FileChooserDescriptorFactory.createSingleFolderDescriptor());
    myDirField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onTestDirChanged(project);
      }
    });

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
    final DocumentAdapter dirListener = new DocumentAdapter() {
      @Override
      protected void textChanged(final DocumentEvent e) {
        onTestDirChanged(project);
      }
    };

    myFileField.getTextField().getDocument().addDocumentListener(documentListener);
    myDirField.getTextField().getDocument().addDocumentListener(dirListener);
    myTestNameField.getDocument().addDocumentListener(documentListener);

    // 'Environment variables' is the widest label, anchored by myTestFileLabel
    myTestFileLabel.setPreferredSize(myEnvironmentVariables.getLabel().getPreferredSize());
    myDirLabel.setPreferredSize(myEnvironmentVariables.getLabel().getPreferredSize());
    myEnvironmentVariables.setAnchor(myTestFileLabel);
  }

  @Override
  protected void resetEditorFrom(DartTestRunConfiguration configuration) {
    final DartTestRunnerParameters parameters = configuration.getRunnerParameters();

    myScopeCombo.setSelectedItem(parameters.getScope());
    String testPath = FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getFilePath()));
    if (parameters.getScope() == Scope.FOLDER) {
      myDirField.setText(testPath);
      myTargetNameField.setText(parameters.getTargetName());
    }
    else {
      myFileField.setText(testPath);
    }
    myTestNameField.setText(parameters.getScope().expectsTestName() ? StringUtil.notNullize(parameters.getTestName()) : "");
    myTestRunnerOptionsField.setText(parameters.getTestRunnerOptions());
    myEnvironmentVariables.setEnvs(parameters.getEnvs());
    myEnvironmentVariables.setPassParentEnvs(parameters.isIncludeParentEnvs());

    onScopeChanged();
  }

  @Override
  protected void applyEditorTo(DartTestRunConfiguration configuration) throws ConfigurationException {
    final DartTestRunnerParameters parameters = configuration.getRunnerParameters();

    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    parameters.setScope(scope);
    TextFieldWithBrowseButton pathSource = scope == Scope.FOLDER ? myDirField : myFileField;
    parameters.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(pathSource.getText().trim())));
    parameters.setTestName(scope.expectsTestName() ? StringUtil.nullize(myTestNameField.getText().trim()) : null);
    parameters.setTargetName(scope == Scope.FOLDER ? StringUtil.nullize(myTargetNameField.getText().trim()) : null);
    parameters.setTestRunnerOptions(StringUtil.nullize(myTestRunnerOptionsField.getText().trim()));
    parameters.setEnvs(myEnvironmentVariables.getEnvs());
    parameters.setIncludeParentEnvs(myEnvironmentVariables.isPassParentEnvs());
  }

  private void onScopeChanged() {
    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    myTestNameLabel.setVisible(scope == Scope.GROUP || scope == Scope.METHOD);
    myTestNameField.setVisible(scope == Scope.GROUP || scope == Scope.METHOD);
    myTestNameLabel.setText(scope == Scope.GROUP ? DartBundle.message("dart.unit.group.name") : DartBundle.message("dart.unit.test.name"));
    boolean on = scope == Scope.FOLDER;
    boolean projectWithoutPubspec = Registry.is("dart.projects.without.pubspec", false);
    myFileField.setVisible(!on);
    myTestFileLabel.setVisible(!on);
    myDirField.setVisible(on);
    myDirLabel.setVisible(on);
    myTargetNameField.setVisible(on && projectWithoutPubspec);
    myTargetNameLabel.setVisible(on && projectWithoutPubspec);
  }

  private void onTestNameChanged() {
    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    if (scope == Scope.FOLDER) return;

    final String filePath = FileUtil.toSystemIndependentName(myFileField.getText().trim());
    if (filePath.isEmpty()) {
      return;
    }

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (file == null || file.isDirectory()) {
      return;
    }

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

  private void onTestDirChanged(Project project) {
    if (!isDirApplicable(myDirField.getText(), project)) {
      myDirField.getTextField().setForeground(JBColor.RED);
      final String message = DartBundle.message("test.dir.not.in.project");
      myDirField.getTextField().setToolTipText(message);
    }
    else {
      myDirField.getTextField().setForeground(UIUtil.getFieldForegroundColor());
      myDirField.getTextField().setToolTipText(null);
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }

  private static boolean isDirApplicable(@NotNull final String path, @NotNull final Project project) {
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
    return file != null && file.isDirectory() && PubspecYamlUtil.findPubspecYamlFile(project, file) != null;
  }
}
