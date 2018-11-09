// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.language

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptFormatterTest
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.ES6ModulesDependenciesInspection
import com.intellij.lang.javascript.modules.JSImportHighlightingAndCompletionTestBase
import com.intellij.lang.typescript.intentions.TypeScriptAddImportStatementFix
import com.intellij.util.ArrayUtil
import com.intellij.util.Consumer
import com.intellij.util.ThrowableRunnable

private const val BASE_PATH = "/ts_imports"

class VueModuleImportTest: JSImportHighlightingAndCompletionTestBase() {
  
  override fun getBasePath(): String = BASE_PATH
  override fun getExtension(): String = "vue"
  override fun getTestDataPath(): String = getVueTestDataPath()

  override fun configureLocalInspectionTools(): Array<LocalInspectionTool> {
    return ArrayUtil.append(super.configureLocalInspectionTools(), ES6ModulesDependenciesInspection())
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
    doTestActionWithCopyDirectory(TypeScriptAddImportStatementFix.getActionName(), "ts", null)
  }

  fun testAutoImportFromVueWithJs() {
    JavaScriptFormatterTest.setTempSettings(project, JavascriptLanguage.INSTANCE, Consumer<JSCodeStyleSettings> { 
      it.USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.BooleanWithGlobalOption.TRUE
    })
    
    JSTestUtils.testES6(project, ThrowableRunnable<RuntimeException> {
      doTestActionWithCopyDirectory("Insert 'import HelloWorld from \"./src/HelloWorld.vue\"'", "vue", null)
    })
  }
}