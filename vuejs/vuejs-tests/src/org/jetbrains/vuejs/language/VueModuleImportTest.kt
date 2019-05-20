// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.language

import com.intellij.lang.javascript.JavaScriptFormatterTest
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.ES6ModulesDependenciesInspection
import com.intellij.lang.javascript.modules.JSImportHighlightingAndCompletionLightTestBase
import com.intellij.lang.typescript.intentions.TypeScriptAddImportStatementFix
import com.intellij.util.Consumer

private const val BASE_PATH = "/ts_imports"

class VueModuleImportTest : JSImportHighlightingAndCompletionLightTestBase() {

  override fun getBasePath(): String = BASE_PATH
  override fun getExtension(): String = "vue"
  override fun getTestDataPath(): String = getVueTestDataPath() + basePath

  override fun setUp() {
    super.setUp()

    myFixture.enableInspections(ES6ModulesDependenciesInspection())
  }

  fun testTypesModule() {
    doTestWithCopyDirectory()
  }

  fun testSimpleDeclare() {
    doTestWithCopyDirectory()
  }

  fun testReferenceGlobalTyping() {
    doTestWithCopyDirectory()
  }

  fun testAutoImportFromTs() {
    doTestAutoImportWithCopyDirectory()
  }

  fun testPathMappingResolve() {
    doTestAutoImportWithCopyDirectory()
  }

  fun testAutoImportFromVue() {
    doTestAutoImportWithCopyDirectory()
  }

  fun testAutoImportVueFileToTs() {
    doTestActionWithCopyDirectory(TypeScriptAddImportStatementFix.getActionName(), "ts", null, null)
  }

  fun testAutoImportFromVueWithJs() {
    JavaScriptFormatterTest.setTempSettings(project, JavascriptLanguage.INSTANCE, Consumer<JSCodeStyleSettings> {
      it.USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.BooleanWithGlobalOption.TRUE
    })

    doTestActionWithCopyDirectory("Insert 'import HelloWorld from \"./src/HelloWorld.vue\"'", "vue", null, null)
  }

  fun testAmbientTypingsInVue() {
    doTestWithCopyDirectory()
  }

  fun testSimpleVarFromES6() {
    doTestWithCopyDirectory()
    checkAfterFile(extension)
  }
}
