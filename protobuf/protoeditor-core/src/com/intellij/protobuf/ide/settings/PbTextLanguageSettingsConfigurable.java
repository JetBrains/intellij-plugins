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

/** A {@link Configurable} that provides a protobuf text format language settings panel. */
public class PbTextLanguageSettingsConfigurable
    extends ConfigurableBase<PbTextLanguageSettingsForm, PbTextLanguageSettings> {

  private static final String ID = "google.prototext.language";

  private final Project project;

  public PbTextLanguageSettingsConfigurable(Project project) {
    super(ID, PbIdeBundle.message("prototext.settings.project.display"), /* helpTopic= */ null);
    this.project = project;
  }

  @NotNull
  @Override
  protected PbTextLanguageSettings getSettings() {
    return PbTextLanguageSettings.getInstance(project);
  }

  @Override
  protected PbTextLanguageSettingsForm createUi() {
    return new PbTextLanguageSettingsForm(project);
  }
}
