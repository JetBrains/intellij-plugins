package org.jetbrains.qodana.jvm.coverage

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.util.lang.JavaVersion
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.junit.Test

class JvmMultiModuleCoverageInspectionTest : QodanaCoverageInspectionTest("JvmCoverageInspection") {
  override fun setUpProject() {
    super.setUpProject()
    WriteAction.runAndWait<RuntimeException> {
      ProjectRootManagerEx.getInstanceEx(myProject).mergeRootsChangesDuring {
        setUpJdk()
      }
    }
  }

  override fun getTestProjectJdk(): Sdk = IdeaTestUtil.getMockJdk(JavaVersion.compose(17))

  @Test
  fun regular() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertSarifResults()
  }

  @Test
  fun incrementalSecondStage() {
    runIncrementalAnalysis(QodanaCoverageComputationState.SKIP_REPORT, SCOPE)
    assertChangedLinesMatchesGolden()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertSarifResults()
  }

  private companion object {
    // welcome() of App spans lines 6-8; line 7 (`return ...`) is covered, so fresh coverage is non-zero.
    private const val SCOPE = """
      {
        "files" : [ {
          "path" : "app/src/main/kotlin/com/example/app/App.kt",
          "added" : [ {
            "firstLine" : 6,
            "count" : 3
          } ],
          "deleted" : [ ]
        } ]
      }
    """
  }
}
