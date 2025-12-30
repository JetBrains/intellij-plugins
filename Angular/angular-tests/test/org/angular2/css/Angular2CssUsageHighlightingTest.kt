package org.angular2.css

import org.angular2.Angular2TestCase

class Angular2CssUsageHighlightingTest : Angular2TestCase("css/usageHighlighting", false) {

  fun testClassFromBinding() = checkUsageHighlighting()

  fun testClassFromCss() = checkUsageHighlighting()

  fun testClassFromHostBinding() = checkUsageHighlighting()

  fun testClassFromHostBindingAttribute() = checkUsageHighlighting()

  fun testClassFromHostBindingDecorator() = checkUsageHighlighting()

  fun testClassFromHtmlAttribute() = checkUsageHighlighting()

  fun testClassFromNgClassBinding() = checkUsageHighlighting()
}