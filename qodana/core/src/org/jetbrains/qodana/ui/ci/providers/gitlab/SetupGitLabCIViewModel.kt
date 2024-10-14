package org.jetbrains.qodana.ui.ci.providers.gitlab

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
import org.jetbrains.qodana.ui.originHost
import java.nio.file.Path
import kotlin.io.path.exists

class SetupGitLabCIViewModel(
  private val projectNioPath: Path,
  val project: Project,
  scope: CoroutineScope,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) : SetupCIViewModel {
  val baseSetupCIViewModel = BaseSetupCIViewModel(
    projectNioPath, project, GITLAB_CI_FILE, scope,
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
      QodanaBundle.message("qodana.add.to.ci.finish.notification.gitlab.title"),
      QodanaBundle.message("qodana.add.to.ci.finish.notification.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

  @Suppress("LocalVariableName")
  private suspend fun defaultConfigurationText(): String {
    val CI_PROJECT_DIR_ENV = "\$CI_PROJECT_DIR"
    val QODANA_CLOUD_TOKEN_ENV = "\$qodana_token"

    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches() + "merge_requests"

    @Language("YAML")
    val branchesText = """
      qodana:
        only:
      
    """.trimIndent() + branchesToAdd.joinToString(separator = "\n", postfix = "\n") { "    - $it" }

    val baselineText = getSarifBaseline(project)?.let { "--baseline=$it " } ?: ""

    @Language("YAML")
    val yamlConfiguration = branchesText + """
        image:
          name: ${getQodanaImageNameMatchingIDE(useVersionPostfix = false)}
          entrypoint: [""]
        variables:
          QODANA_TOKEN: $QODANA_CLOUD_TOKEN_ENV
        script:
          - qodana --save-report $baselineText--results-dir=$CI_PROJECT_DIR_ENV/.qodana
    """.replaceIndent("  ")
    return yamlConfiguration
  }

  override suspend fun isCIPresentInProject(): Boolean {
    if (projectNioPath.resolve(GITLAB_CI_FILE).exists()) return true
    return projectVcsDataProvider.originHost()?.startsWith("gitlab.com") ?: false
  }

  private fun createBannerContentProvider(): BannerContentProvider {
    val text = QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.gitlab.text")
    val getTokenAction = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.get.token")) {
      openBrowserWithCurrentQodanaCloudFrontend()
    }
    val howToAddTokenToGitLabCi = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.how.add.token")) {
      BrowserUtil.browse("https://www.jetbrains.com/help/qodana/gitlab.html#Forward+reports+to+Qodana+Cloud")
    }
    return BannerContentProvider(text, listOf(getTokenAction, howToAddTokenToGitLabCi), onClose = {
      baseSetupCIViewModel.isBannerVisibleStateFlow.value = false
    })
  }
}