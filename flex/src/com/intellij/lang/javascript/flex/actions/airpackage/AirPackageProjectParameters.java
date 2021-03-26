// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

@State(
  name = "AirPackageProjectParameters",
  storages = {
    @Storage(StoragePathMacros.WORKSPACE_FILE)
  }
)
public class AirPackageProjectParameters implements PersistentStateComponent<AirPackageProjectParameters> {
  private static final String NATIVE_INSTALLER_EXTENSION =
    SystemInfo.isWindows ? ".exe"
                           : SystemInfo.isMac ? ".dmg"
                                              : StringUtil.toLowerCase(SystemInfo.OS_NAME).contains("ubuntu") ? ".deb"
                                                                                                                : ".rpm";

  public enum DesktopPackageType {
    AirInstaller("installer (*.air)", ".air"),
    NativeInstaller("native installer (*" + NATIVE_INSTALLER_EXTENSION + ")", NATIVE_INSTALLER_EXTENSION),
    CaptiveRuntimeBundle("captive runtime bundle" + (SystemInfo.isMac ? " (*.app)" : ""), SystemInfo.isMac ? ".app" : ""),
    Airi("unsigned package (*.airi)", ".airi");

    DesktopPackageType(final String presentableName, final String extension) {
      myPresentableName = presentableName;
      myFileExtension = extension;
    }

    private final String myPresentableName;
    private final String myFileExtension;

    public String toString() {
      return myPresentableName;
    }

    public String getFileExtension() {
      return myFileExtension;
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
    DebugOverUSB("debug over USB"),
    DebugOverNetwork("debug over network"),
    TestOnSimulator("test on iOS Simulator"),
    DebugOnSimulator("debug on iOS Simulator"),
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
  public int apkDebugListenPort = AirPackageUtil.DEBUG_PORT_DEFAULT;

  public IOSPackageType iosPackageType = IOSPackageType.values()[0];
  public boolean iosFastPackaging = true;

  // String value consists of one or more "[module name] \t [bc name]" entries separated with new line ("\n").
  public String deselectedBCs = "";

  private boolean myPackagingInProgress = false;

  @Transient
  private final PasswordStore myPasswordStore = new PasswordStore();

  public static AirPackageProjectParameters getInstance(final Project project) {
    return project.getService(AirPackageProjectParameters.class);
  }

  public static PasswordStore getPasswordStore(final Project project) {
    return getInstance(project).myPasswordStore;
  }

  @Override
  public AirPackageProjectParameters getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull final AirPackageProjectParameters state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Transient
  public void setPackagingInProgress(final boolean packagingInProgress) {
    myPackagingInProgress = packagingInProgress;
  }

  @Transient
  public boolean isPackagingInProgress() {
    return myPackagingInProgress;
  }
}