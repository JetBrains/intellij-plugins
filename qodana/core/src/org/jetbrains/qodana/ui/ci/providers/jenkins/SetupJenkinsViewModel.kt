package org.jetbrains.qodana.ui.ci.providers.jenkins

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
import org.jetbrains.qodana.extensions.ci.JenkinsConfigHandler
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

internal const val JENKINS_FILE = "Jenkinsfile"


class SetupJenkinsViewModel(
  private val projectNioPath: Path,
  val project: Project,
  scope: CoroutineScope,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) : SetupCIViewModel {
  val baseSetupCIViewModel = BaseSetupCIViewModel(
    projectNioPath, project, JENKINS_FILE, scope,
    BaseSetupCIViewModel.Actions(
      ::createBannerContentProvider, ::spawnAddedConfigurationNotification,
      ::defaultConfigurationText, ::getInMemoryPatchOfPhysicalConfig
    )
  )

  override val finishProviderFlow: Flow<SetupCIFinishProvider?> = baseSetupCIViewModel.createFinishProviderFlow()

  override fun unselected() {
    baseSetupCIViewModel.unselected()
  }

  suspend fun getInMemoryPatchOfPhysicalConfig(configFile: VirtualFile): CIConfigFileState.InMemoryPatchOfPhysicalFile? {
    val physicalDocument = readAction { FileDocumentManager.getInstance().getDocument(configFile) } ?: return null
    val physicalText = physicalDocument.immutableCharSequence
    val contentOfInMemoryPatchDocument = JenkinsConfigHandler.addStage(project, physicalText.toString(), defaultStage())
    val inMemoryPatchDocument = createInMemoryDocument(project, contentOfInMemoryPatchDocument, configFile.name)

    return CIConfigFileState.InMemoryPatchOfPhysicalFile(project, inMemoryPatchDocument, physicalDocument, configFile.toNioPath())
  }

  private fun spawnAddedConfigurationNotification() {
    QodanaNotifications.General.notification(
      QodanaBundle.message("qodana.add.to.ci.finish.notification.jenkins.title"),
      QodanaBundle.message("qodana.add.to.ci.finish.notification.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

  @Suppress("LocalVariableName")
  private suspend fun defaultConfigurationText(): String {
    val WORKSPACE_ENV = "\${WORKSPACE}"

    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()

    val toAdd = branchesToAdd.joinToString(separator = "\n", postfix = "\n                  }") { "                      branch '$it'" }
    val baselineText = getSarifBaseline(project)?.let { " --baseline=$it" } ?: ""

    @Language("GROOVY")
    val branchesText = if (branchesToAdd.isNotEmpty()) {
      "\n                  when {\n$toAdd"
    } else ""

    @Suppress("UnnecessaryVariable")
    val jenkinsFileConfiguration = ("""
      pipeline {
          environment {
              QODANA_TOKEN = credentials('qodana-token')
          }
          agent {
              docker {
                  args '''
                      -v "$WORKSPACE_ENV":/data/project
                      --entrypoint=""
                      '''
                  image '${getQodanaImageNameMatchingIDE(useVersionPostfix = false)}'
              }
          }
          stages {
              stage('Qodana') {""" + branchesText + """
                  steps {
                      sh '''qodana$baselineText'''
                  }
              }
          }
      }
    """).trimIndent()
    return jenkinsFileConfiguration
  }

  @Suppress("LocalVariableName")
  private suspend fun defaultStage(): String {
    val WORKSPACE_ENV = "\${WORKSPACE}"

    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()

    val toAdd = branchesToAdd.joinToString(separator = "\n", postfix = "\n          }") { "              branch '$it'" }
    val baselineText = getSarifBaseline(project)?.let { " --baseline=$it" } ?: ""


    @Language("GROOVY")
    val branchesText = if (branchesToAdd.isNotEmpty()) {
      "\n          when {\n$toAdd"
    } else ""

    @Suppress("UnnecessaryVariable")
    val jenkinsFileConfiguration = ("""
      stage('Qodana') {
          environment {
              QODANA_TOKEN = credentials('qodana-token')
          }
          agent {
              docker {
                  args '''
                      -v "$WORKSPACE_ENV":/data/project
                      --entrypoint=""
                      '''
                  image '${getQodanaImageNameMatchingIDE(useVersionPostfix = false)}'
              }
          }""" + branchesText + """
          steps {
              sh '''qodana$baselineText'''
          }
      }
    """).replaceIndent("        ")
    return jenkinsFileConfiguration
  }

  override suspend fun isCIPresentInProject(): Boolean {
    return projectNioPath.resolve(JENKINS_FILE).exists()
  }

  private fun createBannerContentProvider(): BannerContentProvider {
    val text = QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.jenkins.text")
    val getTokenAction = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.get.token")) {
      openBrowserWithCurrentQodanaCloudFrontend()
    }
    val howToAddTokenToJenkins = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.how.add.token")) {
      BrowserUtil.browse("https://www.jetbrains.com/help/qodana/jenkins.html#Basic+configuration")
    }
    return BannerContentProvider(text, listOf(getTokenAction, howToAddTokenToJenkins), onClose = {
      baseSetupCIViewModel.isBannerVisibleStateFlow.value = false
    })
  }
}