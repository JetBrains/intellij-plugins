// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.hybridmode

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.typescript.service.VueLanguageToolsVersion

class VueHybridModeDiagnosticsDefaultTest : VueHybridModeDiagnosticsTestBase(
  bundledVersion = VueLanguageToolsVersion.DEFAULT,
) {
  override val vueTestModule: VueTestModule = VueTestModule.VUE_3_5_0

  fun `test no errors in valid code`() {
    myFixture.configureByText("App.vue", VALID_VUE_FILE)
    val highlighting = myFixture.doHighlighting()

    val errors = highlighting.filter { it.severity == HighlightSeverity.ERROR }
    assertTrue(
      "Expected no errors in valid Vue file, but got: ${errors.map { it.description }}",
      errors.isEmpty(),
    )
  }

  fun `test undefined variable in template`() {
    myFixture.configureByFile("undefinedVariable/App.vue")
    val highlighting = myFixture.doHighlighting()

    val errors = highlighting.filter {
      it.severity >= HighlightSeverity.WARNING
      && isFromTsService(it)
    }
    assertTrue(
      "Expected TS service error for undefined 'nonExistent' in template, but got: ${errors.map { it.description }}",
      errors.any { it.description?.contains("nonExistent") == true },
    )
  }

  companion object {
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
