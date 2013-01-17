package org.jetbrains.plugins.cucumber.inspections.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.inspections.model.CreateStepDefinitionFileModel;
import org.jetbrains.plugins.cucumber.inspections.model.FileTypeComboboxItem;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * User: Andrey.Vokin
 * Date: 1/3/11
 */
public class CreateStepDefinitionFileDialog extends DialogWrapper {

  private JTextField myFileNameTextField;

  private JComboBox myFileTypeCombobox;

  private JPanel myContentPanel;
  private TextFieldWithBrowseButton myDirectoryTextField;
  private JLabel myDirectoryLabel;

  private InputValidator myValidator;

  private CreateStepDefinitionFileModel myModel;

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

        if (oldItem.getDefaultFileName().equals(myFileNameTextField.getText())) {
          myFileNameTextField.setText(newItem.getDefaultFileName());
          myModel.setFileName(newItem.getDefaultFileName());
        }
        myDirectoryTextField.setText(FileUtil.toSystemDependentName(model.getDirectory().getVirtualFile().getPath()));
      }
    });

    myFileNameTextField.setText(model.getFileName());
    myFileNameTextField.addKeyListener(new FileNameKeyListener());

    String folderChooserTitle = CucumberBundle.message("cucumber.quick.fix.create.step.folder.chooser.title");
    final FileChooserDescriptor folderChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    folderChooserDescriptor.setTitle(folderChooserTitle);
    folderChooserDescriptor.setRoots(model.getDirectory().getVirtualFile());
    folderChooserDescriptor.setIsTreeRootVisible(true);
    folderChooserDescriptor.setShowFileSystemRoots(false);
    folderChooserDescriptor.setHideIgnored(true);

    myDirectoryTextField.addBrowseFolderListener(folderChooserTitle, null, project, folderChooserDescriptor);
    myDirectoryTextField.setText(FileUtil.toSystemDependentName(model.getDirectory().getVirtualFile().getPath()));

    validateFileName();
  }

  public JComponent getPreferredFocusedComponent() {
    return myFileNameTextField;
  }

  @Override
  protected void doOKAction() {
    String fileName = myFileNameTextField.getText();
    if (myValidator == null) {
      close(OK_EXIT_CODE);
    }
    else {
      if (myValidator.checkInput(fileName) && myValidator.canClose(fileName)) {
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

  protected void validateFileName() {
    final String fileName = myFileNameTextField.getText();

    Project project = myModel.getDirectory().getProject();
    boolean fileNameIsOk = fileName != null &&
                           CucumberStepsIndex.getInstance(project).validateNewStepDefinitionFileName(myModel.getDirectory(), fileName,
                                                                                                     myModel.getSelectedFileType());

    if (!fileNameIsOk) {
      setErrorText(CucumberBundle.message("cucumber.quick.fix.create.step.file.error.incorrect.file.name"));
    } else {
      PsiFile file = myModel.getDirectory().findFile(myModel.getFileNameWithExtension());
      VirtualFile vFile = file != null ? file.getVirtualFile() : null;
      if (vFile != null) {
        fileNameIsOk = false;
        setErrorText(CucumberBundle.message("cucumber.quick.fix.create.step.file.error.file.exists",
                                     (myModel.getFileNameWithExtension())));
      } else {
        setErrorText(null);
      }
    }
    setOKActionEnabled(fileNameIsOk);
  }

  class FileNameKeyListener implements KeyListener {
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
      myModel.setFileName(myFileNameTextField.getText());
      validateFileName();
    }
  }
}
