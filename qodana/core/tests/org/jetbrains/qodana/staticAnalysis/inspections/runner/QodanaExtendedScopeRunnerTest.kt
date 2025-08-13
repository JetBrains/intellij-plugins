package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.registry.QodanaRegistry.SCOPE_EXTENDING_ENABLE_KEY
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.SkipResultStrategy
import org.jetbrains.qodana.staticAnalysis.scopes.InspectionToolScopeExtender
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaScopeExtenderProvider
import org.jetbrains.qodana.staticAnalysis.script.scoped.*
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText
import kotlin.io.path.writeText

@TestDataPath($$"$CONTENT_ROOT/testData/QodanaExtendedScopeRunnerTest")
class QodanaExtendedScopeRunnerTest : QodanaRunnerTestCase() {
  override fun setUp() {
    super.setUp()
    val registryValue = Registry.get(SCOPE_EXTENDING_ENABLE_KEY)
    registryValue.setValue(true)
    Disposer.register(testRootDisposable) {
      registryValue.resetToDefault()
    }
    InspectionToolScopeExtender.EP_NAME.point.registerExtension(InspectionToolScopeExtenderMock, testRootDisposable)
    QodanaScopeExtenderProvider.EP_NAME.point.registerExtension(QodanaScopeExtenderProviderMock, testRootDisposable)
  }

  @Test
  fun `testScoped-script-with-extended-scope`() {
    val scope = qodanaConfig.projectPath.resolve("scope")
    scope.writeText("""
      {
        "files" : [{
          "path" : "test-module/A.java",
          "added" : [ ],
          "deleted" : [ ]
        }]
      }
    """.trimIndent())
    runReverseScopedScript(scope, Stage.NEW)
  }

  @Test
  fun `testScoped-script-old-stage-empty-extended-scope`() {
    val scope = qodanaConfig.projectPath.resolve("scope")
    scope.writeText("""
      {
        "files" : [{
          "path" : "test-module/A.java",
          "added" : [ ],
          "deleted" : [ ]
        }]
      }
    """.trimIndent())
    try {
      System.setProperty(SCOPED_BASELINE_PROPERTY, "test-module/baseline.sarif.json")
      runReverseScopedScript(scope, Stage.OLD)
    }
    finally {
      System.clearProperty(SCOPED_BASELINE_PROPERTY)
    }
  }

  @Test
  fun `testScoped-script-old-stage-with-extended-scope`() {
    val scope = qodanaConfig.projectPath.resolve("scope")
    scope.writeText("""
      {
        "files" : [{
          "path" : "test-module/A.java",
          "added" : [ ],
          "deleted" : [ ]
        }],
        "extendedFiles":[{
          "path":"test-module/B.java",
          "extenders":["InspectionToolScopeExtenderMock"]
        }]
      }
    """.trimIndent())
    try {
      System.setProperty(SCOPED_BASELINE_PROPERTY, "test-module/baseline.sarif.json")
      runReverseScopedScript(scope, Stage.OLD)
    }
    finally {
      System.clearProperty(SCOPED_BASELINE_PROPERTY)
    }
  }

  private fun runReverseScopedScript(scope: Path, stage: Stage) {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig(REVERSE_SCOPED_SCRIPT_NAME, mapOf(
          SCOPE_ARG to scope.toString(),
          STAGE_ARG to stage.name
        )),
        profile = QodanaProfileConfig.named("qodana.single:ConstantValue"),
        skipResultStrategy = SkipResultStrategy.ANY,
      )
    }
    try {
      System.setProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY, "true")
      runAnalysis()
      assertSarifResults()

      val expectedScope = getTestDataPath("scope.json").absolutePathString()
      assertSameLinesWithFile(expectedScope, scope.readText().trimIndent())
    } finally {
      System.clearProperty(COVERAGE_SKIP_COMPUTATION_PROPERTY)
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
