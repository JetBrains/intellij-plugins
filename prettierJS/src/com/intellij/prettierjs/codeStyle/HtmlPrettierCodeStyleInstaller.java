// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.codeStyle;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.prettierjs.PrettierConfig;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettings.QuoteStyle;
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller.applyCommonPrettierSettings;
import static com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller.commonPrettierSettingsApplied;

public class HtmlPrettierCodeStyleInstaller implements PrettierCodeStyleInstaller {
  @Override
  public void install(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings) {
    applyCommonPrettierSettings(config, settings, HTMLLanguage.INSTANCE);

    HtmlCodeStyleSettings htmlSettings = settings.getCustomSettings(HtmlCodeStyleSettings.class);
    // Default prettier settings
    htmlSettings.HTML_SPACE_INSIDE_EMPTY_TAG = true;
    htmlSettings.HTML_QUOTE_STYLE = config.singleQuote ? QuoteStyle.Single : QuoteStyle.Double;
    htmlSettings.HTML_ENFORCE_QUOTES = true;
  }

  @Override
  public boolean isInstalled(@NotNull Project project, @NotNull PrettierConfig config, @NotNull CodeStyleSettings settings) {
    HtmlCodeStyleSettings htmlSettings = settings.getCustomSettings(HtmlCodeStyleSettings.class);
    return htmlSettings.HTML_SPACE_INSIDE_EMPTY_TAG
           && htmlSettings.HTML_QUOTE_STYLE == (config.singleQuote ? QuoteStyle.Single : QuoteStyle.Double)
           && htmlSettings.HTML_ENFORCE_QUOTES
           && commonPrettierSettingsApplied(config, settings, HTMLLanguage.INSTANCE);
  }
}
