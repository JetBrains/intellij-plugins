package org.angular2.css

import org.angular2.Angular2TestCase
import org.angular2.TestNoService
import org.angular2.TestTsGoFork
import org.junit.Test

@TestNoService
@TestTsGoFork
class Angular2CssUsageHighlightingTest : Angular2TestCase("css/usageHighlighting") {

  @Test
  fun testClassFromBinding() = doUsageHighlightingTest()

  @Test
  fun testClassFromCss() = doUsageHighlightingTest()

  @Test
  fun testClassFromHostBinding() = doUsageHighlightingTest()

  @Test
  fun testClassFromHostBindingAttribute() = doUsageHighlightingTest()

  @Test
  fun testClassFromHostBindingDecorator() = doUsageHighlightingTest()

  @Test
  fun testClassFromHtmlAttribute() = doUsageHighlightingTest()

  @Test
  fun testClassFromNgClassBinding() = doUsageHighlightingTest()
}