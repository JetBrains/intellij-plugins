package org.intellij.plugins.postcss.settings;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
  @NotNull
  @Override
  public Language getLanguage() {
    return PostCssLanguage.INSTANCE;
  }

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    return "h1 {\n" +
           "  color: white;\n" +
           "}\n" +
           "h2 {\n" +
           "  color: white;\n" +
           "  @nest h3 & {\n" +
           "    color: green;\n" +
           "  }\n" +
           "  & h3 {\n" +
           "    color: white;\n" +
           "  }\n" +
           "  @media print {\n" +
           "    @page {\n" +
           "      background-color: #fff;\n" +
           "      color: #000;\n" +
           "    }\n" +
           "    .foo {\n" +
           "      font-family: \"Georgia\", \"Tahoma\", serif;\n" +
           "    }\n" +
           "  }\n" +
           "}\n";
  }

  @Override
  public CommonCodeStyleSettings getDefaultCommonSettings() {
    CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(PostCssLanguage.INSTANCE);
    defaultSettings.LINE_COMMENT_AT_FIRST_COLUMN = false;
    defaultSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false;
    CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();
    indentOptions.INDENT_SIZE = 2;
    return defaultSettings;
  }

  @Override
  public IndentOptionsEditor getIndentOptionsEditor() {
    return new SmartIndentOptionsEditor();
  }
}