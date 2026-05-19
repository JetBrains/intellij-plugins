// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.application.options.CodeStyle
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.Language
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.PrettierJSTestUtil
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.utils.vfs.getPsiFile

@TestNpmPackage(PRETTIER_3_8_1_TEST_PACKAGE_SPEC)
class PrettierCodeStyleV3Test : PrettierPackageLockTest() {

  var originalSettings: CodeStyleSettings? = null

  override fun setUp() {
    super.setUp()
    CodeStyle.dropTemporarySettings(project)
    originalSettings = CodeStyle.createTestSettings(CodeStyle.getSettings(project))
    myFixture.testDataPath = PrettierJSTestUtil.getTestDataPath() + "codeStyle"
  }

  override fun tearDown() {
    try {
      val state = PrettierConfiguration.getInstance(project)
        .withLinterPackage(NodePackageRef.create(getNodePackage()))
        .state
      state.configurationMode = PrettierConfiguration.ConfigurationMode.DISABLED
      state.codeStyleSettingsModifierEnabled = false
      CodeStyle.getSettings(project).copyFrom(originalSettings!!)
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testJavaScriptFileSettings() = withInstallation {
    configurePrettierForCodeStyle {
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
  }

  fun testJavaScriptFileSettings_1() = withInstallation {
    configurePrettierForCodeStyle {
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
  }

  fun testTypeScriptFileSettings() = withInstallation {
    configurePrettierForCodeStyle {
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
  }

  fun testTypeScriptFileSettings_1() = withInstallation {
    configurePrettierForCodeStyle {
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
  }

  fun testWithoutConfigFileSettings() = withInstallation {
    configurePrettierForCodeStyle {
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
  }

  fun testHtmlFileSettings() = withInstallation {
    configurePrettierForCodeStyle {
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
  }

  fun testNested() = withInstallation {
    configurePrettierForCodeStyle {
      val settings = getCodeStyleSettingsForFile("root.js")
      val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
      assertEquals(20, indentOptions?.INDENT_SIZE)

      val nestedSettings = getCodeStyleSettingsForFile("subdir/nested.js")
      val nestedIndentOptions = nestedSettings.getCommonSettings(JavascriptLanguage).indentOptions
      assertEquals(10, nestedIndentOptions?.INDENT_SIZE)
    }
  }

  fun testNoDependencyFormatOutOfScope() = withSubdirInstallation("noDependencyFormatOutOfScope", "subdir") {
    configurePrettierForCodeStyle {
      val settings = getCodeStyleSettingsForFile("index.js")
      val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
      assertEquals(12, indentOptions?.INDENT_SIZE)
    }
  }

  fun testNoDependency() = withSubdirInstallation("noDependency", "subdir") {
    configurePrettierForCodeStyle {
      try {
        PrettierConfiguration.getInstance(project).state.formatFilesOutsideDependencyScope = false

        val settings = getCodeStyleSettingsForFile("index.js")
        val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
        assertEquals(12, indentOptions?.INDENT_SIZE)
      }
      finally {
        PrettierConfiguration.getInstance(project).state.formatFilesOutsideDependencyScope = false
      }
    }
  }

  fun testPlainText() = withInstallation {
    configurePrettierForCodeStyle {
      val (language, settings) = getInfoForFile("text.txt")
      assertContainsElements(settings.getSoftMargins(language), emptyList())
    }
  }

  fun testCssFile() = withInstallation {
    configurePrettierForCodeStyle {
      val (language, settings) = getInfoForFile("style.css")
      assertContainsElements(settings.getSoftMargins(language), listOf(70))
    }
  }

  fun testInjectedLanguages() = withInstallation {
    configurePrettierForCodeStyle {
      for (fileName in listOf("javascript.js", "typescript.ts")) {
        val settings = getCodeStyleSettingsForFile(fileName)

        for (language in listOf("CSS", "HTML")) {
          val indentOptions = settings.getCommonSettings(language).indentOptions
          assertNotNull(indentOptions)
          indentOptions?.let {
            assertEquals(12, it.INDENT_SIZE)
            assertEquals(12, it.CONTINUATION_INDENT_SIZE)
            assertEquals(12, it.TAB_SIZE)
            assertTrue(it.USE_TAB_CHARACTER)
          }
          assertEquals(settings.LINE_SEPARATOR, "\r\n")
        }
      }
    }
  }

  private fun configurePrettierForCodeStyle(block: () -> Unit) {
    val state = PrettierConfiguration.getInstance(project)
      .withLinterPackage(NodePackageRef.create(getNodePackage()))
      .state
    val filesPattern = state.filesPattern
    val configurationMode = state.configurationMode
    val codeStyleSettingsModifierEnabled = state.codeStyleSettingsModifierEnabled

    state.filesPattern = "**/*"
    state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL
    state.codeStyleSettingsModifierEnabled = true

    // Warm up the Prettier language service (starts Node.js process) before querying code style.
    // Without this, the 500ms timeout in PrettierCodeStyleSettingsModifier may fire on Windows.
    warmUpPrettierService(myFixture.findFileInTempDir("package.json") ?: myFixture.tempDirFixture.getFile(".")!!)

    try {
     block()
    }
    finally {
        state.filesPattern = filesPattern
        state.configurationMode = configurationMode
        state.codeStyleSettingsModifierEnabled = codeStyleSettingsModifierEnabled
    }
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
