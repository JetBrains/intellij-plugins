// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.openapi.util.text.StringUtil
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil
import org.intellij.idea.lang.javascript.intention.JSIntentionBundle

class Angular2IntentionsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "intentions"
  }

  fun testComputeConstantInTemplate() {
    doTestForFile(getTestName(true), JSIntentionBundle.message("string.join-concatenated-string-literals.display-name"))
  }

  fun testFlipConditionalInTemplate() {
    doTestForFile(getTestName(true), JSIntentionBundle.message("conditional.flip-conditional.display-name"))
  }

  fun testDeMorgansLawInTemplate() {
    doTestForFile(getTestName(true), JSIntentionBundle.message("bool.de-morgans-law.display-name.ANDAND"))
  }

  private fun doTestForFile(name: String, intentionHint: String) {
    myFixture.setCaresAboutInjection(false)
    myFixture.configureByFiles("$name.html", "package.json")
    val action = myFixture.getAvailableIntentions().find { StringUtil.equals(it.getText(), intentionHint) }
                 ?: throw RuntimeException("Could not find intention by text $intentionHint")
    myFixture.launchAction(action)
    myFixture.checkResultByFile(getTestName(true) + "_after.html")
  }
}
