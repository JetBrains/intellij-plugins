// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.psi.codeStyle.*;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rustam Vishnyakov
 */
public class ActionScriptLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
  @NotNull
  @Override
  public Language getLanguage() {
    return JavaScriptSupportLoader.ECMA_SCRIPT_L4;
  }

  @Override
  public IndentOptionsEditor getIndentOptionsEditor() {
    return new ActionScriptIndentOptionsEditor();
  }

  @NotNull
  @Override
  public CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
    return new ActionScriptCodeStyleSettingsConfigurable(settings, modelSettings);
  }

  @Nullable
  @Override
  public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
    return new ECMA4CodeStyleSettings(settings);
  }

  @Nullable
  @Override
  public String getConfigurableDisplayName() {
    return "ActionScript";
  }

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    switch (settingsType) {
      case WRAPPING_AND_BRACES_SETTINGS:
        return WRAPPING_CODE_SAMPLE;
      case BLANK_LINES_SETTINGS:
        return BLANK_LINE_CODE_SAMPLE;
      case SPACING_SETTINGS:
        return GENERAL_CODE_SAMPLE;
      case LANGUAGE_SPECIFIC:
        return GENERAL_CODE_SAMPLE;
      case INDENT_SETTINGS:
        return INDENT_CODE_SAMPLE;
    }
    return GENERAL_CODE_SAMPLE;
  }

  @Override
  public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      consumer.showStandardOptions(JSLanguageCodeStyleSettingsProvider.STANDARD_SPACING_OPTIONS);
      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_BEFORE_PROPERTY_COLON",
                          JavaScriptBundle.message("space.before.name.value.separator"),
                          CodeStyleSettingsCustomizable.SPACES_OTHER);
      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_AFTER_PROPERTY_COLON",
                          JavaScriptBundle.message("space.after.name.value.separator"),
                          CodeStyleSettingsCustomizable.SPACES_OTHER);
      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_AFTER_DOTS_IN_REST_PARAMETER",
                          JavaScriptBundle.message("actionscript.space.after.dots.in.rest.parameter"),
                          CodeStyleSettingsCustomizable.SPACES_OTHER);
      consumer.showCustomOption(JSCodeStyleSettings.class, "SPACE_BEFORE_FUNCTION_LEFT_PARENTH",
                                JavaScriptBundle.message("space.before.function.left.parenth"),
                                CodeStyleSettingsCustomizable.SPACES_BEFORE_PARENTHESES);
      consumer.showCustomOption(ECMA4CodeStyleSettings.class,
                                "SPACE_WITHIN_ARRAY_INITIALIZER_BRACKETS",
                                JavaScriptBundle.message("spaces.within.array.initializer"),
                                CodeStyleSettingsCustomizable.SPACES_WITHIN);
      consumer.renameStandardOption("SPACE_WITHIN_BRACKETS", JavaScriptBundle.message("spaces.within.indexer.brackets"));

      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_BEFORE_TYPE_COLON",
                          JavaScriptBundle.message("space.before.type.colon"),
                          CodeStyleSettingsCustomizable.SPACES_OTHER);
      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_AFTER_TYPE_COLON",
                          JavaScriptBundle.message("space.after.type.colon"),
                          CodeStyleSettingsCustomizable.SPACES_OTHER);

      consumer.showCustomOption(ECMA4CodeStyleSettings.class,
                                "SPACES_WITHIN_OBJECT_LITERAL_BRACES",
                                JavaScriptBundle.message("spaces.within.object.literal.braces"),
                                CodeStyleSettingsCustomizable.SPACES_WITHIN);
    }
    else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
      String[] blankLinesOptions = new String[]{"KEEP_BLANK_LINES_IN_CODE",
        "BLANK_LINES_AFTER_IMPORTS",
        "BLANK_LINES_BEFORE_IMPORTS",
        "BLANK_LINES_AROUND_METHOD",
        "KEEP_BLANK_LINES_IN_CODE",
        "BLANK_LINES_BEFORE_PACKAGE",
        "BLANK_LINES_AFTER_PACKAGE"};
      consumer.showStandardOptions(blankLinesOptions);
      consumer.showCustomOption(ECMA4CodeStyleSettings.class, "BLANK_LINES_AROUND_FUNCTION",
                                JavaScriptBundle.message("js.blank.lines.around.function"), CodeStyleSettingsCustomizable.BLANK_LINES);
    }
    else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
      List<String> wrappingOptions = new ArrayList<>();
      wrappingOptions.addAll(Arrays.asList(JSLanguageCodeStyleSettingsProvider.STANDARD_WRAPPING_OPTIONS));
      wrappingOptions.addAll(Arrays.asList("CLASS_BRACE_STYLE",
                                           "EXTENDS_LIST_WRAP",
                                           "ALIGN_MULTILINE_EXTENDS_LIST",
                                           "EXTENDS_KEYWORD_WRAP"));
      consumer.showStandardOptions(ArrayUtilRt.toStringArray(wrappingOptions));
      consumer.renameStandardOption("ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE", JavaScriptBundle.message("js.array.new.line.after.left.bracket"));
      consumer.renameStandardOption("ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE", JavaScriptBundle
        .message("js.array.new.line.before.right.bracket"));
      consumer.showCustomOption(ECMA4CodeStyleSettings.class, "FUNCTION_EXPRESSION_BRACE_STYLE",
                                JavaScriptBundle.message("js.function.expression.brace.style"),
                                CodeStyleSettingsCustomizable.WRAPPING_BRACES,
                                CodeStyleSettingsCustomizable.OptionAnchor.AFTER,
                                "METHOD_BRACE_STYLE",
                                CodeStyleSettingsCustomizable.BRACE_PLACEMENT_OPTIONS,
                                CodeStyleSettingsCustomizable.BRACE_PLACEMENT_VALUES);

      consumer.showCustomOption(ECMA4CodeStyleSettings.class, "REFORMAT_C_STYLE_COMMENTS",
                                JavaScriptBundle.message("js.format.cstyle.comments"),
                                CodeStyleSettingsCustomizable.WRAPPING_COMMENTS);

      //object literals
      consumer.showCustomOption(ECMA4CodeStyleSettings.class,
                                "OBJECT_LITERAL_WRAP",
                                "Object literals",
                                null,
                                CodeStyleSettingsCustomizable.WRAP_OPTIONS,
                                CodeStyleSettingsCustomizable.WRAP_VALUES);
      consumer.showCustomOption(ECMA4CodeStyleSettings.class, "ALIGN_OBJECT_PROPERTIES",
                                JavaScriptBundle.message("js.code.style.align.caption"),
                                JavaScriptBundle.message("js.code.style.object.literals.category.name"),
                                JSLanguageCodeStyleSettingsProvider.ALIGN_OBJECT_PROPERTIES_OPTIONS,
                                JSLanguageCodeStyleSettingsProvider.ALIGN_OBJECT_PROPERTIES_VALUES);

      //var statements
      consumer.showCustomOption(ECMA4CodeStyleSettings.class,
                                "VAR_DECLARATION_WRAP",
                                JavaScriptBundle.message("js.wrap.settings.var.group.name"),
                                null,
                                CodeStyleSettingsCustomizable.WRAP_OPTIONS,
                                CodeStyleSettingsCustomizable.WRAP_VALUES);
      consumer.showCustomOption(ECMA4CodeStyleSettings.class, "ALIGN_VAR_STATEMENTS",
                                JavaScriptBundle.message("js.code.style.align.caption"),
                                JavaScriptBundle.message("js.wrap.settings.var.group.name"),
                                JSLanguageCodeStyleSettingsProvider.ALIGN_VAR_STATEMENT_OPTIONS,
                                JSLanguageCodeStyleSettingsProvider.ALIGN_VAR_STATEMENT_VALUES);
    }
    else if (settingsType == SettingsType.LANGUAGE_SPECIFIC) {
      consumer.showStandardOptions("LINE_COMMENT_AT_FIRST_COLUMN");
    }
  }
  protected String getPreviewText() {
    return "/*\n" +
           " Multiline\n" +
           " C-style\n" +
           " Comment\n" +
           " */\n" +
           "var myLink = {\n" +
           "      img: \"btn.gif\",\n" +
           "      text: \"Button\",\n" +
           "      width: 128\n" +
           "    },\n" +
           "    local = true,\n" +
           "    initial = -1;\n" +
           "var cssClasses = [\"bold\", \"red\",]\n" +
           "var selector = \"#id\";\n" +
           "\n" +
           "var color = \"red\";\n" +
           "var offset = 10;\n" +
           "\n" +
           "varName = val;";
  }

  @Override
  public String getFileExt() {
    return "as";
  }

  @Override
  public String getLanguageName() {
    return "ActionScript";
  }

  @Override
  protected void customizeDefaults(@NotNull CommonCodeStyleSettings commonSettings,
                                   @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
    commonSettings.BLANK_LINES_AFTER_PACKAGE = 0;
    commonSettings.initIndentOptions();
  }

  @NotNull
  @Override
  public String getExternalLanguageId() {
    return "actionscript";
  }

  public final static String GENERAL_CODE_SAMPLE =
    "package {\n" +
    "class Foo {\n" +
    "public function foo(x:int, z) {\n" +
    "var arr = [\"zero\", \"one\"];\n" +
    "var x = {0:\"zero\", 1:\"one\"};\n" +
    "var y = (x ^ 0x123) << 2;\n" +
    "var k = x > 15 ? 1 : 2;\n" +
    "do {\n" +
    "try {\n" +
    "if (0 < x && x < 10) {\n" +
    "while (x != y) {\n" +
    "x = f(x * 3 + 5);\n" +
    "}\n" +
    "z += 2;\n" +
    "} else if (x > 20) {\n" +
    "z = x << 1;\n" +
    "} else {\n" +
    "z = x | 2;\n" +
    "}\n" +
    "switch (k) {\n" +
    "case 0:\n" +
    "var s1 = 'zero';\n" +
    "break;\n" +
    "case 2:\n" +
    "var s1 = 'two';\n" +
    "break;\n" +
    "default:\n" +
    "var s1 = 'other';\n" +
    "}\n" +
    "} catch (e:Exception) {\n" +
    "var message = arr[0];\n" +
    "}\n" +
    "} while (x < 0);\n" +
    "}\n" +
    "\n" +
    "    function sum(...args) {\n" +
    "        for (var i:uint = 0; i < args.length; i++) {\n" +
    "            trace(args[i]);\n" +
    "        }\n" +
    "    }" +
    "\n" +
    "}\n" +
    "}";

  public final static String WRAPPING_CODE_SAMPLE =
    "function buzz() { return 0; }\n\n" +
    "class Foo {\n" +
    "\n" +
    "numbers : ['one', 'two', 'three', 'four', 'five', 'six'];\n" +
    "\n" +
    "// function fBar (x,y);\n" +
    "function fOne(argA, argB, argC, argD, argE, argF, argG, argH) {\n" +
    "x = argA + argB + argC + argD + argE + argF + argG + argH;\n" +
    "this.fTwo(argA, argB, argC, this.fThree(argD, argE, argF, argG, argH));\n" +
    "var z = argA == 'Some string' ? 'yes' : 'no';\n" +
    "var colors = ['red', 'green', 'blue', 'black', 'white', 'gray'];\n" +
    "for (var colorIndex = 0; colorIndex < colors.length; colorIndex++) \n" +
    "var colorString = this.numbers[colorIndex];\n" +
    "}\n" +
    "\n" +
    "function fTwo(strA, strB, strC, strD) {\n" +
    "if (true)\nreturn strC;\n" +
    "if (strA == 'one' || \n" +
    "strB == 'two' || strC == 'three') {\n" +
    "return strA + strB + strC;\n" +
    "} else return strD\n" +
    "return strD;\n" +
    "}\n" +
    "\n" +
    "function fThree(strA, strB, strC, strD, strE) {\n" +
    "return strA + strB + strC + strD + strE;\n" +
    "}\n" +
    "/*\n" +
    " Multiline\n" +
    "   C-style\n" +
    "     Comment\n" +
    " */\n" +
    "function fFour() {\n" +
    "    var myLinkText = \"Button\",\n" +
    "            local = true,\n" +
    "            initial = -1;\n" +
    "    var cssClasses = [\"bold\", \"red\",]\n" +
    "    var selector = \"#id\";\n" +
    "\n" +
    "    var color = \"red\";\n" +
    "    var offset = 10;\n" +
    "    }" +
    "}";

  public final static String BLANK_LINE_CODE_SAMPLE =
    "package {\n" +
    "import com.jetbrains.flex.Demo;\n" +
    "import com.jetbrains.flex.Sample;\n" +
    "class Foo {\n" +
    "    var demo : Demo;\n" +
    "    var sample : Sample;\n" +
    "    public function foo(x:int, z) {\n" +
    "        var y = x * z;\n\n\n" +
    "        return y;\n" +
    "    }\n" +
    "    public function getSample() {\n" +
    "        return sample;\n" +
    "    }\n" +
    "}\n" +
    "\n" +
    "}";

  public final static String INDENT_CODE_SAMPLE =
    "package {\n" +
    "class Foo {\n" +
    "    public function foo(x:int, z) {\n" +
    "        var y = x * z;\n\n\n" +
    "        return y;\n" +
    "    }\n" +
    "}\n" +
    "}";
}
