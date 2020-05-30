package org.jetbrains.plugins.cucumber.inspections.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.inspections.model.CreateStepDefinitionFileModel;
import org.jetbrains.plugins.cucumber.inspections.model.FileTypeComboboxItem;
import org.jetbrains.plugins.cucumber.steps.CucumberStepHelper;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class CreateStepDefinitionFileDialog extends DialogWrapper {
  private JTextField myFileNameTextField;

  private JComboBox<FileTypeComboboxItem> myFileTypeCombobox;

  private JPanel myContentPanel;
  private TextFieldWithBrowseButton myDirectoryTextField;

  private final InputValidator myValidator;

  private final CreateStepDefinitionFileModel myModel;

  public CreateStepDefinitionFileDialog(@NotNull final Project project, @NotNull final CreateStepDefinitionFileModel model,
                                        @NotNull final InputValidator validator) {
    super(project);
    myModel = model;
    myValidator = validator;

    setTitle(CucumberBundle.message("cucumber.quick.fix.create.step.choose.new.file.dialog.title"));

    init();

    myFileTypeCombobox.setModel(model.getFileTypeModel());
    myFileTypeCombobox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        FileTypeComboboxItem newItem = (FileTypeComboboxItem)myFileTypeCombobox.getSelectedItem();
        FileTypeComboboxItem oldItem = (FileTypeComboboxItem)e.getItem();

        if (newItem != null && oldItem.getDefaultFileName().equals(myFileNameTextField.getText())) {
          myFileNameTextField.setText(newItem.getDefaultFileName());
          myModel.setFileName(newItem.getDefaultFileName());
        }
        myDirectoryTextField.setText(FileUtil.toSystemDependentName(model.getDefaultDirectory()));
      }
    });

    myFileNameTextField.setText(model.getFileName());
    final KeyAdapter keyListener = new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        myModel.setFileName(myFileNameTextField.getText());
        myModel.setDirectory(myDirectoryTextField.getText());
        validateAll();
      }
    };
    myFileNameTextField.addKeyListener(keyListener);

    String folderChooserTitle = CucumberBundle.message("cucumber.quick.fix.create.step.folder.chooser.title");
    final FileChooserDescriptor folderChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    folderChooserDescriptor.setTitle(folderChooserTitle);


    VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(model.getStepDefinitionFolderPath()));
    if (virtualFile == null) {
      virtualFile = model.getContext().getContainingFile().getContainingDirectory().getVirtualFile();
    }
    folderChooserDescriptor.setRoots(virtualFile);
    folderChooserDescriptor.withTreeRootVisible(true);
    folderChooserDescriptor.setShowFileSystemRoots(false);
    folderChooserDescriptor.setHideIgnored(true);

    myDirectoryTextField.addBrowseFolderListener(folderChooserTitle, null, project, folderChooserDescriptor);
    myDirectoryTextField.getTextField().addKeyListener(keyListener);
    myDirectoryTextField.setText(FileUtil.toSystemDependentName(model.getDefaultDirectory()));
    validateAll();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myFileNameTextField;
  }

  @Override
  protected void doOKAction() {
    String directoryName = myDirectoryTextField.getText();
    if (directoryName.length() > 1 && directoryName.endsWith(File.separator)) {
      directoryName = directoryName.substring(0, directoryName.length() - 1);
    }
    myModel.setDirectory(directoryName);

    String fileName = myFileNameTextField.getText();
    if (myValidator == null) {
      close(OK_EXIT_CODE);
    }
    else {
      if (myValidator.checkInput(fileName) && myValidator.canClose(fileName)) {
        myModel.setFileName(myFileNameTextField.getText());
        close(OK_EXIT_CODE);
      }
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    return myContentPanel;
  }

  @Nullable
  @Override
  protected String getDimensionServiceKey() {
    return CreateStepDefinitionFileDialog.class.getName();
  }

  private static boolean isValidPath(@NotNull String path) {
    while (path.length() > 0) {
      if (!PathUtil.isValidFileName(PathUtil.getFileName(path))) {
        return false;
      }
      path = PathUtil.getParentPath(path);
    }
    return true;
  }

  protected void validateAll() {
    if (!isValidPath(myDirectoryTextField.getText())) {
      setErrorText(CucumberBundle.message("cucumber.quick.fix.create.step.file.error.incorrect.directory"), myDirectoryTextField);
    } else {
      setErrorText(null, myDirectoryTextField);
    }

    final String fileName = myFileNameTextField.getText();

    boolean fileNameIsOk = fileName != null && PathUtil.isValidFileName(fileName) &&
                           CucumberStepHelper
                             .validateNewStepDefinitionFileName(myModel.getProject(), fileName, myModel.getSelectedFileType());

    if (!fileNameIsOk) {
      setErrorText(CucumberBundle.message("cucumber.quick.fix.create.step.file.error.incorrect.file.name"), myFileNameTextField);
    } else {
      String fileUrl = VfsUtilCore.pathToUrl(FileUtil.join(myModel.getStepDefinitionFolderPath(), myModel.getFileNameWithExtension()));
      VirtualFile vFile = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
      if (vFile != null) {
        setErrorText(CucumberBundle.message("cucumber.quick.fix.create.step.file.error.file.exists", (myModel.getFileNameWithExtension())), myFileNameTextField);
      }
    }
  }
}
