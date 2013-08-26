package com.jetbrains.lang.dart.ide.actions.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author: Fedor.Korotkov
 */
public class Dart2JSSettingsDialog extends DialogWrapper {
  private TextFieldWithBrowseButton myInputFilePath;
  private TextFieldWithBrowseButton myOutputFilePath;
  private JCheckBox myCheckedMode;
  private JCheckBox myMinify;
  private JPanel myMainPanel;

  public Dart2JSSettingsDialog(@Nullable final Project project, @Nullable String inputFile, @Nullable String outputFile) {
    super(project, true);
    if (outputFile != null) {
      myOutputFilePath.setText(FileUtil.toSystemDependentName(outputFile));
    }
    if (inputFile != null) {
      myInputFilePath.setText(FileUtil.toSystemDependentName(inputFile));
    }

    myCheckedMode.setSelected(PropertiesComponent.getInstance().getBoolean("dart2js.checked.mode", false));
    myMinify.setSelected(PropertiesComponent.getInstance().getBoolean("dart2js.minify", false));

    myCheckedMode.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        PropertiesComponent.getInstance().setValue("dart2js.checked.mode", Boolean.toString(myCheckedMode.isSelected()));
      }
    });
    myMinify.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        PropertiesComponent.getInstance().setValue("dart2js.minify", Boolean.toString(myMinify.isSelected()));
      }
    });
    myInputFilePath.getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        final VirtualFile file = FileChooser.chooseFile(descriptor, myMainPanel, null, project == null ? null : project.getBaseDir());
        if (file != null) {
          final String filePath = FileUtil.toSystemDependentName(file.getPath());
          myInputFilePath.setText(filePath);
          if (StringUtil.isEmpty(myOutputFilePath.getText())) {
            myOutputFilePath.setText(filePath + ".js");
          }
        }
      }
    });
    myOutputFilePath.getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        final VirtualFile file = FileChooser.chooseFile(descriptor, myMainPanel, null, null);
        if (file != null) {
          myOutputFilePath.setText(FileUtil.toSystemDependentName(file.getPath()));
        }
      }
    });

    setTitle("Dart2JS");
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    if (StringUtil.isEmpty(getInputPath()) ||
        VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(getInputPath())) == null) {
      return new ValidationInfo(DartBundle.message("dart2js.input.problem.description"), myInputFilePath);
    }
    if (StringUtil.isEmpty(getOutputPath())) {
      return new ValidationInfo(DartBundle.message("dart2js.output.problem.description"), myOutputFilePath);
    }
    return super.doValidate();
  }

  public String getInputPath() {
    return FileUtil.toSystemIndependentName(myInputFilePath.getText());
  }

  public String getOutputPath() {
    return FileUtil.toSystemIndependentName(myOutputFilePath.getText());
  }

  public boolean isCheckedMode() {
    return myCheckedMode.isSelected();
  }

  public boolean isMinify() {
    return myMinify.isSelected();
  }

  public void disableInput() {
    myInputFilePath.setEnabled(false);
  }
}
