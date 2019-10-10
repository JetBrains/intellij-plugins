// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.ui;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserFactory;
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
import com.intellij.ui.PortField;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.webdev.DartWebdevConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.webdev.DartWebdevParameters;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartWebdevConfigurationEditorForm extends SettingsEditor<DartWebdevConfiguration> {
  private JPanel myMainPanel;
  private JLabel myHTMLFileLabel;
  private TextFieldWithBrowseButton myHTMLFileField;
  private JLabel myWebdevPortLabel;
  private PortField myWebdevPortField;

  public DartWebdevConfigurationEditorForm(final Project project) {
    initDartFileTextWithBrowse(project, myHTMLFileField);
  }

  public static void initDartFileTextWithBrowse(@NotNull final Project project,
                                                @NotNull final TextFieldWithBrowseButton textWithBrowse) {
    textWithBrowse.getButton().addActionListener(e -> {
      final String initialPath = FileUtil.toSystemIndependentName(textWithBrowse.getText().trim());
      final VirtualFile initialFile = initialPath.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(initialPath);
      final PsiFile initialPsiFile = initialFile == null ? null : PsiManager.getInstance(project).findFile(initialFile);

      final TreeFileChooser fileChooser = TreeFileChooserFactory.getInstance(project)
        .createFileChooser(DartBundle.message("choose.html.main.file"), initialPsiFile, HtmlFileType.INSTANCE, null);

      fileChooser.showDialog();

      final PsiFile selectedFile = fileChooser.getSelectedFile();
      final VirtualFile virtualFile = selectedFile == null ? null : selectedFile.getVirtualFile();
      if (virtualFile != null) {
        final String path = FileUtil.toSystemDependentName(virtualFile.getPath());
        textWithBrowse.setText(path);
      }
    });
  }

  @Override
  protected void resetEditorFrom(@NotNull final DartWebdevConfiguration configuration) {
    final DartWebdevParameters parameters = configuration.getParameters();

    myHTMLFileField.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getHTMLFilePath())));

    if(parameters.getWebdevPort() > 0) {
      myWebdevPortField.setNumber(parameters.getWebdevPort());
    }
  }

  @Override
  protected void applyEditorTo(@NotNull final DartWebdevConfiguration configuration) throws ConfigurationException {
    final DartWebdevParameters parameters = configuration.getParameters();

    parameters.setHTMLFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(myHTMLFileField.getText().trim()), true));
    if(myWebdevPortField.getNumber() <= 0) {
      parameters.setWebdevPort(-1);
    } else {
      parameters.setWebdevPort(myWebdevPortField.getNumber());
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }
}
