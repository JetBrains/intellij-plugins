package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.LinkedList;
import java.util.List;

public class AirInstallerParametersBase {
  protected final Sdk myFlexSdk;
  public String AIR_DESCRIPTOR_PATH;
  public String SDK_NAME;
  public String INSTALLER_FILE_NAME;
  public String INSTALLER_FILE_LOCATION;
  @AbstractCollection(elementTypes = {FilePathAndPathInPackage.class})
  public List<FilePathAndPathInPackage> FILES_TO_PACKAGE;
  public String KEYSTORE_PATH;
  public String KEYSTORE_TYPE;
  protected final String myKeystorePassword;
  public String KEY_ALIAS;
  protected final String myKeyPassword;
  public String PROVIDER_CLASS;
  public String TSA;

  public AirInstallerParametersBase(final Sdk flexSdk,
                                    final String airDescriptorPath,
                                    final String installerFileName,
                                    final String installerFileLocation,
                                    final List<FilePathAndPathInPackage> filesToPackage,
                                    final String keystorePath,
                                    final String keystoreType,
                                    final String keystorePassword,
                                    final String keyAlias,
                                    final String keyPassword,
                                    final String provider,
                                    final String tsa) {
    myFlexSdk = flexSdk;
    SDK_NAME = flexSdk == null ? "" : flexSdk.getName();
    AIR_DESCRIPTOR_PATH = airDescriptorPath;
    INSTALLER_FILE_NAME = installerFileName;
    INSTALLER_FILE_LOCATION = installerFileLocation;
    FILES_TO_PACKAGE = filesToPackage;
    KEYSTORE_PATH = keystorePath;
    KEYSTORE_TYPE = keystoreType;
    myKeystorePassword = keystorePassword;
    KEY_ALIAS = keyAlias;
    myKeyPassword = keyPassword;
    PROVIDER_CLASS = provider;
    TSA = tsa;
  }

  @Transient
  public Sdk getFlexSdk() {
    return myFlexSdk;
  }

  @Transient
  public String getKeyPassword() {
    return myKeyPassword;
  }

  @Transient
  public String getKeystorePassword() {
    return myKeystorePassword;
  }

  public static List<FilePathAndPathInPackage> cloneList(final List<FilePathAndPathInPackage> filesToPackage) {
    final List<FilePathAndPathInPackage> clonedList = new LinkedList<FilePathAndPathInPackage>();
    for (FilePathAndPathInPackage filePathAndPathInPackage : filesToPackage) {
      clonedList.add(filePathAndPathInPackage.clone());
    }
    return clonedList;
  }

  public static class FilePathAndPathInPackage implements Cloneable {
    public String FILE_PATH = "";
    public String PATH_IN_PACKAGE = "";

    public FilePathAndPathInPackage() {
    }

    public FilePathAndPathInPackage(final String filePath, final String pathInPackage) {
      FILE_PATH = filePath;
      PATH_IN_PACKAGE = pathInPackage;
    }

    protected FilePathAndPathInPackage clone() {
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
