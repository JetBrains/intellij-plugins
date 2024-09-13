package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.*
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
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
    runTestWithProfilePath(getTestDataPath("profile.yaml").absolutePathString())
  }

  @Test
  fun testIncorrectFormatting() {
    runTestWithProfilePath(getTestDataPath("profile.yaml").absolutePathString())
  }

  @Test
  fun testIncorrectFormattingWithAnotherInspections() {
    runTestWithProfilePath(getTestDataPath("profile.yaml").absolutePathString())
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
}

private class TestGlobalSimpleInspectionTool : GlobalSimpleInspectionTool() {
  override fun getShortName(): String {
    return "testGlobalSimpleInspectionName"
  }

  override fun getGroupDisplayName(): String {
    return "testGroup"
  }

  override fun checkFile(file: PsiFile,
                         manager: InspectionManager,
                         problemsHolder: ProblemsHolder,
                         context: GlobalInspectionContext,
                         processor: ProblemDescriptionsProcessor) {
    val fileText = file.text
    if (!fileText.startsWith("// test")) {
      problemsHolder.registerProblem(
        file,
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