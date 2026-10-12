package org.angular2.codeInsight

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.psi.codeStyle.arrangement.RearrangerTestUtil
import com.intellij.testFramework.junit5.TestApplication
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureDependencies
import org.angular2.lang.expr.Angular20Language
import org.angular2.lang.html.Angular20HtmlFileType
import org.junit.jupiter.api.Test

@TestApplication
class Angular2RearrangerTest {

  private val projectFixture = codeInsightProjectFixture()

  private val myFixture by codeInsightFixture(projectFixture)

  @Test
  fun testAttributesSortingWithinBlock() {
    doTestWithDefaultSettings(
      """
        @if (show) {
          <input
            (click)="onClick()"
            id="idName"
            type="number"
            formControlName="controlName"
            class="className"
          >
        }
      """,
      """
        @if (show) {
          <input
            (click)="onClick()"
            class="className"
            formControlName="controlName"
            id="idName"
            type="number"
          >
        }
      """)
  }

  private fun doTestWithDefaultSettings(before: String, expected: String) {
    myFixture.configureDependencies(Angular2TestModule.ANGULAR_CORE_20_1_4)
    RearrangerTestUtil.doTestWithSettings(myFixture, Angular20HtmlFileType, Angular20Language, before.trimIndent(), expected.trimIndent(), null, null)
  }
}