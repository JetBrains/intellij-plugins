package org.jetbrains.astro.codeInsight

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSStringConcatenationToES6TemplateInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.inspections.AstroMissingComponentImportInspection
import kotlin.reflect.KClass

class AstroInspectionsTest : AstroCodeInsightTestCase("codeInsight/inspections") {

  fun testMissingComponentImport() = doTest(AstroMissingComponentImportInspection::class,
                                            AstroBundle.message("astro.quickfix.import.component.name", "Component"),
                                            additionalFiles = listOf("Component.astro"))

  fun testMissingComponentImportFalsePositive() = doTest(AstroMissingComponentImportInspection::class,
                                                         null, additionalFiles = listOf("Nav.astro"))

  fun testMissingTsSymbolImport() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                           "Insert 'import {Colors} from \"./colors\"'",
                                           additionalFiles = listOf("colors.ts"))

  fun testUnresolvedVariableInExprNoFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                           JavaScriptBundle.message("javascript.create.variable.intention.name", "test"))

  fun testUnresolvedVariableInExprWithFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                             JavaScriptBundle.message("javascript.create.variable.intention.name", "test"))

  fun testUnresolvedVariableInFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                     JavaScriptBundle.message("javascript.create.variable.intention.name", "test"))

  fun testUnresolvedFunctionCallInExprNoFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                               JavaScriptBundle.message("javascript.create.function.intention.name",
                                                                                        "test"))

  fun testUnresolvedFunctionCallInExprWithFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                                 JavaScriptBundle.message("javascript.create.function.intention.name",
                                                                                          "test"))

  fun testUnresolvedFunctionCallInFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                         JavaScriptBundle.message("javascript.create.function.intention.name",
                                                                                  "test"))

  fun testReplaceWithTemplateString() = doTest(JSStringConcatenationToES6TemplateInspection::class,
                                               JavaScriptBundle.message("js.replace.string.concatenation.with.es6.template.fix.text"))

  //region Test configuration and helper methods

  private fun doTest(inspection: KClass<out LocalInspectionTool>,
                     quickFixName: String? = null,
                     additionalFiles: List<String> = emptyList()) {
    doConfiguredTest(additionalFiles = additionalFiles, checkResult = quickFixName != null) {
      enableInspections(inspection.java)
      checkHighlighting()
      if (quickFixName == null) {
        return@doConfiguredTest
      }
      launchAction(findSingleIntention(quickFixName))
    }
  }

  //endregion
}