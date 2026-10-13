package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalSimpleInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.ModCommandQuickFix
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.ModUpdateFileText
import com.intellij.modcommand.Presentation
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestDataPath
import com.intellij.util.concurrency.ThreadingAssertions
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.incorrectFormatting.IncorrectFormattingResultHandlerProviderQodana.Companion.QODANA_ENABLE_NEW_INCORRECT_FORMATTING_OUTPUT_PROPERTY
import org.jetbrains.qodana.staticAnalysis.sarif.ALLOW_NON_BATCH_FIXES
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

  @Test
  fun testModCommandChooseActionExecutedWithoutWriteLock() {
    runTestWithGlobalSimpleTool(ChooseActionGlobalSimpleInspectionTool())
  }

  @Test
  fun testNextModCommandFixAppliedWhenPreviousFails() {
    runTestWithGlobalSimpleTool(OutdatedFixFirstGlobalSimpleInspectionTool())
  }

  @Test
  fun testUnavailableModCommandFixSkipped() {
    runTestWithGlobalSimpleTool(UnavailableFixFirstGlobalSimpleInspectionTool())
  }

  @Test
  fun testNonModCommandFixAppliedInWriteCommand() {
    val previousValue = System.getProperty(ALLOW_NON_BATCH_FIXES)
    System.setProperty(ALLOW_NON_BATCH_FIXES, "true")
    try {
      runTestWithGlobalSimpleTool(NonModCommandGlobalSimpleInspectionTool())
    }
    finally {
      if (previousValue == null) System.clearProperty(ALLOW_NON_BATCH_FIXES) else System.setProperty(ALLOW_NON_BATCH_FIXES, previousValue)
    }
  }

  private fun runTestWithGlobalSimpleTool(tool: TestGlobalSimpleInspectionTool) {
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

private open class TestGlobalSimpleInspectionTool : GlobalSimpleInspectionTool() {
  protected open fun createFixes(): Array<LocalQuickFix> = arrayOf(AddCommentQuickFix())

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
        *createFixes()
      )
    }
  }
}

private class AddCommentQuickFix: PsiUpdateModCommandQuickFix() {
  override fun getFamilyName() = "Add blank line"

  override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
    ThreadingAssertions.assertBackgroundThread()
    ThreadingAssertions.assertReadAccess()
    val file = element.containingFile
    if (file != null) {
      file.viewProvider.document?.insertString(0, "// test\n")
    }
  }
}

private class ChooseActionGlobalSimpleInspectionTool : TestGlobalSimpleInspectionTool() {
  override fun createFixes(): Array<LocalQuickFix> = arrayOf(ChooseAddCommentQuickFix())
}

private class OutdatedFixFirstGlobalSimpleInspectionTool : TestGlobalSimpleInspectionTool() {
  override fun createFixes(): Array<LocalQuickFix> = arrayOf(OutdatedTextQuickFix(), AddCommentQuickFix())
}

private class UnavailableFixFirstGlobalSimpleInspectionTool : TestGlobalSimpleInspectionTool() {
  override fun createFixes(): Array<LocalQuickFix> = arrayOf(LocalQuickFix.from(UnavailableAction())!!, AddCommentQuickFix())
}

private class NonModCommandGlobalSimpleInspectionTool : TestGlobalSimpleInspectionTool() {
  override fun createFixes(): Array<LocalQuickFix> = arrayOf(NonModCommandAddCommentQuickFix())
}

private class ChooseAddCommentQuickFix : ModCommandQuickFix() {
  override fun getFamilyName() = "Choose how to add comment"

  override fun perform(project: Project, descriptor: ProblemDescriptor): ModCommand {
    val file = descriptor.psiElement.containingFile
    return ModCommand.chooseAction("Add comment", ModCommandAction.of("Add comment") {
      ThreadingAssertions.assertBackgroundThread()
      ModUpdateFileText(file.virtualFile, file.text, "// test: chosen action\n" + file.text, emptyList())
    })
  }
}

private class OutdatedTextQuickFix : ModCommandQuickFix() {
  override fun getFamilyName() = "Outdated fix"

  override fun perform(project: Project, descriptor: ProblemDescriptor): ModCommand {
    val file = descriptor.psiElement.containingFile
    return ModUpdateFileText(file.virtualFile, "// outdated\n" + file.text, file.text, emptyList())
  }
}

private class UnavailableAction : ModCommandAction {
  override fun getFamilyName() = "Unavailable fix"

  override fun getPresentation(context: ActionContext): Presentation? = null

  override fun perform(context: ActionContext): ModCommand = throw AssertionError("Unavailable fix must not be performed")
}

private class NonModCommandAddCommentQuickFix : LocalQuickFix {
  override fun getFamilyName() = "Add comment without ModCommand"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    ThreadingAssertions.assertEventDispatchThread()
    ThreadingAssertions.assertWriteAccess()
    descriptor.psiElement.containingFile.viewProvider.document?.insertString(0, "// test: non-ModCommand fix\n")
  }
}