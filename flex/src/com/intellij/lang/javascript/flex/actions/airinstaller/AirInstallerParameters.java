package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;
import java.util.List;

@State(
  name = "AirInstallerParameters",
  storages = {
    @Storage(id = "other", file = "$WORKSPACE_FILE$")
  }
)
public class AirInstallerParameters implements PersistentStateComponent<AirInstallerParameters> {

  private final Sdk myFlexSdk;
  public String AIR_DESCRIPTOR_PATH;
  public String INSTALLER_FILE_NAME;
  public String INSTALLER_FILE_LOCATION;
  @AbstractCollection(elementTypes = {FilePathAndPathInPackage.class})
  public List<FilePathAndPathInPackage> FILES_TO_PACKAGE;
  public boolean DO_NOT_SIGN;
  public String KEYSTORE_PATH;
  public String KEYSTORE_TYPE;
  private final String myKeystorePassword;
  public String KEY_ALIAS;
  private final String myKeyPassword;
  public String PROVIDER_CLASS;
  public String TSA;

  public static AirInstallerParameters getInstance(final Project project) {
    return ServiceManager.getService(project, AirInstallerParameters.class);
  }

  public AirInstallerParameters() {
    this(null, "", "", "", new ArrayList<FilePathAndPathInPackage>(), false, "", "PKCS12", "", "", "", "", "");
  }

  public AirInstallerParameters(final Sdk flexSdk,
                                final String airDescriptorPath,
                                final String installerFileName,
                                final String installerFileLocation,
                                final List<FilePathAndPathInPackage> filesToPackage,
                                final boolean doNotSign,
                                final String keystorePath,
                                final String keystoreType,
                                final String keystorePassword,
                                final String keyAlias,
                                final String keyPassword,
                                final String provider,
                                final String tsa) {
    myFlexSdk = flexSdk;
    AIR_DESCRIPTOR_PATH = airDescriptorPath;
    INSTALLER_FILE_NAME = installerFileName;
    INSTALLER_FILE_LOCATION = installerFileLocation;
    FILES_TO_PACKAGE = filesToPackage;
    DO_NOT_SIGN = doNotSign;
    KEYSTORE_PATH = keystorePath;
    KEYSTORE_TYPE = keystoreType;
    myKeystorePassword = keystorePassword;
    KEY_ALIAS = keyAlias;
    myKeyPassword = keyPassword;
    PROVIDER_CLASS = provider;
    TSA = tsa;
  }

  public AirInstallerParameters getState() {
    return this;
  }

  public void loadState(final AirInstallerParameters state) {
    XmlSerializerUtil.copyBean(state, this);
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