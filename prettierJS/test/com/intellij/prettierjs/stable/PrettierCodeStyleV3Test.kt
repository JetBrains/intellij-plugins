// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.application.options.CodeStyle
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.Language
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.stable.PrettierPackageLockFixture.Companion.prettierPackageLockFixture
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.testFramework.utils.vfs.getPsiFile
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
@TestNpmPackage(PRETTIER_3_8_1_TEST_PACKAGE_SPEC)
class PrettierCodeStyleV3Test {
  private val prettier by prettierPackageLockFixture("codeStyle")

  var originalSettings: CodeStyleSettings? = null

  @BeforeEach
  fun setUp() {
    CodeStyle.dropTemporarySettings(prettier.project)
    originalSettings = CodeStyle.createTestSettings(CodeStyle.getSettings(prettier.project))
  }

  @AfterEach
  fun tearDown() {
    // The setup of the fixture did not complete, for example without Node.js.
    val original = originalSettings ?: return
    val state = PrettierConfiguration.getInstance(prettier.project)
      .withLinterPackage(NodePackageRef.create(prettier.nodePackage))
      .state
    state.configurationMode = PrettierConfiguration.ConfigurationMode.DISABLED
    state.codeStyleSettingsModifierEnabled = false
    CodeStyle.getSettings(prettier.project).copyFrom(original)
  }

  @Test
  fun testJavaScriptFileSettings(): Unit = prettier.withInstallation {
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
        assertTrue(settings.getSoftMargins(language).containsAll(listOf(70)))

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

  @Test
  fun testJavaScriptFileSettings_1(): Unit = prettier.withInstallation {
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
        assertTrue(settings.getSoftMargins(language).containsAll(listOf(100)))

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

  @Test
  fun testTypeScriptFileSettings(): Unit = prettier.withInstallation {
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
        assertTrue(settings.getSoftMargins(language).containsAll(listOf(70)))

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

  @Test
  fun testTypeScriptFileSettings_1(): Unit = prettier.withInstallation {
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
        assertTrue(settings.getSoftMargins(language).containsAll(listOf(100)))

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

  @Test
  fun testWithoutConfigFileSettings(): Unit = prettier.withInstallation {
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
      assertTrue(settings.getSoftMargins(language).containsAll(listOf(80)))

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

  @Test
  fun testHtmlFileSettings(): Unit = prettier.withInstallation {
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
      assertTrue(settings.getSoftMargins(language).containsAll(listOf(70)))

      val customSettings = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)

      assertTrue(customSettings.HTML_SPACE_INSIDE_EMPTY_TAG)
    }
  }

  @Test
  fun testNested(): Unit = prettier.withInstallation {
    configurePrettierForCodeStyle {
      val settings = getCodeStyleSettingsForFile("root.js")
      val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
      assertEquals(20, indentOptions?.INDENT_SIZE)

      val nestedSettings = getCodeStyleSettingsForFile("subdir/nested.js")
      val nestedIndentOptions = nestedSettings.getCommonSettings(JavascriptLanguage).indentOptions
      assertEquals(10, nestedIndentOptions?.INDENT_SIZE)
    }
  }

  @Test
  fun testNoDependencyFormatOutOfScope(): Unit = prettier.withSubdirInstallation("noDependencyFormatOutOfScope", "subdir") {
    configurePrettierForCodeStyle {
      val settings = getCodeStyleSettingsForFile("index.js")
      val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
      assertEquals(12, indentOptions?.INDENT_SIZE)
    }
  }

  @Test
  fun testNoDependency(): Unit = prettier.withSubdirInstallation("noDependency", "subdir") {
    configurePrettierForCodeStyle {
      try {
        PrettierConfiguration.getInstance(prettier.project).state.formatFilesOutsideDependencyScope = false

        val settings = getCodeStyleSettingsForFile("index.js")
        val indentOptions = settings.getCommonSettings(JavascriptLanguage).indentOptions
        assertEquals(12, indentOptions?.INDENT_SIZE)
      }
      finally {
        PrettierConfiguration.getInstance(prettier.project).state.formatFilesOutsideDependencyScope = false
      }
    }
  }

  @Test
  fun testPlainText(): Unit = prettier.withInstallation {
    configurePrettierForCodeStyle {
      val (language, settings) = getInfoForFile("text.txt")
      assertTrue(settings.getSoftMargins(language).containsAll(emptyList()))
    }
  }

  @Test
  fun testCssFile(): Unit = prettier.withInstallation {
    configurePrettierForCodeStyle {
      val (language, settings) = getInfoForFile("style.css")
      assertTrue(settings.getSoftMargins(language).containsAll(listOf(70)))
    }
  }

  @Test
  fun testInjectedLanguages(): Unit = prettier.withInstallation {
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

  @Test
  fun testSettingsFollowSavedConfigChange(): Unit = prettier.withInstallation {
    configurePrettierForCodeStyle {
      assertEquals(3, indexJsIndentSizeWithoutEditor())

      // the user changes the config in the editor and saves it
      val configFile = prettier.fixture.findFileInTempDir(".prettierrc.json")
      val document = runReadAction { FileDocumentManager.getInstance().getDocument(configFile) }!!
      WriteCommandAction.runWriteCommandAction(prettier.project) { document.setText("""{"tabWidth": 7}""") }
      runInEdtAndWait { FileDocumentManager.getInstance().saveDocument(document) }
      // The save terminated the old service. Start the new service now, because the code style modifier waits only 500 ms for a result.
      prettier.warmUpPrettierService(prettier.fixture.findFileInTempDir("package.json"))

      assertEquals(7, indexJsIndentSizeWithoutEditor())
    }
  }

  /**
   * Returns the indent size of `index.js` and does not open an editor.
   * An open editor can compute the code style settings before the new service starts.
   */
  private fun indexJsIndentSizeWithoutEditor(): Int {
    val indexJs = prettier.fixture.findFileInTempDir("index.js")
    val psiFile = runReadAction { indexJs.getPsiFile(prettier.project) }
    val settings = timeoutRunBlocking { CodeStyle.getSettings(psiFile) }
    return settings.getCommonSettings(psiFile.language).indentOptions!!.INDENT_SIZE
  }

  private fun configurePrettierForCodeStyle(block: () -> Unit) {
    val state = PrettierConfiguration.getInstance(prettier.project)
      .withLinterPackage(NodePackageRef.create(prettier.nodePackage))
      .state
    val filesPattern = state.filesPattern
    val configurationMode = state.configurationMode
    val codeStyleSettingsModifierEnabled = state.codeStyleSettingsModifierEnabled

    state.filesPattern = "**/*"
    state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL
    state.codeStyleSettingsModifierEnabled = true

    // Warm up the Prettier language service (starts Node.js process) before querying code style.
    // Without this, the 500ms timeout in PrettierCodeStyleSettingsModifier may fire on Windows.
    prettier.warmUpPrettierService(prettier.fixture.findFileInTempDir("package.json") ?: prettier.fixture.tempDirFixture.getFile(".")!!)

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
    val file = prettier.fixture.findFileInTempDir(fileName)
    return timeoutRunBlocking {
      prettier.fixture.configureFromExistingVirtualFile(file)
      CodeStyle.getSettings(prettier.project, file)
    }
  }

  private fun getLanguageForFile(fileName: String): Language {
    val file = prettier.fixture.findFileInTempDir(fileName)
    return runReadAction { file.getPsiFile(prettier.project).language }
  }
}
