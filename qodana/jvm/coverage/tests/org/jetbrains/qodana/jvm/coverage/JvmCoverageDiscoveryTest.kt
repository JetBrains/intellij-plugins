package org.jetbrains.qodana.jvm.coverage

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.util.lang.JavaVersion
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest

/**
 * Base for JVM coverage auto-discovery tests: like [QodanaCoverageDiscoveryTest] but installs a mock JDK
 * required for the opened project to be a valid Java module.
 */
abstract class JvmCoverageDiscoveryTest(
  inspection: String,
  case: Case,
) : QodanaCoverageDiscoveryTest(inspection, case) {
  override fun setUpProject() {
    super.setUpProject()
    WriteAction.runAndWait<RuntimeException> {
      ProjectRootManagerEx.getInstanceEx(myProject).mergeRootsChangesDuring {
        setUpJdk()
      }
    }
  }

  override fun getTestProjectJdk(): Sdk = IdeaTestUtil.getMockJdk(JavaVersion.compose(17))
}
