// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.deprecated

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.containers.ContainerUtil
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureDependencies
import org.angular2.Angular2TestUtil

@Deprecated("Use test appropriate for IDE feature being tested - e.g. completion/resolve/highlighting ")
class Angular2NgForTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "deprecated/ngFor"
  }

  fun testNgFor() {
    val variants = myFixture.getCompletionVariants("NgFor.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json")
    assertNotNull(variants)
    UsefulTestCase.assertSameElements(variants!!, "isPrototypeOf", "propertyIsEnumerable", "valueOf", "is_hidden", "constructor",
                                      "created_at",
                                      "hasOwnProperty", "updated_at", "toString", "email", "username", "toLocaleString")
  }

  fun testNgForInspections() {
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())
    myFixture.configureByFiles("NgForInspections.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json")
    myFixture.checkHighlighting()
  }

  fun testNgForWithinAttribute() {
    val variants = myFixture.getCompletionVariants(
      "NgForWithinAttribute.ts", "ng_for_of.ts", "iterable_differs.ts", "package.json")
    assertNotNull(variants)
    assertTrue(variants!!.size >= 2)
    assertEquals("created_at", variants[0])
    assertEquals("email", variants[1])
  }

  fun testNgForWithinAttributeHTML() {
    val variants = myFixture.getCompletionVariants(
      "NgForWithinAttributeHTML.html", "NgForWithinAttributeHTML.ts", "ng_for_of.ts", "iterable_differs.ts",
      "package.json")
    assertNotNull(variants)
    assertTrue(variants!!.size >= 2)
    assertEquals("created_at", variants[0])
    assertEquals("email", variants[1])
  }

  fun testNgForWithPipe() { // WEB-51209
    myFixture.enableInspections(*JSDaemonAnalyzerLightTestCase.configureDefaultLocalInspectionTools().toTypedArray())
    myFixture.configureByFiles("NgForWithPipeHTML.html", "NgForWithPipe.ts", "package.json")
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_13_3_5, Angular2TestModule.ANGULAR_COMMON_13_3_5)
    myFixture.checkHighlighting()
    myFixture.type('.')
    myFixture.complete(CompletionType.BASIC)
    UsefulTestCase.assertOrderedEquals(ContainerUtil.getFirstItems(
      Angular2TestUtil.renderLookupItems(myFixture, false, true, true), 2),
                                       "key (typeText='string')", "value (typeText='any')")
  }
}
