// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution;

import com.intellij.javascript.JSRunConfigurationBuilderBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class KarmaRunConfigurationBuilder extends JSRunConfigurationBuilderBase<KarmaRunConfiguration, KarmaRunSettings> {

  public KarmaRunConfigurationBuilder(@NotNull Project project) {
    super(project, "karma", KarmaConfigurationType.getInstance(),
          KarmaRunConfiguration::getRunSettings, KarmaRunConfiguration::setRunSettings);
  }

  @Override
  protected @NotNull KarmaRunSettings createRunSettings(@Nullable VirtualFile baseDir,
                                                        @Nullable String configPath,
                                                        @NotNull Map<String, Object> options) {
    assert baseDir != null && configPath != null;
    return new KarmaRunSettings.Builder()
      .setConfigPath(configPath)
      .setWorkingDirectory(baseDir.getPath())
      .build();
  }

  @Override
  protected boolean isSimilar(@NotNull KarmaRunSettings s1, @NotNull KarmaRunSettings s2) {
    return s1.getConfigPathSystemDependent().equals(s2.getConfigPathSystemDependent())
           && Objects.equals(s1.getKarmaPackage(), s2.getKarmaPackage());
  }
}
