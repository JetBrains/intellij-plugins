// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import org.angular2.Angular2TestCase

class Angular2UsageHighlightingTest : Angular2TestCase("usageHighlighting", false) {

  fun testPrivateComponentField() = doUsageHighlightingTest()

  fun testPublicComponentField() = doUsageHighlightingTest()

  fun testDirectiveInputFromField() = doUsageHighlightingTest()

  fun testDirectiveInputFromMapping() = doUsageHighlightingTest()

  fun testDirectiveInputFromTemplate() = doUsageHighlightingTest()

  fun testDirectiveSelectorFromDefinition() = doUsageHighlightingTest()

  fun testDirectiveSelectorFromUsage() = doUsageHighlightingTest()

  fun testDirectiveExportAsFromDefinition() = doUsageHighlightingTest()

  fun testDirectiveExportAsFromUsage() = doUsageHighlightingTest()

  fun testStructuralDirectiveWithNgTemplateSelectorFromBinding() = doUsageHighlightingTest()

  fun testStructuralDirectiveWithNgTemplateSelectorFromPlainAttr() = doUsageHighlightingTest()

  fun testStructuralDirectiveWithNgTemplateSelectorFromDefinition() = doUsageHighlightingTest()

  fun testModelSignalFromDeclaration() = doUsageHighlightingTest()

  fun testModelSignalFromUsage1() = doUsageHighlightingTest()

  fun testModelSignalFromUsage2() = doUsageHighlightingTest()

  fun testModelSignalFromUsage3() = doUsageHighlightingTest()

  fun testModelSignalAliasedFromDeclaration() = doUsageHighlightingTest()

  fun testModelSignalAliasedFromUsage1() = doUsageHighlightingTest()

  fun testModelSignalAliasedFromUsage2() = doUsageHighlightingTest()

  fun testModelSignalAliasedFromUsage3() = doUsageHighlightingTest()

  fun testInputAndSelectorFromSelector() = doUsageHighlightingTest()

  fun testInputAndSelectorFromInput() = doUsageHighlightingTest()

  fun testInputAndSelectorFromUsage() = doUsageHighlightingTest()

  fun testInputAndSelectorSeparateDirectivesFromInputField() = doUsageHighlightingTest()

  fun testInputAndSelectorSeparateDirectivesFromInputDecorator() = doUsageHighlightingTest()

  fun testInputAndSelectorSeparateDirectivesFromSelector1() = doUsageHighlightingTest()

  fun testInputAndSelectorSeparateDirectivesFromSelector2() = doUsageHighlightingTest()

  fun testInputAndSelectorNotImportedDirective() = doUsageHighlightingTest()

  fun testNgClass() = doUsageHighlightingTest()
}