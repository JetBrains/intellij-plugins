package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Type of BDD framework. Cucumber, behave and lettuce are good examples.
 *
 * @author Ilya.Kazakevich
 */
public class BDDFrameworkType {
  @NotNull
  private final FileType myFileType;
  @Nullable
  private final String myAdditionalInfo;

  /**
   * @param fileType file type to be used as step definitions for this framework
   */
  public BDDFrameworkType(@NotNull final FileType fileType) {
    this(fileType, null);
  }

  /**
   * @param fileType       file type to be used as step definitions for this framework
   * @param additionalInfo additional information about this framework to be displayed to user (when filetype is not enough)
   */
  public BDDFrameworkType(@NotNull final FileType fileType,
                          @Nullable final String additionalInfo) {
    myFileType = fileType;
    myAdditionalInfo = additionalInfo;
  }


  /**
   * @return file type to be used as step definitions for this framework
   */
  @NotNull
  public FileType getFileType() {
    return myFileType;
  }

  /**
   * @return additional information about this framework to be displayed to user (when filetype is not enough)
   */
  @Nullable
  public String getAdditionalInfo() {
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
    if (!(o instanceof BDDFrameworkType)) return false;

    BDDFrameworkType type = (BDDFrameworkType)o;

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
