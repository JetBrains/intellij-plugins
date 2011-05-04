package com.intellij.lang.javascript.formatter;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;

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
    }
    return GENERAL_CODE_SAMPLE;
  }

  @Override
  public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      consumer.showStandardOptions("SPACE_AFTER_COLON",
                                   "SPACE_AFTER_COMMA",
                                   "SPACE_AFTER_QUEST",
                                   "SPACE_AROUND_ADDITIVE_OPERATORS",
                                   "SPACE_AROUND_ASSIGNMENT_OPERATORS",
                                   "SPACE_AROUND_BITWISE_OPERATORS",
                                   "SPACE_AROUND_BITWISE_OPERATORS",
                                   "SPACE_AROUND_EQUALITY_OPERATORS",
                                   "SPACE_AROUND_LOGICAL_OPERATORS",
                                   "SPACE_AROUND_MULTIPLICATIVE_OPERATORS",
                                   "SPACE_AROUND_RELATIONAL_OPERATORS",
                                   "SPACE_BEFORE_CATCH_LBRACE",
                                   "SPACE_BEFORE_CLASS_LBRACE",
                                   "SPACE_BEFORE_COLON",
                                   "SPACE_BEFORE_COMMA",
                                   "SPACE_BEFORE_ELSE_LBRACE",
                                   "SPACE_BEFORE_FINALLY_LBRACE",
                                   "SPACE_BEFORE_FOR_LBRACE",
                                   "SPACE_BEFORE_FOR_PARENTHESES",
                                   "SPACE_BEFORE_IF_LBRACE",
                                   "SPACE_BEFORE_IF_PARENTHESES",
                                   "SPACE_BEFORE_METHOD_CALL_PARENTHESES",
                                   "SPACE_BEFORE_METHOD_LBRACE",
                                   "SPACE_BEFORE_METHOD_PARENTHESES",
                                   "SPACE_BEFORE_QUEST",
                                   "SPACE_BEFORE_SEMICOLON",
                                   "SPACE_BEFORE_SWITCH_LBRACE",
                                   "SPACE_BEFORE_SWITCH_PARENTHESES",
                                   "SPACE_BEFORE_TRY_LBRACE",
                                   "SPACE_BEFORE_TYPE",
                                   "SPACE_BEFORE_WHILE_LBRACE",
                                   "SPACE_BEFORE_WHILE_PARENTHESES",
                                   "SPACE_WITHIN_CATCH_PARENTHESES",
                                   "SPACE_WITHIN_FOR_PARENTHESES",
                                   "SPACE_WITHIN_IF_PARENTHESES",
                                   "SPACE_WITHIN_METHOD_CALL_PARENTHESES",
                                   "SPACE_WITHIN_METHOD_PARENTHESES",
                                   "SPACE_WITHIN_SWITCH_PARENTHESES",
                                   "SPACE_WITHIN_WHILE_PARENTHESES");
    }
    else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
      consumer.showStandardOptions("BLANK_LINES_AFTER_IMPORTS",
                                   "BLANK_LINES_BEFORE_IMPORTS",
                                   "BLANK_LINES_AROUND_METHOD",
                                   "KEEP_BLANK_LINES_IN_CODE",
                                   "BLANK_LINES_BEFORE_PACKAGE",
                                   "BLANK_LINES_AFTER_PACKAGE");
    }
    else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
      consumer.showAllStandardOptions();
    }
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
  public CommonCodeStyleSettings getDefaultCommonSettings() {
    CommonCodeStyleSettings commonSettings = new CommonCodeStyleSettings(getLanguage());
    commonSettings.BLANK_LINES_AFTER_PACKAGE = 0;
    return commonSettings;
  }

  public final static String GENERAL_CODE_SAMPLE =
    "package {\n" +
    "class Foo {\n" +
    "public function foo(x:int, z) {\n" +
    "var arr = [\"zero\", \"one\"];\n" +
    "for (var i:int = 0; i < x; i++) {\n" +
    "var y = (x ^ 0x123) << 2;\n" +
    "}\n" +
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
    "}\n" +
    "\n" +
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
}
