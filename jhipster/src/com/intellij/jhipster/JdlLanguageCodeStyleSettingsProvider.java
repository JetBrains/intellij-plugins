// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.jhipster.JdlCodeStyleSettings.PropertyAlignment;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions.getInstance;

final class JdlLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

  private static final String[] ALIGN_OPTIONS = Arrays.stream(PropertyAlignment.values())
    .map(PropertyAlignment::getDescription)
    .toArray(String[]::new);

  private static final int[] ALIGN_VALUES = ArrayUtil.toIntArray(
    ContainerUtil.map(PropertyAlignment.values(), PropertyAlignment::getId));

  @org.intellij.lang.annotations.Language("JDL")
  @Override
  public @NotNull String getCodeSample(@NotNull SettingsType settingsType) {
    return """
      application {
        config {
          baseName cars
          serverPort 8080
          languages [en, fr]
        }
        entities Car
        dto * with mapstruct
      }

      // The comment
      entity Car {
        departmentName String required
        handle String required
      }

      deployment {
        deploymentType docker-compose
        dockerRepositoryName "YourDockerLoginName"
      }""";
  }

  @Override
  public IndentOptionsEditor getIndentOptionsEditor() {
    return new SmartIndentOptionsEditor(this);
  }

  @Override
  public @NotNull Language getLanguage() {
    return JdlLanguage.INSTANCE;
  }

  @Override
  protected void customizeDefaults(@NotNull CommonCodeStyleSettings commonSettings,
                                   CommonCodeStyleSettings.@NotNull IndentOptions indentOptions) {
    super.customizeDefaults(commonSettings, indentOptions);

    indentOptions.INDENT_SIZE = 2;
  }

  @Override
  public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      consumer.showStandardOptions(
        "SPACE_WITHIN_BRACKETS",
        "SPACE_WITHIN_BRACES",
        "SPACE_AFTER_COMMA",
        "SPACE_BEFORE_COMMA"
      );
      consumer.renameStandardOption("SPACE_WITHIN_BRACES", JdlBundle.message("formatter.space_within_braces.label"));
      consumer.showCustomOption(JdlCodeStyleSettings.class, "SPACE_BEFORE_LBRACE",
                                JdlBundle.message("formatter.space_before_brace.label"), getInstance().SPACES_OTHER);
    }
    else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
      consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
    }
    else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
      consumer.showStandardOptions(
        "RIGHT_MARGIN",
        "WRAP_ON_TYPING",
        "KEEP_LINE_BREAKS",
        "WRAP_LONG_LINES"
      );

      consumer.showCustomOption(JdlCodeStyleSettings.class,
                                "PROPERTY_ALIGNMENT",
                                JdlBundle.message("formatter.align.properties.caption"),
                                JdlBundle.message("formatter.objects.label"),
                                ALIGN_OPTIONS,
                                ALIGN_VALUES);

      consumer.showCustomOption(JdlCodeStyleSettings.class,
                                "FIELD_ALIGNMENT",
                                JdlBundle.message("formatter.align.entity.fields.caption"),
                                JdlBundle.message("formatter.objects.label"),
                                ALIGN_OPTIONS,
                                ALIGN_VALUES);
    }
  }
}
