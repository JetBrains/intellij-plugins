package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VueEmmetTest : BasePlatformTestCase() {
  fun testImplicitTag() {
    myFixture.configureByText("a.vue", "<template>.cls<caret></template>")
    JSTestUtils.runEmmetTemplate(myFixture)
    myFixture.checkResult("<template><div class=\"cls\"></div></template>")
  }
}

