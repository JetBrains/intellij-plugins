package org.jetbrains.plugins.cucumber.inspections.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateStepDefinitionFileModel {
  private String myFileName;

  private final Map<BDDFrameworkType, String> myFileTypeToDefaultDirectoryMap;

  private final DefaultComboBoxModel<FileTypeComboboxItem> myFileTypeModel;

  private String myDirectory;

  private final Project myProject;

  private final @NotNull PsiFile myContext;

  public CreateStepDefinitionFileModel(@NotNull PsiFile context,
                                       @NotNull final Map<BDDFrameworkType, String> fileTypeToDefaultNameMap,
                                       @NotNull final Map<BDDFrameworkType, String> fileTypeToDefaultDirectoryMap) {
    myContext = context;
    myProject = context.getProject();
    List<FileTypeComboboxItem> myFileTypeList = new ArrayList<>();
    for (Map.Entry<BDDFrameworkType, String> entry : fileTypeToDefaultNameMap.entrySet()) {
      if (myFileName == null) {
        myFileName = entry.getValue();
      }
      FileTypeComboboxItem item = new FileTypeComboboxItem(entry.getKey(), entry.getValue());
      myFileTypeList.add(item);
    }
    myFileTypeToDefaultDirectoryMap = fileTypeToDefaultDirectoryMap;
    myFileTypeModel = new DefaultComboBoxModel<>(myFileTypeList.toArray(new FileTypeComboboxItem[0]));
    myDirectory = fileTypeToDefaultDirectoryMap.get(getSelectedFileType());
  }

  public String getFilePath() {
    return FileUtil.join(getStepDefinitionFolderPath(), getFileNameWithExtension());
  }

  public String getFileNameWithExtension() {
    return myFileName + '.' + getSelectedFileType().getFileType().getDefaultExtension();
  }

  public String getFileName() {
    return myFileName;
  }

  public void setFileName(@NotNull final String fileName) {
    myFileName = fileName;
  }

  public String getDefaultDirectory() {
    return myFileTypeToDefaultDirectoryMap.get(getSelectedFileType());
  }

  public String getStepDefinitionFolderPath() {
    return myDirectory;
  }

  public void setDirectory(@Nullable final String directory) {
    myDirectory = directory;
  }

  public BDDFrameworkType getSelectedFileType() {
    final FileTypeComboboxItem selectedItem = (FileTypeComboboxItem) myFileTypeModel.getSelectedItem();
    return selectedItem != null ? selectedItem.getFrameworkType() : null;
  }

  public DefaultComboBoxModel<FileTypeComboboxItem> getFileTypeModel() {
    return myFileTypeModel;
  }

  public Project getProject() {
    return myProject;
  }

  public @NotNull PsiFile getContext() {
    return myContext;
  }
}
