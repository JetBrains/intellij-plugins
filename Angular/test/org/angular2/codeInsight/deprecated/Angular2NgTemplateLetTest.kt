// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.deprecated

import com.intellij.testFramework.UsefulTestCase
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureDependencies
import org.angular2.Angular2TestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2NgTemplateLetTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "deprecated/ngTemplateLet"
  }

  fun testNgFor() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    val variants = myFixture.getCompletionVariants("NgFor.ts")
    assertNotNull(variants)
    UsefulTestCase.assertSameElements(variants!!, "isPrototypeOf", "propertyIsEnumerable", "valueOf", "is_hidden", "constructor",
                                      "created_at",
                                      "hasOwnProperty", "updated_at", "toString", "email", "username", "toLocaleString")
  }

  fun testNgForInspections() {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_8_2_14, Angular2TestModule.ANGULAR_COMMON_8_2_14)
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("NgForInspections.ts")
    myFixture.checkHighlighting()
  }
}
