package org.jetbrains.vuejs.language

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class VueEmmetTest : LightPlatformCodeInsightFixtureTestCase() {
  fun testImplicitTag() {
    myFixture.configureByText("a.vue", "<template>.cls<caret></template>")
    myFixture.type("\t")
    myFixture.checkResult("<template><div class=\"cls\"></div></template>")
  }
}

