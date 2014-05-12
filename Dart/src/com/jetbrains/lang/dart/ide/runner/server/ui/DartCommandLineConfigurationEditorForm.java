package com.jetbrains.lang.dart.ide.runner.server.ui;

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
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
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

  public DartCommandLineConfigurationEditorForm(final Project project) {
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

    myVMOptions.setDialogCaption(DartBundle.message("config.vmoptions.caption"));
    myArguments.setDialogCaption(DartBundle.message("config.progargs.caption"));
  }

  @Override
  protected void resetEditorFrom(DartCommandLineRunConfiguration configuration) {
    myFileField.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(configuration.getFilePath())));
    myArguments.setText(StringUtil.notNullize(configuration.getArguments()));
    myVMOptions.setText(StringUtil.notNullize(configuration.getVMOptions()));
    myWorkingDirectory.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(configuration.getWorkingDirectory())));
  }

  @Override
  protected void applyEditorTo(DartCommandLineRunConfiguration configuration) throws ConfigurationException {
    configuration.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(myFileField.getText().trim()), true));
    configuration.setArguments(StringUtil.nullize(myArguments.getText(), true));
    configuration.setVMOptions(StringUtil.nullize(myVMOptions.getText(), true));
    configuration.setWorkingDirectory(StringUtil.nullize(FileUtil.toSystemIndependentName(myWorkingDirectory.getText().trim()), true));
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }
}
