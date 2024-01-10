// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import org.angular2.Angular2TestCase

class Angular2GotoSymbolTest : Angular2TestCase("navigation/symbol/") {

  fun testElementSelector() = checkGotoSymbol("app-my-table")

  fun testAttributeSelector() = checkGotoSymbol("app-my-table")

  fun testAttrAndElementSelector() = checkGotoSymbol("app-my-table")

  fun testPipe() = checkGotoSymbol("foo", detailed = false)

}
