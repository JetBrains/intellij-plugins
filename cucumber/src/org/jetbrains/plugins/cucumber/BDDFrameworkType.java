// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Type of BDD framework. Cucumber, behave etc
 *
 * @param fileType       file type to be used as step definitions for this framework
 * @param additionalInfo additional information about this framework to be displayed to user (when filetype is not enough)
 * @author Ilya.Kazakevich
 */
public record BDDFrameworkType(@NotNull FileType fileType, @Nullable String additionalInfo) {
  public BDDFrameworkType(@NotNull FileType fileType) {
    this(fileType, null);
  }

  public @NotNull FileType getFileType() {
    return fileType;
  }

  public @Nullable String getAdditionalInfo() {
    return additionalInfo;
  }
}
