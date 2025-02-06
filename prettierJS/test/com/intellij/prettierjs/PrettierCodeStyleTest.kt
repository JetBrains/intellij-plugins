// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.application.options.CodeStyle
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.Language
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.utils.vfs.getPsiFile

class PrettierCodeStyleTest : JSExternalToolIntegrationTest() {

  var originalSettings: CodeStyleSettings? = null

  override fun getMainPackageName(): String? {
    return PrettierUtil.PACKAGE_NAME
  }

  override fun setUp() {
    super.setUp()
    CodeStyle.dropTemporarySettings(project)
    originalSettings = CodeStyle.createTestSettings(CodeStyle.getSettings(project))
    myFixture.setTestDataPath(PrettierJSTestUtil.getTestDataPath() + "codeStyle")
    val state = PrettierConfiguration.getInstance(project)
      .withLinterPackage(NodePackageRef.create(nodePackage))
      .state
    state.filesPattern = "**/*"
    state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL
    state.codeStyleSettingsModifierEnabled = true
    myFixture.copyDirectoryToProject(getTestName(true), "")
  }

  override fun tearDown() {
    try {
      val state = PrettierConfiguration.getInstance(project)
        .withLinterPackage(NodePackageRef.create(nodePackage))
        .state
      state.configurationMode = PrettierConfiguration.ConfigurationMode.DISABLED
      state.codeStyleSettingsModifierEnabled = false
      CodeStyle.getSettings(project).copyFrom(originalSettings)
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testJavaScriptFileSettings() {
    for (fileName in listOf("index.js", "index.jsx")) {
      val (language, settings) = getInfoForFile(fileName)

      val indentOptions = settings.getCommonSettings(language).indentOptions
      assertNotNull(indentOptions)
      indentOptions?.let {
        assertEquals(12, it.INDENT_SIZE)
        assertEquals(12, it.CONTINUATION_INDENT_SIZE)
        assertEquals(12, it.TAB_SIZE)
        assertTrue(it.USE_TAB_CHARACTER)
      }
      assertEquals(settings.LINE_SEPARATOR, "\r\n")
      assertContainsElements(settings.getSoftMargins(language), listOf(70))

      val customSettings = settings.getCustomSettings(JSCodeStyleSettings::class.java)

      assertFalse(customSettings.USE_DOUBLE_QUOTES)
      assertTrue(customSettings.USE_SEMICOLON_AFTER_STATEMENT)
      assertTrue(customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES)
      assertTrue(customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES)
      assertTrue(customSettings.SPACES_WITHIN_IMPORTS)
      assertEquals(JSCodeStyleSettings.TrailingCommaOption.Remove, customSettings.ENFORCE_TRAILING_COMMA)

      assertTrue(customSettings.FORCE_QUOTE_STYlE)
      assertTrue(customSettings.FORCE_SEMICOLON_STYLE)
      assertFalse(customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH)
    }
  }

  fun testJavaScriptFileSettings_1() {
    for (fileName in listOf("index.js", "index.jsx")) {
      val (language, settings) = getInfoForFile(fileName)

      val indentOptions = settings.getCommonSettings(language).indentOptions
      assertNotNull(indentOptions)
      indentOptions?.let {
        assertEquals(20, it.INDENT_SIZE)
        assertEquals(20, it.CONTINUATION_INDENT_SIZE)
        assertEquals(20, it.TAB_SIZE)
        assertFalse(it.USE_TAB_CHARACTER)
      }
      assertEquals(settings.LINE_SEPARATOR, "\r")
      assertContainsElements(settings.getSoftMargins(language), listOf(100))

      val customSettings = settings.getCustomSettings(JSCodeStyleSettings::class.java)

      assertTrue(customSettings.USE_DOUBLE_QUOTES)
      assertFalse(customSettings.USE_SEMICOLON_AFTER_STATEMENT)
      assertFalse(customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES)
      assertFalse(customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES)
      assertFalse(customSettings.SPACES_WITHIN_IMPORTS)
      assertEquals(JSCodeStyleSettings.TrailingCommaOption.WhenMultiline, customSettings.ENFORCE_TRAILING_COMMA)

      assertTrue(customSettings.FORCE_QUOTE_STYlE)
      assertTrue(customSettings.FORCE_SEMICOLON_STYLE)
      assertFalse(customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH)
    }
  }

  fun testTypeScriptFileSettings() {
    for (fileName in listOf("index.ts", "index.d.ts", "index.tsx")) {
      val (language, settings) = getInfoForFile(fileName)

      val indentOptions = settings.getCommonSettings(language).indentOptions
      assertNotNull(indentOptions)
      indentOptions?.let {
        assertEquals(12, it.INDENT_SIZE)
        assertEquals(12, it.CONTINUATION_INDENT_SIZE)
        assertEquals(12, it.TAB_SIZE)
        assertTrue(it.USE_TAB_CHARACTER)
      }
      assertEquals(settings.LINE_SEPARATOR, "\r\n")
      assertContainsElements(settings.getSoftMargins(language), listOf(70))

      val customSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings::class.java)

      assertFalse(customSettings.USE_DOUBLE_QUOTES)
      assertTrue(customSettings.USE_SEMICOLON_AFTER_STATEMENT)
      assertTrue(customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES)
      assertTrue(customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES)
      assertTrue(customSettings.SPACES_WITHIN_IMPORTS)
      assertEquals(JSCodeStyleSettings.TrailingCommaOption.Remove, customSettings.ENFORCE_TRAILING_COMMA)

      assertTrue(customSettings.FORCE_QUOTE_STYlE)
      assertTrue(customSettings.FORCE_SEMICOLON_STYLE)
      assertFalse(customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH)
    }
  }

  fun testTypeScriptFileSettings_1() {
    for (fileName in listOf("index.ts", "index.d.ts", "index.tsx")) {
      val (language, settings) = getInfoForFile(fileName)

      val indentOptions = settings.getCommonSettings(language).indentOptions
      assertNotNull(indentOptions)
      indentOptions?.let {
        assertEquals(20, it.INDENT_SIZE)
        assertEquals(20, it.CONTINUATION_INDENT_SIZE)
        assertEquals(20, it.TAB_SIZE)
        assertFalse(it.USE_TAB_CHARACTER)
      }
      assertEquals(settings.LINE_SEPARATOR, "\r")
      assertContainsElements(settings.getSoftMargins(language), listOf(100))

      val customSettings = settings.getCustomSettings(TypeScriptCodeStyleSettings::class.java)

      assertTrue(customSettings.USE_DOUBLE_QUOTES)
      assertFalse(customSettings.USE_SEMICOLON_AFTER_STATEMENT)
      assertFalse(customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES)
      assertFalse(customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES)
      assertFalse(customSettings.SPACES_WITHIN_IMPORTS)
      assertEquals(JSCodeStyleSettings.TrailingCommaOption.WhenMultiline, customSettings.ENFORCE_TRAILING_COMMA)

      assertTrue(customSettings.FORCE_QUOTE_STYlE)
      assertTrue(customSettings.FORCE_SEMICOLON_STYLE)
      assertFalse(customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH)
    }
  }

  fun testWithoutConfigFileSettings() {
    val (language, settings) = getInfoForFile("index.js")

    val indentOptions = settings.getCommonSettings(language).indentOptions
    assertNotNull(indentOptions)
    indentOptions?.let {
      assertEquals(2, it.INDENT_SIZE)
      assertEquals(2, it.CONTINUATION_INDENT_SIZE)
      assertEquals(2, it.TAB_SIZE)
      assertFalse(it.USE_TAB_CHARACTER)
    }
    assertNull(settings.LINE_SEPARATOR)
    assertContainsElements(settings.getSoftMargins(language), listOf(80))

    val customSettings = settings.getCustomSettings(JSCodeStyleSettings::class.java)

    assertTrue(customSettings.USE_DOUBLE_QUOTES)
    assertTrue(customSettings.USE_SEMICOLON_AFTER_STATEMENT)
    assertTrue(customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES)
    assertTrue(customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES)
    assertTrue(customSettings.SPACES_WITHIN_IMPORTS)
    assertEquals(JSCodeStyleSettings.TrailingCommaOption.Remove, customSettings.ENFORCE_TRAILING_COMMA)

    assertTrue(customSettings.FORCE_QUOTE_STYlE)
    assertTrue(customSettings.FORCE_SEMICOLON_STYLE)
    assertFalse(customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH)
  }

  fun testHtmlFileSettings() {
    val (language, settings) = getInfoForFile("index.html")

    val indentOptions = settings.getCommonSettings(language).indentOptions
    assertNotNull(indentOptions)
    indentOptions?.let {
      assertEquals(12, it.INDENT_SIZE)
      assertEquals(12, it.CONTINUATION_INDENT_SIZE)
      assertEquals(12, it.TAB_SIZE)
      assertTrue(it.USE_TAB_CHARACTER)
    }
    assertEquals(settings.LINE_SEPARATOR, "\r\n")
    assertContainsElements(settings.getSoftMargins(language), listOf(70))

    val customSettings = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)

    assertTrue(customSettings.HTML_SPACE_INSIDE_EMPTY_TAG)
  }

  fun testNested() {
    val settings = getCodeStyleSettingsForFile("root.js")
    val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
    assertEquals(20, indentOptions?.INDENT_SIZE)

    val nestedSettings = getCodeStyleSettingsForFile("subdir/nested.js")
    val nestedIndentOptions = nestedSettings.getCommonSettings(JavascriptLanguage).indentOptions
    assertEquals(10, nestedIndentOptions?.INDENT_SIZE)
  }

  fun noDependencyFormatOutOfScope() {
    val settings = getCodeStyleSettingsForFile("index.js")
    val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
    assertEquals(12, indentOptions?.INDENT_SIZE)
  }

  fun testNoDependency() {
    try {
      PrettierConfiguration.getInstance(project).state.formatFilesOutsideDependencyScope = false

      val settings = getCodeStyleSettingsForFile("index.js")
      val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
      assertEquals(4, indentOptions?.INDENT_SIZE)
    }
    finally {
      PrettierConfiguration.getInstance(project).state.formatFilesOutsideDependencyScope = false
    }
  }

  fun testPlainText() {
    val (language, settings) = getInfoForFile("text.txt")
    assertContainsElements(settings.getSoftMargins(language), emptyList())
  }

  fun testCssFile() {
    val (language, settings) = getInfoForFile("style.css")
    assertContainsElements(settings.getSoftMargins(language), listOf(70))
  }

  private fun getInfoForFile(fileName: String): Pair<Language, CodeStyleSettings> {
    val language = getLanguageForFile(fileName)
    val settings = getCodeStyleSettingsForFile(fileName)

    return Pair(language, settings)
  }

  private fun getCodeStyleSettingsForFile(fileName: String): CodeStyleSettings {
    val file = myFixture.findFileInTempDir(fileName)
    return timeoutRunBlocking {
      myFixture.configureFromExistingVirtualFile(file)
      CodeStyle.getSettings(project, file)
    }
  }

  private fun getLanguageForFile(fileName: String): Language {
    val file = myFixture.findFileInTempDir(fileName)
    return file.getPsiFile(project).language
  }
}