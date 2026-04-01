package org.angular2.codeInsight.navigation

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2JumpToSourceTest : Angular2TestCase("navigation/jumpToSource/", true) {

  fun testDirectiveInput() =
    doJumpToSourceTest(
      "<caret>test = input<string>(\"\")",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )

  fun testElementSelector() =
    doJumpToSourceTest(
      "selector: '<caret>app-root'",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )

  fun testJsReference() =
    doJumpToSourceTest(
      "<caret>test = input<string>(\"\")",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )
}