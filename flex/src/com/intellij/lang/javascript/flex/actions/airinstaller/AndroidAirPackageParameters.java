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
  name = "AndroidAirPackageParameters",
  storages = {
    @Storage(id = "other", file = "$WORKSPACE_FILE$")
  }
)
public class AndroidAirPackageParameters extends AirInstallerParametersBase implements PersistentStateComponent<AndroidAirPackageParameters> {

  public boolean IS_DEBUG;
  public boolean IS_DEBUG_CONNECT;
  public String DEBUG_CONNECT_HOST;
  public boolean IS_DEBUG_LISTEN;
  public int DEBUG_LISTEN_PORT;
  public String AIR_DOWNLOAD_URL;

  public static AndroidAirPackageParameters getInstance(final Project project) {
    return ServiceManager.getService(project, AndroidAirPackageParameters.class);
  }

  public AndroidAirPackageParameters() {
    this(null, "", "", "", new ArrayList<FilePathAndPathInPackage>(), false, true, "", false, 7936, "", "", "PKCS12", "", "", "", "", "");
  }

  public AndroidAirPackageParameters(final Sdk flexSdk,
                                     final String airDescriptorPath,
                                     final String installerFileName,
                                     final String installerFileLocation,
                                     final List<FilePathAndPathInPackage> filesToPackage,
                                     final boolean isDebug,
                                     final boolean isDebugConnect,
                                     final String debugConnectHost,
                                     final boolean isDebugListen,
                                     final int debugListenPort,
                                     final String airDownloadUrl,
                                     final String keystorePath,
                                     final String keystoreType,
                                     final String keystorePassword,
                                     final String keyAlias,
                                     final String keyPassword,
                                     final String provider,
                                     final String tsa) {
    super(keyAlias, installerFileName, keystorePath, keyPassword, provider, filesToPackage, tsa, installerFileLocation, keystoreType,
          keystorePassword, flexSdk, airDescriptorPath);
    IS_DEBUG = isDebug;
    IS_DEBUG_CONNECT = isDebugConnect;
    DEBUG_CONNECT_HOST = debugConnectHost;
    IS_DEBUG_LISTEN = isDebugListen;
    DEBUG_LISTEN_PORT = debugListenPort;
    AIR_DOWNLOAD_URL = airDownloadUrl;
  }

  public AndroidAirPackageParameters getState() {
    return this;
  }

  public void loadState(final AndroidAirPackageParameters state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}