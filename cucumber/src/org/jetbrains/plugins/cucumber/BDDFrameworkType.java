// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Type of BDD framework. Cucumber, behave etc
 *
 * @author Ilya.Kazakevich
 */
public class BDDFrameworkType {
  private final @NotNull FileType myFileType;
  private final @Nullable String myAdditionalInfo;

  /**
   * @param fileType file type to be used as step definitions for this framework
   */
  public BDDFrameworkType(final @NotNull FileType fileType) {
    this(fileType, null);
  }

  /**
   * @param fileType       file type to be used as step definitions for this framework
   * @param additionalInfo additional information about this framework to be displayed to user (when filetype is not enough)
   */
  public BDDFrameworkType(final @NotNull FileType fileType,
                          final @Nullable String additionalInfo) {
    myFileType = fileType;
    myAdditionalInfo = additionalInfo;
  }


  /**
   * @return file type to be used as step definitions for this framework
   */
  public @NotNull FileType getFileType() {
    return myFileType;
  }

  /**
   * @return additional information about this framework to be displayed to user (when filetype is not enough)
   */
  public @Nullable String getAdditionalInfo() {
    return myAdditionalInfo;
  }

  @Override
  public String toString() {
    return "BDDFrameworkType{" +
           "myFileType=" + myFileType +
           ", myAdditionalInfo='" + myAdditionalInfo + '\'' +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BDDFrameworkType type)) return false;

    if (myAdditionalInfo != null ? !myAdditionalInfo.equals(type.myAdditionalInfo) : type.myAdditionalInfo != null) return false;
    if (!myFileType.equals(type.myFileType)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myFileType.hashCode();
    result = 31 * result + (myAdditionalInfo != null ? myAdditionalInfo.hashCode() : 0);
    return result;
  }
}
