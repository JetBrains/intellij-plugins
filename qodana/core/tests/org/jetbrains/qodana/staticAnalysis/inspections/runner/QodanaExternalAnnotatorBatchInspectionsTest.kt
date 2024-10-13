package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.junit.Test

class QodanaExternalAnnotatorBatchInspectionsTest : QodanaRunnerTestCase() {
  @Test
  fun testSeveralProblemsLocalInspection() {
    registerTool(LocalTestTool)
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:${LocalTestTool.shortName}"),
      )
    }

    runAnalysis()
    assertSarifResults()
  }
}

private object LocalTestTool : LocalInspectionTool(), ExternalAnnotatorBatchInspection {
  override fun getGroupDisplayName() = "test"
  override fun getShortName() = "localTest"
  override fun checkFile(file: PsiFile, context: GlobalInspectionContext, manager: InspectionManager): Array<ProblemDescriptor> {
    val problems = mutableListOf<ProblemDescriptor>()
    runBlockingCancellable {
      readAction {
        file.accept(object : JavaRecursiveElementVisitor() {
          override fun visitMethod(method: PsiMethod) {
            problems.add(manager.createProblemDescriptor(method,
                                                         "test",
                                                         false,
                                                         emptyArray(),
                                                         ProblemHighlightType.ERROR))
          }
        })
      }
    }
    return problems.toTypedArray()
  }
}