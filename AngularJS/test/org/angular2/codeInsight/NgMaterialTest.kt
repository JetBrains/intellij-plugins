// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.lang.javascript.inspections.UnterminatedStatementJSInspection
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angularjs.AngularTestUtil

class NgMaterialTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "ngMaterial"
  }

  fun testTemplatesWithSuperConstructors() {
    myFixture.enableInspections(UnterminatedStatementJSInspection::class.java,
                                HtmlUnknownAttributeInspection::class.java,
                                AngularUndefinedBindingInspection::class.java)
    myFixture.copyDirectoryToProject("node_modules", ".")
    myFixture.configureByFiles("templateTest.html", "package.json")
    myFixture.checkHighlighting()
  }
}
