// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

  @Override
  public @NotNull List<? extends JpsProjectExtensionSerializer> getProjectExtensionSerializers() {
    return Arrays.asList(JpsFlexCompilerProjectExtension.createProjectExtensionSerializerIws(),
                         JpsFlexCompilerProjectExtension.createProjectExtensionSerializer(),
                         JpsFlexProjectLevelCompilerOptionsExtension.createProjectExtensionSerializerIws(),
                         JpsFlexProjectLevelCompilerOptionsExtension.createProjectExtensionSerializer());
  }

  @Override
  public @NotNull List<? extends JpsSdkPropertiesSerializer<?>> getSdkPropertiesSerializers() {
    return Arrays.asList(JpsFlexSdkType.createJpsSdkPropertiesSerializer(),
                         JpsFlexmojosSdkType.createSdkPropertiesSerializer());
  }

  @Override
  public @NotNull List<? extends JpsLibraryPropertiesSerializer<?>> getLibraryPropertiesSerializers() {
    return Collections.singletonList(JpsFlexLibraryType.createLibraryPropertiesSerializer());
  }

  @Override
  public @NotNull List<? extends JpsModulePropertiesSerializer<?>> getModulePropertiesSerializers() {
    return Collections.singletonList(JpsFlexModuleType.createModulePropertiesSerializer());
  }

  @Override
  public @NotNull List<? extends JpsRunConfigurationPropertiesSerializer<?>> getRunConfigurationPropertiesSerializers() {
    return Arrays.asList(JpsFlashRunConfigurationType.createRunConfigurationSerializer(),
                         JpsFlexUnitRunConfigurationType.createRunConfigurationSerializer());
  }
}
