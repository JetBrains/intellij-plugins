// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.web.moveToOffsetBySignature
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle

class VueIntentionsTest : BasePlatformTestCase() {

  override fun getBasePath(): String {
    return "" // not used
  }

  override fun getTestDataPath(): String = getVueTestDataPath() + "/intentions"

  fun testComputeConstant() {
    doIntentionTest(JSIntentionBundle.message("string.join-concatenated-string-literals.display-name"))
  }

  fun testFlipConditional() {
    doIntentionTest(JSIntentionBundle.message("conditional.flip-conditional.display-name"))
  }

  fun testPropagateToDestructuring() {
    doIntentionTest(JavaScriptBundle.message("refactoring.destructuring.vars.intention.propagate"))
  }

  fun testPropagateToDestructuringWhenWithIndexer() {
    doIntentionTest(JavaScriptBundle.message("refactoring.destructuring.vars.intention.propagate"))
  }

  fun testDepropagateFromDestructuring() {
    doIntentionTest(JavaScriptBundle.message("refactoring.destructuring.vars.intention.depropagate"))
  }

  fun testReplaceIfElseWithElvis() {
    doIntentionTest(JSIntentionBundle.message("trivialif.replace-if-with-conditional.display-name"))
  }

  fun testReplaceWithIndexerAccess() {
    JSTestUtils.testWithTempCodeStyleSettings<RuntimeException>(project) { settings ->
      val custom = settings.getCustomSettings(JSCodeStyleSettings::class.java)
      custom.USE_DOUBLE_QUOTES = true
      doIntentionTest("Replace with indexer access")
    }
  }

  fun testExpandVModel() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    myFixture.configureByFile("expandVModel.vue")
    for (signature in listOf("v-<caret>model=","v-<caret>model.lazy","v-<caret>model.number","v-<caret>model.trim")) {
      myFixture.moveToOffsetBySignature(signature)
      myFixture.findSingleIntention("Expand v-model").invoke(project, myFixture.editor, myFixture.file)
    }
    myFixture.checkResultByFile("expandVModel.after.vue")
  }

  private fun doIntentionTest(name: String) {
    val intention = myFixture.getAvailableIntention(name, getTestName(true) + ".vue")
    if (intention == null) {
      TestCase.fail("Intention by name $name not found")
      return
    }
    WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(myFixture.project, myFixture.editor, myFixture.file) }

    myFixture.checkResultByFile(getTestName(true) + "_after.vue")
  }
}
