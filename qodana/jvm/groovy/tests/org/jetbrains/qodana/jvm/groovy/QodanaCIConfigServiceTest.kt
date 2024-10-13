package org.jetbrains.qodana.jvm.groovy

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.assertions.Assertions.assertThat
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.jenkins.JenkinsCIFileChecker
import kotlin.io.path.Path
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/test-data/QodanaCIConfigServiceTest")
class QodanaCIConfigServiceTest : QodanaPluginHeavyTestBase() {
  override fun getBasePath(): String = Path(super.getBasePath(), "QodanaCIConfigServiceTest").pathString

  override fun setUp() {
    super.setUp()
    setUpProject()
  }

  private fun setUpProject() {
    invokeAndWaitIfNeeded {
      copyProjectTestData(getTestName(true).trim())
    }
  }

  override fun runInDispatchThread(): Boolean = false

  fun `test jenkins file with qodana`() = runDispatchingOnUi {
    val checker = JenkinsCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isNotNull
    assertThat(presentFile).isInstanceOf(CIFile.ExistingWithQodana::class.java)
  }

  fun `test jenkins file without qodana`() = runDispatchingOnUi {
    val checker = JenkinsCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isNotNull
    assertThat(presentFile).isInstanceOf(CIFile.Existing::class.java)
  }
}