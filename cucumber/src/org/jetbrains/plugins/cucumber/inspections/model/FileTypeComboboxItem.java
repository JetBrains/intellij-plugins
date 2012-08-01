package org.jetbrains.plugins.cucumber.inspections.model;

import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;

/**
 * User: Andrey.Vokin
 * Date: 1/3/11
 */
public class FileTypeComboboxItem {

  private final FileType myFileType;

  private final String myDefaultFileName;

  public FileTypeComboboxItem(@NotNull final FileType fileType, @NotNull final String defaultFileName) {
    myFileType = fileType;
    myDefaultFileName = defaultFileName;
  }

  @Override
  public String toString() {
    return myFileType.getName();
  }

  public FileType getFileType() {
    return myFileType;
  }

  public String getDefaultFileName() {
    return myDefaultFileName;
  }
}
