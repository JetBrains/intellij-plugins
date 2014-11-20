package com.jetbrains.lang.dart.ide.runner.unittest.ui;

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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartCommandLineConfigurationEditorForm;
import com.jetbrains.lang.dart.ide.runner.unittest.DartTestLocationProvider;
import com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunConfigurationProducer;
import com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunnerParameters;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import static com.jetbrains.lang.dart.ide.runner.unittest.DartUnitRunnerParameters.Scope;

@SuppressWarnings("Since15")
public class DartUnitConfigurationEditorForm extends SettingsEditor<DartUnitRunConfiguration> {
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
  private final Color myStandardForegroundColor;

  public DartUnitConfigurationEditorForm(final Project project) {
    DartCommandLineConfigurationEditorForm.initDartFileTextWithBrowse(project, myFileField);

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
        onTestNameChanged(project); // Scope changes can invalidate test label
      }
    });

    myTestNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(final DocumentEvent e) {
        onTestNameChanged(project);
      }
    });

    myStandardForegroundColor = myTestNameField.getForeground();

    myVMOptions.setDialogCaption(DartBundle.message("config.vmoptions.caption"));
    myArguments.setDialogCaption(DartBundle.message("config.progargs.caption"));

    // 'Environment variables' is the widest label, anchored by myTestFileLabel
    myTestFileLabel.setPreferredSize(myEnvironmentVariables.getLabel().getPreferredSize());
    myEnvironmentVariables.setAnchor(myTestFileLabel);
  }

  @Override
  protected void resetEditorFrom(DartUnitRunConfiguration configuration) {
    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();

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
  protected void applyEditorTo(DartUnitRunConfiguration configuration) throws ConfigurationException {
    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();

    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    parameters.setScope(scope);
    parameters.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(myFileField.getText().trim()), true));
    parameters.setTestName(scope == Scope.ALL ? null : StringUtil.nullize(myTestNameField.getText()));
    parameters.setArguments(StringUtil.nullize(myArguments.getText(), true));
    parameters.setVMOptions(StringUtil.nullize(myVMOptions.getText(), true));
    parameters.setCheckedMode(myCheckedModeCheckBox.isSelected());
    parameters.setWorkingDirectory(StringUtil.nullize(FileUtil.toSystemIndependentName(myWorkingDirectory.getText().trim()), true));
    parameters.setEnvs(myEnvironmentVariables.getEnvs());
    parameters.setIncludeParentEnvs(myEnvironmentVariables.isPassParentEnvs());
  }

  private void onScopeChanged() {
    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    myTestNameLabel.setVisible(scope == Scope.GROUP || scope == Scope.METHOD);
    myTestNameField.setVisible(scope == Scope.GROUP || scope == Scope.METHOD);
    myTestNameLabel.setText(scope == Scope.GROUP
                            ? DartBundle.message("dart.unit.group.name")
                            : DartBundle.message("dart.unit.method.name"));
  }

  private void onTestNameChanged(final Project project) {
    final Scope scope = (Scope)myScopeCombo.getSelectedItem();
    final String testLabel = myTestNameField.getText();
    final String filePath = myFileField.getText();
    if (filePath != null) {
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
      if (file != null) {
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {

          PsiElementProcessor.CollectElements<PsiElement> collector = new PsiElementProcessor.CollectElements<PsiElement>() {
            @Override
            public boolean execute(@NotNull final PsiElement element) {

              if (element instanceof DartCallExpression) {
                DartCallExpression expression = (DartCallExpression)element;
                if (scope == Scope.METHOD && DartUnitRunConfigurationProducer.isTest(expression) ||
                    DartUnitRunConfigurationProducer.isGroup(expression)) {
                  if (DartTestLocationProvider.testLabelMatches(expression, testLabel)) {
                    // Collect the match
                    super.execute(expression);
                  }
                }
              }
              return getCollection().isEmpty();
            }
          };

          PsiTreeUtil.processElements(psiFile, collector);

          if (collector.getCollection().isEmpty()) {
            myTestNameField.setForeground(JBColor.RED);
            myTestNameField.setToolTipText(DartBundle.message("config.test.unfound.label.warning", testLabel));
          } else {
            myTestNameField.setForeground(myStandardForegroundColor);
            myTestNameField.setToolTipText(null);
          }
        }
      }
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }
}
