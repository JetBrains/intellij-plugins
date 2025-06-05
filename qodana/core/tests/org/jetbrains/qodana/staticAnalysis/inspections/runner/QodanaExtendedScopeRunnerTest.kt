package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.registry.QodanaRegistry.SCOPE_EXTENDING_ENABLE_KEY
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
      System.setProperty(SCOPE_EXTENDING_ENABLE_KEY, "true")
      runAnalysis()
      assertSarifResults()
    } finally {
      System.clearProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY)
      System.clearProperty(SCOPE_EXTENDING_ENABLE_KEY)
    }
  }
}

object QodanaScopeExtenderProviderMock: QodanaScopeExtenderProvider {
  const val INSPECTION_NAME = "ConstantValue"
  override val name: String
    get() = InspectionToolScopeExtenderMock.name

  override fun isApplicable(inspectionId: String): Boolean {
    return inspectionId == INSPECTION_NAME
  }
}

object InspectionToolScopeExtenderMock: InspectionToolScopeExtender {
  override val name: String
    get() = javaClass.simpleName

  private val extendingMap: Map<String, String> = mapOf(
    "A.java" to "B.java",
    "B.java" to "C.java"
  )

  override suspend fun extendScope(virtualFile: VirtualFile, project: Project, acceptedFiles: Map<VirtualFile, Set<String>>): List<VirtualFile> {
    val fileName = extendingMap[virtualFile.name] ?: return emptyList()
    return listOfNotNull(project.guessProjectDir()?.findChild(fileName))
  }
}
