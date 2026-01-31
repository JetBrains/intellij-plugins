package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalSimpleInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.incorrectFormatting.IncorrectFormattingResultHandlerProviderQodana.Companion.QODANA_ENABLE_NEW_INCORRECT_FORMATTING_OUTPUT_PROPERTY
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Test
import kotlin.io.path.absolutePathString

@TestDataPath("\$CONTENT_ROOT/testData/QodanaQuickFixesApplyTest")
class QodanaQuickFixesApplyTest: QodanaQuickFixesCommonTests(FixesStrategy.APPLY) {

  @Test
  fun testUnnecessaryCompare() {
    runTest("qodana.recommended")
  }

  @Test
  fun testIncorrectFormattingSimple() {
    withNewIncorrectFormattingOutput {
      runTestWithProfilePath(getTestDataPath("profile.yaml").absolutePathString())
      assertIncorrectFormattingRegionInvariant()
    }
  }

  @Test
  fun testIncorrectFormatting() {
    withNewIncorrectFormattingOutput {
      runTestWithProfilePath(getTestDataPath("profile.yaml").absolutePathString())
      assertIncorrectFormattingRegionInvariant()
    }
  }

  @Test
  fun testIncorrectFormattingWithAnotherInspections() {
    withNewIncorrectFormattingOutput {
      runTestWithProfilePath(getTestDataPath("profile.yaml").absolutePathString())
      assertIncorrectFormattingRegionInvariant()
    }
  }

  @Test
  fun testUnusedImports() {
    runTest("qodana.single:UNUSED_IMPORT")
  }

  @Test
  fun testSeveralGlobalSimpleInspections() {
    val tool = TestGlobalSimpleInspectionTool()
    registerGlobalTool(tool)
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    runTestWithProfilePath(getTestDataPath("profile.yaml").absolutePathString())
  }

  private fun assertIncorrectFormattingRegionInvariant() {
    val results = manager.sarifRun.results
    results.filter { it.properties?.get("problemType") == ProblemType.INCORRECT_FORMATTING.toString() }.forEach { result ->
      for (location in result.locations) {
        assertEquals(location.physicalLocation?.region?.startLine, 0)
        assertEquals(location.physicalLocation?.region?.startColumn, 0)
        assertEquals(location.physicalLocation?.region?.charLength, 0)
      }
    }
  }

  private fun withNewIncorrectFormattingOutput(action: () -> Unit) {
    System.setProperty(QODANA_ENABLE_NEW_INCORRECT_FORMATTING_OUTPUT_PROPERTY, "true")
    action()
    System.clearProperty(QODANA_ENABLE_NEW_INCORRECT_FORMATTING_OUTPUT_PROPERTY)
  }
}

private class TestGlobalSimpleInspectionTool : GlobalSimpleInspectionTool() {
  override fun getShortName(): String {
    return "testGlobalSimpleInspectionName"
  }

  override fun getGroupDisplayName(): String {
    return "testGroup"
  }

  override fun checkFile(psiFile: PsiFile,
                         manager: InspectionManager,
                         problemsHolder: ProblemsHolder,
                         context: GlobalInspectionContext,
                         processor: ProblemDescriptionsProcessor) {
    val fileText = psiFile.text
    if (!fileText.startsWith("// test")) {
      problemsHolder.registerProblem(
        psiFile,
        "The file does not start with '// test' comment",
        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        TextRange(0, 0),
        AddCommentQuickFix()
      )
    }
  }
}

private class AddCommentQuickFix: PsiUpdateModCommandQuickFix() {
  override fun getFamilyName() = "Add blank line"

  override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
    val file = element.containingFile
    if (file != null) {
      val document = file.viewProvider.document
      if (document != null) {
        WriteAction.run<RuntimeException> {
          document.insertString(0, "// test\n")
        }
      }
    }
  }
}