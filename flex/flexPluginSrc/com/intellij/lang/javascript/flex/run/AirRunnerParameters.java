package com.intellij.lang.javascript.flex.run;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

public class AirRunnerParameters extends FlexRunnerParameters {

  public enum AirRunMode {
    AppDescriptor, MainClass
  }

  private @NotNull AirRunMode myAirRunMode = AirRunMode.AppDescriptor;
  private @NotNull String myAirDescriptorPath = "";
  private @NotNull String myAirRootDirPath = "";
  private @NotNull String myAdlOptions = "";
  private @NotNull String myAirProgramParameters = "";

  public AirRunnerParameters() {
  }

  @NotNull
  @Attribute("air_descriptor_path")
  public String getAirDescriptorPath() {
    return myAirDescriptorPath;
  }

  public void setAirDescriptorPath(final @NotNull String airDescriptorPath) {
    myAirDescriptorPath = airDescriptorPath;
  }

  @NotNull
  @Attribute("air_root_dir_path")
  public String getAirRootDirPath() {
    return myAirRootDirPath;
  }

  public void setAirRootDirPath(final @NotNull String airRootDirPath) {
    myAirRootDirPath = airRootDirPath;
  }

  @NotNull
  @Attribute("air_run_mode")
  public AirRunMode getAirRunMode() {
    return myAirRunMode;
  }

  public void setAirRunMode(@NotNull final AirRunMode airRunMode) {
    myAirRunMode = airRunMode;
  }

  @NotNull
  @Attribute("air_publisher_id")
  /**
   * @deprecated this option removed from AIR run configuration, 'AIR Debug Launcher options' used instead
   */
  public String getAirPublisherId() {
    return "";
  }

  /**
   * @deprecated this option removed from AIR run configuration, 'AIR Debug Launcher options' used instead
   */
  public void setAirPublisherId(@NotNull final String airPublisherId) {
    // backward compatibility
    if (!StringUtil.isEmptyOrSpaces(airPublisherId) && "".equals(myAdlOptions)) {
      myAdlOptions = "-pubid " + airPublisherId;
    }
  }

  @NotNull
  @Attribute("adl_options")
  public String getAdlOptions() {
    return myAdlOptions;
  }

  public void setAdlOptions(@NotNull final String adlOptions) {
    myAdlOptions = adlOptions;
  }

  @NotNull
  @Attribute("air_program_params")
  public String getAirProgramParameters() {
    return myAirProgramParameters;
  }

  public void setAirProgramParameters(@NotNull final String airProgramParameters) {
    myAirProgramParameters = airProgramParameters;
  }

  public AirRunnerParameters clone() {
    return (AirRunnerParameters)super.clone();
  }
}
