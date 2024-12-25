// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public final class DartRemoteDebugConfigurationType extends ConfigurationTypeBase implements DumbAware {
  public static DartRemoteDebugConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(DartRemoteDebugConfigurationType.class);
  }

  public DartRemoteDebugConfigurationType() {
    super("DartRemoteDebugConfigurationType",
          DartBundle.message("remote.debug.configuration.name"),
          DartBundle.message("remote.debug.configuration.description"),
          NotNullLazyValue.createValue(() -> DartIcons.Dart_remote));
    addFactory(new ConfigurationFactory(this) {
      @Override
      public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new DartRemoteDebugConfiguration(project, DartRemoteDebugConfigurationType.this, "Dart Remote");
      }

      @Override
      public boolean isApplicable(@NotNull Project project) {
        return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }

      @Override
      public @NotNull String getId() {
        return "Dart Remote Debug";
      }
    });
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.DartRemoteDebugConfigurationType";
  }
}
