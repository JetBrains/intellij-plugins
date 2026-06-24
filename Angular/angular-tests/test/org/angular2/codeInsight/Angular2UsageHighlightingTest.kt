// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import org.angular2.Angular2TestCase
import org.angular2.SkipTsGoProxy
import org.angular2.TestNoService
import org.angular2.TestTsGoProxy
import org.junit.Test

@TestNoService
@TestTsGoProxy
class Angular2UsageHighlightingTest : Angular2TestCase("usageHighlighting") {

  @Test
  @SkipTsGoProxy // Fails
  fun testPrivateComponentField() = doUsageHighlightingTest()

  @Test
  @SkipTsGoProxy // Fails
  fun testPublicComponentField() = doUsageHighlightingTest()

  @Test
  fun testDirectiveInputFromField() = doUsageHighlightingTest()

  @Test
  @SkipTsGoProxy // Fails
  fun testDirectiveInputFromMapping() = doUsageHighlightingTest()

  @Test
  fun testDirectiveInputFromTemplate() = doUsageHighlightingTest()

  @Test
  fun testDirectiveSelectorFromDefinition() = doUsageHighlightingTest()

  @Test
  fun testDirectiveSelectorFromUsage() = doUsageHighlightingTest()

  @Test
  fun testDirectiveExportAsFromDefinition() = doUsageHighlightingTest()

  @Test
  fun testDirectiveExportAsFromUsage() = doUsageHighlightingTest()

  @Test
  fun testStructuralDirectiveWithNgTemplateSelectorFromBinding() = doUsageHighlightingTest()

  @Test
  fun testStructuralDirectiveWithNgTemplateSelectorFromPlainAttr() = doUsageHighlightingTest()

  @Test
  fun testStructuralDirectiveWithNgTemplateSelectorFromDefinition() = doUsageHighlightingTest()

  @Test
  fun testModelSignalFromDeclaration() = doUsageHighlightingTest()

  @Test
  fun testModelSignalFromUsage1() = doUsageHighlightingTest()

  @Test
  fun testModelSignalFromUsage2() = doUsageHighlightingTest()

  @Test
  fun testModelSignalFromUsage3() = doUsageHighlightingTest()

  @Test
  fun testModelSignalAliasedFromDeclaration() = doUsageHighlightingTest()

  @Test
  fun testModelSignalAliasedFromUsage1() = doUsageHighlightingTest()

  @Test
  fun testModelSignalAliasedFromUsage2() = doUsageHighlightingTest()

  @Test
  fun testModelSignalAliasedFromUsage3() = doUsageHighlightingTest()

  @Test
  fun testInputAndSelectorFromSelector() = doUsageHighlightingTest()

  @Test
  fun testInputAndSelectorFromInput() = doUsageHighlightingTest()

  @Test
  fun testInputAndSelectorFromUsage() = doUsageHighlightingTest()

  @Test
  fun testInputAndSelectorSeparateDirectivesFromInputField() = doUsageHighlightingTest()

  @Test
  fun testInputAndSelectorSeparateDirectivesFromInputDecorator() = doUsageHighlightingTest()

  @Test
  fun testInputAndSelectorSeparateDirectivesFromSelector1() = doUsageHighlightingTest()

  @Test
  fun testInputAndSelectorSeparateDirectivesFromSelector2() = doUsageHighlightingTest()

  @Test
  fun testInputAndSelectorNotImportedDirective() = doUsageHighlightingTest()

  @Test
  fun testNgClass() = doUsageHighlightingTest()
}