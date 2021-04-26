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
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.protobuf.ide.highlighter.PbSyntaxHighlighter;
import com.intellij.protobuf.lang.PbLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PbCodeStyleSettingsProvider extends CodeStyleSettingsProvider {

  private static final String DISPLAY_NAME = "Protocol Buffer";

  @Override
  public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
    return new PbCodeStyleSettings(settings);
  }

  @Override
  public String getConfigurableDisplayName() {
    return DISPLAY_NAME;
  }

  @NotNull
  @Override
  public CodeStyleConfigurable createConfigurable(
      @NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings originalSettings) {
    return new CodeStyleAbstractConfigurable(settings, originalSettings, DISPLAY_NAME) {
      @Override
      protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
        return new ProtoCodeStyleMainPanel(getCurrentSettings(), settings);
      }

      @Nullable
      @Override
      public String getHelpTopic() {
        return null;
      }
    };
  }

  private static class ProtoCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
    ProtoCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
      super(PbLanguage.INSTANCE, currentSettings, settings);
    }

    @Override
    protected EditorHighlighter createHighlighter(EditorColorsScheme scheme) {
      return EditorHighlighterFactory.getInstance()
          .createEditorHighlighter(new PbSyntaxHighlighter(true), scheme);
    }
  }
}
