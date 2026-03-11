// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.hybridmode

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.platform.lsp.tests.waitForDiagnosticsFromLspServer
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.jetbrains.vuejs.options.VueSettings

/**
 * Abstract base for LSP diagnostics tests in Vue Hybrid Mode.
 * Runs shared diagnostic tests against a specific [VueLanguageToolsVersion].
 */
abstract class VueHybridModeDiagnosticsTestBase(
  bundledVersion: VueLanguageToolsVersion,
) : VueHybridModeTestBase(bundledVersion) {

  protected abstract val vueTestModule: VueTestModule

  override fun getBasePath(): String =
    "${vueRelativeTestDataPath()}/service/hybrid/diagnostics"

  protected fun isFromTsService(info: HighlightInfo): Boolean =
    info.description?.let { TS_SERVICE_PREFIX.containsMatchIn(it) } == true

  protected fun isFromLspServer(info: HighlightInfo): Boolean =
    info.description?.startsWith(LSP_SERVER_PREFIX) == true

  override fun setUp() {
    super.setUp()
    myFixture.configureVueDependencies(vueTestModule)
    myFixture.configureByText(VueTsConfigFile.FILE_NAME, VueTsConfigFile.DEFAULT_TSCONFIG_CONTENT)
  }

  fun `test type error detected in script setup`() {
    myFixture.configureByFile("typeErrorInScriptSetup/App.vue")
    val highlighting = myFixture.doHighlighting()

    val errors = highlighting.filter {
      it.severity == HighlightSeverity.ERROR
      && isFromTsService(it)
    }
    assertFalse(
      "Expected a TS service error (TS<code>: ...) for assigning number to string, but got: ${errors.map { it.description }}",
      errors.isEmpty(),
    )
  }

  fun `test multiple type errors in script setup`() {
    myFixture.configureByFile("wrongPropType/App.vue")
    val highlighting = myFixture.doHighlighting()

    val errors = highlighting.filter {
      it.severity == HighlightSeverity.ERROR
      && isFromTsService(it)
    }
    assertTrue(
      "Expected at least 2 TS service errors (TS<code>: ...), but got ${errors.size}: ${errors.map { it.description }}",
      errors.size >= 2,
    )
  }

  fun `test wrong function argument type`() {
    myFixture.configureByFile("wrongFunctionArgType/App.vue")
    val highlighting = myFixture.doHighlighting()

    val errors = highlighting.filter {
      it.severity == HighlightSeverity.ERROR
      && isFromTsService(it)
    }
    assertFalse(
      "Expected a TS service error (TS<code>: ...) for passing number to string parameter, but got: ${errors.map { it.description }}",
      errors.isEmpty(),
    )
  }

  fun `test non-existent property access`() {
    myFixture.configureByFile("nonExistentProperty/App.vue")
    val highlighting = myFixture.doHighlighting()

    val errors = highlighting.filter {
      it.severity == HighlightSeverity.ERROR
      && isFromTsService(it)
    }
    assertTrue(
      "Expected TS service error for accessing non-existent 'email' property, but got: ${errors.map { it.description }}",
      errors.any { it.description?.contains("email") == true },
    )
  }

  fun `test v-else without v-if`() {
    myFixture.configureByFile("vElseWithoutVIf/App.vue")
    waitForDiagnosticsFromLspServer(project, file.virtualFile)
    val highlighting = myFixture.doHighlighting()

    val vueErrors = highlighting.filter {
      it.severity >= HighlightSeverity.WARNING
      && isFromLspServer(it)
    }
    assertFalse(
      "Expected Vue LSP error for v-else without v-if, but got none",
      vueErrors.isEmpty(),
    )
  }

  fun `test correct bundled version is configured`() {
    myFixture.configureByText("App.vue", VALID_VUE_FILE)
    myFixture.doHighlighting()

    assertLspServiceActive()
    assertTsPluginServiceActive()

    val settings = VueSettings.instance(project).manualSettings
    val expectedLspVersion = SemVer.parseFromText(bundledVersion.versionString)
    val expectedTsPluginVersion = SemVer.parseFromText(bundledVersion.versionString)
    assertEquals(
      "LSP hybrid mode package version mismatch",
      expectedLspVersion,
      settings.lspHybridModePackage.version,
    )
    assertEquals(
      "TS plugin package version mismatch",
      expectedTsPluginVersion,
      settings.tsPluginPackage.version,
    )
  }

  companion object {
    private val TS_SERVICE_PREFIX = Regex("""^TS\d+: """)
    private const val LSP_SERVER_PREFIX = "Vue: "

    @Suppress("HtmlUnknownAttribute")
    // language=vue
    private val VALID_VUE_FILE = """
      <script setup lang="ts">
      const msg: string = 'hello'
      </script>
      <template>
        <div>{{ msg }}</div>
      </template>
    """.trimIndent()
  }
}
