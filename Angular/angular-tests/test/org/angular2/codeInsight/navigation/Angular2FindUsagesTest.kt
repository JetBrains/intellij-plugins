// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import org.angular2.Angular2TestCase

class Angular2FindUsagesTest : Angular2TestCase("navigation/findUsages", false) {

  fun testPrivateComponentField() = doFindUsagesTest()

  fun testPrivateComponentMethod() = doFindUsagesTest()

  fun testPrivateConstructorField() = doFindUsagesTest()

  fun testComponentCustomElementSelector() = doFindUsagesTest()

  fun testSlotComponentAttributeSelector() = doFindUsagesTest()

  fun testAttrVariable() = doFindUsagesTest(extension = "html")

  fun testExportAs() = doFindUsagesTest()

  fun testHostDirectiveOneTimeBinding() = doFindUsagesTest(configureFileName = "mouseenter.directive.ts")

  fun testDollarSymbolInInlineTemplate() = doFindUsagesTest(configureFileName = "hello1.service.ts")

  fun testComponentClassUsagesInTemplates() = doFindUsagesTest()
}
