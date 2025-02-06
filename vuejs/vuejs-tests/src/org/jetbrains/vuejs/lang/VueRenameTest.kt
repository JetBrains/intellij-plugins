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

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestUtil
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import com.intellij.webSymbols.testFramework.renameWebSymbol

class VueRenameTest : BasePlatformTestCase() {

  override fun getBasePath(): String {
    return "" // not used
  }

  override fun getTestDataPath(): String = getVueTestDataPath() + "/rename"

  fun testComponentFieldFromTemplate() {
    doTest("newName")
  }

  fun testComponentFieldFromStringUsageInTemplate() {
    doTest("newName")
  }

  fun testTemplateLocalVariable() {
    doTest("newName")
  }

  fun testDestructuringInVFor() {
    doTest("newName")
  }

  fun testSlotProps() {
    doTest("newName")
  }

  fun testQualifiedWatchProperty() {
    doTest("newName")
  }

  fun testWatchProperty() {
    doTest("newName")
  }

  fun testInlineFieldRename() {
    myFixture.configureByFile("inlineField.vue")
    CodeInsightTestUtil.doInlineRename(VariableInplaceRenameHandler(), "foo", myFixture)
    myFixture.checkResultByFile("inlineField_after.vue")
  }

  fun testComponentNameFromDeclaration() {
    val testName = getTestName(true)
    val testFiles = listOf("1.vue", "2.vue", ".html", ".ts", ".js").map { testName + it }
    val afterFiles = listOf("1_after.vue", "2_after.vue").map { testName + it }
    testFiles.reversed().forEach { myFixture.configureByFile(it) }
    myFixture.testRename(afterFiles[0], "AfterComponent")
    testFiles.indices.forEach {
      myFixture.checkResultByFile(testFiles[it], afterFiles.getOrNull(it) ?: testFiles[it], true)
    }
  }

  fun testComponentNameFromPropertyName() {
    myFixture.configureByFile("componentNameFromDeclaration1.vue")
    doTest("AfterComponent")
  }

  fun testCssVBind() {
    doTest("newColor")
  }

  fun testCssVBindScriptSetup() {
    doTest("newColor", true)
  }

  fun testCreateAppComponent() {
    myFixture.copyDirectoryToProject("../common/createApp", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("main.ts")
    myFixture.moveToOffsetBySignature("\"C<caret>ar")
    myFixture.renameWebSymbol("NewCar")
    checkResultByDir()
  }

  fun testCreateAppComponentFromUsage() {
    myFixture.copyDirectoryToProject("../common/createApp", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("App.vue")
    myFixture.moveToOffsetBySignature("<C<caret>ar")
    myFixture.renameWebSymbol("NewCar")
    checkResultByDir("createAppComponent_after")
  }

  fun testCreateAppDirective() {
    myFixture.copyDirectoryToProject("../common/createApp", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("main.ts")
    myFixture.moveToOffsetBySignature("\"f<caret>oo")
    myFixture.renameWebSymbol("bar")
    checkResultByDir()
  }

  fun testCreateAppDirectiveFromUsage() {
    myFixture.copyDirectoryToProject("../common/createApp", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("TheComponent.vue")
    myFixture.moveToOffsetBySignature("v-f<caret>oo")
    myFixture.renameWebSymbol("bar")
    checkResultByDir("createAppDirective_after")
  }

  fun testNamespacedComponents() {
    myFixture.copyDirectoryToProject("../completion/namespacedComponents", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("scriptSetup.vue")
    myFixture.type("Forms.FooBars.Input")
    myFixture.moveToOffsetBySignature(".In<caret>put")
    myFixture.renameWebSymbol("NewName")
    checkResultByDir("namespacedComponents_after")
  }

  fun testCompositionApiLocalDirective() {
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    myFixture.configureFromTempProjectFile("scriptSetup.vue")
    myFixture.renameWebSymbol("vNewName")
    checkResultByDir("${getTestName(true)}_after")
  }

  fun testModelDeclaration() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    doTest("alignment")
  }

  fun testModelDeclarationWithVar() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    doTest("alignment")
  }

  fun testModelDeclarationProp() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    doTestDir("count")
  }

  fun testModelDeclarationEvent() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    doTestDir("count")
  }

  fun testDefinePropsRecordType() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    doTest("alignment")
  }

  fun testDefinePropsArrayLiteral() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    doTest("alignment")
  }

  fun testInjectLiteral() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    doTestDir("newName", true)
  }

  fun testComponentFile() {
    doTestRenameComponent("OrdersListView.vue", "SomeComponent.vue", false)
  }

  fun testComponentFileWithUsages() {
    doTestRenameComponent("OrdersListView.vue", "SomeComponent.vue", true)
  }

  fun testComponentFileWithReexports() {
    doTestRenameComponent("OrdersListView.vue", "SomeComponent.vue", true)
  }

  private fun doTest(newName: String, usingHandler: Boolean = false) {
    myFixture.configureByFile(getTestName(true) + ".vue")
    if (usingHandler) {
      val oldSetting = myFixture.editor.settings.isVariableInplaceRenameEnabled
      myFixture.editor.settings.isVariableInplaceRenameEnabled = false
      try {
        myFixture.renameElementAtCaretUsingHandler(newName)
      }
      finally {
        myFixture.editor.settings.isVariableInplaceRenameEnabled = oldSetting
      }
    }
    else {
      myFixture.renameElementAtCaret(newName)
    }
    myFixture.checkResultByFile(getTestName(true) + "_after.vue")
  }

  private fun doTestDir(newName: String, checkByDir: Boolean = false) {
    val dirName = getTestName(true)
    val testName = getTestName(false)
    myFixture.copyDirectoryToProject(dirName, "")
    myFixture.configureFromTempProjectFile("$testName.vue")
    myFixture.renameWebSymbol(newName)
    if (checkByDir) {
      checkResultByDir("${dirName}_after")
    }
    else {
      myFixture.checkResultByFile("$dirName/${testName}_after.vue")
    }
  }

  private fun doTestRenameComponent(newName: String, fileName: String, renameUsages: Boolean) {
    val dirName = getTestName(true)
    myFixture.copyDirectoryToProject(dirName, "")
    myFixture.configureFromTempProjectFile(fileName)

    withRenameUsages(renameUsages) {
      myFixture.renameElement(myFixture.file, newName)
      WriteCommandAction.runWriteCommandAction(project) { PostprocessReformattingAspect.getInstance(project).doPostponedFormatting() }
      FileDocumentManager.getInstance().saveAllDocuments()
    }

    checkResultByDir()
  }

  private fun checkResultByDir(resultsDir: String = getTestName(true) + "_after") {
    val extensions = setOf("vue", "html", "ts", "js")
    myFixture.tempDirFixture.findOrCreateDir(".")
      .children
      .filter { !it.isDirectory && extensions.contains(it.extension) }.forEach {
        myFixture.checkResultByFile(it.name, resultsDir + "/" + it.name, true)
      }
  }

}
