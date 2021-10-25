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

import com.intellij.idea.Bombed
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestUtil

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

  fun testInlineFieldRename() {
    myFixture.configureByFile("inlineField.vue")
    CodeInsightTestUtil.doInlineRename(VariableInplaceRenameHandler(), "foo", myFixture)
    myFixture.checkResultByFile("inlineField_after.vue")
  }

  @Bombed(year = 2021, month = 12, day = 1, user = "piotr.tomiak", description = "Implement WebSymbol based rename")
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

  private fun doTest(newName: String) {
    myFixture.configureByFile(getTestName(true) + ".vue")
    myFixture.renameElementAtCaret(newName)
    myFixture.checkResultByFile(getTestName(true) + "_after.vue")
  }

}
