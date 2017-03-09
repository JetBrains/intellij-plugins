package org.jetbrains.vuejs.language

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class VueCompletionTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testCompleteCssClasses() {
    myFixture.configureByText("a.css", ".externalClass {}")
    myFixture.configureByText("a.vue", "<template><div class=\"<caret>\"></div></template><style>.internalClass</style>")
    myFixture.completeBasic()
    assertSameElements(myFixture.lookupElementStrings!!, "externalClass", "internalClass")
  }
}

