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

import com.google.jstestdriver.idea.MessageBundle;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import icons.JsTestDriverIcons;

/**
 * Top-level of the plugin - this class is registered in the plugin XML.
 * Provides a new type of Run Configuration which launches the JSTestDriver server.
 * @author alexeagle@google.com (Alex Eagle)
 */
public class JstdConfigurationType extends ConfigurationTypeBase {

  public JstdConfigurationType() {
    super("JSTestDriver:ConfigurationType", MessageBundle.getPluginName(),
          MessageBundle.getPluginName(), JsTestDriverIcons.JsTestDriver);
    addFactory(new ConfigurationFactory(this) {
      @Override
      public RunConfiguration createTemplateConfiguration(Project project) {
        return new JstdRunConfiguration(project, this, MessageBundle.getPluginName());
      }
    });
  }

  public static JstdConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(JstdConfigurationType.class);
  }

}
