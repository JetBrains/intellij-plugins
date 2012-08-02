package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.config.JstdConfigFileType;
import com.google.jstestdriver.idea.config.JstdConfigFileUtils;
import com.google.jstestdriver.idea.execution.JstdSettingsUtil;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ConfigFileRunSettingsSection extends AbstractRunSettingsSection {

  private final TextFieldWithBrowseButton myConfigFileTextFieldWithBrowseButton;
  private final JBLabel myLabel = new JBLabel("Configuration file:");

  ConfigFileRunSettingsSection() {
    myConfigFileTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    setAnchor(myLabel);
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myConfigFileTextFieldWithBrowseButton.setText(runSettings.getConfigFile());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    runSettingsBuilder.setConfigFile(ObjectUtils.notNull(myConfigFileTextFieldWithBrowseButton.getText(), ""));
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 0,
          1, 1,
          0.0, 0.0,
          GridBagConstraints.EAST,
          GridBagConstraints.NONE,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, UIUtil.DEFAULT_HGAP),
          0, 0
      );
      myLabel.setDisplayedMnemonic('C');
      myLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      myLabel.setLabelFor(myConfigFileTextFieldWithBrowseButton.getTextField());
      panel.add(myLabel, c);
    }
    {
      GridBagConstraints c = new GridBagConstraints(
          1, 0,
          1, 1,
          1.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, 0),
          0, 0
      );
      final Project project = creationContext.getProject();
      final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
        @Override
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
          boolean visible = super.isFileVisible(file, showHiddenFiles);
          if (visible && !file.isDirectory() && !showHiddenFiles) {
            visible = JstdConfigFileUtils.isJstdConfigFile(file);
          }
          return visible;
        }
      };
      final JTextField configFileTextField = myConfigFileTextFieldWithBrowseButton.getTextField();
      FileChooserFactory.getInstance().installFileCompletion(configFileTextField, descriptor, false, null);
      myConfigFileTextFieldWithBrowseButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          PsiFile initialPsiFile = getConfigFile(project);
          TreeFileChooserDialog fileChooser = new TreeFileChooserDialog(
            project,
            "Select JsTestDriver configuration file",
            initialPsiFile,
            JstdConfigFileType.INSTANCE,
            new TreeFileChooser.PsiFileFilter() {
              @Override
              public boolean accept(PsiFile psiFile) {
                return true;
              }
            },
            false,
            false
          );
          boolean jstdConfigFilesExists = JstdSettingsUtil.areJstdConfigFilesInProject(project);
          if (jstdConfigFilesExists) {
            fileChooser.selectSearchByNameTab();
          }
          fileChooser.showDialog();
          PsiFile psiFile = fileChooser.getSelectedFile();
          if (psiFile != null) {
            VirtualFile vFile = psiFile.getVirtualFile();
            if (vFile != null) {
              String path = FileUtil.toSystemDependentName(vFile.getPath());
              configFileTextField.setText(path);
            }
          }
        }
      });
      panel.add(myConfigFileTextFieldWithBrowseButton, c);
    }
    SwingUtils.addGreedyBottomRow(panel);
    return panel;
  }

  @Nullable
  private PsiFile getConfigFile(@NotNull Project project) {
    String directoryName = myConfigFileTextFieldWithBrowseButton.getText();
    if (directoryName.length() == 0) return null;
    directoryName = directoryName.replace(File.separatorChar, '/');
    VirtualFile path = LocalFileSystem.getInstance().findFileByPath(directoryName);
    while (path == null && directoryName.length() > 0) {
      int pos = directoryName.lastIndexOf('/');
      if (pos <= 0) break;
      directoryName = directoryName.substring(0, pos);
      path = LocalFileSystem.getInstance().findFileByPath(directoryName);
    }
    if (path != null) {
      PsiManager psiManager = PsiManager.getInstance(project);
      if (!path.isDirectory()) {
        return psiManager.findFile(path);
      }
    }
    return null;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
  }

}
