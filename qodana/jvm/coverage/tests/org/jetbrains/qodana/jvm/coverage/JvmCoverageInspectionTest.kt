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
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine")
    assertSarifResults()
  }

  @Test
  fun icWithProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine")
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
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine")
    assertSarifResults()
  }

  @Test
  fun execWithoutProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine")
    assertSarifResults()
  }

  @Test
  fun icWithoutProblemReport() {
    runUnderCover()
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine")
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
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine")
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
    runIncrementalAnalysis(QodanaCoverageComputationState.SKIP_REPORT, SCOPE)
    assertChangedLines(mapOf("src/foo/FooClass.java" to setOf(4, 5, 6)))
    assertCoverageProjectDataMatchesGolden("JavaCoverageEngine")
    assertSarifResults()
  }

  private companion object {
    // method1() of FooClass spans lines 4-6; line 5 (`return 1;`) is covered, so fresh coverage is non-zero.
    private const val SCOPE = """
      {
        "files" : [ {
          "path" : "src/foo/FooClass.java",
          "added" : [ {
            "firstLine" : 4,
            "count" : 3
          } ],
          "deleted" : [ ]
        } ]
      }
    """
  }
}