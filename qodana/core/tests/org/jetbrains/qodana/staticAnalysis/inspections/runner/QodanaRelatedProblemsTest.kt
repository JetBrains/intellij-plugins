package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInsight.daemon.impl.RELATED_PROBLEMS_CHILD_HASH
import com.intellij.codeInsight.daemon.impl.RELATED_PROBLEMS_ROOT_HASH
import com.intellij.codeInsight.daemon.impl.withUserData
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.junit.Test


@TestDataPath("\$CONTENT_ROOT/testData/QodanaRunnerTest")
class QodanaRelatedProblemsTest : QodanaRunnerTestCase(){
  @Test
  fun testRelatedProblems() {

    val tool = RelatedProblemsTool()
    registerTool(tool)
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:${tool.shortName}"),
      )
    }

    runAnalysis()
    assertSarifResults()
  }
}

private class RelatedProblemsTool : LocalInspectionTool() {
  override fun getShortName() = "tool"

  override fun getGroupDisplayName() = "test"

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly  : Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : JavaElementVisitor() {
      override fun visitMethod(method: PsiMethod) {
        val element = method.getNameIdentifier()!!
        addRootProblem(element, holder)
        method.body?.statements?.forEach {
          addChildProblem(it, element.text, holder)
        }
      }
    }
  }

  private fun addRootProblem(element: PsiIdentifier, holder: ProblemsHolder) {
    val descriptor = holder.manager
      .createProblemDescriptor(element,
                               "Root problem",
                               false,
                               null,
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      .withUserData {
        putUserData(RELATED_PROBLEMS_ROOT_HASH, element.text)
      }

    holder.registerProblem(descriptor)
  }

  private fun addChildProblem(element: PsiElement, hash: String, holder: ProblemsHolder) {
    val descriptor = holder.manager
      .createProblemDescriptor(element,
                               "Child problem",
                               false,
                               null,
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      .withUserData {
        putUserData(RELATED_PROBLEMS_CHILD_HASH, hash)
      }

    holder.registerProblem(descriptor)
  }
}