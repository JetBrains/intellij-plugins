// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.test;

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

public final class DartTestRunConfigurationType extends ConfigurationTypeBase implements DumbAware {
  public DartTestRunConfigurationType() {
    super("DartTestRunConfigurationType", DartBundle.message("runner.test.configuration.name"),
          DartBundle.message("runner.test.configuration.description"), NotNullLazyValue.createValue(() -> DartIcons.Dart_test));
    addFactory(new DartTestConfigurationFactory(this));
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.DartTestRunConfigurationType";
  }

  public static DartTestRunConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(DartTestRunConfigurationType.class);
  }

  public static class DartTestConfigurationFactory extends ConfigurationFactory {
    protected DartTestConfigurationFactory(DartTestRunConfigurationType type) {
      super(type);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new DartTestRunConfiguration(project, this, "Dart");
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
      return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    }

    @Override
    public @NotNull String getId() {
      return "Dart Test";
    }
  }
}
