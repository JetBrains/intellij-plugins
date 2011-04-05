package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.List;

public class AirInstallerParametersBase {
  protected final Sdk myFlexSdk;
  public String AIR_DESCRIPTOR_PATH;
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

  public AirInstallerParametersBase(
    final String keyAlias,
    final String installerFileName,
    final String keystorePath,
    final String keyPassword,
    final String provider,
    final List<FilePathAndPathInPackage> filesToPackage,
    final String tsa,
    final String installerFileLocation,
    final String keystoreType,
    final String keystorePassword, final Sdk flexSdk, final String airDescriptorPath) {
    KEY_ALIAS = keyAlias;
    INSTALLER_FILE_NAME = installerFileName;
    KEYSTORE_PATH = keystorePath;
    myKeyPassword = keyPassword;
    PROVIDER_CLASS = provider;
    FILES_TO_PACKAGE = filesToPackage;
    TSA = tsa;
    INSTALLER_FILE_LOCATION = installerFileLocation;
    KEYSTORE_TYPE = keystoreType;
    myKeystorePassword = keystorePassword;
    myFlexSdk = flexSdk;
    AIR_DESCRIPTOR_PATH = airDescriptorPath;
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

  public static class FilePathAndPathInPackage {
    public String FILE_PATH = "";
    public String PATH_IN_PACKAGE = "";

    public FilePathAndPathInPackage() {
    }

    public FilePathAndPathInPackage(final String filePath, final String pathInPackage) {
      FILE_PATH = filePath;
      PATH_IN_PACKAGE = pathInPackage;
    }
  }
}
