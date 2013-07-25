/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.mxunit;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import icons.CFMLIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CfmlUnitRunConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myFactory;

  public CfmlUnitRunConfigurationType() {
    myFactory = new ConfigurationFactory(this) {
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new CfmlUnitRunConfiguration(project, this, "");
      }
    };
  }

  public String getDisplayName() {
    return "MXUnit";
  }

  public String getConfigurationTypeDescription() {
    return "MXUnit";
  }

  public Icon getIcon() {
    return CFMLIcons.Cfunit;
  }

  @NotNull
  public String getId() {
    return "CfmlUnitRunConfigurationType";
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  @Nullable
  public static CfmlUnitRunConfigurationType getInstance() {
    return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), CfmlUnitRunConfigurationType.class);
  }
}
