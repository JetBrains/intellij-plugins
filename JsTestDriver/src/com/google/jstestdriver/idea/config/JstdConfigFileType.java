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
package com.google.jstestdriver.idea.config;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import icons.JsTestDriverIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JstdConfigFileType extends LanguageFileType {

  public static final JstdConfigFileType INSTANCE = new JstdConfigFileType();

  /**
   * Creates a language file type for the specified language.
   */
  private JstdConfigFileType() {
    super(findLanguage());
  }

  @NotNull
  private static Language findLanguage() {
    Language language = Language.findLanguageByID("yaml");
    if (language == null) {
      language = PlainTextLanguage.INSTANCE;
    }
    return language;
  }

  @NotNull
  @Override
  public String getName() {
    return "JsTestDriver";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "JsTestDriver configuration";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "jstd";
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return JsTestDriverIcons.JsTestDriver;
  }

}
