// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.codeStyle.*;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions.getInstance;

public final class ActionScriptLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
  public static final @NlsSafe String CONFIGURABLE_DISPLAY_NAME = "ActionScript";

  public static final String GENERAL_CODE_SAMPLE =
    """
      package {
      class Foo {
      public function foo(x:int, z) {
      var arr = ["zero", "one"];
      var x = {0:"zero", 1:"one"};
      var y = (x ^ 0x123) << 2;
      var k = x > 15 ? 1 : 2;
      do {
      try {
      if (0 < x && x < 10) {
      while (x != y) {
      x = f(x * 3 + 5);
      }
      z += 2;
      } else if (x > 20) {
      z = x << 1;
      } else {
      z = x | 2;
      }
      switch (k) {
      case 0:
      var s1 = 'zero';
      break;
      case 2:
      var s1 = 'two';
      break;
      default:
      var s1 = 'other';
      }
      } catch (e:Exception) {
      var message = arr[0];
      }
      } while (x < 0);
      }

          function sum(...args) {
              for (var i:uint = 0; i < args.length; i++) {
                  trace(args[i]);
              }
          }
      }
      }""";

  @Override
  public IndentOptionsEditor getIndentOptionsEditor() {
    return new ActionScriptIndentOptionsEditor();
  }
  public static final String WRAPPING_CODE_SAMPLE =
    """
      function buzz() { return 0; }

      class Foo {

      numbers : ['one', 'two', 'three', 'four', 'five', 'six'];

      // function fBar (x,y);
      function fOne(argA, argB, argC, argD, argE, argF, argG, argH) {
      x = argA + argB + argC + argD + argE + argF + argG + argH;
      this.fTwo(argA, argB, argC, this.fThree(argD, argE, argF, argG, argH));
      var z = argA == 'Some string' ? 'yes' : 'no';
      var colors = ['red', 'green', 'blue', 'black', 'white', 'gray'];
      for (var colorIndex = 0; colorIndex < colors.length; colorIndex++)\s
      var colorString = this.numbers[colorIndex];
      }

      function fTwo(strA, strB, strC, strD) {
      if (true)
      return strC;
      if (strA == 'one' ||\s
      strB == 'two' || strC == 'three') {
      return strA + strB + strC;
      } else return strD
      return strD;
      }

      function fThree(strA, strB, strC, strD, strE) {
      return strA + strB + strC + strD + strE;
      }
      /*
       Multiline
         C-style
           Comment
       */
      function fFour() {
          var myLinkText = "Button",
                  local = true,
                  initial = -1;
          var cssClasses = ["bold", "red",]
          var selector = "#id";

          var color = "red";
          var offset = 10;
          }}""";
  public static final String BLANK_LINE_CODE_SAMPLE =
    """
      package {
      import com.jetbrains.flex.Demo;
      import com.jetbrains.flex.Sample;
      class Foo {
          var demo : Demo;
          var sample : Sample;
          public function foo(x:int, z) {
              var y = x * z;


              return y;
          }
          public function getSample() {
              return sample;
          }
      }

      }""";
  public static final String INDENT_CODE_SAMPLE =
    """
      package {
      class Foo {
          public function foo(x:int, z) {
              var y = x * z;


              return y;
          }
      }
      }""";

  @Override
  public String getCodeSample(@NotNull SettingsType settingsType) {
    return switch (settingsType) {
      case WRAPPING_AND_BRACES_SETTINGS -> WRAPPING_CODE_SAMPLE;
      case BLANK_LINES_SETTINGS -> BLANK_LINE_CODE_SAMPLE;
      case INDENT_SETTINGS -> INDENT_CODE_SAMPLE;
      default -> GENERAL_CODE_SAMPLE;
    };
  }

  @Override
  public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
    if (settingsType == SettingsType.SPACING_SETTINGS) {
      consumer.showStandardOptions(JSLanguageCodeStyleSettingsProvider.STANDARD_SPACING_OPTIONS);
      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_BEFORE_PROPERTY_COLON",
                          JavaScriptBundle.message("space.before.name.value.separator"),
                          getInstance().SPACES_OTHER);
      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_AFTER_PROPERTY_COLON",
                          JavaScriptBundle.message("space.after.name.value.separator"),
                          getInstance().SPACES_OTHER);
      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_AFTER_DOTS_IN_REST_PARAMETER",
                          JavaScriptBundle.message("actionscript.space.after.dots.in.rest.parameter"),
                          getInstance().SPACES_OTHER);
      consumer.showCustomOption(JSCodeStyleSettings.class, "SPACE_BEFORE_FUNCTION_LEFT_PARENTH",
                                JavaScriptBundle.message("space.before.function.left.parenth"),
                                getInstance().SPACES_BEFORE_PARENTHESES);
      consumer.showCustomOption(ECMA4CodeStyleSettings.class,
                                "SPACE_WITHIN_ARRAY_INITIALIZER_BRACKETS",
                                JavaScriptBundle.message("spaces.within.array.initializer"),
                                getInstance().SPACES_WITHIN);
      consumer.renameStandardOption("SPACE_WITHIN_BRACKETS", JavaScriptBundle.message("spaces.within.indexer.brackets"));

      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_BEFORE_TYPE_COLON",
                          JavaScriptBundle.message("space.before.type.colon"),
                          getInstance().SPACES_OTHER);
      consumer
        .showCustomOption(ECMA4CodeStyleSettings.class, "SPACE_AFTER_TYPE_COLON",
                          JavaScriptBundle.message("space.after.type.colon"),
                          getInstance().SPACES_OTHER);

      consumer.showCustomOption(ECMA4CodeStyleSettings.class,
                                "SPACES_WITHIN_OBJECT_LITERAL_BRACES",
                                JavaScriptBundle.message("spaces.within.object.literal.braces"),
                                getInstance().SPACES_WITHIN);
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
                                JavaScriptBundle.message("js.blank.lines.around.function"), getInstance().BLANK_LINES);
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
                                getInstance().WRAPPING_BRACES,
                                CodeStyleSettingsCustomizable.OptionAnchor.AFTER,
                                "METHOD_BRACE_STYLE",
                                getInstance().BRACE_PLACEMENT_OPTIONS,
                                CodeStyleSettingsCustomizable.BRACE_PLACEMENT_VALUES);

      consumer.showCustomOption(ECMA4CodeStyleSettings.class, "REFORMAT_C_STYLE_COMMENTS",
                                JavaScriptBundle.message("js.format.cstyle.comments"),
                                getInstance().WRAPPING_COMMENTS);

      //object literals
      consumer.showCustomOption(ECMA4CodeStyleSettings.class,
                                "OBJECT_LITERAL_WRAP",
                                "Object literals",
                                null,
                                getInstance().WRAP_OPTIONS,
                                CodeStyleSettingsCustomizable.WRAP_VALUES);
      consumer.showCustomOption(ECMA4CodeStyleSettings.class, "ALIGN_OBJECT_PROPERTIES",
                                JavaScriptBundle.message("js.code.style.align.caption"),
                                JavaScriptBundle.message("js.code.style.object.literals.category.name"),
                                JSLanguageCodeStyleSettingsProvider.getAlignObjectPropertiesOptions(),
                                JSLanguageCodeStyleSettingsProvider.ALIGN_OBJECT_PROPERTIES_VALUES);

      //var statements
      consumer.showCustomOption(ECMA4CodeStyleSettings.class,
                                "VAR_DECLARATION_WRAP",
                                JavaScriptBundle.message("js.wrap.settings.var.group.name"),
                                null,
                                getInstance().WRAP_OPTIONS,
                                CodeStyleSettingsCustomizable.WRAP_VALUES);
      consumer.showCustomOption(ECMA4CodeStyleSettings.class, "ALIGN_VAR_STATEMENTS",
                                JavaScriptBundle.message("js.code.style.align.caption"),
                                JavaScriptBundle.message("js.wrap.settings.var.group.name"),
                                JSLanguageCodeStyleSettingsProvider.getAlignVarStatementOptions(),
                                JSLanguageCodeStyleSettingsProvider.ALIGN_VAR_STATEMENT_VALUES);
    }
    else if (settingsType == SettingsType.LANGUAGE_SPECIFIC) {
      consumer.showStandardOptions("LINE_COMMENT_AT_FIRST_COLUMN");
    }
  }
  private String getPreviewText() {
    return """
      /*
       Multiline
       C-style
       Comment
       */
      var myLink = {
            img: "btn.gif",
            text: "Button",
            width: 128
          },
          local = true,
          initial = -1;
      var cssClasses = ["bold", "red",]
      var selector = "#id";

      var color = "red";
      var offset = 10;

      varName = val;""";
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

  @Override
  public @NotNull Language getLanguage() {
    return FlexSupportLoader.ECMA_SCRIPT_L4;
  }

  @Override
  public @NotNull CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
    return new ActionScriptCodeStyleSettingsConfigurable(settings, modelSettings);
  }

  @Override
  public @Nullable CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
    return new ECMA4CodeStyleSettings(settings);
  }

  @Override
  public @Nullable String getConfigurableDisplayName() {
    return CONFIGURABLE_DISPLAY_NAME;
  }

  @Override
  public @NotNull String getExternalLanguageId() {
    return "actionscript";
  }
}
