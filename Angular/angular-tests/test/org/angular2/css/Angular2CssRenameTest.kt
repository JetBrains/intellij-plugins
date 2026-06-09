package org.angular2.css

import org.angular2.Angular2TestCase
import org.angular2.TestNoService
import org.angular2.TestTsGoProxy
import org.junit.Test

@TestNoService
@TestTsGoProxy
class Angular2CssRenameTest : Angular2TestCase("css/refactoring/rename") {

  @Test
  fun testClassName() =
    doSymbolRenameTest("foo", dir = false)

}