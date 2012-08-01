package org.jetbrains.plugins.cucumber.inspections.model;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Andrey.Vokin
 * Date: 1/3/11
 */
public class CreateStepDefinitionFileModel {

  private String myFileName;

  private final Map<FileType, PsiDirectory> myFileTypeToDefaultDirectoryMap;

  DefaultComboBoxModel myFileTypeModel;

  public CreateStepDefinitionFileModel(@NotNull final Map<FileType, String> fileTypeToDefaultNameMap, @NotNull final Map<FileType, PsiDirectory> fileTypeToDefaultDirectoryMap) {
    List<FileTypeComboboxItem> myFileTypeList = new ArrayList<FileTypeComboboxItem>();
    for (Map.Entry<FileType, String> entry : fileTypeToDefaultNameMap.entrySet()) {
      if (myFileName == null) {
        myFileName = entry.getValue();
      }
      FileTypeComboboxItem item = new FileTypeComboboxItem(entry.getKey(), entry.getValue());
      myFileTypeList.add(item);
    }
    myFileTypeModel = new DefaultComboBoxModel(myFileTypeList.toArray());
    myFileTypeToDefaultDirectoryMap = fileTypeToDefaultDirectoryMap;
  }

  public String getFilePath() {
    final StringBuilder result = new StringBuilder();
    result.append(getDirectory().getVirtualFile().getPath()).append(File.separator).append(getFileNameWithExtension());
    return result.toString();
  }

  public String getFileNameWithExtension() {
    final StringBuilder result = new StringBuilder();
    result.append(myFileName)
      .append('.').append(getSelectedFileType().getDefaultExtension());
    return result.toString();
  }

  public String getFileName() {
    return myFileName;
  }

  public void setFileName(@NotNull final String fileName) {
    myFileName = fileName;
  }

  public PsiDirectory getDirectory() {
    return myFileTypeToDefaultDirectoryMap.get(getSelectedFileType());
  }

  public FileType getSelectedFileType() {
    final FileTypeComboboxItem selectedItem = (FileTypeComboboxItem) myFileTypeModel.getSelectedItem();
    return selectedItem != null ? selectedItem.getFileType() : null;
  }

  public DefaultComboBoxModel getFileTypeModel() {
    return myFileTypeModel;
  }
}
