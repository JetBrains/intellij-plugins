// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.codeInsight.hints.settings.ParameterNameHintsSettings
import com.intellij.codeInsight.hints.settings.ParameterNameHintsSettings.Companion.getInstance
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil

class Angular2ParameterHintsTest : Angular2CodeInsightFixtureTestCase() {
  @Throws(Exception::class)
  override fun tearDown() {
    try {
      val def = ParameterNameHintsSettings()
      getInstance().loadState(def.getState())
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun doTest() {
    val testName = getTestName(false)
    myFixture.configureByFiles("$testName.html", "$testName.ts", "package.json")
    myFixture.testInlays()
  }

  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "editor"
  }

  fun testParameterHintsInHtml() {
    doTest()
  }
}
