// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.lang.javascript.inspections.JSOctalIntegerInspection
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil

class Angular2SuppressionsTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "suppressions"
  }

  fun testTemplateSuppressions() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.enableInspections(JSOctalIntegerInspection())
    myFixture.configureByFiles("template.html", "template.after.html", "package.json")
    for (location in mutableListOf("test1", "007", "pipe1")) {
      try {
        myFixture.moveToOffsetBySignature(location[0].toString() + "<caret>" + location.substring(1))
        myFixture.launchAction(myFixture.findSingleIntention("Suppress for expression"))
      }
      catch (err: AssertionError) {
        throw AssertionError("Failed at $location", err)
      }
    }
    for (location in mutableListOf("foo1", "var1")) {
      try {
        myFixture.moveToOffsetBySignature(location[0].toString() + "<caret>" + location.substring(1))
        UsefulTestCase.assertEmpty(myFixture.filterAvailableIntentions("Suppress for expression"))
        myFixture.launchAction(myFixture.findSingleIntention("Suppress for tag"))
      }
      catch (err: AssertionError) {
        throw AssertionError("Failed at $location", err)
      }
    }
    val after = myFixture.getPsiManager().findFile(
      myFixture.getTempDirFixture().getFile("template.after.html")!!)
    val data = ExpectedHighlightingData(
      myFixture.getDocument(after!!), true, true, false)
    data.init()
    (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
  }
}
