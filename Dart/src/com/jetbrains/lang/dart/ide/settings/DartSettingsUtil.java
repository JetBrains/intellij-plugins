package com.jetbrains.lang.dart.ide.settings;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartSettingsUtil {
  public static final String DART_SDK_PATH = "dart_sdk_path";

  public static void setSettings(DartSettings settings) {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    propertiesComponent.setValue(DART_SDK_PATH, settings.getSdkPath());
  }

  @NotNull
  public static DartSettings getSettings() {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    final String value = propertiesComponent.getValue(DART_SDK_PATH);
    return new DartSettings(value == null ? "" : value);
  }
}
