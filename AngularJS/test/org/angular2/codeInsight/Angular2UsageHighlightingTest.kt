// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import org.angular2.Angular2TestCase

class Angular2UsageHighlightingTest : Angular2TestCase("usageHighlighting") {

  fun testPrivateComponentField() = checkUsageHighlighting()

  fun testPublicComponentField() = checkUsageHighlighting()

  fun testDirectiveInputFromField() = checkUsageHighlighting()

  fun testDirectiveInputFromMapping() = checkUsageHighlighting()

  // TODO platform - no usage highlighting outside of injection - com.intellij.codeInsight.highlighting.getSymbolUsageRanges()
  fun _testDirectiveInputFromTemplate() = checkUsageHighlighting()

  fun testDirectiveSelectorFromDefinition() = checkUsageHighlighting()

  // TODO platform - no usage highlighting outside of injection - com.intellij.codeInsight.highlighting.getSymbolUsageRanges()
  fun _testDirectiveSelectorFromUsage() = checkUsageHighlighting()

  fun testDirectiveExportAsFromDefinition() = checkUsageHighlighting()

  // TODO platform - no usage highlighting outside of injection - com.intellij.codeInsight.highlighting.getSymbolUsageRanges()
  fun _testDirectiveExportAsFromUsage() = checkUsageHighlighting()

}