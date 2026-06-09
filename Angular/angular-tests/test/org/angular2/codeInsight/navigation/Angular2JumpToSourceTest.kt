package org.angular2.codeInsight.navigation

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestTsGoProxy
import org.angular2.TestTsNode
import org.junit.Test

@TestTsNode
@TestTsGoProxy
class Angular2JumpToSourceTest : Angular2TestCase("navigation/jumpToSource/") {

  @Test
  fun testDirectiveInput() =
    doJumpToSourceTest(
      "<caret>test = input<string>(\"\")",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )

  @Test
  fun testElementSelector() =
    doJumpToSourceTest(
      "selector: '<caret>app-root'",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )

  @Test
  fun testJsReference() =
    doJumpToSourceTest(
      "<caret>test = input<string>(\"\")",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )
}