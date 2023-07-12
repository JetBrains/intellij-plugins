// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.semantics

import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.modules.Angular2TestModule
import org.angular2.modules.Angular2TestModule.Companion.configureCopy
import org.angularjs.AngularTestUtil

class Angular2SemanticsHighlightingTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass)
  }

  private fun checkSymbolNames() {
    val data = ExpectedHighlightingData(myFixture.getDocument(myFixture.getFile()), false, true, true, false)
    data.checkSymbolNames()
    data.init()
    (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
  }

  fun testCustomComponent() {
    configureCopy(myFixture, Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14,
                  Angular2TestModule.ANGULAR_FORMS_8_2_14)
    myFixture.configureByFiles("customComponent.html", "customComponent.ts", "customComponent2.ts")
    checkSymbolNames()
  }
}
