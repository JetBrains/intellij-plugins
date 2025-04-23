// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import org.angular2.Angular2TestCase

class Angular2FindUsagesTest : Angular2TestCase("navigation/findUsages", false) {

  fun testPrivateComponentField() = checkUsages()

  fun testPrivateComponentMethod() = checkUsages()

  fun testPrivateConstructorField() = checkUsages()

  fun testComponentCustomElementSelector() = checkUsages()

  fun testSlotComponentAttributeSelector() = checkUsages()

  fun testAttrVariable() = checkUsages(extension = "html")

  fun testExportAs() = checkUsages()

  fun testHostDirectiveOneTimeBinding() = checkUsages(fileName = "mouseenter.directive.ts")

  fun testDollarSymbolInInlineTemplate() = checkUsages(fileName = "hello1.service.ts")

  fun testComponentClassUsagesInTemplates() = checkUsages()
}
