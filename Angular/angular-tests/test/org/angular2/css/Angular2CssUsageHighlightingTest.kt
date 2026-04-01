package org.angular2.css

import org.angular2.Angular2TestCase

class Angular2CssUsageHighlightingTest : Angular2TestCase("css/usageHighlighting", false) {

  fun testClassFromBinding() = doUsageHighlightingTest()

  fun testClassFromCss() = doUsageHighlightingTest()

  fun testClassFromHostBinding() = doUsageHighlightingTest()

  fun testClassFromHostBindingAttribute() = doUsageHighlightingTest()

  fun testClassFromHostBindingDecorator() = doUsageHighlightingTest()

  fun testClassFromHtmlAttribute() = doUsageHighlightingTest()

  fun testClassFromNgClassBinding() = doUsageHighlightingTest()
}