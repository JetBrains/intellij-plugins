package org.jetbrains.plugins.cucumber.inspections.model;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;

public class FileTypeComboboxItem {

  private final BDDFrameworkType myFrameworkType;

  private final String myDefaultFileName;

  public FileTypeComboboxItem(@NotNull final BDDFrameworkType frameworkType, @NotNull final String defaultFileName) {
    myFrameworkType = frameworkType;
    myDefaultFileName = defaultFileName;
  }

  @Override
  public String toString() {
    final String fileType = StringUtil.capitalizeWords(myFrameworkType.getFileType().getName().toLowerCase(), true);
    final String additionalInfo = myFrameworkType.getAdditionalInfo();
    // Display additional info in bracets (if exists)
    return (additionalInfo != null) ? String.format("%s (%s)", fileType, additionalInfo) : fileType;
  }

  public BDDFrameworkType getFrameworkType() {
    return myFrameworkType;
  }

  public String getDefaultFileName() {
    return myDefaultFileName;
  }
}
