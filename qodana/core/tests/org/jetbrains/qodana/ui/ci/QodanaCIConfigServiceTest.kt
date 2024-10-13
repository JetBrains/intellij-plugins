package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.writeAction
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.extensions.ci.JenkinsConfigHandler
import org.jetbrains.qodana.reinstansiateService
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.ui.ci.providers.azure.AzurePipelinesCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.bitbucket.BitbucketCIFIleChecker
import org.jetbrains.qodana.ui.ci.providers.circleci.CircleCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.github.GitHubCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.gitlab.GitLabCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.jenkins.JenkinsCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.space.SpaceAutomationCIFileChecker
import kotlin.io.path.Path
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/test-data/QodanaCIConfigServiceTest")
class QodanaCIConfigServiceTest : QodanaPluginHeavyTestBase() {
  override fun getBasePath(): String = Path(super.getBasePath(), "QodanaCIConfigServiceTest").pathString

  override fun setUp() {
    super.setUp()
    setUpProject()
    reinstansiateService(project, QodanaCIConfigService(project, scope))
  }

  private fun setUpProject() {
    invokeAndWaitIfNeeded {
      copyProjectTestData(getTestName(true).trim())
    }
  }

  override fun runInDispatchThread(): Boolean = false

  fun `test config service`() = runDispatchingOnUi {
    ExtensionTestUtil.maskExtensions(JenkinsConfigHandler.EP_NAME, listOf(), testRootDisposable)

    val service = QodanaCIConfigService.getInstance(project)
    val presentFile = service.presentCIFile.filterNotNull().first()

    assertThat(presentFile.path).contains(".gitlab-ci.yml")

    dispatchAllTasksOnUi()
    deletePhysicalConfigYml(".gitlab-ci.yml")

    val newCiFile = service.presentCIFile.filter { it !== presentFile }.first()
    assertThat(newCiFile).isNull()

    val text = """
      pipeline {
          environment {
              QODANA_TOKEN=credentials('qodana-token')
          }
          agent {
              docker {
                  args '''
                    -v "{$}{WORKSPACE}":/data/project
                    --entrypoint=""
                    '''
                  image 'jetbrains/qodana-<linter>'
              }
          }
          stages {
              stage('Qodana') {
                  steps {
                      sh '''qodana'''
                  }
              }
          }
      }
    """.trimIndent()

    dispatchAllTasksOnUi()
    createPhysicalConfigYml("Jenkinsfile", text)

    val lastCiFile = service.presentCIFile.filter { it !== newCiFile }.first()
    assertThat(lastCiFile!!.path).contains("Jenkinsfile")
  }

  fun `test gitlab file with qodana`() = runDispatchingOnUi {
    val checker = GitLabCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.ExistingWithQodana::class.java)
  }

  fun `test gitlab file without qodana`() = runDispatchingOnUi {
    val checker = GitLabCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.Existing::class.java)
  }

  fun `test gitlab no file`() = runDispatchingOnUi {
    val checker = GitLabCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.NotExisting::class.java)
  }

  fun `test github file with qodana`() = runDispatchingOnUi {
    val checker = GitHubCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.ExistingWithQodana::class.java)
  }

  fun `test github file without qodana`() = runDispatchingOnUi {
    val checker = GitHubCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.Existing::class.java)
  }

  fun `test github no file`() = runDispatchingOnUi {
    val checker = GitHubCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.NotExisting::class.java)
  }

  fun `test jenkins file without qodana dummy`() = runDispatchingOnUi {
    ExtensionTestUtil.maskExtensions(JenkinsConfigHandler.EP_NAME, listOf(), testRootDisposable)

    val checker = JenkinsCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.Existing::class.java)
  }

  fun `test jenkins no file`() = runDispatchingOnUi {
    val checker = JenkinsCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.NotExisting::class.java)
  }

  fun `test azure file with qodana`() = runDispatchingOnUi {
    val checker = AzurePipelinesCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.ExistingWithQodana::class.java)
  }

  fun `test azure file without qodana`() = runDispatchingOnUi {
    val checker = AzurePipelinesCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.Existing::class.java)
  }

  fun `test azure no file`() = runDispatchingOnUi {
    val checker = AzurePipelinesCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.NotExisting::class.java)
  }

  fun `test circleci file with qodana`() = runDispatchingOnUi {
    val checker = CircleCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.ExistingWithQodana::class.java)
  }

  fun `test circleci file without qodana`() = runDispatchingOnUi {
    val checker = CircleCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.Existing::class.java)
  }

  fun `test circleci no file`() = runDispatchingOnUi {
    val checker = CircleCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.NotExisting::class.java)
  }

  fun `test bitbucket file with qodana`() = runDispatchingOnUi {
    val checker = BitbucketCIFIleChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.ExistingWithQodana::class.java)
  }

  fun `test bitbucket file without qodana`() = runDispatchingOnUi {
    val checker = BitbucketCIFIleChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.Existing::class.java)
  }

  fun `test bitbucket no file`() = runDispatchingOnUi {
    val checker = BitbucketCIFIleChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.NotExisting::class.java)
  }

  fun `test space file with qodana`() = runDispatchingOnUi {
    val checker = SpaceAutomationCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.ExistingWithQodana::class.java)
  }

  fun `test space file without qodana`() = runDispatchingOnUi {
    val checker = SpaceAutomationCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.Existing::class.java)
  }

  fun `test space no file`() = runDispatchingOnUi {
    val checker = SpaceAutomationCIFileChecker(project, scope)
    val presentFile = checker.ciFileFlow.filterNotNull().first()

    assertThat(presentFile).isInstanceOf(CIFile.NotExisting::class.java)
  }


  private fun createPhysicalConfigYml(name: String, expectedText: String) {
    myFixture.tempDirFixture.createFile(name, expectedText)
  }

  private suspend fun deletePhysicalConfigYml(name: String) {
    writeAction {
      myFixture.tempDirFixture.getFile(name)!!.delete(this)
    }
  }
}