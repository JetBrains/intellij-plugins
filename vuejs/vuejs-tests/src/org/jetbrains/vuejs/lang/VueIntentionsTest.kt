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

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.inspections.ES6ShorthandObjectPropertyInspection
import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueIntentionsTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/intentions")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @Test
  fun testComputeConstant(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doIntentionTest(JSIntentionBundle.message("string.join-concatenated-string-literals.display-name"))
  }

  @Test
  fun testFlipConditional(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doIntentionTest(JSIntentionBundle.message("conditional.flip-conditional.display-name"))
  }

  @Test
  fun testPropagateToDestructuring(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doIntentionTest(JavaScriptBundle.message("refactoring.destructuring.vars.intention.propagate"))
  }

  @Test
  fun testPropagateToDestructuringWhenWithIndexer(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doIntentionTest(JavaScriptBundle.message("refactoring.destructuring.vars.intention.propagate"))
  }

  @Test
  fun testDepropagateFromDestructuring(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doIntentionTest(JavaScriptBundle.message("refactoring.destructuring.vars.intention.depropagate"))
  }

  @Test
  fun testReplaceIfElseWithElvis(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doIntentionTest(JSIntentionBundle.message("trivialif.replace-if-with-conditional.display-name"))
  }

  @Test
  fun testExpandShorthandPropertyJS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(ES6ShorthandObjectPropertyInspection())
    doIntentionTest(JavaScriptBundle.message("js.expand.shorthand.property.quick.fix"))
    myFixture.checkHighlighting()
  }

  @Test
  fun testExpandShorthandPropertyTS(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(ES6ShorthandObjectPropertyInspection())
    doIntentionTest(JavaScriptBundle.message("js.expand.shorthand.property.quick.fix"))
    myFixture.checkHighlighting()
  }

  @Test
  fun testReplaceWithIndexerAccess(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    JSTestUtils.testWithTempCodeStyleSettings<RuntimeException>(project) { settings ->
      val custom = settings.getCustomSettings(JSCodeStyleSettings::class.java)
      custom.USE_DOUBLE_QUOTES = true
      doIntentionTest("Replace with indexer access")
    }
  }

  @Test
  fun testExpandVModel(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    myFixture.configureByFile("expandVModel.vue")
    for (signature in listOf("v-<caret>model=", "v-<caret>model.lazy", "v-<caret>model.number", "v-<caret>model.trim")) {
      myFixture.moveToOffsetBySignature(signature)
      val intention = myFixture.findSingleIntention("Expand v-model")
      assertTrue(intention.startInWriteAction())
      WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }
    }
    myFixture.checkResultByFile("expandVModel.after.vue")
  }

  @Test
  fun testExternalSymbolsImport(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")

    fun doTest() {
      for (signature in listOf("\"Co<caret>lor.", "in it<caret>ems", "get<caret>Text()")) {
        myFixture.moveToOffsetBySignature(signature)
        val intention = myFixture.availableIntentions
                          .singleOrNull {
                            it.text.startsWith("Add import") // TS
                            || it.text.startsWith("Insert \"import") || it.text.startsWith("Insert \'import") // JS
                          }
                        ?: throw AssertionError("Failed to find single 'insert import' intention for $signature. " +
                                                "Available intentions: ${
                                                  myFixture.availableIntentions.map {
                                                    StringUtil.shortenPathWithEllipsis(it.text, 25)
                                                  }
                                                }")
        WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }
      }
    }

    myFixture.configureFromTempProjectFile("HelloWorld.vue")
    doTest()
    myFixture.checkResultByFile("${hybrid.getTestName(true)}/HelloWorld.after.vue")


    myFixture.configureFromTempProjectFile("HelloWorldClassic.vue")
    doTest()
    myFixture.checkResultByFile("${hybrid.getTestName(true)}/HelloWorldClassic.after.vue")
  }

  @Test
  fun testImportNoScriptOrScriptSetupComponentInCode(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0)
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")

    myFixture.configureFromTempProjectFile("test.ts")
    for (signature in listOf("NoScript<caret>Component", "Script<caret>SetupComponent")) {
      myFixture.moveToOffsetBySignature(signature)
      val intention = myFixture.findSingleIntention("Insert 'import")
      myFixture.launchAction(intention)
    }
    myFixture.checkResultByFile("${hybrid.getTestName(true)}/test.after.ts")
  }

  @Test
  fun testImportGlobalComponent(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0, VueTestModule.PRIMEVUE_3_8_2)
    myFixture.configureFromTempProjectFile("test.vue")

    for (signature in listOf("<F<caret>oo", "<A<caret>vatar")) {
      myFixture.moveToOffsetBySignature(signature)
      val intention = try {
        myFixture.findSingleIntention("Import component locally")
      }
      catch (e: AssertionError) {
        throw AssertionError("Failed for $signature: ${e.message}", e)
      }
      WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }
    }

    myFixture.checkResultByFile("${hybrid.getTestName(true)}/test.after.vue")
  }

  @Test
  fun testAddMissingComponentImport(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0)

    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.moveToOffsetBySignature("to-be-<caret>imported")

    val intention = myFixture.findSingleIntention("Import 'ToBeImported' component")
    WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }

    myFixture.checkResultByFile("${hybrid.getTestName(true)}/test.after.vue")
  }

  @Test
  fun testAddMissingComponentImportWithConflictingTagName(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0, additionalDependencies = mapOf("@inertiajs/vue3" to "1.0.14"))

    myFixture.configureFromTempProjectFile("${hybrid.getTestName(true)}.vue")
    myFixture.moveToOffsetBySignature("Li<caret>nk")

    val intention = myFixture.findSingleIntention("Import 'Link' component")
    WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }

    myFixture.checkResultByFile("${hybrid.getTestName(true)}/test.after.vue")
  }

  @Test
  fun testSkipMissingComponentImportForHtmlTags(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0, additionalDependencies = mapOf("@inertiajs/vue3" to "1.0.14"))

    myFixture.configureFromTempProjectFile("${hybrid.getTestName(true)}.vue")
    myFixture.moveToOffsetBySignature("li<caret>nk")

    assertNull(myFixture.getAvailableIntention("Import 'Link' component"))
  }

  @Test
  fun testAddMissingFunctionImport(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0)

    myFixture.configureFromTempProjectFile("components/Test.vue")
    myFixture.moveToOffsetBySignature("get<caret>Client()")

    val intention = myFixture.findSingleIntention("Insert 'import")
    WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }

    myFixture.checkResultByFile("${hybrid.getTestName(true)}/components/Test.after.vue")
  }


  private fun doIntentionTest(name: String) {
    val intention = myFixture.getAvailableIntention(name, hybrid.getTestName(true) + ".vue")
    if (intention == null) {
      fail<Unit>("Intention by name $name not found")
      return
    }
    WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(myFixture.project, myFixture.editor, myFixture.file) }

    myFixture.checkResultByFile(hybrid.getTestName(true) + "_after.vue")
  }
}
