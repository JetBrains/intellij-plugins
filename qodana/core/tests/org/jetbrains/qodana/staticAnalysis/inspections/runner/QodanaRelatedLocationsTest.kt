package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInsight.daemon.impl.ProblemRelatedLocation
import com.intellij.codeInsight.daemon.impl.withRelatedLocations
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMethod
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.junit.Test


@TestDataPath("\$CONTENT_ROOT/testData/QodanaRunnerTest")
class QodanaRelatedLocationsTest : QodanaRunnerTestCase(){
  @Test
  fun testRelatedLocations() {

    val tool = TestInspectionTool()
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

private class TestInspectionTool : LocalInspectionTool() {
  override fun getShortName(): String {
    return "testName"
  }

  override fun getGroupDisplayName(): String {
    return "test"
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return object : JavaElementVisitor() {
      override fun visitMethod(method: PsiMethod) {
        val element = method.getNameIdentifier()!!
        val descriptor = holder.manager
          .createProblemDescriptor(element,
                                   "Test problem",
                                   false,
                                   null,
                                   ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
          .withRelatedLocations(listOf(ProblemRelatedLocation(element, element, "Not related at all")))

        holder.registerProblem(descriptor)
      }
    }
  }
}