// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.language

import com.intellij.lang.javascript.modules.JSImportHighlightingAndCompletionTestBase

private const val BASE_PATH = "/ts_imports"

class VueTypeScriptModuleTest: JSImportHighlightingAndCompletionTestBase() {
  
  override fun getBasePath(): String = BASE_PATH
  override fun getExtension(): String = "vue"
  override fun getTestDataPath(): String = getVueTestDataPath()

  fun testTypesModule() {
    doTestWithCopyDirectory()
  }

  fun testSimpleDeclare() {
    doTestWithCopyDirectory()
  }

  fun testReferenceGlobalTyping() {
    doTestWithCopyDirectory()
  }
}