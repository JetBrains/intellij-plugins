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

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.GaugeBundle;
import icons.GaugeIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class SpecFileType extends LanguageFileType {
  public static final SpecFileType INSTANCE = new SpecFileType();

  private SpecFileType() {
    super(Specification.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Specification";
  }

  @NotNull
  @Override
  public String getDescription() {
    return GaugeBundle.message("filetype.gauge.specification.description");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return GaugeConstants.SPEC_EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return GaugeIcons.Gauge;
  }
}
