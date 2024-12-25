// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.inspections.model;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;

public class FileTypeComboboxItem {

  private final BDDFrameworkType myFrameworkType;

  private final String myDefaultFileName;

  public FileTypeComboboxItem(final @NotNull BDDFrameworkType frameworkType, final @NotNull String defaultFileName) {
    myFrameworkType = frameworkType;
    myDefaultFileName = defaultFileName;
  }

  @Override
  public String toString() {
    final String fileType = StringUtil.capitalizeWords(StringUtil.toLowerCase(myFrameworkType.getFileType().getName()), true);
    final String additionalInfo = myFrameworkType.getAdditionalInfo();
    if (additionalInfo != null && (fileType.equals("Javascript") || fileType.equals("Typescript"))) return additionalInfo;
    // Display additional info in brackets (if exists)
    return (additionalInfo != null ) ? String.format("%s (%s)", fileType, additionalInfo) : fileType;
  }

  public BDDFrameworkType getFrameworkType() {
    return myFrameworkType;
  }

  public String getDefaultFileName() {
    return myDefaultFileName;
  }
}
