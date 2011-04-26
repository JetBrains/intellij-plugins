package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Transient;

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
