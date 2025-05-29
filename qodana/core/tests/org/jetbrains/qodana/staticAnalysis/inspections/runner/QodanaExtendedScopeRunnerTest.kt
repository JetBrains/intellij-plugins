package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.scopes.InspectionToolScopeExtender
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaScopeExtenderProvider
import org.jetbrains.qodana.staticAnalysis.script.scoped.COVERAGE_SKIP_COMPUTATION_PROPERTY
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.junit.Test
import kotlin.io.path.writeText

@TestDataPath("\$CONTENT_ROOT/testData/QodanaExtendedScopeRunnerTest")
class QodanaExtendedScopeRunnerTest : QodanaRunnerTestCase() {
  override fun setUp() {
    super.setUp()
    InspectionToolScopeExtender.EP_NAME.point.registerExtension(InspectionToolScopeExtenderMock, testRootDisposable)
    QodanaScopeExtenderProvider.EP_NAME.point.registerExtension(QodanaScopeExtenderProviderMock, testRootDisposable)
  }

  @Test
  fun `testScoped-script-with-extended-scope`() {
    val scope = qodanaConfig.projectPath.resolve("scope")

    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scope.toString())),
        profile = QodanaProfileConfig.named("qodana.single:ConstantValue"),
      )
    }

    scope.writeText("""
      {
        "files" : [ {
          "path" : "test-module/A.java",
          "added" : [ ],
          "deleted" : [ ]
        }]
      }
    """.trimIndent())

    try {
      System.setProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY, "true")
      runAnalysis()
      assertSarifResults()
    } finally {
      System.clearProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY)
    }
  }
}

private object QodanaScopeExtenderProviderMock: QodanaScopeExtenderProvider {
  const val INSPECTION_NAME = "ConstantValue"
  override val name: String
    get() = InspectionToolScopeExtenderMock.name

  override fun isApplicable(inspectionId: String): Boolean {
    return inspectionId == INSPECTION_NAME
  }
}

private object InspectionToolScopeExtenderMock: InspectionToolScopeExtender {
  override val name: String
    get() = InspectionToolScopeExtenderMock::class.java.simpleName

  override suspend fun extendScope(virtualFile: VirtualFile, project: Project, acceptedFiles: Map<VirtualFile, Set<String>>): List<VirtualFile> {
    return listOfNotNull(project.guessProjectDir()?.findChild("B.java"))
  }
}
