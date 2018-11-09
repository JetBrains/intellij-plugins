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
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle

class VueIntentionsTest : LightPlatformCodeInsightFixtureTestCase() {

  override fun getBasePath(): String {
    return "" // not used
  }

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/intentions"

  fun testComputeConstant() {
    doIntentionTest(JSIntentionBundle.message("constant.constant-expression.display-name"))
  }

  fun testFlipConditional() {
    doIntentionTest(JSIntentionBundle.message("conditional.flip-conditional.display-name"))
  }

  private fun doIntentionTest(name: String) {
    val intention = myFixture.getAvailableIntention(name, getTestName(true) + ".vue")
    if (intention == null){
      TestCase.fail("Intention by name $name not found")
      return
    }
    WriteCommandAction.runWriteCommandAction(myFixture.project, { intention.invoke(myFixture.project, myFixture.editor, myFixture.file) })
    
    myFixture.checkResultByFile(getTestName(true) + "_after.vue")
  }
}