package org.jetbrains.qodana.ui.ci.providers.azure

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.openBrowserWithCurrentQodanaCloudFrontend
import org.jetbrains.qodana.extensions.ci.AzureCIConfigHandler
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import org.jetbrains.qodana.ui.ci.BaseSetupCIViewModel
import org.jetbrains.qodana.ui.ci.SetupCIFinishProvider
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import org.jetbrains.qodana.ui.ci.providers.CIConfigFileState
import org.jetbrains.qodana.ui.ci.providers.getSarifBaseline
import org.jetbrains.qodana.ui.ciRelevantBranches
import org.jetbrains.qodana.ui.createInMemoryDocument
import org.jetbrains.qodana.ui.getQodanaImageNameMatchingIDE
import java.nio.file.Path
import kotlin.io.path.exists


class SetupAzurePipelinesViewModel(
  val projectNioPath: Path,
  val project: Project,
  val scope: CoroutineScope,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) : SetupCIViewModel {
  val baseSetupCIViewModel = BaseSetupCIViewModel(
    projectNioPath, project, AZURE_PIPELINES_FILE, scope,
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
      QodanaBundle.message("qodana.add.to.ci.finish.notification.azure.pipelines.title"),
      QodanaBundle.message("qodana.add.to.ci.finish.notification.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

  private suspend fun getInMemoryPatchOfPhysicalConfig(configFile: VirtualFile): CIConfigFileState.InMemoryPatchOfPhysicalFile? {
    val physicalDocument = readAction { FileDocumentManager.getInstance().getDocument(configFile) } ?: return null
    val physicalText = physicalDocument.immutableCharSequence
    val contentOfInMemoryPatchDocument = AzureCIConfigHandler.insertStepToAzurePipelinesBuild(project, physicalText.toString(), defaultQodanaTaskText())
    val inMemoryPatchDocument = createInMemoryDocument(project, contentOfInMemoryPatchDocument, configFile.name)

    return CIConfigFileState.InMemoryPatchOfPhysicalFile(project, inMemoryPatchDocument, physicalDocument, configFile.toNioPath())
  }

  private suspend fun defaultConfigurationText(): String {
    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()

    @Language("YAML")
    val branchesText = """
      trigger:
      
    """.trimIndent() + branchesToAdd.joinToString(separator = "\n", postfix = "\n") { "  - $it" }

    val baselineText = getSarifBaseline(project)?.let { "--baseline,$it," } ?: ""

    val ideMajorVersion = ApplicationInfo.getInstance().majorVersion

    @Language("YAML")
    val yamlConfiguration = branchesText + """
      
      pool:
        vmImage: ubuntu-latest
      
      steps:
        - task: Cache@2  # Not required, but Qodana will open projects with cache faster.
          inputs:
            key: '"$(Build.Repository.Name)" | "$(Build.SourceBranchName)" | "$(Build.SourceVersion)"'
            path: '$(Agent.TempDirectory)/qodana/cache'
            restoreKeys: |
              "$(Build.Repository.Name)" | "$(Build.SourceBranchName)"
              "$(Build.Repository.Name)"
        - task: QodanaScan@${ideMajorVersion}
          inputs:
            args: ${baselineText}-l,${getQodanaImageNameMatchingIDE(true)}
          env:
            QODANA_TOKEN: $(QODANA_TOKEN)
    """.trimIndent()
    return yamlConfiguration
  }

  private suspend fun defaultQodanaTaskText(): String {
    val ideMajorVersion = ApplicationInfo.getInstance().majorVersion
    val baselineText = getSarifBaseline(project)?.let { "--baseline,$it," } ?: ""
    return """
      task: QodanaScan@${ideMajorVersion}
        inputs:
          args: ${baselineText}-l,${getQodanaImageNameMatchingIDE(true)}
        env:
          QODANA_TOKEN: $(QODANA_TOKEN)
    """.trimIndent()
  }

  override suspend fun isCIPresentInProject(): Boolean {
    return projectNioPath.resolve(AZURE_PIPELINES_FILE).exists()
  }

  private fun createBannerContentProvider(): BannerContentProvider {
    val text = QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.azure.text")
    val getTokenAction = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.get.token")) {
      openBrowserWithCurrentQodanaCloudFrontend()
    }
    val howToAddTokenToAzurePipelines = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.how.add.token")) {
      BrowserUtil.browse("https://www.jetbrains.com/help/qodana/qodana-azure-pipelines.html#Qodana+Cloud")
    }
    return BannerContentProvider(text, listOf(getTokenAction, howToAddTokenToAzurePipelines), onClose = {
      baseSetupCIViewModel.isBannerVisibleStateFlow.value = false
    })
  }
}