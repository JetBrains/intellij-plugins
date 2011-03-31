package com.intellij.lang.javascript.flex.run;

import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

public class AirMobileRunnerParameters extends AirRunnerParameters {


  public enum AirMobileRunTarget {
    Emulator, AndroidDevice
  }

  public enum Emulator {
    E480("720 x 480", "480", 720, 480, 720, 480),
    E720("1280 x 720", "720", 1280, 720, 1280, 720),
    E1080("1920 x 1080", "1080", 1920, 1080, 1920, 1080),
    iPad("iPad", "iPad", 768, 1004, 768, 1024),
    iPhone("iPhone", "iPhone", 320, 460, 320, 480),
    //iPhoneRetina("iPhone Retina", "iPhoneRetina", 640, 920, 640, 960),
    iPod("iPod", "iPod", 320, 460, 320, 480),
    //iPodRetina("iPodRetina", "iPodRetina", ),
    NexusOne("Google Nexus One", "NexusOne", 480, 762, 480, 800),
    Droid("Motorola Droid", "Droid", 480, 816, 480, 854),
    //SamsungGalaxyS("Samsung Galaxy S", "SamsungGalaxyS", 480, 762, 780, 800),
    //SamsungGalaxyTab("Samsung Galaxy Tab", "SamsungGalaxyTab", 600, 986, 600, 1024),

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
    AppDescriptor, MainClass/*, AndroidApk*/

  }

  private @NotNull AirMobileRunTarget myAirMobileRunTarget = AirMobileRunTarget.Emulator;
  private @NotNull AirMobileRunMode myAirMobileRunMode = AirMobileRunMode.AppDescriptor;
  private @NotNull Emulator myEmulator = Emulator.NexusOne;
  private int myScreenWidth = 0;
  private int myScreenHeight = 0;
  private int myFullScreenWidth = 0;
  private int myFullScreenHeight = 0;

  @NotNull
  @Attribute("air_mobile_run_target")
  public AirMobileRunTarget getAirMobileRunTarget() {
    return myAirMobileRunTarget;
  }

  public void setAirMobileRunTarget(@NotNull AirMobileRunTarget airMobileRunTarget) {
    myAirMobileRunTarget = airMobileRunTarget;
  }

  @NotNull
  @Attribute("air_mobile_run_mode")
  public AirMobileRunMode getAirMobileRunMode() {
    return myAirMobileRunMode;
  }

  public void setAirMobileRunMode(@NotNull AirMobileRunMode airMobileRunMode) {
    myAirMobileRunMode = airMobileRunMode;
  }

  @NotNull
  @Attribute("emulator_type")
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

  public AirMobileRunnerParameters clone() {
    return (AirMobileRunnerParameters)super.clone();
  }
}
