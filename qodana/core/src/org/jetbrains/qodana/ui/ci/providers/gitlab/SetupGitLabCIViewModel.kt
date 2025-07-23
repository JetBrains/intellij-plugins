package org.jetbrains.qodana.ui.ci.providers.gitlab

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationInfo
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

  private val HEADER_TEXT = """
      #-------------------------------------------------------------------------------#
      #        Discover additional configuration options in our documentation         #
      #              https://www.jetbrains.com/help/qodana/gitlab.html                #
      #-------------------------------------------------------------------------------#
      
      
    """.trimIndent()

  private fun spawnAddedConfigurationNotification() {
    QodanaNotifications.General.notification(
      QodanaBundle.message("qodana.add.to.ci.finish.notification.gitlab.title"),
      QodanaBundle.message("qodana.add.to.ci.finish.notification.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

  private suspend fun defaultConfigurationText(): String {
    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()

    val branchesText = branchesToAdd.joinToString(separator = "\n", prefix = "\n") { $$"        - if: $CI_COMMIT_BRANCH == \"$$it\"" }

    @Language("YAML")
    val rulesText = $$"""
    rules:
      - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    """.replaceIndent("      ").trimStart() + branchesText

    val argsText = getSarifBaseline(project)?.let { "args: --baseline=$it\n" } ?: ""
    val inputsText = argsText + """
      # When mr-mode is set to true, Qodana analyzes only the files that have been changed
      mr-mode: false
      use-caches: true
      post-mr-comment: true
      # Upload Qodana results (SARIF, other artifacts, logs) as an artifact to the job
      upload-result: false
      # quick-fixes available in Ultimate and Ultimate Plus plans
      push-fixes: 'none'
    """.trimIndent()

    @Language("YAML")
    val yamlConfiguration = HEADER_TEXT + $$"""
    include:
      - component: $CI_SERVER_FQDN/qodana/qodana/qodana-gitlab-ci@v$${ApplicationInfo.getInstance().majorVersion}.$${ApplicationInfo.getInstance().minorVersionMainPart}
        inputs:
          $${inputsText.replaceIndent("          ").trimStart()}
    
    qodana:
      $$rulesText
    """.trimIndent()
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