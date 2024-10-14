package org.jetbrains.qodana.ui.ci.providers.space

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.openBrowserWithCurrentQodanaCloudFrontend
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import org.jetbrains.qodana.ui.ci.BaseSetupCIViewModel
import org.jetbrains.qodana.ui.ci.SetupCIFinishProvider
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import org.jetbrains.qodana.ui.ci.providers.getSarifBaseline
import org.jetbrains.qodana.ui.ciRelevantBranches
import org.jetbrains.qodana.ui.getQodanaImageNameMatchingIDE
import java.nio.file.Path
import kotlin.io.path.exists

class SetupSpaceAutomationViewModel(
  private val projectNioPath: Path,
  val project: Project,
  scope: CoroutineScope,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) : SetupCIViewModel {
  val baseSetupCIViewModel = BaseSetupCIViewModel(
    projectNioPath, project, SPACE_AUTOMATION_CI_FILE, scope,
    BaseSetupCIViewModel.Actions(
      ::createBannerContentProvider, ::spawnAddedConfigurationNotification,
      ::defaultConfigurationText, null
    )
  )

  override val finishProviderFlow: Flow<SetupCIFinishProvider?> = baseSetupCIViewModel.createFinishProviderFlow()

  override fun unselected() {
    baseSetupCIViewModel.unselected()
  }

  private fun spawnAddedConfigurationNotification() {
    QodanaNotifications.General.notification(
      QodanaBundle.message("qodana.add.to.ci.finish.notification.space.title"),
      QodanaBundle.message("qodana.add.to.ci.finish.notification.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

  private suspend fun defaultConfigurationText(): String {
    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()

    @Language("YAML")
    val branchesText = if (branchesToAdd.isNotEmpty()) """
        startOn {
          gitPush {
            anyBranchMatching {
              ${branchesToAdd.joinToString(separator = "\n              ") { "+\"$it\"" }}
            }
          }
          codeReviewOpened{}
        }""" else ""

    val baselineText = getSarifBaseline(project)?.let { " --baseline $it" } ?: ""

    @Language("YAML")
    val spaceJobConfiguration = ("""
      job("Qodana") {""" + branchesText + """
        container("${getQodanaImageNameMatchingIDE(useVersionPostfix = false)}") {
          env["QODANA_TOKEN"] = "{{ project:qodana-token }}"
          shellScript {
            content = "qodana$baselineText"
          }
        }
      }
    """).trimIndent()
    return spaceJobConfiguration
  }

  override suspend fun isCIPresentInProject(): Boolean {
    return projectNioPath.resolve(SPACE_AUTOMATION_CI_FILE).exists()
  }

  private  fun createBannerContentProvider(): BannerContentProvider {
    val text = QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.space.text")
    val getTokenAction = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.get.token")) {
      openBrowserWithCurrentQodanaCloudFrontend()
    }
    val howToAddTokenToSpaceAutomation = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.how.add.token")) {
      BrowserUtil.browse("https://www.jetbrains.com/help/qodana/space-automation.html#Basic+configuration")
    }
    return BannerContentProvider(text, listOf(getTokenAction, howToAddTokenToSpaceAutomation), onClose = {
      baseSetupCIViewModel.isBannerVisibleStateFlow.value = false
    })
  }
}