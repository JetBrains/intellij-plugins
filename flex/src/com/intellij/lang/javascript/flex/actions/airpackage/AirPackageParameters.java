package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;

@State(
  name = "AirPackageParameters",
  storages = {
    @Storage(file = "$WORKSPACE_FILE$")
  }
)
public class AirPackageParameters implements PersistentStateComponent<AirPackageParameters> {

  public enum DesktopPackageType {
    AirInstaller("installer (*.air)"),
    Airi("unsigned package (*.airi)");  // todo support native installer and captive runtime

    DesktopPackageType(final String presentableName) {
      this.myPresentableName = presentableName;
    }

    private final String myPresentableName;

    public String toString() {
      return myPresentableName;
    }
  }

  public enum AndroidPackageType {
    Release("release"),
    DebugOverUSB("debug over USB"),
    DebugOverNetwork("debug over network");

    AndroidPackageType(final String presentableName) {
      this.myPresentableName = presentableName;
    }

    private final String myPresentableName;

    public String toString() {
      return myPresentableName;
    }
  }

  public enum IOSPackageType {
    Test("test without debugging"),
    DebugOverNetwork("debug over network"),
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

  public DesktopPackageType desktopPackageType = DesktopPackageType.values()[0];

  public AndroidPackageType androidPackageType = AndroidPackageType.values()[0];
  public boolean apkCaptiveRuntime = true;
  /**
   * empty value means {@link com.intellij.lang.javascript.flex.FlexUtils#getOwnIpAddress()}
   */
  public String apkDebugConnectHost = "";
  public int apkDebugListenPort = MobileAirUtil.DEBUG_PORT_DEFAULT;

  public IOSPackageType iosPackageType = IOSPackageType.values()[0];
  public boolean iosFastPackaging = true;

  //public Collection<String> selectedPaths

  @Transient
  private boolean myPackagingInProgress = false;

  public void setPackagingInProgress(final boolean packagingInProgress) {
    myPackagingInProgress = packagingInProgress;
  }

  public boolean isPackagingInProgress() {
    return myPackagingInProgress;
  }

  public static AirPackageParameters getInstance(final Project project) {
    return ServiceManager.getService(project, AirPackageParameters.class);
  }

  public AirPackageParameters getState() {
    return this;
  }

  public void loadState(final AirPackageParameters state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}