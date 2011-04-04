package com.intellij.lang.javascript.flex.actions.airdescriptor;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class AirDescriptorParameters {

  private final @NotNull String myDescriptorFileName;
  private final @NotNull String myDescriptorFolderPath;
  private final @NotNull String myAirVersion;
  private final @NotNull String myApplicationId;
  private final @NotNull String myApplicationFileName;
  private final @NotNull String myApplicationName;
  private final @NotNull String myApplicationVersion;
  private final @NotNull String myApplicationContent;
  private final @NotNull String myApplicationTitle;
  private final int myApplicationWidth;
  private final int myApplicationHeight;
  private final boolean myAndroidPermissionsEnabled;

  public AirDescriptorParameters(final @NotNull String descriptorFileName,
                                 final @NotNull String descriptorFolderPath,
                                 final @NotNull String airVersion,
                                 final @NotNull String applicationId,
                                 final @NotNull String applicationFileName,
                                 final @NotNull String applicationName,
                                 final @NotNull String applicationVersion,
                                 final @NotNull String applicationContent,
                                 final @NotNull String applicationTitle,
                                 final int applicationWidth,
                                 final int applicationHeight, boolean androidPermissionsEnabled) {
    myDescriptorFileName = descriptorFileName;
    myDescriptorFolderPath = descriptorFolderPath;
    myAirVersion = airVersion;
    myApplicationId = applicationId;
    myApplicationFileName = applicationFileName;
    myApplicationName = applicationName;
    myApplicationVersion = applicationVersion;
    myApplicationContent = applicationContent;
    myApplicationTitle = applicationTitle;
    myApplicationWidth = applicationWidth;
    myApplicationHeight = applicationHeight;
    myAndroidPermissionsEnabled = androidPermissionsEnabled;
  }

  @NotNull
  public String getDescriptorFileName() {
    return myDescriptorFileName;
  }

  @NotNull
  public String getDescriptorFolderPath() {
    return myDescriptorFolderPath;
  }

  @NotNull
  public String getAirVersion() {
    return myAirVersion;
  }

  @NotNull
  public String getApplicationId() {
    return myApplicationId;
  }

  @NotNull
  public String getApplicationFileName() {
    return myApplicationFileName;
  }

  @NotNull
  public String getApplicationName() {
    return myApplicationName;
  }

  @NotNull
  public String getApplicationVersion() {
    return myApplicationVersion;
  }

  @NotNull
  public String getApplicationContent() {
    return myApplicationContent;
  }

  @NotNull
  public String getApplicationTitle() {
    return myApplicationTitle;
  }

  public int getApplicationWidth() {
    return myApplicationWidth;
  }

  public int getApplicationHeight() {
    return myApplicationHeight;
  }

  public boolean isAndroidPermissionsEnabled() {
    return myAndroidPermissionsEnabled;
  }
}
