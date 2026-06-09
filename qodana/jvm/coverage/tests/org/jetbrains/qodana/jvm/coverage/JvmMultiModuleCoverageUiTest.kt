package org.jetbrains.qodana.jvm.coverage

import com.intellij.coverage.JavaCoverageEngine
import com.intellij.coverage.view.CoverageViewManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.rt.coverage.data.LineCoverage
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.util.lang.JavaVersion
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.coverage.CHANGED_LINES_ARTIFACT_ID
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageUiTestBase
import org.junit.Test

class JvmMultiModuleCoverageUiTest : QodanaCoverageUiTestBase("JvmMultiModuleCoverageInspectionTest") {

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
  fun loadsRegularCoverageReport(): Unit = runBlocking {
    val metadata = mapOf<String, ReportMetadata>(
      JVM_COVERAGE to coverageArtifact("regular", "JavaCoverageEngine.ic", JVM_COVERAGE),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertTrue("Unexpected engine: ${bundle!!.coverageEngine}", bundle.coverageEngine is JavaCoverageEngine)
    assertEquals(1, manager.activeSuites().size)

    // The tool window view is created and activated through QodanaCoverageToolWindowActivator.
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle))

    // Full report: both covered files are shown in the tool window tree.
    assertCoverageTree(bundle, """
    -all
     -com.example
      -lib
       Library
      -app
       App
    """.trimIndent())

    openFileInEditor("app/src/main/kotlin/com/example/app/App.kt")
    assertEquals(
      mapOf(5 to LineCoverage.FULL, 7 to LineCoverage.FULL, 12 to LineCoverage.NONE),
      gutterCoverage("app/src/main/kotlin/com/example/app/App.kt")
    )

    openFileInEditor("lib/src/main/kotlin/com/example/lib/Library.kt")
    assertEquals(
      mapOf(3 to LineCoverage.FULL, 5 to LineCoverage.FULL, 10 to LineCoverage.NONE),
      gutterCoverage("lib/src/main/kotlin/com/example/lib/Library.kt")
    )
  }

  @Test
  fun loadsIncrementalCoverageReport(): Unit = runBlocking {
    val metadata = mapOf(
      JVM_COVERAGE to coverageArtifact("incrementalSecondStage", "JavaCoverageEngine.ic", JVM_COVERAGE),
      CHANGED_LINES_ARTIFACT_ID to changedLinesArtifact("incrementalSecondStage"),
    )
    highlightCoverageReport(metadata)

    val bundle = manager.currentSuitesBundle
    assertNotNull("Coverage suite was not opened", bundle)
    assertTrue("Unexpected engine: ${bundle!!.coverageEngine}", bundle.coverageEngine is JavaCoverageEngine)
    assertEquals(1, manager.activeSuites().size)

    // The tool window view is created and activated through QodanaCoverageToolWindowActivator.
    assertNotNull("Coverage view was not created", CoverageViewManager.getInstance(project).getView(bundle))

    // Full report: both covered files are shown in the tool window tree.
    assertCoverageTree(bundle, """
      -all
       -com.example.app
        App
    """.trimIndent())

    openFileInEditor("app/src/main/kotlin/com/example/app/App.kt")
    assertEquals(
      mapOf(7 to LineCoverage.FULL),
      gutterCoverage("app/src/main/kotlin/com/example/app/App.kt")
    )
  }
}
