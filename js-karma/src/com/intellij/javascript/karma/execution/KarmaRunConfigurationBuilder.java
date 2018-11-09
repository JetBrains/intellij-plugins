// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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

  @NotNull
  @Override
  protected KarmaRunSettings createRunSettings(@Nullable VirtualFile baseDir,
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
