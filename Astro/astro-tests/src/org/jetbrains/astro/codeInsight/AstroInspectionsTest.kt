package org.jetbrains.astro.codeInsight

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.inspections.JSStringConcatenationToES6TemplateInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.AstroFixture
import org.jetbrains.astro.astroFixture
import org.jetbrains.astro.inspections.AstroMissingComponentImportInspection
import kotlin.reflect.KClass
import org.junit.jupiter.api.Test

@TestApplication
class AstroInspectionsTest {

  private val astro: AstroFixture by astroFixture("codeInsight/inspections")

  @Test
  fun testMissingComponentImport() = doTest(AstroMissingComponentImportInspection::class,
                                            AstroBundle.message("astro.quickfix.import.component.name", "Component"),
                                            additionalFiles = listOf("Component.astro"))

  @Test
  fun testMissingComponentImportFalsePositive() = doTest(AstroMissingComponentImportInspection::class,
                                                         null, additionalFiles = listOf("Nav.astro"))

  @Test
  fun testMissingTsSymbolImport() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                           "Insert 'import {Colors} from \"./colors\"'",
                                           additionalFiles = listOf("colors.ts"))

  @Test
  fun testUnresolvedVariableInExprNoFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                           JavaScriptBundle.message("javascript.create.variable.intention.name", "test"))

  @Test
  fun testUnresolvedVariableInExprWithFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                             JavaScriptBundle.message("javascript.create.variable.intention.name", "test"))

  @Test
  fun testUnresolvedVariableInFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                     JavaScriptBundle.message("javascript.create.variable.intention.name", "test"))

  @Test
  fun testUnresolvedVariableInRawTextExpression() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                           JavaScriptBundle.message("javascript.create.variable.intention.name", "test"))

  @Test
  fun testUnresolvedFunctionCallInExprNoFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                               JavaScriptBundle.message("javascript.create.function.intention.name",
                                                                                        "test"))

  @Test
  fun testUnresolvedFunctionCallInExprWithFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                                 JavaScriptBundle.message("javascript.create.function.intention.name",
                                                                                          "test"))

  @Test
  fun testUnresolvedFunctionCallInFrontmatter() = doTest(TypeScriptUnresolvedReferenceInspection::class,
                                                         JavaScriptBundle.message("javascript.create.function.intention.name",
                                                                                  "test"))

  @Test
  fun testReplaceWithTemplateString() = doTest(JSStringConcatenationToES6TemplateInspection::class,
                                               JavaScriptBundle.message("js.replace.string.concatenation.with.es6.template.fix.text"))

  //region Test configuration and helper methods

  private fun doTest(
    inspection: KClass<out LocalInspectionTool>,
    quickFixName: String? = null,
    additionalFiles: List<String> = emptyList(),
  ) {
    astro.doConfiguredTest(additionalFiles = additionalFiles, checkResult = quickFixName != null) {
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