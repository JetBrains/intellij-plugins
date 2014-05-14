package com.jetbrains.lang.dart.ide.runner.server.ui;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserFactory;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.RawCommandLineEditor;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.DartWritingAccessProvider;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DartCommandLineConfigurationEditorForm extends SettingsEditor<DartCommandLineRunConfiguration> {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton myFileField;
  private RawCommandLineEditor myVMOptions;
  private RawCommandLineEditor myArguments;
  private TextFieldWithBrowseButton myWorkingDirectory;
  private EnvironmentVariablesComponent myEnvironmentVariables;

  public DartCommandLineConfigurationEditorForm(final Project project) {
    initDartFileTextWithBrowse(project, myFileField);

    myWorkingDirectory.addBrowseFolderListener(ExecutionBundle.message("select.working.directory.message"), null, project,
                                               FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myVMOptions.setDialogCaption(DartBundle.message("config.vmoptions.caption"));
    myArguments.setDialogCaption(DartBundle.message("config.progargs.caption"));
  }

  public static void initDartFileTextWithBrowse(final @NotNull Project project,
                                                final @NotNull TextFieldWithBrowseButton textWithBrowse) {
    textWithBrowse.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final String initialPath = FileUtil.toSystemIndependentName(textWithBrowse.getText().trim());
        final VirtualFile initialFile = initialPath.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(initialPath);
        final PsiFile initialPsiFile = initialFile == null ? null : PsiManager.getInstance(project).findFile(initialFile);

        TreeFileChooser fileChooser = TreeFileChooserFactory.getInstance(project).createFileChooser(
          DartBundle.message("choose.dart.main.file"),
          initialPsiFile,
          DartFileType.INSTANCE,
          new TreeFileChooser.PsiFileFilter() {
            public boolean accept(PsiFile file) {
              return !DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(file);
            }
          }
        );

        fileChooser.showDialog();

        final PsiFile selectedFile = fileChooser.getSelectedFile();
        final VirtualFile virtualFile = selectedFile == null ? null : selectedFile.getVirtualFile();
        if (virtualFile != null) {
          final String path = FileUtil.toSystemDependentName(virtualFile.getPath());
          textWithBrowse.setText(path);
        }
      }
    });
  }

  @Override
  protected void resetEditorFrom(DartCommandLineRunConfiguration configuration) {
    myFileField.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(configuration.getFilePath())));
    myArguments.setText(StringUtil.notNullize(configuration.getArguments()));
    myVMOptions.setText(StringUtil.notNullize(configuration.getVMOptions()));
    myWorkingDirectory.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(configuration.getWorkingDirectory())));
    myEnvironmentVariables.setEnvs(configuration.getEnvs());
  }

  @Override
  protected void applyEditorTo(DartCommandLineRunConfiguration configuration) throws ConfigurationException {
    configuration.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(myFileField.getText().trim()), true));
    configuration.setArguments(StringUtil.nullize(myArguments.getText(), true));
    configuration.setVMOptions(StringUtil.nullize(myVMOptions.getText(), true));
    configuration.setWorkingDirectory(StringUtil.nullize(FileUtil.toSystemIndependentName(myWorkingDirectory.getText().trim()), true));
    configuration.setEnvs(myEnvironmentVariables.getEnvs());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }

  private void createUIComponents() {
    myEnvironmentVariables = new EnvironmentVariablesComponent();
  }
}
