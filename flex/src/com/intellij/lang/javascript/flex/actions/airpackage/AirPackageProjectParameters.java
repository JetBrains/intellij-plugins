package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;

@State(
  name = "AirPackageProjectParameters",
  storages = {
    @Storage(file = "$WORKSPACE_FILE$")
  }
)
public class AirPackageProjectParameters implements PersistentStateComponent<AirPackageProjectParameters> {

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
  public int apkDebugListenPort = AirPackageUtil.DEBUG_PORT_DEFAULT;

  public IOSPackageType iosPackageType = IOSPackageType.values()[0];
  public boolean iosFastPackaging = true;

  //public Collection<String> selectedPaths

  @Transient
  private boolean myPackagingInProgress = false;

  @Transient
  private PasswordStore myPasswordStore = new PasswordStore();

  public static AirPackageProjectParameters getInstance(final Project project) {
    return ServiceManager.getService(project, AirPackageProjectParameters.class);
  }

  public static PasswordStore getPasswordStore(final Project project) {
    return getInstance(project).myPasswordStore;
  }

  public AirPackageProjectParameters getState() {
    return this;
  }

  public void loadState(final AirPackageProjectParameters state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public void setPackagingInProgress(final boolean packagingInProgress) {
    myPackagingInProgress = packagingInProgress;
  }

  public boolean isPackagingInProgress() {
    return myPackagingInProgress;
  }

  public String getDesktopPackageFileExtension() {
    switch (desktopPackageType) {
      case AirInstaller:
        return ".air";
      case Airi:
        return ".airi";
      default:
        assert false : desktopPackageType.toString();
        return "";
    }
  }
}