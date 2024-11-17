package org.jetbrains.qodana.jvm.coverage

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.util.lang.JavaVersion
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
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
    assertSarifResults()
  }

  @Test
  fun icWithProblemReport() {
    runUnderCover()
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
    assertSarifResults()
  }

  @Test
  fun execWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun icWithoutProblemReport() {
    runUnderCover()
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
    assertSarifResults()
  }

  @Test
  fun warnMissingCoverage() {
    runUnderCover("inspection-profile.xml")
    assertSarifResults()
  }
}