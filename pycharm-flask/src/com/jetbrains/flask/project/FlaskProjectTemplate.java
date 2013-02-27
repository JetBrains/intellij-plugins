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
package com.jetbrains.flask.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.platform.ProjectTemplate;
import com.jetbrains.python.module.PyModuleService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class FlaskProjectTemplate implements ProjectTemplate {
  @NotNull
  @Override
  public String getName() {
    return "Flask Project";
  }

  @Nullable
  @Override
  public String getDescription() {
    return "Creates a new Python Web application using the <a href=\"http://flask.pocoo.org\">Flask</a> microframework";
  }

  @NotNull
  @Override
  public ModuleBuilder createModuleBuilder() {
    return PyModuleService.getInstance().createPythonModuleBuilder(new FlaskProjectGenerator(true));
  }

  @Nullable
  @Override
  public ValidationInfo validateSettings() {
    return null;
  }
}
