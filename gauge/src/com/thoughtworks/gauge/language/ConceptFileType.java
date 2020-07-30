/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.language;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.thoughtworks.gauge.Constants;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.idea.icon.GaugeIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class ConceptFileType extends LanguageFileType {
  public static final FileType INSTANCE = new ConceptFileType();

  public ConceptFileType() {
    super(Concept.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Concept";
  }

  @NotNull
  @Override
  public String getDescription() {
    return GaugeBundle.message("gauge.concept");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return Constants.CONCEPT_EXTENSION;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return GaugeIcon.GAUGE_CONCEPT_FILE_ICON;
  }
}
