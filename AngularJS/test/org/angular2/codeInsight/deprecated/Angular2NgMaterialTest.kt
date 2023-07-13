// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.lang.javascript.inspections.UnterminatedStatementJSInspection
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.inspections.AngularUndefinedBindingInspection
import org.angularjs.AngularTestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2NgMaterialTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "codeInsight/deprecated/ngMaterial"
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
