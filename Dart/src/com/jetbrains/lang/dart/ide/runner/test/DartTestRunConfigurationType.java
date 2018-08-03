// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.LazyUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class DartTestRunConfigurationType extends ConfigurationTypeBase implements DumbAware {
  protected DartTestRunConfigurationType() {
    super("DartTestRunConfigurationType", DartBundle.message("runner.test.configuration.name"),
          DartBundle.message("runner.test.configuration.description"), LazyUtil.create(() -> DartIcons.Dart_test));
    addFactory(new DartTestConfigurationFactory(this));
  }

  public static DartTestRunConfigurationType getInstance() {
    return Extensions.findExtension(CONFIGURATION_TYPE_EP, DartTestRunConfigurationType.class);
  }

  public static class DartTestConfigurationFactory extends ConfigurationFactory {
    protected DartTestConfigurationFactory(DartTestRunConfigurationType type) {
      super(type);
    }

    @Override
    @NotNull
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      return new DartTestRunConfiguration(project, this, "Dart");
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
      return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project));
    }
  }
}
