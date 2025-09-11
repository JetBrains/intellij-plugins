package org.angular2.codeInsight

import com.intellij.psi.codeStyle.arrangement.AbstractRearrangerTest
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureDependencies
import org.angular2.lang.expr.Angular20Language
import org.angular2.lang.html.Angular20HtmlFileType

class Angular2RearrangerTest  : AbstractRearrangerTest(){

  override fun setUp() {
    super.setUp()
    fileType = Angular20HtmlFileType
    language = Angular20Language
  }

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
    doTestWithSettings(before.trimIndent(), expected.trimIndent(), null, null)
  }
}