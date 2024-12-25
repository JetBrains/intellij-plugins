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
package com.intellij.protobuf.ide.style;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.protobuf.ide.PbIdeBundle;
import com.intellij.protobuf.lang.PbTextLanguage;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** {@link CodeStyleSettingsProvider} for use with prototext files. */
public class PbTextCodeStyleSettingsProvider extends CodeStyleSettingsProvider {

  @Override
  public CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
    return new PbTextCodeStyleSettings(settings);
  }

  @Override
  public String getConfigurableDisplayName() {
    return PbIdeBundle.message("prototext.name");
  }

  @Override
  public @NotNull CodeStyleConfigurable createConfigurable(
      @NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings originalSettings) {
    return new CodeStyleAbstractConfigurable(settings, originalSettings, PbIdeBundle.message("prototext.name")) {
      @Override
      protected @NotNull CodeStyleAbstractPanel createPanel(@NotNull CodeStyleSettings settings) {
        return new ProtoTextCodeStyleMainPanel(getCurrentSettings(), settings);
      }

      @Override
      public @Nullable String getHelpTopic() {
        return null;
      }
    };
  }

  private static class ProtoTextCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
    ProtoTextCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
      super(PbTextLanguage.INSTANCE, currentSettings, settings);
    }
  }
}
