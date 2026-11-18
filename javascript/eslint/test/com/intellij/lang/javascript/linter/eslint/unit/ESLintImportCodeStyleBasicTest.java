package com.intellij.lang.javascript.linter.eslint.unit;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.LineSeparator;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.psi.codeStyle.CommonCodeStyleSettings.DO_NOT_WRAP;
import static com.intellij.psi.codeStyle.CommonCodeStyleSettings.WRAP_ALWAYS;

@TestApplication
public class ESLintImportCodeStyleBasicTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @Test
  public void testIndentAppliedWithDefaultValue() {
    doImportTest("{\"rules\": {\"indent\": \"error\"}}",
                 (common, custom) -> {
                   common.getIndentOptions().INDENT_SIZE = 1;
                   common.ALIGN_MULTILINE_PARAMETERS = true;
                   common.INDENT_CASE_FROM_SWITCH = true;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(4, common.getIndentOptions().INDENT_SIZE);
                   Assertions.assertFalse(common.INDENT_CASE_FROM_SWITCH);
                   Assertions.assertFalse(common.ALIGN_MULTILINE_PARAMETERS);
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(1, common.getIndentOptions().INDENT_SIZE);
                   Assertions.assertTrue(common.INDENT_CASE_FROM_SWITCH);
                   Assertions.assertTrue(common.ALIGN_MULTILINE_PARAMETERS);
                 });
  }

  @Test
  public void testIndentAppliedWithDefaultValueInPackageJson() {
    doImportTest("{\"eslintConfig\": {\"rules\": {\"indent\": \"error\"}}}",
                 (common, custom) -> common.getIndentOptions().INDENT_SIZE = 1,
                 (common, custom) -> Assertions.assertEquals(4, common.getIndentOptions().INDENT_SIZE),
                 (common, custom) -> Assertions.assertEquals(1, common.getIndentOptions().INDENT_SIZE),
                 PackageJsonUtil.FILE_NAME);
  }

  @Test
  public void testIndentNotAppliedIfOff() {
    ESLintImportCodeStyleTestUtil.doTestNoDataToImport(codeInsight.get(), "{\"rules\": {\"indent\": \"off\"}}", null);
  }

  @Test
  public void testIndentNotAppliedIfMisconfigured() {
    ESLintImportCodeStyleTestUtil.doTestNoDataToImport(codeInsight.get(), "{\"rules\": {\"indent\": 4}}", null);
  }

  @Test
  public void testIndentNotAppliedIfOptionMisconfigured() {
    ESLintImportCodeStyleTestUtil.doTestNoDataToImport(codeInsight.get(), "{\"rules\": {\"indent\": [\"error\", \"wrong\"]}}", null);
  }

  @Test
  public void testIndentWithIndentOption() {
    doImportTest("{\"rules\": {\"indent\": [\"error\", 5]}}",
                 (common, custom) -> {
                   common.getIndentOptions().INDENT_SIZE = 1;
                   common.getIndentOptions().CONTINUATION_INDENT_SIZE = 3;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(5, common.getIndentOptions().INDENT_SIZE);
                   Assertions.assertEquals(5, common.getIndentOptions().CONTINUATION_INDENT_SIZE);
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(1, common.getIndentOptions().INDENT_SIZE);
                   Assertions.assertEquals(3, common.getIndentOptions().CONTINUATION_INDENT_SIZE);
                 });
  }

  @Test
  public void testIndentWithTabOption() {
    doImportTest("{\"rules\": {\"indent\": [\"error\", \"tab\"]}}",
                 (common, custom) -> common.getIndentOptions().USE_TAB_CHARACTER = false,
                 (common, custom) -> Assertions.assertTrue(common.getIndentOptions().USE_TAB_CHARACTER),
                 (common, custom) -> Assertions.assertFalse(common.getIndentOptions().USE_TAB_CHARACTER));
  }

  @Test
  public void testIndentLegacyWithTabOption() {
    doImportTest("{\"rules\": {\"indent-legacy\": [\"error\", \"tab\"]}}",
                 (common, custom) -> common.getIndentOptions().USE_TAB_CHARACTER = false,
                 (common, custom) -> Assertions.assertTrue(common.getIndentOptions().USE_TAB_CHARACTER),
                 (common, custom) -> Assertions.assertFalse(common.getIndentOptions().USE_TAB_CHARACTER));
  }

  @Test
  public void testQuotes() {
    doImportTest("{\"rules\": {\"quotes\": [\"warn\", \"single\"]}}",
                 (common, custom) -> {
                   custom.USE_DOUBLE_QUOTES = true;
                   custom.FORCE_QUOTE_STYlE = false;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(custom.USE_DOUBLE_QUOTES);
                   Assertions.assertTrue(custom.FORCE_QUOTE_STYlE);
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(custom.USE_DOUBLE_QUOTES);
                   Assertions.assertFalse(custom.FORCE_QUOTE_STYlE);
                 });
  }

  @Test
  public void testSemi() {
    doImportTest("{\"rules\": {\"semi\": [\"warn\", \"always\"]}}",
                 (common, custom) -> {
                   custom.USE_SEMICOLON_AFTER_STATEMENT = false;
                   custom.FORCE_SEMICOLON_STYLE = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(custom.USE_SEMICOLON_AFTER_STATEMENT);
                   Assertions.assertTrue(custom.FORCE_SEMICOLON_STYLE);
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT);
                   Assertions.assertFalse(custom.FORCE_SEMICOLON_STYLE);
                 });
  }

  @Test
  public void testSortImports() {
    doImportTest("{\"rules\": {\"sort-imports\": [\"warn\", {\"ignoreMemberSort\": false}]}}",
                 (common, custom) -> {
                   custom.IMPORT_SORT_MEMBERS = false;
                   custom.IMPORT_SORT_MODULE_NAME = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(custom.IMPORT_SORT_MEMBERS);
                   Assertions.assertTrue(custom.IMPORT_SORT_MODULE_NAME);
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(custom.IMPORT_SORT_MEMBERS);
                   Assertions.assertFalse(custom.IMPORT_SORT_MODULE_NAME);
                 });
  }

  @Test
  public void testArrowSpacing() {
    doImportTest("{\"rules\": {\"arrow-spacing\": [\"error\", { \"before\": true, \"after\": true }]}}",
                 (common, custom) -> custom.SPACE_AROUND_ARROW_FUNCTION_OPERATOR = false,
                 (common, custom) -> Assertions.assertTrue(custom.SPACE_AROUND_ARROW_FUNCTION_OPERATOR),
                 (common, custom) -> Assertions.assertFalse(custom.SPACE_AROUND_ARROW_FUNCTION_OPERATOR));
  }

  @Test
  public void testSpacedComment() {
    doImportTest("{\"rules\": {\"spaced-comment\": [\"error\", \"always\"]}}",
                 (common, custom) -> {
                   common.LINE_COMMENT_ADD_SPACE = false;
                   common.BLOCK_COMMENT_ADD_SPACE = false;
                   common.LINE_COMMENT_AT_FIRST_COLUMN = true;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.LINE_COMMENT_ADD_SPACE);
                   Assertions.assertTrue(common.BLOCK_COMMENT_ADD_SPACE);
                   Assertions.assertFalse(common.LINE_COMMENT_AT_FIRST_COLUMN);
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(common.LINE_COMMENT_ADD_SPACE);
                   Assertions.assertFalse(common.BLOCK_COMMENT_ADD_SPACE);
                   Assertions.assertTrue(common.LINE_COMMENT_AT_FIRST_COLUMN);
                 });
  }

  @Test
  public void testMaxLen() {
    doImportTest("{\"rules\": {\"max-len\": [\"error\", 15]}}",
                 (common, custom) -> {
                   common.RIGHT_MARGIN = 80;
                   common.getIndentOptions().TAB_SIZE = 1;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(15, common.RIGHT_MARGIN);
                   Assertions.assertEquals(4, common.getIndentOptions().TAB_SIZE);
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(80, common.RIGHT_MARGIN);
                   Assertions.assertEquals(1, common.getIndentOptions().TAB_SIZE);
                 });
  }

  @Test
  public void testMaxLenTwoOptions() {
    doImportTest("{\"rules\": {\"max-len\": [\"error\", 15, 5]}}",
                 (common, custom) -> {
                   common.RIGHT_MARGIN = 80;
                   common.getIndentOptions().TAB_SIZE = 1;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(15, common.RIGHT_MARGIN);
                   Assertions.assertEquals(5, common.getIndentOptions().TAB_SIZE);
                 });
  }

  @Test
  public void testMaxLenInObjectOption() {
    doImportTest("{\"rules\": {\"max-len\": [\"error\", {\"code\": 87, \"tabWidth\": 5}]}}",
                 (common, custom) -> {
                   common.RIGHT_MARGIN = 80;
                   common.getIndentOptions().TAB_SIZE = 1;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(87, common.RIGHT_MARGIN);
                   Assertions.assertEquals(5, common.getIndentOptions().TAB_SIZE);
                 });
  }

  @Test
  public void testNoTabs() {
    doImportTest("{\"rules\": {\"no-tabs\": [\"error\"]}}",
                 (common, custom) -> common.getIndentOptions().USE_TAB_CHARACTER = true,
                 (common, custom) -> Assertions.assertFalse(common.getIndentOptions().USE_TAB_CHARACTER));
  }

  @Test
  public void testNoTrailingSpaces() {
    EditorSettingsExternalizable editorSettings = EditorSettingsExternalizable.getInstance();
    String before = editorSettings.getStripTrailingSpaces();
    try {
      editorSettings.setStripTrailingSpaces(EditorSettingsExternalizable.STRIP_TRAILING_SPACES_NONE);
      ESLintImportCodeStyleTestUtil.performImport(codeInsight.get(), "{\"rules\": {\"no-trailing-spaces\": [\"error\"]}}", null);
      Assertions.assertEquals(EditorSettingsExternalizable.STRIP_TRAILING_SPACES_WHOLE, editorSettings.getStripTrailingSpaces());
    }
    finally {
      editorSettings.setStripTrailingSpaces(before);
    }
  }

  @Test
  public void testNoMultipleEmptyLinesWithOption() {
    doImportTest("{\"rules\": {\"no-multiple-empty-lines\": [\"error\", {\"max\": 7}]}}",
                 (common, custom) -> common.KEEP_BLANK_LINES_IN_CODE = 1,
                 (common, custom) -> Assertions.assertEquals(7, common.KEEP_BLANK_LINES_IN_CODE));
  }

  @Test
  public void testNoMultipleEmptyLines() {
    doImportTest("{\"rules\": {\"no-multiple-empty-lines\": [\"error\"]}}",
                 (common, custom) -> common.KEEP_BLANK_LINES_IN_CODE = 1,
                 (common, custom) -> Assertions.assertEquals(2, common.KEEP_BLANK_LINES_IN_CODE));
  }

  @Test
  public void testMultilineTernary() {
    doImportTest("{\"rules\": {\"multiline-ternary\": [\"error\"]}}",
                 (common, custom) -> common.TERNARY_OPERATION_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, common.TERNARY_OPERATION_WRAP));
  }

  @Test
  public void testMultilineTernaryOption() {
    doImportTest("{\"rules\": {\"multiline-ternary\": [\"error\", \"never\"]}}",
                 (common, custom) -> common.TERNARY_OPERATION_WRAP = WRAP_ALWAYS,
                 (common, custom) -> Assertions.assertEquals(DO_NOT_WRAP, common.TERNARY_OPERATION_WRAP));
  }

  @Test
  public void testNewlinePerChainedCall() {
    doImportTest("{\"rules\": {\"newline-per-chained-call\": \"error\"}}",
                 (common, custom) -> common.METHOD_CALL_CHAIN_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, common.METHOD_CALL_CHAIN_WRAP));
  }

  @Test
  public void testLinebreakStyle() {
    doImportTest("{\"rules\": {\"linebreak-style\": \"error\"}}",
                 (common, custom) -> common.getRootSettings().LINE_SEPARATOR = LineSeparator.CRLF.getSeparatorString(),
                 (common, custom) -> Assertions.assertEquals(LineSeparator.LF.getSeparatorString(), common.getRootSettings().LINE_SEPARATOR));
  }

  @Test
  public void testKeywordSpacingWithOverrides() {
    doImportTest("{\"rules\": {\"keyword-spacing\": [\"error\", {" +
                 "\"before\": false, \"overrides\": {" +
                 "\"while\": {\"before\": true, \"after\": false}, \"catch\": {\"after\": false}}}]}}",
                 (common, custom) -> common.SPACE_BEFORE_IF_PARENTHESES = false,
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_BEFORE_IF_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_FOR_PARENTHESES);
                   Assertions.assertFalse(common.SPACE_BEFORE_WHILE_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_SWITCH_PARENTHESES);
                   Assertions.assertFalse(common.SPACE_BEFORE_CATCH_PARENTHESES);

                   Assertions.assertFalse(common.SPACE_BEFORE_ELSE_KEYWORD);
                   Assertions.assertTrue(common.SPACE_BEFORE_WHILE_KEYWORD);
                   Assertions.assertTrue(common.SPACE_BEFORE_CATCH_KEYWORD);
                   Assertions.assertFalse(common.SPACE_BEFORE_FINALLY_KEYWORD);
                 });
  }

  @Test
  public void testKeywordSpacingDefault() {
    doImportTest("{\"rules\": {\"keyword-spacing\": \"error\"}}",
                 (common, custom) -> common.SPACE_BEFORE_IF_PARENTHESES = false,
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_BEFORE_IF_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_FOR_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_WHILE_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_SWITCH_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_CATCH_PARENTHESES);

                   Assertions.assertTrue(common.SPACE_BEFORE_ELSE_KEYWORD);
                   Assertions.assertTrue(common.SPACE_BEFORE_WHILE_KEYWORD);
                   Assertions.assertTrue(common.SPACE_BEFORE_CATCH_KEYWORD);
                   Assertions.assertTrue(common.SPACE_BEFORE_FINALLY_KEYWORD);
                 });
  }

  @Test
  public void testKeywordSpacingSimple() {
    doImportTest("{\"rules\": {\"keyword-spacing\": [\"error\", {\"before\": false}]}}",
                 (common, custom) -> common.SPACE_BEFORE_IF_PARENTHESES = false,
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_BEFORE_IF_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_FOR_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_WHILE_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_SWITCH_PARENTHESES);
                   Assertions.assertTrue(common.SPACE_BEFORE_CATCH_PARENTHESES);

                   Assertions.assertFalse(common.SPACE_BEFORE_ELSE_KEYWORD);
                   Assertions.assertFalse(common.SPACE_BEFORE_WHILE_KEYWORD);
                   Assertions.assertFalse(common.SPACE_BEFORE_CATCH_KEYWORD);
                   Assertions.assertFalse(common.SPACE_BEFORE_FINALLY_KEYWORD);
                 });
  }

  @Test
  public void testCommaDangle() {
    doImportTest("{\"rules\": {\"comma-dangle\": [\"error\", \"always-multiline\"]}}",
                 (common, custom) -> custom.ENFORCE_TRAILING_COMMA = JSCodeStyleSettings.TrailingCommaOption.Keep,
                 (common, custom) -> Assertions
                   .assertEquals(JSCodeStyleSettings.TrailingCommaOption.WhenMultiline, custom.ENFORCE_TRAILING_COMMA));
  }

  @Test
  public void testBraceStyleDefault() {
    doImportTest("{\"rules\": {\"brace-style\": \"error\"}}",
                 (common, custom) -> {
                   common.BRACE_STYLE = CommonCodeStyleSettings.NEXT_LINE;
                   common.ELSE_ON_NEW_LINE = true;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(CommonCodeStyleSettings.END_OF_LINE, common.BRACE_STYLE);
                   Assertions.assertFalse(common.ELSE_ON_NEW_LINE);
                 });
  }

  @Test
  public void testBraceStyle() {
    doImportTest("{\"rules\": {\"brace-style\": [\"error\", \"allman\"]}}",
                 (common, custom) -> {
                   common.BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;
                   common.ELSE_ON_NEW_LINE = false;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(CommonCodeStyleSettings.NEXT_LINE, common.BRACE_STYLE);
                   Assertions.assertTrue(common.ELSE_ON_NEW_LINE);
                 });
  }

  @Test
  public void testBeforeCommaDefault() {
    doImportTest("{\"rules\": {\"comma-spacing\": \"error\"}}",
                 (common, custom) -> {
                   common.SPACE_BEFORE_COMMA = false;
                   common.SPACE_AFTER_COMMA = false;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(common.SPACE_BEFORE_COMMA);
                   Assertions.assertTrue(common.SPACE_AFTER_COMMA);
                 });
  }

  @Test
  public void testBeforeComma() {
    doImportTest("{\"rules\": {\"comma-spacing\": [\"error\", {\"before\": true}]}}",
                 (common, custom) -> {
                   common.SPACE_BEFORE_COMMA = false;
                   common.SPACE_AFTER_COMMA = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_BEFORE_COMMA);
                   Assertions.assertTrue(common.SPACE_AFTER_COMMA);
                 });
  }

  @Test
  public void testEolLast() {
    EditorSettingsExternalizable editorSettings = EditorSettingsExternalizable.getInstance();
    boolean before = editorSettings.isEnsureNewLineAtEOF();
    try {
      editorSettings.setEnsureNewLineAtEOF(true);
      ESLintImportCodeStyleTestUtil.performImport(codeInsight.get(), "{\"rules\": {\"eol-last\": [\"error\", \"never\"]}}", null);
      Assertions.assertFalse(editorSettings.isEnsureNewLineAtEOF());
    }
    finally {
      editorSettings.setEnsureNewLineAtEOF(before);
    }
  }

  @Test
  public void testEolLastDefault() {
    EditorSettingsExternalizable editorSettings = EditorSettingsExternalizable.getInstance();
    boolean before = editorSettings.isEnsureNewLineAtEOF();
    try {
      editorSettings.setEnsureNewLineAtEOF(false);
      ESLintImportCodeStyleTestUtil.performImport(codeInsight.get(), "{\"rules\": {\"eol-last\": \"error\"}}", null);
      Assertions.assertTrue(editorSettings.isEnsureNewLineAtEOF());
    }
    finally {
      editorSettings.setEnsureNewLineAtEOF(before);
    }
  }

  @Test
  public void testCurly() {
    doImportTest("{\"rules\": {\"curly\": [\"error\", \"multi\"]}}",
                 (common, custom) -> {
                   common.IF_BRACE_FORCE = CommonCodeStyleSettings.DO_NOT_FORCE;
                   common.FOR_BRACE_FORCE = CommonCodeStyleSettings.DO_NOT_FORCE;
                   common.WHILE_BRACE_FORCE = CommonCodeStyleSettings.DO_NOT_FORCE;
                   common.DOWHILE_BRACE_FORCE = CommonCodeStyleSettings.DO_NOT_FORCE;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE, common.IF_BRACE_FORCE);
                   Assertions.assertEquals(CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE, common.FOR_BRACE_FORCE);
                   Assertions.assertEquals(CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE, common.WHILE_BRACE_FORCE);
                   Assertions.assertEquals(CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE, common.DOWHILE_BRACE_FORCE);
                 });
  }

  @Test
  public void testCurlyDefault() {
    doImportTest("{\"rules\": {\"curly\": \"error\"}}",
                 (common, custom) -> {
                   common.IF_BRACE_FORCE = CommonCodeStyleSettings.DO_NOT_FORCE;
                   common.FOR_BRACE_FORCE = CommonCodeStyleSettings.DO_NOT_FORCE;
                   common.WHILE_BRACE_FORCE = CommonCodeStyleSettings.DO_NOT_FORCE;
                   common.DOWHILE_BRACE_FORCE = CommonCodeStyleSettings.DO_NOT_FORCE;
                 },
                 (common, custom) -> {
                   Assertions.assertEquals(CommonCodeStyleSettings.FORCE_BRACES_ALWAYS, common.IF_BRACE_FORCE);
                   Assertions.assertEquals(CommonCodeStyleSettings.FORCE_BRACES_ALWAYS, common.FOR_BRACE_FORCE);
                   Assertions.assertEquals(CommonCodeStyleSettings.FORCE_BRACES_ALWAYS, common.WHILE_BRACE_FORCE);
                   Assertions.assertEquals(CommonCodeStyleSettings.FORCE_BRACES_ALWAYS, common.DOWHILE_BRACE_FORCE);
                 });
  }

  @Test
  public void testDotLocationDefault() {
    doImportTest("{\"rules\": {\"dot-location\": \"error\"}}",
                 (common, custom) -> custom.CHAINED_CALL_DOT_ON_NEW_LINE = true,
                 (common, custom) -> Assertions.assertFalse(custom.CHAINED_CALL_DOT_ON_NEW_LINE));
  }

  @Test
  public void testDotLocation() {
    doImportTest("{\"rules\": {\"dot-location\": [\"error\", \"property\"]}}",
                 (common, custom) -> custom.CHAINED_CALL_DOT_ON_NEW_LINE = false,
                 (common, custom) -> Assertions.assertTrue(custom.CHAINED_CALL_DOT_ON_NEW_LINE));
  }

  @Test
  public void testRestSpreadSpacing() {
    doImportTest("{\"rules\": {\"rest-spread-spacing\": [\"error\", \"always\"]}}",
                 (common, custom) -> custom.SPACE_AFTER_DOTS_IN_REST_PARAMETER = false,
                 (common, custom) -> Assertions.assertTrue(custom.SPACE_AFTER_DOTS_IN_REST_PARAMETER));
  }

  @Test
  public void testRestSpreadSpacingDefault() {
    doImportTest("{\"rules\": {\"rest-spread-spacing\": \"error\"}}",
                 (common, custom) -> custom.SPACE_AFTER_DOTS_IN_REST_PARAMETER = true,
                 (common, custom) -> Assertions.assertFalse(custom.SPACE_AFTER_DOTS_IN_REST_PARAMETER));
  }

  @Test
  public void testYieldStarSpacingDefault() {
    doImportTest("{\"rules\": {\"yield-star-spacing\": \"error\"}}",
                 (common, custom) -> {
                   custom.SPACE_BEFORE_GENERATOR_MULT = true;
                   custom.SPACE_AFTER_GENERATOR_MULT = false;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(custom.SPACE_BEFORE_GENERATOR_MULT);
                   Assertions.assertTrue(custom.SPACE_AFTER_GENERATOR_MULT);
                 });
  }

  @Test
  public void testYieldStarSpacingBoth() {
    doImportTest("{\"rules\": {\"yield-star-spacing\": [\"error\", \"both\"]}}",
                 (common, custom) -> {
                   custom.SPACE_BEFORE_GENERATOR_MULT = false;
                   custom.SPACE_AFTER_GENERATOR_MULT = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(custom.SPACE_BEFORE_GENERATOR_MULT);
                   Assertions.assertTrue(custom.SPACE_AFTER_GENERATOR_MULT);
                 });
  }

  @Test
  public void testYieldStarSpacingNeither() {
    doImportTest("{\"rules\": {\"yield-star-spacing\": [\"error\", \"neither\"]}}",
                 (common, custom) -> {
                   custom.SPACE_BEFORE_GENERATOR_MULT = true;
                   custom.SPACE_AFTER_GENERATOR_MULT = true;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(custom.SPACE_BEFORE_GENERATOR_MULT);
                   Assertions.assertFalse(custom.SPACE_AFTER_GENERATOR_MULT);
                 });
  }

  @Test
  public void testYieldStarSpacing() {
    doImportTest("{\"rules\": {\"yield-star-spacing\": [\"error\", {\"before\": true}]}}",
                 (common, custom) -> {
                   custom.SPACE_BEFORE_GENERATOR_MULT = false;
                   custom.SPACE_AFTER_GENERATOR_MULT = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(custom.SPACE_BEFORE_GENERATOR_MULT);
                   Assertions.assertTrue(custom.SPACE_AFTER_GENERATOR_MULT);
                 });
  }

  @Test
  public void testObjectCurlyNewlineDefault() {
    doImportTest("{\"rules\": {\"object-curly-newline\": \"error\"}}",
                 (common, custom) -> custom.OBJECT_LITERAL_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, custom.OBJECT_LITERAL_WRAP));
  }

  @Test
  public void testObjectCurlyNewlineAlways() {
    doImportTest("{\"rules\": {\"object-curly-newline\": [\"error\", \"always\"]}}",
                 (common, custom) -> custom.OBJECT_LITERAL_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, custom.OBJECT_LITERAL_WRAP));
  }

  @Test
  public void testObjectCurlyNewlineMultiline() {
    doImportTest("{\"rules\": {\"object-curly-newline\": [\"error\", {\"multiline\": true}]}}",
                 (common, custom) -> custom.OBJECT_LITERAL_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, custom.OBJECT_LITERAL_WRAP));
  }

  @Test
  public void testObjectCurlyNewlineMultilineInObjectExpression() {
    doImportTest("{\"rules\": {\"object-curly-newline\": [\"error\", {\n" +
                 "        \"ObjectExpression\": { \"multiline\": true }}]}}",
                 (common, custom) -> custom.OBJECT_LITERAL_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, custom.OBJECT_LITERAL_WRAP));
  }

  @Test
  public void testObjectCurlyNewlineAlwaysInObjectExpression() {
    doImportTest("{\"rules\": {\"object-curly-newline\": [\"error\", {\n" +
                 "        \"ObjectExpression\": \"always\"}]}}",
                 (common, custom) -> custom.OBJECT_LITERAL_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, custom.OBJECT_LITERAL_WRAP));
  }

  @Test
  public void testObjectCurlyNewlineMultilineError() {
    ESLintImportCodeStyleTestUtil.doTestNoDataToImport(codeInsight.get(), "{\"rules\": {\"object-curly-newline\": [\"error\", {\"ObjectExpression\": { \"multiline1\": true }}]}}", null);
  }

  @Test
  public void testObjectCurlySpacingDefault() {
    doImportTest("{\"rules\": {\"object-curly-spacing\": \"error\"}}",
                 (common, custom) -> custom.SPACES_WITHIN_OBJECT_LITERAL_BRACES = true,
                 (common, custom) -> Assertions.assertFalse(custom.SPACES_WITHIN_OBJECT_LITERAL_BRACES));
  }

  @Test
  public void testObjectCurlySpacing() {
    doImportTest("{\"rules\": {\"object-curly-spacing\": [\"error\", \"always\"]}}",
                 (common, custom) -> {
                   custom.SPACES_WITHIN_OBJECT_LITERAL_BRACES = false;
                   custom.SPACES_WITHIN_IMPORTS = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(custom.SPACES_WITHIN_OBJECT_LITERAL_BRACES);
                   Assertions.assertTrue(custom.SPACES_WITHIN_IMPORTS);
                 });
  }

  @Test
  public void testSemiSpacing() {
    doImportTest("{\"rules\": {\"semi-spacing\": [\"error\", {\"before\": true}]}}",
                 (common, custom) -> common.SPACE_BEFORE_SEMICOLON = false,
                 (common, custom) -> Assertions.assertTrue(common.SPACE_BEFORE_SEMICOLON));
  }

  @Test
  public void testSemiSpacingDefault() {
    doImportTest("{\"rules\": {\"semi-spacing\": \"error\"}}",
                 (common, custom) -> common.SPACE_BEFORE_SEMICOLON = true,
                 (common, custom) -> Assertions.assertFalse(common.SPACE_BEFORE_SEMICOLON));
  }

  @Test
  public void testSpaceBeforeFunctionParen() {
    doImportTest("{\"rules\": {\"space-before-function-paren\": [\"error\", \"never\"]}}",
                 (common, custom) -> {
                   common.SPACE_BEFORE_METHOD_PARENTHESES = true;
                   custom.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = true;
                   custom.SPACE_BEFORE_ASYNC_ARROW_LPAREN = true;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(common.SPACE_BEFORE_METHOD_PARENTHESES);
                   Assertions.assertFalse(custom.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
                   Assertions.assertFalse(custom.SPACE_BEFORE_ASYNC_ARROW_LPAREN);
                 });
  }

  @Test
  public void testSpaceBeforeFunctionParenDefault() {
    doImportTest("{\"rules\": {\"space-before-function-paren\": \"error\"}}",
                 (common, custom) -> {
                   common.SPACE_BEFORE_METHOD_PARENTHESES = false;
                   custom.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false;
                   custom.SPACE_BEFORE_ASYNC_ARROW_LPAREN = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_BEFORE_METHOD_PARENTHESES);
                   Assertions.assertTrue(custom.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
                   Assertions.assertTrue(custom.SPACE_BEFORE_ASYNC_ARROW_LPAREN);
                 });
  }

  @Test
  public void testSpaceBeforeFunctionParenWithDetails() {
    doImportTest("""
                   {"rules": {"space-before-function-paren": ["error",{
                           "anonymous": "always",
                           "named": "never",
                           "asyncArrow": "always"
                       }]}}""",
                 (common, custom) -> {
                   common.SPACE_BEFORE_METHOD_PARENTHESES = true;
                   custom.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false;
                   custom.SPACE_BEFORE_ASYNC_ARROW_LPAREN = false;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(common.SPACE_BEFORE_METHOD_PARENTHESES);
                   Assertions.assertTrue(custom.SPACE_BEFORE_FUNCTION_LEFT_PARENTH);
                   Assertions.assertTrue(custom.SPACE_BEFORE_ASYNC_ARROW_LPAREN);
                 });
  }

  @Test
  public void testSpaceInParens() {
    doImportTest("{\"rules\": {\"space-in-parens\": [\"error\", \"always\"]}}",
                 (common, custom) -> common.SPACE_WITHIN_METHOD_CALL_PARENTHESES = false,
                 (common, custom) -> Assertions.assertTrue(common.SPACE_WITHIN_METHOD_CALL_PARENTHESES));
  }

  @Test
  public void testSpaceInParensDefault() {
    doImportTest("{\"rules\": {\"space-in-parens\": \"error\"}}",
                 (common, custom) -> common.SPACE_WITHIN_METHOD_CALL_PARENTHESES = true,
                 (common, custom) -> Assertions.assertFalse(common.SPACE_WITHIN_METHOD_CALL_PARENTHESES));
  }

  @Test
  public void testSpaceInfixOps() {
    doImportTest("{\"rules\": {\"space-infix-ops\": [\"error\", {\"int32Hint\": true}]}}",
                 (common, custom) -> {
                   common.SPACE_AROUND_ASSIGNMENT_OPERATORS = false;
                   common.SPACE_AROUND_LOGICAL_OPERATORS = false;
                   common.SPACE_AROUND_EQUALITY_OPERATORS = false;

                   common.SPACE_AROUND_RELATIONAL_OPERATORS = false;
                   common.SPACE_AROUND_BITWISE_OPERATORS = true;
                   common.SPACE_AROUND_ADDITIVE_OPERATORS = false;

                   common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = false;
                   common.SPACE_AROUND_SHIFT_OPERATORS = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_AROUND_ASSIGNMENT_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_LOGICAL_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_EQUALITY_OPERATORS);

                   Assertions.assertTrue(common.SPACE_AROUND_RELATIONAL_OPERATORS);
                   Assertions.assertFalse(common.SPACE_AROUND_BITWISE_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_ADDITIVE_OPERATORS);

                   Assertions.assertTrue(common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_SHIFT_OPERATORS);
                 });
  }

  @Test
  public void testSpaceInfixOpsDefault() {
    doImportTest("{\"rules\": {\"space-infix-ops\": \"error\"}}",
                 (common, custom) -> {
                   common.SPACE_AROUND_ASSIGNMENT_OPERATORS = false;
                   common.SPACE_AROUND_LOGICAL_OPERATORS = false;
                   common.SPACE_AROUND_EQUALITY_OPERATORS = false;

                   common.SPACE_AROUND_RELATIONAL_OPERATORS = false;
                   common.SPACE_AROUND_BITWISE_OPERATORS = false;
                   common.SPACE_AROUND_ADDITIVE_OPERATORS = false;

                   common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = false;
                   common.SPACE_AROUND_SHIFT_OPERATORS = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_AROUND_ASSIGNMENT_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_LOGICAL_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_EQUALITY_OPERATORS);

                   Assertions.assertTrue(common.SPACE_AROUND_RELATIONAL_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_BITWISE_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_ADDITIVE_OPERATORS);

                   Assertions.assertTrue(common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS);
                   Assertions.assertTrue(common.SPACE_AROUND_SHIFT_OPERATORS);
                 });
  }

  @Test
  public void testSpaceUnaryOps() {
    doImportTest("{\"rules\": {\"space-unary-ops\": [\"error\", {\"nonwords\": true}]}}",
                 (common, custom) -> common.SPACE_AROUND_UNARY_OPERATOR = false,
                 (common, custom) -> Assertions.assertTrue(common.SPACE_AROUND_UNARY_OPERATOR));
  }

  @Test
  public void testSpaceUnaryOpsDefault() {
    doImportTest("{\"rules\": {\"space-unary-ops\": \"error\"}}",
                 (common, custom) -> common.SPACE_AROUND_UNARY_OPERATOR = true,
                 (common, custom) -> Assertions.assertFalse(common.SPACE_AROUND_UNARY_OPERATOR));
  }

  @Test
  public void testFuncCallSpacingDefault() {
    doImportTest("{\"rules\": {\"func-call-spacing\": \"error\"}}",
                 (common, custom) -> common.SPACE_BEFORE_METHOD_CALL_PARENTHESES = true,
                 (common, custom) -> Assertions.assertFalse(common.SPACE_BEFORE_METHOD_CALL_PARENTHESES));
  }

  @Test
  public void testFuncCallSpacingAlways() {
    doImportTest("{\"rules\": {\"func-call-spacing\": [\"error\", \"always\"]}}",
                 (common, custom) -> common.SPACE_BEFORE_METHOD_CALL_PARENTHESES = false,
                 (common, custom) -> Assertions.assertTrue(common.SPACE_BEFORE_METHOD_CALL_PARENTHESES));
  }

  @Test
  public void testArrayBracketNewLineDefault() {
    doImportTest("{\"rules\": {\"array-bracket-newline\": \"error\"}}",
                 (common, custom) -> {
                   common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE = false;
                   common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE);
                   Assertions.assertTrue(common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE);
                 });
  }

  @Test
  public void testArrayBracketNewLineNever() {
    doImportTest("{\"rules\": {\"array-bracket-newline\": [\"error\", \"never\"]}}",
                 (common, custom) -> {
                   common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE = true;
                   common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE = false;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE);
                   Assertions.assertFalse(common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE);
                 });
  }

  @Test
  public void testArrayBracketNewLineMinItemsZero() {
    doImportTest("{\"rules\": {\"array-bracket-newline\": [\"error\", {\"minItems\": 0}]}}",
                 (common, custom) -> {
                   common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE = true;
                   common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE);
                   Assertions.assertTrue(common.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE);
                 });
  }

  @Test
  public void testArrayElementNewLineDefault() {
    doImportTest("{\"rules\": {\"array-element-newline\": \"error\"}}",
                 (common, custom) -> common.ARRAY_INITIALIZER_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, common.ARRAY_INITIALIZER_WRAP));
  }

  @Test
  public void testArrayElementNewLineAlways() {
    doImportTest("{\"rules\": {\"array-element-newline\": [\"error\", \"never\"]}}",
                 (common, custom) -> common.ARRAY_INITIALIZER_WRAP = WRAP_ALWAYS,
                 (common, custom) -> Assertions.assertEquals(DO_NOT_WRAP, common.ARRAY_INITIALIZER_WRAP));
  }

  @Test
  public void testArrayElementNewLineMinItemsZero() {
    doImportTest("{\"rules\": {\"array-element-newline\": [\"error\", {\"minItems\": 0}]}}",
                 (common, custom) -> common.ARRAY_INITIALIZER_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, common.ARRAY_INITIALIZER_WRAP));
  }

  @Test
  public void testKeySpacingDefault() {
    doImportTest("{\"rules\": {\"key-spacing\": \"error\"}}",
                 (common, custom) -> {
                   custom.SPACE_BEFORE_PROPERTY_COLON = true;
                   custom.SPACE_AFTER_PROPERTY_COLON = false;
                   custom.ALIGN_OBJECT_PROPERTIES = JSCodeStyleSettings.ALIGN_ON_VALUE;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(custom.SPACE_BEFORE_PROPERTY_COLON);
                   Assertions.assertTrue(custom.SPACE_AFTER_PROPERTY_COLON);
                   // not changed
                   Assertions.assertEquals(JSCodeStyleSettings.ALIGN_ON_VALUE, custom.ALIGN_OBJECT_PROPERTIES);
                 });
  }

  @Test
  public void testKeySpacingExplicit() {
    doImportTest("{\"rules\": {\"key-spacing\": [\"error\", {" +
                 "\"beforeColon\": true, \"afterColon\": false, \"align\": \"colon\"}]}}",
                 (common, custom) -> {
                   custom.SPACE_BEFORE_PROPERTY_COLON = false;
                   custom.SPACE_AFTER_PROPERTY_COLON = true;
                   custom.ALIGN_OBJECT_PROPERTIES = JSCodeStyleSettings.ALIGN_ON_VALUE;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(custom.SPACE_BEFORE_PROPERTY_COLON);
                   Assertions.assertFalse(custom.SPACE_AFTER_PROPERTY_COLON);
                   Assertions.assertEquals(JSCodeStyleSettings.ALIGN_ON_COLON, custom.ALIGN_OBJECT_PROPERTIES);
                 });
  }

  @Test
  public void testKeySpacingExplicitAlign() {
    doImportTest("{\"rules\": {\"key-spacing\": [\"error\", {\"align\":{" +
                 "\"beforeColon\": true, \"afterColon\": false, \"on\": \"colon\"}}]}}",
                 (common, custom) -> {
                   custom.SPACE_BEFORE_PROPERTY_COLON = false;
                   custom.SPACE_AFTER_PROPERTY_COLON = true;
                   custom.ALIGN_OBJECT_PROPERTIES = JSCodeStyleSettings.ALIGN_ON_VALUE;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(custom.SPACE_BEFORE_PROPERTY_COLON);
                   Assertions.assertFalse(custom.SPACE_AFTER_PROPERTY_COLON);
                   Assertions.assertEquals(JSCodeStyleSettings.ALIGN_ON_COLON, custom.ALIGN_OBJECT_PROPERTIES);
                 });
  }

  @Test
  public void testObjectPropertyNewLine() {
    doImportTest("{\"rules\": {\"object-property-newline\": \"error\"}}",
                 (common, custom) -> custom.OBJECT_LITERAL_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, custom.OBJECT_LITERAL_WRAP));
  }

  @Test
  public void testObjectPropertyNewLineSkipped() {
    ESLintImportCodeStyleTestUtil.doTestNoDataToImport(codeInsight.get(), "{\"rules\": {\"object-property-newline\": [\"error\", {" +
                         "\"allowMultiplePropertiesPerLine\": true}]}}", null);
  }

  @Test
  public void testOneVarDeclarationPerLineDefault() {
    doImportTest("{\"rules\": {\"one-var-declaration-per-line\": \"error\"}}",
                 (common, custom) -> custom.VAR_DECLARATION_WRAP = DO_NOT_WRAP,
                 (common, custom) -> Assertions.assertEquals(WRAP_ALWAYS, custom.VAR_DECLARATION_WRAP));
  }

  @Test
  public void testSpaceBeforeBlocksDefault() {
    doImportTest("{\"rules\": {\"space-before-blocks\": \"error\"}}",
                 (common, custom) -> {
                   common.SPACE_BEFORE_METHOD_LBRACE = false;
                   common.SPACE_BEFORE_IF_LBRACE = false;
                   common.SPACE_BEFORE_ELSE_LBRACE = false;
                   common.SPACE_BEFORE_FOR_LBRACE = false;
                   common.SPACE_BEFORE_WHILE_LBRACE = false;
                   common.SPACE_BEFORE_DO_LBRACE = false;
                   common.SPACE_BEFORE_SWITCH_LBRACE = false;
                   common.SPACE_BEFORE_TRY_LBRACE = false;
                   common.SPACE_BEFORE_CATCH_LBRACE = false;
                   common.SPACE_BEFORE_FINALLY_LBRACE = false;
                   custom.SPACE_BEFORE_CLASS_LBRACE = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_BEFORE_METHOD_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_IF_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_ELSE_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_FOR_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_WHILE_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_DO_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_SWITCH_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_TRY_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_CATCH_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_FINALLY_LBRACE);
                   Assertions.assertTrue(custom.SPACE_BEFORE_CLASS_LBRACE);
                 });
  }

  @Test
  public void testSpaceBeforeBlocksAlways() {
    doImportTest("{\"rules\": {\"space-before-blocks\": [\"error\", \"always\"]}}",
                 (common, custom) -> {
                   common.SPACE_BEFORE_METHOD_LBRACE = false;
                   common.SPACE_BEFORE_IF_LBRACE = false;
                   common.SPACE_BEFORE_ELSE_LBRACE = false;
                   common.SPACE_BEFORE_FOR_LBRACE = false;
                   common.SPACE_BEFORE_WHILE_LBRACE = false;
                   common.SPACE_BEFORE_DO_LBRACE = false;
                   common.SPACE_BEFORE_SWITCH_LBRACE = false;
                   common.SPACE_BEFORE_TRY_LBRACE = false;
                   common.SPACE_BEFORE_CATCH_LBRACE = false;
                   common.SPACE_BEFORE_FINALLY_LBRACE = false;
                   custom.SPACE_BEFORE_CLASS_LBRACE = false;
                 },
                 (common, custom) -> {
                   Assertions.assertTrue(common.SPACE_BEFORE_METHOD_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_IF_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_ELSE_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_FOR_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_WHILE_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_DO_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_SWITCH_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_TRY_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_CATCH_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_FINALLY_LBRACE);
                   Assertions.assertTrue(custom.SPACE_BEFORE_CLASS_LBRACE);
                 });
  }

  @Test
  public void testSpaceBeforeBlocksConfig() {
    doImportTest("{\"rules\": {\"space-before-blocks\": [\"error\", {\"functions\": \"never\", \"keywords\": \"always\"," +
                 " \"classes\": \"never\"}]}}",
                 (common, custom) -> {
                   common.SPACE_BEFORE_METHOD_LBRACE = true;
                   common.SPACE_BEFORE_IF_LBRACE = false;
                   common.SPACE_BEFORE_ELSE_LBRACE = false;
                   common.SPACE_BEFORE_FOR_LBRACE = false;
                   common.SPACE_BEFORE_WHILE_LBRACE = false;
                   common.SPACE_BEFORE_DO_LBRACE = false;
                   common.SPACE_BEFORE_SWITCH_LBRACE = false;
                   common.SPACE_BEFORE_TRY_LBRACE = false;
                   common.SPACE_BEFORE_CATCH_LBRACE = false;
                   common.SPACE_BEFORE_FINALLY_LBRACE = false;
                   custom.SPACE_BEFORE_CLASS_LBRACE = true;
                 },
                 (common, custom) -> {
                   Assertions.assertFalse(common.SPACE_BEFORE_METHOD_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_IF_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_ELSE_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_FOR_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_WHILE_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_DO_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_SWITCH_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_TRY_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_CATCH_LBRACE);
                   Assertions.assertTrue(common.SPACE_BEFORE_FINALLY_LBRACE);
                   Assertions.assertFalse(custom.SPACE_BEFORE_CLASS_LBRACE);
                 });
  }

  @Test
  public void testSpaceAfterUnaryNot() {
    doImportTest("""
                   {
                     "rules": {
                       "space-unary-ops": [
                         "error",
                         {
                           "nonWords": false,
                           "overrides": {
                             "!": true
                           }
                         }
                       ]
                     }
                   }
                   """, null, (common, custom) -> Assertions.assertTrue(custom.SPACE_AFTER_UNARY_NOT));
  }

  private void doImportTest(final @NotNull String text,
                            final @Nullable PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> stylePreset,
                            final @NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> styleChecker) {
    doImportTest(text, stylePreset, styleChecker, null, null);
  }

  private void doImportTest(final @NotNull String text,
                            final @Nullable PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> stylePreset,
                            final @NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> styleChecker,
                            final @NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> notAppliedChecker) {
    doImportTest(text, stylePreset, styleChecker, notAppliedChecker, null);
  }

  private void doImportTest(final @NotNull String text,
                            final @Nullable PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> stylePreset,
                            final @NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> styleChecker,
                            final @Nullable PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> notAppliedChecker,
                            @Nullable String fileName) {
    Consumer<CodeStyleSettings> preset = settings -> {
      if (stylePreset != null) {
        stylePreset.consume(settings.getCommonSettings(JavascriptLanguage.INSTANCE),
                            settings.getCustomSettings(JSCodeStyleSettings.class));
        if (notAppliedChecker != null) {
          stylePreset.consume(settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT),
                              settings.getCustomSettings(TypeScriptCodeStyleSettings.class));
        }
      }
    };
    ESLintImportCodeStyleTestUtil.doImportTest(codeInsight.get(), text, preset, settings -> {
      styleChecker.consume(settings.getCommonSettings(JavascriptLanguage.INSTANCE),
                           settings.getCustomSettings(JSCodeStyleSettings.class));
      if (notAppliedChecker != null) {
        notAppliedChecker.consume(settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT),
                                  settings.getCustomSettings(TypeScriptCodeStyleSettings.class));
      }
    }, fileName);
    if (notAppliedChecker != null) {
      // Check TypeScript
      ESLintImportCodeStyleTestUtil.doImportTest(codeInsight.get(), text.replace("\"rules\"", "\"plugins\":[\"@typescript-eslint\"], \"rules\""), preset, settings -> {
        styleChecker.consume(settings.getCommonSettings(JavascriptLanguage.INSTANCE),
                             settings.getCustomSettings(JSCodeStyleSettings.class));
        styleChecker.consume(settings.getCommonSettings(JavaScriptSupportLoader.TYPESCRIPT),
                             settings.getCustomSettings(TypeScriptCodeStyleSettings.class));
      }, fileName);
    }
  }
}
