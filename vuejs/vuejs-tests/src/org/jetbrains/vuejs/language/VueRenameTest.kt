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
package org.jetbrains.vuejs.language

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class VueRenameTest : LightPlatformCodeInsightFixtureTestCase() {

  override fun getBasePath(): String {
    return "" // not used
  }

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/rename"

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

  fun testComponentNameFromDeclaration() {
    val testFile1 = getTestName(true) + "1.vue"
    val testFile2 = getTestName(true) + "2.vue"
    val testFile3 = getTestName(true) + ".html"
    val testFile4 = getTestName(true) + ".ts"
    val testFile5 = getTestName(true) + ".js"
    val testFileAfter1 = getTestName(true) + "1_after.vue"
    val testFileAfter2 = getTestName(true) + "2_after.vue"
    myFixture.configureByFile(testFile5)
    myFixture.configureByFile(testFile4)
    myFixture.configureByFile(testFile3)
    myFixture.configureByFile(testFile2)
    myFixture.configureByFile(testFile1)
    myFixture.testRename(testFileAfter1, "AfterComponent")
    myFixture.checkResultByFile(testFile1, testFileAfter1, true)
    myFixture.checkResultByFile(testFile2, testFileAfter2, true)
    myFixture.checkResultByFile(testFile3, testFile3, true)
    myFixture.checkResultByFile(testFile4, testFile4, true)
    myFixture.checkResultByFile(testFile5, testFile5, true)
  }

  private fun doTest(newName: String) {
    myFixture.configureByFile(getTestName(true) + ".vue")
    myFixture.renameElementAtCaret(newName)
    myFixture.checkResultByFile(getTestName(true) + "_after.vue")
  }

}