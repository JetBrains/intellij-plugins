package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.xmlb.XmlSerializerUtil;

import java.util.ArrayList;
import java.util.List;

@State(
  name = "AirInstallerParameters",
  storages = {
    @Storage( file = "$WORKSPACE_FILE$")
  }
)
public class AirInstallerParameters extends AirInstallerParametersBase implements PersistentStateComponent<AirInstallerParameters> {

  public boolean DO_NOT_SIGN;

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
    super(flexSdk, airDescriptorPath, installerFileName, installerFileLocation, filesToPackage, keystorePath, keystoreType,
          keystorePassword, keyAlias, keyPassword, provider, tsa);
    DO_NOT_SIGN = doNotSign;
  }

  public AirInstallerParameters getState() {
    return this;
  }

  public void loadState(final AirInstallerParameters state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}