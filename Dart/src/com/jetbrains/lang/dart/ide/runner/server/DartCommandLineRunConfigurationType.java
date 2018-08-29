// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.LazyUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class DartCommandLineRunConfigurationType extends ConfigurationTypeBase implements DumbAware {
  public static DartCommandLineRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(DartCommandLineRunConfigurationType.class);
  }

  public DartCommandLineRunConfigurationType() {
    super("DartCommandLineRunConfigurationType",
          DartBundle.message("runner.command.line.configuration.name"),
          DartBundle.message("runner.command.line.configuration.description"),
          LazyUtil.create(() -> DartIcons.Dart_16));
    addFactory(new ConfigurationFactory(this) {
      @NotNull
      @Override
      public String getName() {
        return "Dart Command Line Application"; // compatibility
      }

      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new DartCommandLineRunConfiguration("Dart", project, DartCommandLineRunConfigurationType.this);
      }

      @Override
      public boolean isApplicable(@NotNull Project project) {
        return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }
    });
  }
}
