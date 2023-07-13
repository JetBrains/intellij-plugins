// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.inspections.AngularInaccessibleComponentMemberInAotModeInspection
import org.angularjs.AngularTestUtil

class Angular2InaccessibleMemberAotInspectionTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "inspections/aot"
  }

  fun testAotInaccessibleMemberTs() {
    myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection::class.java)
    myFixture.configureByFiles("private-ts.ts", "private-ts.html", "package.json")
    myFixture.checkHighlighting()
  }

  fun testAotInaccessibleMemberHtml() {
    myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection::class.java)
    myFixture.configureByFiles("private-html.html", "private-html.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testAotInaccessibleMemberInline() {
    myFixture.setCaresAboutInjection(false)
    myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection::class.java)
    myFixture.configureByFiles("private-inline.ts", "package.json")
    myFixture.checkHighlighting()
  }
}
