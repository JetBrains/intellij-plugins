package com.jetbrains.lang.dart.ide.runner.client.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.LocalDartDebugConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
public class LocalDartConfigurationEditorForm extends SettingsEditor<LocalDartDebugConfiguration> {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton myHtmlFileField;
  private final Project myProject;

  public LocalDartConfigurationEditorForm(@NotNull Project project) {
    myProject = project;
  }

  @Override
  protected void resetEditorFrom(LocalDartDebugConfiguration configuration) {
    final String url = configuration.getFileUrl();
    myHtmlFileField.setText(StringUtil.isEmpty(url) ? "" : VirtualFileManager.extractPath(url));
  }

  @Override
  protected void applyEditorTo(LocalDartDebugConfiguration configuration) throws ConfigurationException {
    String path = FileUtil.toSystemIndependentName(myHtmlFileField.getText());
    if (!StringUtil.isEmpty(path)) {
      configuration.setFileUrl(VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, path));
    }
    else {
      configuration.setFileUrl(null);
    }
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
      @Override
      public boolean isFileSelectable(final VirtualFile file) {
        final FileType fileType = file.getFileType();
        return fileType == StdFileTypes.HTML || fileType == StdFileTypes.XHTML;
      }
    };
    descriptor.setRoots(ProjectRootManager.getInstance(myProject).getContentRoots());
    myHtmlFileField.addBrowseFolderListener(DartBundle.message("dart.debugger.settings.choose.file.title"),
                                            DartBundle.message("dart.debugger.settings.choose.file.subtitle"),
                                            myProject, descriptor);
    return myMainPanel;
  }

  @Override
  protected void disposeEditor() {
  }
}
