// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.css

import org.angular2.Angular2TestCase

class Angular2CssHighlightingTest: Angular2TestCase("css/highlighting", false) {

  // WEB-63400
  fun testCssAmpersandSelector() = checkHighlighting()

  // WEB-63587
  fun testLessParentSelector() = checkHighlighting("less")

  // WEB-63587
  fun testPcssAmpersand() = checkHighlighting("pcss")

  private fun checkHighlighting(
    extension: String = "css"
  ) {
    doConfiguredTest(configureFileName = "$testName.$extension") {
      checkHighlighting(true, true, true)
    }
  }
}
