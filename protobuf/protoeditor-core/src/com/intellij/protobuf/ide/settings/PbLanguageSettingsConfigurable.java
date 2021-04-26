/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableBase;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.ide.PbIdeBundle;
import org.jetbrains.annotations.NotNull;

/** A {@link Configurable} that provides a protobuf language settings panel. */
public class PbLanguageSettingsConfigurable
    extends ConfigurableBase<PbLanguageSettingsForm, PbProjectSettings> {

  private static final String ID = "google.protobuf.language";
  private static final String DISPLAY_NAME = PbIdeBundle.message("settings.project.display");

  private final Project project;

  public PbLanguageSettingsConfigurable(Project project) {
    super(ID, DISPLAY_NAME, null);
    this.project = project;
  }

  @NotNull
  @Override
  protected PbProjectSettings getSettings() {
    return PbProjectSettings.getInstance(project);
  }

  @Override
  protected PbLanguageSettingsForm createUi() {
    return new PbLanguageSettingsForm(project);
  }
}
