// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.javascript.formatter.JSCodeStyleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.util.LineSeparator;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PrettierConfig {

  private static final String BRACKET_SPACING = "bracketSpacing";
  private static final String PRINT_WIDTH = "printWidth";
  private static final String SEMI = "semi";
  private static final String SINGLE_QUOTE = "singleQuote";
  private static final String TAB_WIDTH = "tabWidth";
  private static final String TRAILING_COMMA = "trailingComma";
  private static final String USE_TABS = "useTabs";
  private static final String END_OF_LINE = "endOfLine";
  private static final String JSX_BRACKET_SAME_LINE = "jsxBracketSameLine";
  private static final String VUE_INDENT_SCRIPT_AND_STYLE = "vueIndentScriptAndStyle";

  public static PrettierConfig createFromMap(@Nullable Map<String, Object> map) {
    return DEFAULT.mergeWith(map);
  }

  public static final PrettierConfig DEFAULT = new PrettierConfig(
    false, true, 80,
    true, false, 2, TrailingCommaOption.none,
    false, null, false);

  public final boolean jsxBracketSameLine;
  public final boolean bracketSpacing;
  public final int printWidth;
  public final boolean semi;
  public final boolean singleQuote;
  public final int tabWidth;
  public final TrailingCommaOption trailingComma;
  public final boolean useTabs;
  @Nullable
  public final String lineSeparator;
  public final boolean vueIndentScriptAndStyle;

  public PrettierConfig(boolean jsxBracketSameLine,
                        boolean bracketSpacing,
                        int printWidth,
                        boolean semi,
                        boolean singleQuote,
                        int tabWidth,
                        @NotNull PrettierConfig.TrailingCommaOption trailingComma,
                        boolean useTabs,
                        @Nullable String lineSeparator,
                        boolean vueIndentScriptAndStyle) {
    this.jsxBracketSameLine = jsxBracketSameLine;
    this.bracketSpacing = bracketSpacing;
    this.printWidth = printWidth;
    this.semi = semi;
    this.singleQuote = singleQuote;
    this.tabWidth = tabWidth;
    this.trailingComma = trailingComma;
    this.useTabs = useTabs;
    this.lineSeparator = lineSeparator;
    this.vueIndentScriptAndStyle = vueIndentScriptAndStyle;
  }

  public void install(@NotNull Project project) {
    JSCodeStyleUtil.updateProjectCodeStyle(project, newSettings -> {
      newSettings.LINE_SEPARATOR = this.lineSeparator;
      PrettierCodeStyleInstaller.EP_NAME.extensions().forEach(installer -> installer.install(project, this, newSettings));
    });
  }

  public boolean isInstalled(@NotNull Project project) {
    CodeStyleSettings settings = CodeStyle.getSettings(project);
    return StringUtil.equals(settings.LINE_SEPARATOR, this.lineSeparator)
           && PrettierCodeStyleInstaller.EP_NAME.extensions().allMatch(installer -> installer.isInstalled(project, this, settings));
  }

  public PrettierConfig mergeWith(@Nullable Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
      return this;
    }
    return new PrettierConfig(
      ObjectUtils.coalesce(getBooleanValue(map, JSX_BRACKET_SAME_LINE), jsxBracketSameLine),
      ObjectUtils.coalesce(getBooleanValue(map, BRACKET_SPACING), bracketSpacing),
      ObjectUtils.coalesce(getIntValue(map, PRINT_WIDTH), printWidth),
      ObjectUtils.coalesce(getBooleanValue(map, SEMI), semi),
      ObjectUtils.coalesce(getBooleanValue(map, SINGLE_QUOTE), singleQuote),
      ObjectUtils.coalesce(getIntValue(map, TAB_WIDTH), tabWidth),
      ObjectUtils.coalesce(parseTrailingCommaValue(ObjectUtils.tryCast(map.get(TRAILING_COMMA), String.class)), trailingComma),
      ObjectUtils.coalesce(getBooleanValue(map, USE_TABS), useTabs),
      ObjectUtils.coalesce(parseLineSeparatorValue(ObjectUtils.tryCast(map.get(END_OF_LINE), String.class)), lineSeparator),
      ObjectUtils.coalesce(getBooleanValue(map, VUE_INDENT_SCRIPT_AND_STYLE), vueIndentScriptAndStyle)
    );
  }

  @Override
  public String toString() {
    return "Config{" +
           "jsxBracketSameLine=" + jsxBracketSameLine +
           ", bracketSpacing=" + bracketSpacing +
           ", printWidth=" + printWidth +
           ", semi=" + semi +
           ", singleQuote=" + singleQuote +
           ", tabWidth=" + tabWidth +
           ", trailingComma=" + trailingComma +
           ", useTabs=" + useTabs +
           ", lineSeparator=" + lineSeparator +
           ", vueIndentScriptAndStyle=" + vueIndentScriptAndStyle +
           '}';
  }

  @Nullable
  private static String parseLineSeparatorValue(@Nullable String string) {
    LineSeparator separator = parseLineSeparator(string);
    return separator != null ? separator.getSeparatorString() : null;
  }

  @Nullable
  private static LineSeparator parseLineSeparator(@Nullable String string) {
    if (string == null) {
      return null;
    }
    return StringUtil.parseEnum(StringUtil.toUpperCase(string), null, LineSeparator.class);
  }

  private static Boolean getBooleanValue(@NotNull Map<String, Object> map, String key) {
    Boolean value = ObjectUtils.tryCast(map.get(key), Boolean.class);
    return value == null ? null : value.booleanValue();
  }

  private static Integer getIntValue(@NotNull Map<String, Object> map, String key) {
    Number value = ObjectUtils.tryCast(map.get(key), Number.class);
    return value == null ? null : value.intValue();
  }

  @Nullable
  private static TrailingCommaOption parseTrailingCommaValue(@Nullable String string) {
    return string == null ? null : StringUtil.parseEnum(string, null, TrailingCommaOption.class);
  }

  public enum TrailingCommaOption {
    none,
    all,
    es5
  }
}
