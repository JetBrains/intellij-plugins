package org.angular2.css

import org.angular2.Angular2TestCase

class Angular2CssRenameTest: Angular2TestCase("css/refactoring/rename", false) {

  fun testClassName() =
    doSymbolRenameTest("foo", dir = false)

}