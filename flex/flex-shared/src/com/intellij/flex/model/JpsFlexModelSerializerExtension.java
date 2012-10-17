package com.intellij.flex.model;

import com.intellij.flex.model.lib.JpsFlexLibraryType;
import com.intellij.flex.model.module.JpsFlexModuleType;
import com.intellij.flex.model.run.JpsFlashRunConfigurationType;
import com.intellij.flex.model.run.JpsFlexUnitRunConfigurationType;
import com.intellij.flex.model.sdk.JpsFlexSdkType;
import com.intellij.flex.model.sdk.JpsFlexmojosSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;
import org.jetbrains.jps.model.serialization.library.JpsLibraryPropertiesSerializer;
import org.jetbrains.jps.model.serialization.library.JpsSdkPropertiesSerializer;
import org.jetbrains.jps.model.serialization.module.JpsModulePropertiesSerializer;
import org.jetbrains.jps.model.serialization.runConfigurations.JpsRunConfigurationPropertiesSerializer;

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
    return Arrays.asList(JpsFlexSdkType.createJpsSdkPropertiesSerializer(),
                         JpsFlexmojosSdkType.createSdkPropertiesSerializer());
  }

  @NotNull
  public List<? extends JpsLibraryPropertiesSerializer<?>> getLibraryPropertiesSerializers() {
    return Collections.singletonList(JpsFlexLibraryType.createLibraryPropertiesSerializer());
  }

  @NotNull
  public List<? extends JpsModulePropertiesSerializer<?>> getModulePropertiesSerializers() {
    return Collections.singletonList(JpsFlexModuleType.createModulePropertiesSerializer());
  }

  @NotNull
  public List<? extends JpsRunConfigurationPropertiesSerializer<?>> getRunConfigurationPropertiesSerializers() {
    return Arrays.asList(JpsFlashRunConfigurationType.createRunConfigurationSerializer(),
                         JpsFlexUnitRunConfigurationType.createRunConfigurationSerializer());
  }
}
