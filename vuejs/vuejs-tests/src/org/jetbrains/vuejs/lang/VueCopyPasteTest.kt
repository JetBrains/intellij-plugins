// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSTestUtils.getDefaultFileNames
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VueCopyPasteTest : BasePlatformTestCase() {
  override fun getBasePath(): String = ""
  override fun getTestDataPath(): String = getVueTestDataPath() + "/copyPaste"

  private fun doTest(count: Int) {
    val fileNames = getDefaultFileNames(count, "vue") { this.getTestName(false) }

    val files = myFixture.configureByFiles(*fileNames)
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY)
    myFixture.openFileInEditor(files[count - 1].virtualFile)
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_PASTE)
    myFixture.checkResultByFile(getTestName(false) + "_" + count + "_after.vue")
  }
  
  fun testSimpleWithImports() {
    doTest(3)
  }

  fun testSimpleWithNoImports() {
    doTest(3)
  }

  fun testSimpleWithNoImportsBindingContext() {
    doTest(3)
  }
}