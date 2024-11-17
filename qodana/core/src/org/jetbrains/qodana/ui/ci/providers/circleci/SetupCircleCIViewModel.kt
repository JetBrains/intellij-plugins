package org.jetbrains.qodana.ui.ci.providers.circleci

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
import org.jetbrains.qodana.extensions.ci.CircleCIConfigHandler
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.ui.ci.BaseSetupCIViewModel
import org.jetbrains.qodana.ui.ci.SetupCIFinishProvider
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import org.jetbrains.qodana.ui.ci.providers.CIConfigFileState
import org.jetbrains.qodana.ui.ci.providers.getSarifBaseline
import org.jetbrains.qodana.ui.createInMemoryDocument
import org.jetbrains.qodana.ui.getQodanaImageNameMatchingIDE
import java.nio.file.Path
import kotlin.io.path.exists

class SetupCircleCIViewModel(
  private val projectNioPath: Path,
  val project: Project,
  scope: CoroutineScope
) : SetupCIViewModel {
  val baseSetupCIViewModel = BaseSetupCIViewModel(
    projectNioPath, project, CIRCLE_CI_FILE, scope,
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
      QodanaBundle.message("qodana.add.to.ci.finish.notification.circleci.title"),
      QodanaBundle.message("qodana.add.to.ci.finish.notification.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

  private suspend fun getInMemoryPatchOfPhysicalConfig(configFile: VirtualFile): CIConfigFileState.InMemoryPatchOfPhysicalFile? {
    val physicalDocument = readAction { FileDocumentManager.getInstance().getDocument(configFile) } ?: return null
    val physicalText = physicalDocument.immutableCharSequence.toString()
    val ideMajorVersion = ApplicationInfo.getInstance().majorVersion
    val ideMinorVersion = ApplicationInfo.getInstance().minorVersionMainPart
    val textWithOrb = CircleCIConfigHandler.addOrb(
      project,
      physicalText,
      "qodana",
      "jetbrains/qodana@${ideMajorVersion}.${ideMinorVersion}"
    )

    val baselineText = getSarifBaseline(project)?.let { "--baseline $it " } ?: ""
    @Language("YAML")
    val jobText = """
      code-quality:
        machine:
          image: 'ubuntu-2004:current'
        steps:
          - checkout
          - qodana/scan:
              args: ${baselineText}-l ${getQodanaImageNameMatchingIDE(useVersionPostfix = false)} # use space to separate arguments
    """.trimIndent()
    val textWithJob = CircleCIConfigHandler.addJob(project, textWithOrb, jobText)

    @Language("YAML")
    val workflowJobText = """
      - code-quality:
          context: qodana
    """.trimIndent()

    val finalText = CircleCIConfigHandler.addWorkflowJob(project, textWithJob, "main", workflowJobText)
    val inMemoryPatchDocument = createInMemoryDocument(project, finalText, configFile.name)

    return CIConfigFileState.InMemoryPatchOfPhysicalFile(project, inMemoryPatchDocument, physicalDocument, configFile.toNioPath())
  }

  private suspend fun defaultConfigurationText(): String {
    val baselineText = getSarifBaseline(project)?.let { "--baseline $it " } ?: ""
    val ideMajorVersion = ApplicationInfo.getInstance().majorVersion
    val ideMinorVersion = ApplicationInfo.getInstance().minorVersionMainPart

    @Language("YAML")
    val yamlConfiguration = """
      version: 2.1
      orbs:
        qodana: jetbrains/qodana@${ideMajorVersion}.${ideMinorVersion}
      jobs:
        code-quality:
          machine:
            image: 'ubuntu-2004:current'
          steps:
            - checkout
            - qodana/scan:
                args: ${baselineText}-l ${getQodanaImageNameMatchingIDE(useVersionPostfix = false)} # use space to separate arguments
      workflows:
        main:
          jobs:
            - code-quality:
                context: qodana
    """.trimIndent()
    return yamlConfiguration
  }

  override suspend fun isCIPresentInProject(): Boolean {
    return projectNioPath.resolve(CIRCLE_CI_FILE).exists()
  }

  private fun createBannerContentProvider(): BannerContentProvider {
    val text = QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.circleci.text")
    val getTokenAction = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.get.token")) {
      openBrowserWithCurrentQodanaCloudFrontend()
    }
    val howToAddTokenToCircleCi = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.how.add.token")) {
      BrowserUtil.browse("https://www.jetbrains.com/help/qodana/circleci.html#Qodana+Cloud")
    }
    return BannerContentProvider(text, listOf(getTokenAction, howToAddTokenToCircleCi), onClose = {
      baseSetupCIViewModel.isBannerVisibleStateFlow.value = false
    })
  }
}