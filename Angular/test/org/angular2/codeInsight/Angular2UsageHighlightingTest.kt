// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import org.angular2.Angular2TestCase

class Angular2UsageHighlightingTest : Angular2TestCase("usageHighlighting", false) {

  fun testPrivateComponentField() = checkUsageHighlighting()

  fun testPublicComponentField() = checkUsageHighlighting()

  fun testDirectiveInputFromField() = checkUsageHighlighting()

  fun testDirectiveInputFromMapping() = checkUsageHighlighting()

  fun testDirectiveInputFromTemplate() = checkUsageHighlighting()

  fun testDirectiveSelectorFromDefinition() = checkUsageHighlighting()

  fun testDirectiveSelectorFromUsage() = checkUsageHighlighting()

  fun testDirectiveExportAsFromDefinition() = checkUsageHighlighting()

  fun testDirectiveExportAsFromUsage() = checkUsageHighlighting()

  fun testStructuralDirectiveWithNgTemplateSelectorFromBinding() = checkUsageHighlighting()

  fun testStructuralDirectiveWithNgTemplateSelectorFromPlainAttr() = checkUsageHighlighting()

  fun testStructuralDirectiveWithNgTemplateSelectorFromDefinition() = checkUsageHighlighting()

  fun testModelSignalFromDeclaration() = checkUsageHighlighting()

  fun testModelSignalFromUsage1() = checkUsageHighlighting()

  fun testModelSignalFromUsage2() = checkUsageHighlighting()

  fun testModelSignalFromUsage3() = checkUsageHighlighting()

  fun testModelSignalAliasedFromDeclaration() = checkUsageHighlighting()

  fun testModelSignalAliasedFromUsage1() = checkUsageHighlighting()

  fun testModelSignalAliasedFromUsage2() = checkUsageHighlighting()

  fun testModelSignalAliasedFromUsage3() = checkUsageHighlighting()

  fun testInputAndSelectorFromSelector() = checkUsageHighlighting()

  fun testInputAndSelectorFromInput() = checkUsageHighlighting()

  fun testInputAndSelectorFromUsage() = checkUsageHighlighting()

  fun testInputAndSelectorSeparateDirectivesFromInputField() = checkUsageHighlighting()

  fun testInputAndSelectorSeparateDirectivesFromInputDecorator() = checkUsageHighlighting()

  fun testInputAndSelectorSeparateDirectivesFromSelector1() = checkUsageHighlighting()

  fun testInputAndSelectorSeparateDirectivesFromSelector2() = checkUsageHighlighting()

  fun testInputAndSelectorNotImportedDirective() = checkUsageHighlighting()

  fun testNgClass() = checkUsageHighlighting()
}