// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.javascript.formatter.JSCodeStyleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrettierConfig {
  public static final PrettierConfig DEFAULT = new PrettierConfig();
  public final boolean jsxBracketSameLine;
  public final boolean bracketSpacing;
  public final int printWidth;
  public final boolean semi;
  public final boolean singleQuote;
  public final int tabWidth;
  public final PrettierUtil.TrailingCommaOption trailingComma;
  public final boolean useTabs;
  @Nullable
  public final String lineSeparator;
  public final boolean vueIndentScriptAndStyle;

  private PrettierConfig() {
    this(null, null, null, null, null, null, null, null, null, null);
  }

  public PrettierConfig(@Nullable Boolean jsxBracketSameLine,
                        @Nullable Boolean bracketSpacing,
                        @Nullable Integer printWidth,
                        @Nullable Boolean semi,
                        @Nullable Boolean singleQuote,
                        @Nullable Integer tabWidth,
                        @Nullable PrettierUtil.TrailingCommaOption trailingComma,
                        @Nullable Boolean useTabs,
                        @Nullable String lineSeparator,
                        @Nullable Boolean vueIndentScriptAndStyle) {
    this.jsxBracketSameLine = ObjectUtils.coalesce(jsxBracketSameLine, false);
    this.bracketSpacing = ObjectUtils.coalesce(bracketSpacing, true);
    this.printWidth = ObjectUtils.coalesce(printWidth, 80);
    this.semi = ObjectUtils.coalesce(semi, true);
    this.singleQuote = ObjectUtils.coalesce(singleQuote, false);
    this.tabWidth = ObjectUtils.coalesce(tabWidth, 2);
    this.trailingComma = ObjectUtils.coalesce(trailingComma, PrettierUtil.TrailingCommaOption.none);
    this.useTabs = ObjectUtils.coalesce(useTabs, false);
    this.lineSeparator = lineSeparator;
    this.vueIndentScriptAndStyle = ObjectUtils.coalesce(vueIndentScriptAndStyle, false);
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
}
