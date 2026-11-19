package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class VueEmmetTest {
  private val hybrid by hybridFixture("")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testImplicitTag() {
    myFixture.configureByText("a.vue", "<template>.cls<caret></template>")
    JSTestUtils.runEmmetTemplate(myFixture)
    myFixture.checkResult("<template><div class=\"cls\"></div></template>")
  }
}

