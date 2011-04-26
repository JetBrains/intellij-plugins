package com.intellij.lang.javascript.flex.actions.airmobile;

import com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase;
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
  name = "MobileAirPackageParameters",
  storages = {
    @Storage(id = "other", file = "$WORKSPACE_FILE$")
  }
)
public class MobileAirPackageParameters extends AirInstallerParametersBase
  implements PersistentStateComponent<MobileAirPackageParameters> {

  public enum MobilePlatform {Android, iOS}

  public enum AndroidPackageType {
    DebugOverNetwork("debug over network"),
    DebugOverUSB("debug over USB"),
    NoDebug("no debug");

    AndroidPackageType(final String presentableName) {
      this.myPresentableName = presentableName;
    }

    private final String myPresentableName;

    public String toString() {
      return myPresentableName;
    }
  }

  public enum IOSPackageType {
    DebugOverNetwork("debug over network"),
    Test("test without debugging"),
    AdHoc("ad hoc distribution"),
    AppStore("Apple App Store distribution");

    IOSPackageType(final String presentableName) {
      this.myPresentableName = presentableName;
    }

    private final String myPresentableName;

    public String toString() {
      return myPresentableName;
    }
  }

  public MobilePlatform MOBILE_PLATFORM;
  public AndroidPackageType ANDROID_PACKAGE_TYPE;
  public IOSPackageType IOS_PACKAGE_TYPE;
  public String DEBUG_CONNECT_HOST;
  public int DEBUG_LISTEN_PORT;
  public String AIR_DOWNLOAD_URL;
  public String PROVISIONING_PROFILE_PATH;

  public static MobileAirPackageParameters getInstance(final Project project) {
    return ServiceManager.getService(project, MobileAirPackageParameters.class);
  }

  public MobileAirPackageParameters() {
    this(MobilePlatform.Android, AndroidPackageType.DebugOverNetwork, IOSPackageType.DebugOverNetwork, null, "", "", "",
         new ArrayList<FilePathAndPathInPackage>(), "", MobileAirUtil.DEBUG_PORT_DEFAULT, "", "", "", "PKCS12", "", "", "", "",
         "");
  }

  public MobileAirPackageParameters(final MobilePlatform mobilePlatform,
                                    final AndroidPackageType androidPackageType,
                                    final IOSPackageType iOISPackageType,
                                    final Sdk flexSdk,
                                    final String airDescriptorPath,
                                    final String installerFileName,
                                    final String installerFileLocation,
                                    final List<FilePathAndPathInPackage> filesToPackage,
                                    final String debugConnectHost,
                                    final int debugListenPort,
                                    final String airDownloadUrl,
                                    final String provisioningProfilePath,
                                    final String keystorePath,
                                    final String keystoreType,
                                    final String keystorePassword,
                                    final String keyAlias,
                                    final String keyPassword,
                                    final String provider,
                                    final String tsa) {
    super(flexSdk, airDescriptorPath, installerFileName, installerFileLocation, filesToPackage, keystorePath, keystoreType,
          keystorePassword, keyAlias, keyPassword, provider, tsa);
    MOBILE_PLATFORM = mobilePlatform;
    ANDROID_PACKAGE_TYPE = androidPackageType;
    IOS_PACKAGE_TYPE = iOISPackageType;
    DEBUG_CONNECT_HOST = debugConnectHost;
    DEBUG_LISTEN_PORT = debugListenPort;
    AIR_DOWNLOAD_URL = airDownloadUrl;
    PROVISIONING_PROFILE_PATH = provisioningProfilePath;
  }

  public MobileAirPackageParameters getState() {
    return this;
  }

  public void loadState(final MobileAirPackageParameters state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}