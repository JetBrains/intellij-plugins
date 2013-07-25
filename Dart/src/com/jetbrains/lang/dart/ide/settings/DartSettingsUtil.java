package com.jetbrains.lang.dart.ide.settings;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.jetbrains.lang.dart.DartBundle;
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

  public static boolean isDartSDKConfigured(Project project) {
    final JSLibraryMappings mappings = ServiceManager.getService(project, JSLibraryMappings.class);
    return ContainerUtil.exists(mappings.getSingleLibraries(), new Condition<ScriptingLibraryModel>() {
      @Override
      public boolean value(ScriptingLibraryModel model) {
        return DartBundle.message("dart.sdk.name").equals(model.getName());
      }
    });
  }
}
