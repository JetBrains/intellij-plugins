package com.jetbrains.lang.dart.ide.actions.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author: Fedor.Korotkov
 */
public class Dart2JSSettingsDialog extends DialogWrapper {
  private JLabel myOutputLabel;
  private TextFieldWithBrowseButton myOutputFilePath;
  private JCheckBox myCheckedMode;
  private JCheckBox myMinify;
  private JPanel myMainPanel;

  public Dart2JSSettingsDialog(@Nullable Project project, String jsFilePath) {
    super(project, true);
    myOutputFilePath.setText(FileUtil.toSystemDependentName(jsFilePath));

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
    myOutputFilePath.getButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
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

  public String getOutputPath() {
    return FileUtil.toSystemIndependentName(myOutputFilePath.getText());
  }

  public boolean isCheckedMode() {
    return myCheckedMode.isSelected();
  }

  public boolean isMinify() {
    return myMinify.isSelected();
  }
}
