package org.angular2.codeInsight.navigation

import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2JumpToSourceTest : Angular2TestCase("navigation/jumpToSource/", true) {

  fun testDirectiveInput() =
    checkJumpToSource(
      "<caret>test = input<string>(\"\")",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )

  fun testElementSelector() =
    checkJumpToSource(
      "selector: '<caret>app-root'",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )

  fun testJsReference() =
    checkJumpToSource(
      "<caret>test = input<string>(\"\")",
      Angular2TestModule.ANGULAR_CORE_20_1_4,
      expectedFileName = "app.component.ts",
      configureFileName = "app.component.html",
      dir = true,
    )
}