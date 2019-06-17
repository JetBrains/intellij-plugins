package org.jetbrains.vuejs.lang

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VueEmmetTest : BasePlatformTestCase() {
  fun testImplicitTag() {
    myFixture.configureByText("a.vue", "<template>.cls<caret></template>")
    myFixture.type("\t")
    myFixture.checkResult("<template><div class=\"cls\"></div></template>")
  }
}

