// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import org.angular2.Angular2TestCase

class Angular2FindUsagesTest : Angular2TestCase("navigation/findUsages", false) {

  fun testPrivateComponentField() = doUsagesTest()

  fun testPrivateComponentMethod() = doUsagesTest()

  fun testPrivateConstructorField() = doUsagesTest()

  fun testComponentCustomElementSelector() = doUsagesTest()

  fun testSlotComponentAttributeSelector() = doUsagesTest()

  fun testAttrVariable() = doUsagesTest(extension = "html")

  fun testExportAs() = doUsagesTest()

  fun testHostDirectiveOneTimeBinding() = doUsagesTest(fileName = "mouseenter.directive.ts")

  fun testDollarSymbolInInlineTemplate() = doUsagesTest(fileName = "hello1.service.ts")

  fun testComponentClassUsagesInTemplates() = doUsagesTest()
}
