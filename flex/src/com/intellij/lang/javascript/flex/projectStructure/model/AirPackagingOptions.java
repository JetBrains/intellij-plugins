package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AirPackagingOptions {

  boolean isUseGeneratedDescriptor();

  @NotNull
  String getCustomDescriptorPath();

  @NotNull
  String getPackageFileName();

  @NotNull
  List<FilePathAndPathInPackage> getFilesToPackage();

  @NotNull
  AirSigningOptions getSigningOptions();

  class FilePathAndPathInPackage implements Cloneable {
    @Attribute("file-path")
    public String FILE_PATH = "";
    @Attribute("path-in-package")
    public String PATH_IN_PACKAGE = "";

    public FilePathAndPathInPackage() {
    }

    public FilePathAndPathInPackage(final String filePath, final String pathInPackage) {
      FILE_PATH = filePath;
      PATH_IN_PACKAGE = pathInPackage;
    }

    public FilePathAndPathInPackage clone() {
      try {
        return (FilePathAndPathInPackage)super.clone();
      }
      catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final FilePathAndPathInPackage that = (FilePathAndPathInPackage)o;

      if (FILE_PATH != null ? !FILE_PATH.equals(that.FILE_PATH) : that.FILE_PATH != null) return false;
      if (PATH_IN_PACKAGE != null ? !PATH_IN_PACKAGE.equals(that.PATH_IN_PACKAGE) : that.PATH_IN_PACKAGE != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = FILE_PATH != null ? FILE_PATH.hashCode() : 0;
      result = 31 * result + (PATH_IN_PACKAGE != null ? PATH_IN_PACKAGE.hashCode() : 0);
      return result;
    }
  }
}
