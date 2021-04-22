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

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/** An extension point interface for automatically configuring {@link PbProjectSettings}. */
public interface ProjectSettingsConfigurator {
  ExtensionPointName<ProjectSettingsConfigurator> EP_NAME =
      ExtensionPointName.create("com.intellij.protobuf.projectSettingsConfigurator");

  /**
   * Possibly configure the provided {@link PbProjectSettings} object for the given project.
   *
   * <p>If this {@link ProjectSettingsConfigurator} is able to determine configuration for the
   * project, it should modify <code>settings</code> and return it. Else, if no configuration is
   * possible, it should return <code>null</code>. <code>settings</code> is guaranteed to be a copy,
   * and can be modified even if the return value is <code>null</code>.
   *
   * @param project the project
   * @param settings the initial settings
   * @return updated settings, or <code>null</code>
   */
  @Nullable
  PbProjectSettings configure(Project project, PbProjectSettings settings);

  /**
   * Returns a collection of descriptor file paths that should be suggested to the user in the
   * settings dialog.
   *
   * @param project the project
   * @return a collection of descriptor suggestions, possibly empty.
   */
  @NotNull
  default Collection<String> getDescriptorPathSuggestions(Project project) {
    return Collections.emptyList();
  }
}
