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
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.moveToOffsetBySignature
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

  fun testExpandShorthandPropertyJS() {
    myFixture.enableInspections(ES6ShorthandObjectPropertyInspection())
    doIntentionTest(JavaScriptBundle.message("js.expand.shorthand.property.quick.fix"))
    myFixture.checkHighlighting()
  }

  fun testExpandShorthandPropertyTS() {
    myFixture.enableInspections(ES6ShorthandObjectPropertyInspection())
    doIntentionTest(JavaScriptBundle.message("js.expand.shorthand.property.quick.fix"))
    myFixture.checkHighlighting()
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
    for (signature in listOf("v-<caret>model=", "v-<caret>model.lazy", "v-<caret>model.number", "v-<caret>model.trim")) {
      myFixture.moveToOffsetBySignature(signature)
      val intention = myFixture.findSingleIntention("Expand v-model")
      TestCase.assertTrue(intention.startInWriteAction())
      WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }
    }
    myFixture.checkResultByFile("expandVModel.after.vue")
  }

  fun testExternalSymbolsImport() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.copyDirectoryToProject(getTestName(true), ".")

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
    myFixture.checkResultByFile("${getTestName(true)}/HelloWorld.after.vue")


    myFixture.configureFromTempProjectFile("HelloWorldClassic.vue")
    doTest()
    myFixture.checkResultByFile("${getTestName(true)}/HelloWorldClassic.after.vue")
  }

  fun testImportNoScriptOrScriptSetupComponentInCode() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.copyDirectoryToProject(getTestName(true), ".")

    myFixture.configureFromTempProjectFile("test.ts")
    for (signature in listOf("NoScript<caret>Component", "Script<caret>SetupComponent")) {
      myFixture.moveToOffsetBySignature(signature)
      val intention = myFixture.findSingleIntention("Insert 'import")
      myFixture.launchAction(intention)
    }
    myFixture.checkResultByFile("${getTestName(true)}/test.after.ts")
  }

  fun testImportGlobalComponent() {
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PRIMEVUE_3_8_2)
    myFixture.configureFromTempProjectFile("test.vue")

    for (signature in listOf("<F<caret>oo", "<A<caret>vatar")) {
      myFixture.moveToOffsetBySignature(signature)
      val intention = try {
        myFixture.findSingleIntention("Import component locally")
      } catch (e: AssertionError) {
        throw AssertionError("Failed for $signature: ${e.message}", e)
      }
      WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }
    }

    myFixture.checkResultByFile("${getTestName(true)}/test.after.vue")
  }

  fun testAddMissingComponentImport() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)

    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.moveToOffsetBySignature("to-be-<caret>imported")

    val intention = myFixture.findSingleIntention("Import 'ToBeImported' component")
    WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }

    myFixture.checkResultByFile("${getTestName(true)}/test.after.vue")
  }

  fun testAddMissingFunctionImport() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)

    myFixture.configureFromTempProjectFile("components/Test.vue")
    myFixture.moveToOffsetBySignature("get<caret>Client()")

    val intention = myFixture.findSingleIntention("Insert 'import")
    WriteCommandAction.runWriteCommandAction(myFixture.project) { intention.invoke(project, myFixture.editor, myFixture.file) }

    myFixture.checkResultByFile("${getTestName(true)}/components/Test.after.vue")
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
