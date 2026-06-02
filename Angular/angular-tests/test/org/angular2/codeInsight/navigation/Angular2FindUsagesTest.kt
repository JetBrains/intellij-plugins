// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import org.angular2.Angular2TestCase
import org.angular2.TestNoService
import org.angular2.TestTsGoFork
import org.junit.Test

@TestNoService
@TestTsGoFork
class Angular2FindUsagesTest : Angular2TestCase("navigation/findUsages") {

  @Test
  fun testPrivateComponentField() = doFindUsagesTest()

  @Test
  fun testPrivateComponentMethod() = doFindUsagesTest()

  @Test
  fun testPrivateConstructorField() = doFindUsagesTest()

  @Test
  fun testComponentCustomElementSelector() = doFindUsagesTest()

  @Test
  fun testSlotComponentAttributeSelector() = doFindUsagesTest()

  @Test
  fun testAttrVariable() = doFindUsagesTest(extension = "html")

  @Test
  fun testExportAs() = doFindUsagesTest()

  @Test
  fun testHostDirectiveOneTimeBinding() = doFindUsagesTest(configureFileName = "mouseenter.directive.ts")

  @Test
  fun testDollarSymbolInInlineTemplate() = doFindUsagesTest(configureFileName = "hello1.service.ts")

  @Test
  fun testComponentClassUsagesInTemplates() = doFindUsagesTest()
}
