package org.jetbrains.qodana.jvm.coverage

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.util.lang.JavaVersion
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.junit.Test

class JvmCoverageInspectionTest: QodanaCoverageInspectionTest("JvmCoverageInspection") {
  override fun setUpProject() {
    super.setUpProject()
    WriteAction.runAndWait<RuntimeException> {
      ProjectRootManagerEx.getInstanceEx(myProject).mergeRootsChangesDuring {
        setUpJdk()
      }
    }
  }
  override fun getTestProjectJdk(): Sdk {
    return IdeaTestUtil.getMockJdk(JavaVersion.compose(17))
  }

  @Test
  fun execWithProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertSarifResults()
  }

  @Test
  fun coverageFromCustomLocation() {
    runUnderCoverDataInSources()
    assertSarifResults()
  }

  @Test
  fun coverageFromCustomLocationXml() {
    runUnderCoverDataInSources()
    assertSarifResults()
  }

  @Test
  fun icWithProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertSarifResults()
  }

  @Test
  fun xmlWithProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun xmlPartsWithProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun anonymousClassWithProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertSarifResults()
  }

  @Test
  fun execWithoutProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertChangedLines(mapOf())
    assertSarifResults()
  }

  @Test
  fun icWithoutProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertChangedLines(mapOf())
    assertSarifResults()
  }

  @Test
  fun xmlWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun xmlPartsWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun anonymousClassWithoutProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertSarifResults()
  }

  @Test
  fun warnMissingCoverage() {
    runUnderCover("inspection-profile.xml")
    assertSarifResults()
  }

  @Test
  fun incrementalFirstStage() {
    runIncrementalAnalysis(QodanaCoverageComputationState.SKIP_COMPUTE, SCOPE)
    assertSarifResults()
  }

  @Test
  fun incrementalSecondStage() {
    runIncrementalAnalysis(QodanaCoverageComputationState.INCREMENTAL_REPORT, SCOPE)
    assertChangedLines(mapOf(
      "src/foo/FooClass.java" to setOf(4, 5, 6),
      "src/foo/bar/BarClass.java" to setOf(8, 9, 10),
    ))
    assertChangedLinesMatchesGolden()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine", "JavaCoverageEngine.ic")
    assertSarifResults()
  }

  @Test
  fun incrementalSecondStageWithoutProblemReport() {
    runIncrementalAnalysis(QodanaCoverageComputationState.INCREMENTAL_REPORT, SCOPE)
    assertFalse(qodanaConfig.coverage.reportProblems)
    assertNoCoverageProblems()
    assertSarifResults()
  }

  private companion object {
    // The scope includes covered method1 and uncovered method4 lines.
    private const val SCOPE = """
      {
        "files" : [
          {
            "path" : "src/foo/FooClass.java",
            "added" : [ {
              "firstLine" : 4,
              "count" : 3
            } ],
            "deleted" : [ ]
          },
          {
            "path" : "src/foo/bar/BarClass.java",
            "added" : [ {
              "firstLine" : 8,
              "count" : 3
            } ],
            "deleted" : [ ]
          }
        ]
      }
    """
  }
}
