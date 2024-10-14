package org.jetbrains.qodana.ui.ci.providers.bitbucket

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.openBrowserWithCurrentQodanaCloudFrontend
import org.jetbrains.qodana.extensions.ci.BitbucketCIConfigHandler
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.ui.*
import org.jetbrains.qodana.ui.ci.BaseSetupCIViewModel
import org.jetbrains.qodana.ui.ci.SetupCIFinishProvider
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import org.jetbrains.qodana.ui.ci.providers.CIConfigFileState
import org.jetbrains.qodana.ui.ci.providers.getSarifBaseline
import java.nio.file.Path
import kotlin.io.path.exists

class SetupBitbucketCIViewModel(
  private val projectNioPath: Path,
  val project: Project,
  scope: CoroutineScope,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) : SetupCIViewModel {
  val baseSetupCIViewModel = BaseSetupCIViewModel(
    projectNioPath, project, BITBUCKET_CI_FILE, scope,
    BaseSetupCIViewModel.Actions(
      ::createBannerContentProvider, ::spawnAddedConfigurationNotification,
      ::defaultConfigurationText, ::getInMemoryPatchOfPhysicalConfig
    )
  )

  override val finishProviderFlow: Flow<SetupCIFinishProvider?> = baseSetupCIViewModel.createFinishProviderFlow()

  override fun unselected() {
    baseSetupCIViewModel.unselected()
  }

  private fun spawnAddedConfigurationNotification() {
    QodanaNotifications.General.notification(
      QodanaBundle.message("qodana.add.to.ci.finish.notification.bitbucket.title"),
      QodanaBundle.message("qodana.add.to.ci.finish.notification.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

  private suspend fun getInMemoryPatchOfPhysicalConfig(configFile: VirtualFile): CIConfigFileState.InMemoryPatchOfPhysicalFile? {
    val physicalDocument = readAction { FileDocumentManager.getInstance().getDocument(configFile) } ?: return null
    val physicalText = physicalDocument.immutableCharSequence.toString()

    val QODANA_TOKEN = "\$QODANA_TOKEN"
    val BITBUCKET_CLONE_DIR = "\$BITBUCKET_CLONE_DIR"

    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()
    val baselineText = getSarifBaseline(project)?.let { " --baseline $it" } ?: ""
    @Language("YAML")
    val stepText = """
      - step:
          name: Qodana
          caches:
            - qodana
          image: ${getQodanaImageNameMatchingIDE(useVersionPostfix = false)}
          script:
            - export QODANA_TOKEN=$QODANA_TOKEN  # Export the environment variable
            - qodana$baselineText --results-dir=$BITBUCKET_CLONE_DIR/.qodana --report-dir=$BITBUCKET_CLONE_DIR/.qodana/report --cache-dir=~/.qodana/cache
          artifacts:
            - .qodana/report
    """.trimIndent()
    val textWithSteps = BitbucketCIConfigHandler.addQodanaStepToBranches(project, physicalText, stepText, branchesToAdd)
    val finalText = BitbucketCIConfigHandler.addCachesSection(project, textWithSteps)
    val inMemoryPatchDocument = createInMemoryDocument(project, finalText, configFile.name)

    return CIConfigFileState.InMemoryPatchOfPhysicalFile(project, inMemoryPatchDocument, physicalDocument, configFile.toNioPath())
  }

  @Suppress("LocalVariableName")
  private suspend fun defaultConfigurationText(): String {
    val QODANA_TOKEN = "\$QODANA_TOKEN"
    val BITBUCKET_CLONE_DIR = "\$BITBUCKET_CLONE_DIR"

    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()
    val baselineText = getSarifBaseline(project)?.let { " --baseline $it" } ?: ""

    @Language("YAML")
    val stepText = """
        - step:
            name: Qodana
            caches:
              - qodana
            image: ${getQodanaImageNameMatchingIDE(useVersionPostfix = false)}
            script:
              - export QODANA_TOKEN=$QODANA_TOKEN  # Export the environment variable
              - qodana$baselineText --results-dir=$BITBUCKET_CLONE_DIR/.qodana --report-dir=$BITBUCKET_CLONE_DIR/.qodana/report --cache-dir=~/.qodana/cache
            artifacts:
              - .qodana/report
    """.replaceIndent("      ")

    val pipelinesText = branchesToAdd.joinToString(separator = "\n", prefix = "\n", postfix = "\n") { "    $it:\n$stepText" }

    @Language("YAML")
    val yamlConfiguration = """
      image: atlassian/default-image:4
      
      pipelines:
        branches:
    """.trimIndent() + pipelinesText + """
      definitions:
        caches:
          qodana: ~/.qodana/cache
    """.trimIndent()
    return yamlConfiguration
  }

  override suspend fun isCIPresentInProject(): Boolean {
    if (projectNioPath.resolve(BITBUCKET_CI_FILE).exists()) return true
    return projectVcsDataProvider.originHost()?.startsWith("bitbucket.org") ?: false
  }

  private fun createBannerContentProvider(): BannerContentProvider {
    val text = QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.bitbucket.text")
    val getTokenAction = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.get.token")) {
      openBrowserWithCurrentQodanaCloudFrontend()
    }
    val howToAddTokenToBitbucket = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.how.add.token")) {
      BrowserUtil.browse("https://www.jetbrains.com/help/qodana/bitbucket.html#Basic+configuration")
    }
    return BannerContentProvider(text, listOf(getTokenAction, howToAddTokenToBitbucket), onClose = {
      baseSetupCIViewModel.isBannerVisibleStateFlow.value = false
    })
  }
}