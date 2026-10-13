package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalSimpleInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandQuickFix
import com.intellij.modcommand.ModUpdateFileText
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestDataPath
import com.intellij.util.concurrency.ThreadingAssertions
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.sarif.ALLOW_NON_BATCH_FIXES
import org.jetbrains.qodana.staticAnalysis.sarif.FixesLogger
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Test
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@TestDataPath($$"$CONTENT_ROOT/testData/QodanaQuickFixesLoggingTest")
class QodanaQuickFixesLoggingTest : QodanaRunnerTestCase() {

  @Test
  fun cleanupGeneral() {
    run(FixesStrategy.CLEANUP)
    assertDefaultLogData()
  }

  @Test
  fun cleanupDiff() {
    runWithDiffEnabled {
      run(FixesStrategy.CLEANUP)
    }
    assertDiffLogData()
  }

  @Test
  fun applyModCommandGeneral() {
    run(FixesStrategy.APPLY)
    assertDefaultLogData()
  }

  @Test
  fun applyModCommandDiff() {
    runWithDiffEnabled {
      run(FixesStrategy.APPLY)
    }
    assertDiffLogData()
  }

  @Test
  fun applyModCommandFixNameReadInReadAction() {
    registerInspectionTool(MakeInnerClassStaticModCommandInspection())
    run(FixesStrategy.APPLY)
    assertDefaultLogData()
  }

  @Test
  fun applyNonModCommandGeneral() {
    registerInspectionTool(MakeInnerClassStaticInspection())
    runWithNonCommandEnabled {
      run(FixesStrategy.APPLY)
    }
    assertDefaultLogData()
  }

  @Test
  fun applyNonModCommandDiff() {
    registerInspectionTool(MakeInnerClassStaticInspection())
    runWithDiffEnabled {
      runWithNonCommandEnabled {
        run(FixesStrategy.APPLY)
      }
    }
    assertDiffLogData()
  }

  private fun registerInspectionTool(tool: MakeInnerClassStaticInspection) {
    registerGlobalTool(tool)
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
  }

  private fun run(strategy: FixesStrategy) {
    updateQodanaConfig {
      it.copy(
        fixesStrategy = strategy,
        disableSanityInspections = true,
        profile = QodanaProfileConfig.fromPath(getTestDataPath("inspection-profile.yaml").absolutePathString())
      )
    }
    runAnalysis()
  }

  private fun runWithDiffEnabled(runnable: () -> Unit) {
    val previousValue: String? = System.getProperty(FixesLogger.INCLUDE_FIXES_DIFF_KEY)
    System.setProperty(FixesLogger.INCLUDE_FIXES_DIFF_KEY, "true")
    try {
      runnable()
    }
    finally {
      if (previousValue == null) {
        System.clearProperty(FixesLogger.INCLUDE_FIXES_DIFF_KEY)
      }
      else {
        System.setProperty(FixesLogger.INCLUDE_FIXES_DIFF_KEY, previousValue)
      }
    }
  }

  private fun runWithNonCommandEnabled(runnable: () -> Unit) {
    val previousValue: String? = System.getProperty(ALLOW_NON_BATCH_FIXES)
    System.setProperty(ALLOW_NON_BATCH_FIXES, "true")
    try {
      runnable()
    }
    finally {
      if (previousValue == null) {
        System.clearProperty(ALLOW_NON_BATCH_FIXES)
      }
      else {
        System.setProperty(ALLOW_NON_BATCH_FIXES, previousValue)
      }
    }
  }

  private fun assertDefaultLogData() {
    val logFile = Path.of(PathManager.getLogPath(), "qodana", "fixes.json").toFile().readText()
    val expectedLogFile = getTestDataPath("expected-fixes.json").toFile().readText()
    assertSameLines(expectedLogFile, logFile)
  }

  private fun assertDiffLogData() {
    val logFile = Path.of(PathManager.getLogPath(), "qodana", "files-modifications.json").toFile().readText()
    val expectedLogFile = getTestDataPath("expected-diffs.json").toFile().readText()
    assertSameLines(expectedLogFile, logFile)
  }
}

private const val INNER_CLASS_TEXT = "class InnerA {"

private open class MakeInnerClassStaticInspection : GlobalSimpleInspectionTool() {
  protected open fun createFix(): LocalQuickFix = MakeInnerClassStaticQuickFix()

  override fun getShortName() = "testMakeInnerClassStatic"

  override fun getDisplayName() = "Test inner class may be 'static'"

  override fun getGroupDisplayName() = "testGroup"

  override fun checkFile(psiFile: PsiFile,
                         manager: InspectionManager,
                         problemsHolder: ProblemsHolder,
                         context: GlobalInspectionContext,
                         processor: ProblemDescriptionsProcessor) {
    val offset = psiFile.text.indexOf(INNER_CLASS_TEXT)
    if (offset < 0) return
    problemsHolder.registerProblem(
      psiFile,
      "Inner class 'InnerA' may be 'static'",
      ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
      TextRange(offset, offset + INNER_CLASS_TEXT.length),
      createFix()
    )
  }
}

private class MakeInnerClassStaticQuickFix : LocalQuickFix {
  override fun getFamilyName() = "Make 'static' without ModCommand"

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val document = descriptor.psiElement.containingFile.viewProvider.document ?: return
    document.insertString(document.text.indexOf(INNER_CLASS_TEXT), "static ")
  }
}

private class MakeInnerClassStaticModCommandInspection : MakeInnerClassStaticInspection() {
  override fun createFix(): LocalQuickFix = MakeInnerClassStaticModCommandQuickFix()
}

private class MakeInnerClassStaticModCommandQuickFix : ModCommandQuickFix() {
  override fun getFamilyName() = "Make 'static' with ModCommand"

  override fun getName(): String {
    ThreadingAssertions.assertReadAccess()
    return familyName
  }

  override fun perform(project: Project, descriptor: ProblemDescriptor): ModCommand {
    val file = descriptor.psiElement.containingFile
    val text = file.text
    return ModUpdateFileText(file.virtualFile, text, text.replace(INNER_CLASS_TEXT, "static $INNER_CLASS_TEXT"), emptyList())
  }
}