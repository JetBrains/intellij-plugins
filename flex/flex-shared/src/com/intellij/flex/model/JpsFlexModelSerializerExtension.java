package com.intellij.flex.model;

import com.intellij.flex.model.lib.JpsFlexLibraryType;
import com.intellij.flex.model.module.JpsFlexModuleType;
import com.intellij.flex.model.sdk.JpsFlexSdkType;
import com.intellij.flex.model.sdk.JpsFlexmojosSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.serialization.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JpsFlexModelSerializerExtension extends JpsModelSerializerExtension {

  @NotNull
  public List<? extends JpsProjectExtensionSerializer> getProjectExtensionSerializers() {
    return Arrays.asList(JpsFlexCompilerProjectExtension.createProjectExtensionSerializer(),
                         JpsFlexProjectLevelCompilerOptionsExtension.createProjectExtensionSerializer());
  }

  @NotNull
  public List<? extends JpsSdkPropertiesSerializer<?>> getSdkPropertiesSerializers() {
    return Arrays.asList(JpsFlexSdkType.createJpsSdkPropertiesSerializer(), JpsFlexmojosSdkType.createSdkPropertiesSerializer());
  }

  @NotNull
  public List<? extends JpsLibraryPropertiesSerializer<?>> getLibraryPropertiesSerializers() {
    return Collections.singletonList(JpsFlexLibraryType.createLibraryPropertiesSerializer());
  }

  @NotNull
  public List<? extends JpsModulePropertiesSerializer<?>> getModulePropertiesSerializers() {
    return Collections.singletonList(JpsFlexModuleType.createModulePropertiesSerializer());
  }
}
