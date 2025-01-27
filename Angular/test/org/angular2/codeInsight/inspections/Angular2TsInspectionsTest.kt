// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.htmltools.codeInspection.htmlInspections.HtmlFormInputWithoutLabelInspection
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.inspections.*
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

/**
 * @see Angular2DecoratorInspectionsTest
 * @see Angular2TemplateInspectionsTest
 */
class Angular2TsInspectionsTest : Angular2TestCase("inspections/ts", false) {

  fun testUnusedSymbol() =
    checkHighlighting(Angular2TestModule.ANGULAR_CORE_16_2_8,
                      configureFileName = "unused.ts",
                      dir = true,
                      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java,
                                           JSUnusedLocalSymbolsInspection::class.java))

  fun testUnusedSymbolNg17() =
    checkHighlighting(Angular2TestModule.ANGULAR_CORE_17_3_0,
                      configureFileName = "unused.ts",
                      dir = true,
                      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java,
                                           JSUnusedLocalSymbolsInspection::class.java))

  fun testUnusedSetter() =
    checkHighlighting(Angular2TestModule.ANGULAR_CORE_16_2_8,
                      dir = true,
                      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java,
                                           JSUnusedLocalSymbolsInspection::class.java))

  fun testMethodCanBeStatic() =
    checkHighlighting(Angular2TestModule.ANGULAR_CORE_16_2_8, dir = true) {
      val canBeStaticInspection = JSMethodCanBeStaticInspection()
      JSTestUtils.setInspectionHighlightLevel(project, canBeStaticInspection, HighlightDisplayLevel.WARNING, testRootDisposable)
      enableInspections(canBeStaticInspection)
    }

  fun testUnterminated() =
    checkHighlighting(Angular2TestModule.ANGULAR_CORE_16_2_8,
                      inspections = listOf(UnterminatedStatementJSInspection::class.java))

  fun testUnusedReference() =
    doConfiguredTest(dir = true, checkResult = true, extension = "html") {
      enableInspections(JSUnusedGlobalSymbolsInspection::class.java,
                        JSUnusedLocalSymbolsInspection::class.java)
      checkHighlighting()
      for (attrToRemove in mutableListOf("notUsedRef", "anotherNotUsedRef", "notUsedRefWithAttr", "anotherNotUsedRefWithAttr")) {
        moveToOffsetBySignature("<caret>$attrToRemove")
        launchAction(
          try {
            findSingleIntention("Remove unused variable '$attrToRemove'")
          }
          catch (e: AssertionError) {
            findSingleIntention("Remove unused constant '$attrToRemove'")
          })
      }
    }

  fun testId() =
    checkHighlighting(Angular2TestModule.ANGULAR_CORE_16_2_8,
                      inspections = listOf(JSUnusedGlobalSymbolsInspection::class.java,
                                           JSUnusedLocalSymbolsInspection::class.java))

  fun testPipeAndArgResolution() =
    checkHighlighting(inspections = listOf(TypeScriptUnresolvedReferenceInspection::class.java),
                      dir = true,
                      extension = "html")

  fun testHtmlTargetWithInterpolation() =
    checkHighlighting(inspections = listOf(HtmlUnknownTargetInspection::class.java),
                      extension = "html")

  fun testDuplicateDeclarationOff() =
    checkHighlighting(inspections = listOf(JSDuplicatedDeclarationInspection::class.java),
                      dir = true,
                      extension = "html")

  fun testDuplicateDeclarationOffTemplate() =
    checkHighlighting(Angular2TestModule.ANGULAR_CORE_16_2_8,
                      inspections = listOf(JSDuplicatedDeclarationInspection::class.java))

  fun testEmptyVarDefinition() =
    checkHighlighting(inspections = listOf(JSUnusedLocalSymbolsInspection::class.java),
                      extension = "html")

  fun testMissingLabelSuppressed() =
    checkHighlighting(inspections = listOf(HtmlFormInputWithoutLabelInspection::class.java),
                      dir = true,
                      extension = "html")

}
