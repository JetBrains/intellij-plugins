package com.intellij.lang.javascript.flex.run;

import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import org.jetbrains.annotations.NotNull;

public class AirMobileRunnerParameters extends AirRunnerParameters {

  public enum AirMobileRunTarget {
    Emulator, AndroidDevice, iOSDevice
  }

  public enum Emulator {
    E480("720 x 480", "480", 720, 480, 720, 480),
    E720("1280 x 720", "720", 1280, 720, 1280, 720),
    E1080("1920 x 1080", "1080", 1920, 1080, 1920, 1080),
    iPad("Apple iPad", "iPad", 768, 1004, 768, 1024),
    iPhone("Apple iPhone", "iPhone", 320, 460, 320, 480),
    iPhoneRetina("Apple iPhone Retina", "iPhoneRetina", 640, 920, 640, 960),
    //iPod("Apple iPod", "iPod", 320, 460, 320, 480),
    //iPodRetina("Apple iPod Retina", "iPodRetina", 640, 920, 640, 960),
    NexusOne("Google Nexus One", "NexusOne", 480, 762, 480, 800),
    Droid("Motorola Droid", "Droid", 480, 816, 480, 854),
    SamsungGalaxyS("Samsung Galaxy S", "SamsungGalaxyS", 480, 762, 780, 800),
    SamsungGalaxyTab("Samsung Galaxy Tab", "SamsungGalaxyTab", 600, 986, 600, 1024),

    FWQVGA("FWQVGA", "FWQVGA", 240, 432, 240, 432),
    FWVGA("FWVGA", "FWVGA", 480, 584, 480, 854),
    HVGA("HVGA", "HVGA", 320, 480, 320, 480),
    QVGA("QVGA", "QVGA", 240, 320, 240, 320),
    WQVGA("WQVGA", "WQVGA", 240, 400, 240, 400),
    WVGA("WVGA", "WVGA", 480, 800, 480, 800),

    Other("Other...", null, 0, 0, 0, 0);

    public final String name;
    public final String adlAlias;
    public final int screenWidth;
    public final int screenHeight;
    public final int fullScreenWidth;
    public final int fullScreenHeight;

    Emulator(String name, String adlAlias, int screenWidth, int screenHeight, int fullScreenWidth, int fullScreenHeight) {
      this.name = name;
      this.adlAlias = adlAlias;
      this.screenWidth = screenWidth;
      this.screenHeight = screenHeight;
      this.fullScreenWidth = fullScreenWidth;
      this.fullScreenHeight = fullScreenHeight;
    }
  }

  public enum AirMobileRunMode {
    AppDescriptor, MainClass, ExistingPackage
  }

  public enum AirMobileDebugTransport {
    Network, USB
  }

  private @NotNull AirMobileRunTarget myAirMobileRunTarget = AirMobileRunTarget.Emulator;
  private @NotNull AirMobileRunMode myAirMobileRunMode = AirMobileRunMode.AppDescriptor;
  private @NotNull String myExistingPackagePath = "";
  private @NotNull Emulator myEmulator = Emulator.NexusOne;
  private int myScreenWidth = 0;
  private int myScreenHeight = 0;
  private int myFullScreenWidth = 0;
  private int myFullScreenHeight = 0;
  private @NotNull String myMobilePackageFileName = "";
  private @NotNull AirMobileDebugTransport myDebugTransport = AirMobileDebugTransport.USB;
  private int myUsbDebugPort = MobileAirUtil.DEBUG_PORT_DEFAULT;

  @NotNull
  public AirMobileRunTarget getAirMobileRunTarget() {
    return myAirMobileRunTarget;
  }

  public void setAirMobileRunTarget(@NotNull AirMobileRunTarget airMobileRunTarget) {
    myAirMobileRunTarget = airMobileRunTarget;
  }

  @NotNull
  public AirMobileRunMode getAirMobileRunMode() {
    return myAirMobileRunMode;
  }

  public void setAirMobileRunMode(@NotNull AirMobileRunMode airMobileRunMode) {
    myAirMobileRunMode = airMobileRunMode;
  }

  @NotNull
  public String getExistingPackagePath() {
    return myExistingPackagePath;
  }

  public void setExistingPackagePath(@NotNull final String existingPackagePath) {
    myExistingPackagePath = existingPackagePath;
  }

  @NotNull
  public Emulator getEmulator() {
    return myEmulator;
  }

  public void setEmulator(@NotNull Emulator emulator) {
    myEmulator = emulator;
  }

  public int getScreenWidth() {
    return myScreenWidth;
  }

  public void setScreenWidth(int screenWidth) {
    myScreenWidth = screenWidth;
  }

  public int getScreenHeight() {
    return myScreenHeight;
  }

  public void setScreenHeight(int screenHeight) {
    myScreenHeight = screenHeight;
  }

  public int getFullScreenWidth() {
    return myFullScreenWidth;
  }

  public void setFullScreenWidth(int fullScreenWidth) {
    myFullScreenWidth = fullScreenWidth;
  }

  public int getFullScreenHeight() {
    return myFullScreenHeight;
  }

  public void setFullScreenHeight(int fullScreenHeight) {
    myFullScreenHeight = fullScreenHeight;
  }

  @NotNull
  public String getMobilePackageFileName() {
    return myMobilePackageFileName;
  }

  public void setMobilePackageFileName(@NotNull final String mobilePackageFileName) {
    myMobilePackageFileName = mobilePackageFileName;
  }

  @NotNull
  public AirMobileDebugTransport getDebugTransport() {
    return myDebugTransport;
  }

  public void setDebugTransport(@NotNull final AirMobileDebugTransport debugTransport) {
    myDebugTransport = debugTransport;
  }

  public int getUsbDebugPort() {
    return myUsbDebugPort;
  }

  public void setUsbDebugPort(final int usbDebugPort) {
    myUsbDebugPort = usbDebugPort;
  }

  public AirMobileRunnerParameters clone() {
    return (AirMobileRunnerParameters)super.clone();
  }
}
