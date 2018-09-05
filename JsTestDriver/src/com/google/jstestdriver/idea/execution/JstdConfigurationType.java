/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.execution;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.JsTestDriverIcons;
import org.jetbrains.annotations.NotNull;

/**
 * Top-level of the plugin - this class is registered in the plugin XML.
 * Provides a new type of Run Configuration which launches the JSTestDriver server.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public final class JstdConfigurationType extends ConfigurationTypeBase implements DumbAware {
  private static final String NAME = "JsTestDriver";
  public static final String ID = "JsTestDriver-test-runner";

  public JstdConfigurationType() {
    super(ID, NAME, NAME, JsTestDriverIcons.JsTestDriver);
    addFactory(new ConfigurationFactory(this) {
      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new JstdRunConfiguration(project, this, NAME);
      }

      @NotNull
      @Override
      public RunConfigurationSingletonPolicy getSingletonPolicy() {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY;
      }
    });
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.JsTestDriver-test-runner";
  }

  public static JstdConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(JstdConfigurationType.class);
  }
}
