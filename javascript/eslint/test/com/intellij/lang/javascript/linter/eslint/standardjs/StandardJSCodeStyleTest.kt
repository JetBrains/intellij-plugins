package com.intellij.lang.javascript.linter.eslint.standardjs

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.formatter.StandardJSCodeStyle
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class StandardJSCodeStyleTest : BasePlatformTestCase() {

  fun testApplyAndIsInstalled() {
    JSTestUtils.testWithTempCodeStyleSettings<RuntimeException>(project) { settings ->
      val common: CommonCodeStyleSettings = settings.getCommonSettings(JavascriptLanguage)
      val js: JSCodeStyleSettings = settings.getCustomSettings(JSCodeStyleSettings::class.java)
      val indent: CommonCodeStyleSettings.IndentOptions = common.indentOptions!!

      // preset non-standard values to verify apply() changes them
      indent.INDENT_SIZE = 4
      indent.CONTINUATION_INDENT_SIZE = 8
      indent.TAB_SIZE = 4
      indent.USE_TAB_CHARACTER = true
      js.USE_DOUBLE_QUOTES = true
      js.FORCE_QUOTE_STYlE = false
      js.USE_SEMICOLON_AFTER_STATEMENT = true
      js.FORCE_SEMICOLON_STYLE = false
      common.SPACE_BEFORE_METHOD_PARENTHESES = false
      js.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false
      js.SPACES_WITHIN_IMPORTS = false
      js.SPACES_WITHIN_OBJECT_LITERAL_BRACES = false

      assertFalse(StandardJSCodeStyle.isInstalled(project))

      StandardJSCodeStyle().apply(settings)

      // indentation
      assertEquals(2, indent.INDENT_SIZE)
      assertEquals(2, indent.CONTINUATION_INDENT_SIZE)
      assertEquals(2, indent.TAB_SIZE)
      assertFalse(indent.USE_TAB_CHARACTER)

      // alignment disabled
      assertFalse(common.ALIGN_MULTILINE_PARAMETERS_IN_CALLS)
      assertFalse(common.ALIGN_MULTILINE_PARAMETERS)
      assertFalse(common.ALIGN_MULTILINE_TERNARY_OPERATION)
      assertFalse(common.ALIGN_MULTILINE_ARRAY_INITIALIZER_EXPRESSION)
      assertFalse(common.ALIGN_MULTILINE_CHAINED_METHODS)
      assertFalse(common.ALIGN_MULTILINE_FOR)

      // quotes
      assertFalse(js.USE_DOUBLE_QUOTES)
      assertTrue(js.FORCE_QUOTE_STYlE)

      // keyword spacing
      assertTrue(common.SPACE_BEFORE_IF_LBRACE)
      assertTrue(common.SPACE_BEFORE_ELSE_LBRACE)
      assertTrue(common.SPACE_BEFORE_FOR_LBRACE)
      assertTrue(common.SPACE_BEFORE_WHILE_LBRACE)
      assertTrue(common.SPACE_BEFORE_DO_LBRACE)
      assertTrue(common.SPACE_BEFORE_SWITCH_LBRACE)
      assertTrue(common.SPACE_BEFORE_TRY_LBRACE)
      assertTrue(common.SPACE_BEFORE_CATCH_LBRACE)
      assertTrue(common.SPACE_BEFORE_FINALLY_LBRACE)
      assertTrue(common.SPACE_BEFORE_METHOD_LBRACE)
      assertTrue(common.SPACE_BEFORE_IF_PARENTHESES)
      assertTrue(common.SPACE_BEFORE_FOR_PARENTHESES)
      assertTrue(common.SPACE_BEFORE_WHILE_PARENTHESES)
      assertTrue(common.SPACE_BEFORE_SWITCH_PARENTHESES)
      assertTrue(common.SPACE_BEFORE_CATCH_PARENTHESES)
      assertTrue(common.SPACE_BEFORE_WHILE_KEYWORD)
      assertTrue(common.SPACE_BEFORE_ELSE_KEYWORD)
      assertTrue(common.SPACE_BEFORE_CATCH_KEYWORD)
      assertTrue(common.SPACE_BEFORE_FINALLY_KEYWORD)

      // function parentheses
      assertTrue(js.SPACE_BEFORE_FUNCTION_LEFT_PARENTH)
      assertTrue(common.SPACE_BEFORE_METHOD_PARENTHESES)

      // infix operators
      assertTrue(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)
      assertTrue(common.SPACE_AROUND_LOGICAL_OPERATORS)
      assertTrue(common.SPACE_AROUND_EQUALITY_OPERATORS)
      assertTrue(common.SPACE_AROUND_RELATIONAL_OPERATORS)
      assertTrue(common.SPACE_AROUND_BITWISE_OPERATORS)
      assertTrue(common.SPACE_AROUND_ADDITIVE_OPERATORS)
      assertTrue(common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
      assertTrue(common.SPACE_AROUND_SHIFT_OPERATORS)
      assertTrue(js.SPACE_AROUND_ARROW_FUNCTION_OPERATOR)

      // comma
      assertTrue(common.SPACE_AFTER_COMMA)

      // braces and blank lines
      assertTrue(common.KEEP_SIMPLE_BLOCKS_IN_ONE_LINE)
      assertTrue(common.KEEP_SIMPLE_METHODS_IN_ONE_LINE)
      assertEquals(1, common.KEEP_BLANK_LINES_IN_CODE)

      // ternary
      assertTrue(common.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE)
      assertTrue(common.SPACE_AFTER_QUEST)
      assertTrue(common.SPACE_BEFORE_QUEST)
      assertTrue(common.SPACE_BEFORE_COLON)
      assertTrue(common.SPACE_AFTER_COLON)

      // semicolons
      assertFalse(js.USE_SEMICOLON_AFTER_STATEMENT)
      assertTrue(js.FORCE_SEMICOLON_STYLE)

      // imports and object braces
      assertTrue(js.SPACES_WITHIN_IMPORTS)
      assertTrue(js.SPACES_WITHIN_OBJECT_LITERAL_BRACES)

      // before parentheses
      assertFalse(common.SPACE_BEFORE_METHOD_CALL_PARENTHESES)

      // unary
      assertFalse(common.SPACE_AROUND_UNARY_OPERATOR)

      // within parentheses
      assertFalse(js.SPACE_WITHIN_ARRAY_INITIALIZER_BRACKETS)
      assertFalse(common.SPACE_WITHIN_METHOD_PARENTHESES)
      assertFalse(common.SPACE_WITHIN_METHOD_CALL_PARENTHESES)
      assertFalse(common.SPACE_WITHIN_IF_PARENTHESES)
      assertFalse(common.SPACE_WITHIN_FOR_PARENTHESES)
      assertFalse(common.SPACE_WITHIN_WHILE_PARENTHESES)
      assertFalse(common.SPACE_WITHIN_CATCH_PARENTHESES)
      assertFalse(common.SPACE_WITHIN_SWITCH_PARENTHESES)

      // other spacing
      assertFalse(common.SPACE_BEFORE_COMMA)
      assertFalse(common.SPACE_BEFORE_SEMICOLON)
      assertFalse(js.SPACE_BEFORE_PROPERTY_COLON)
      assertTrue(js.SPACE_AFTER_PROPERTY_COLON)

      // generator spacing
      assertTrue(js.SPACE_BEFORE_GENERATOR_MULT)
      assertTrue(js.SPACE_AFTER_GENERATOR_MULT)

      // isInstalled should now return true
      assertTrue(StandardJSCodeStyle.isInstalled(project))
    }
  }

  fun testIsNotInstalledWithNonStandardSettings() {
    JSTestUtils.testWithTempCodeStyleSettings<RuntimeException>(project) { settings: CodeStyleSettings ->
      StandardJSCodeStyle().apply(settings)
      assertTrue(StandardJSCodeStyle.isInstalled(project))

      // changing any checked setting should make isInstalled return false
      val common: CommonCodeStyleSettings = settings.getCommonSettings(JavascriptLanguage)
      common.getIndentOptions()!!.INDENT_SIZE = 4
      assertFalse(StandardJSCodeStyle.isInstalled(project))
    }
  }
}
