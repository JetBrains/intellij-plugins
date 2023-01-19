// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSTestOption
import com.intellij.lang.javascript.JSTestOptions
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.JSImportHighlightingAndCompletionLightTestBase
import com.intellij.lang.javascript.modules.JSImportTestUtil
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection
import com.intellij.util.Consumer

private const val BASE_PATH = "/ts_imports"

class VueModuleImportTest : JSImportHighlightingAndCompletionLightTestBase() {

  override fun getBasePath(): String = BASE_PATH
  override fun getExtension(): String = "vue"
  override fun getTestDataPath(): String = getVueTestDataPath() + basePath
  
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
    doTestActionWithCopyDirectory(JSImportTestUtil.getActionName(), "ts", null, null)
  }

  fun testAutoImportFromVueWithJs() {
    JavaScriptFormatterTestBase.setTempSettings(project, JavascriptLanguage.INSTANCE, Consumer<JSCodeStyleSettings> {
      it.USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.UseExplicitExtension.TRUE
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

  fun testSimpleJSConfig() {
    doTestWithCopyDirectory()
    checkAfterFile(extension)
  }

  @JSTestOptions(selectLookupItem = 0)
  fun testEnumImport() {
    doTestWithCopyDirectory(1, true, extension)
    checkAfterFile(extension)
  }

  fun testReExportWIthJs() {
    doTestWithCopyDirectory(1, true, "ts")
  }
  
  @JSTestOptions(JSTestOption.WithSymbolNames)
  fun testCustomComponentHighlighting() {
    doTestWithCopyDirectory()
  }

  fun testCssReferencePathMapping() {
    myFixture.enableInspections(CssUnknownTargetInspection())
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile("src/${getTestName(false)}.vue")
    myFixture.testHighlighting()
  }

  @JSTestOptions(selectLookupItem = 0)
  fun testVueFileNameCompletion() {
    doTestWithCopyDirectory()
    checkAfterFile(extension)
  }
}
