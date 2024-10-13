package org.jetbrains.qodana.ui.ci.providers.teamcity

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.IntentionsUI
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.application
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.openBrowserWithCurrentQodanaCloudFrontend
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.UiAnyModality
import org.jetbrains.qodana.getFileTypeByFilename
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import org.jetbrains.qodana.ui.ci.SetupCIFinishProvider
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import org.jetbrains.qodana.ui.ci.providers.getSarifBaseline
import org.jetbrains.qodana.ui.ciRelevantBranches
import org.jetbrains.qodana.ui.createEditor
import org.jetbrains.qodana.ui.createInMemoryDocument
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists

class SetupTeamcityDslViewModel(
  private val projectNioPath: Path,
  val project: Project,
  private val scope: CoroutineScope,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) : SetupCIViewModel {
  val configEditorDeferred: Deferred<Editor> = createEditorDeferred()

  override val finishProviderFlow: Flow<SetupCIFinishProvider?> = flowOf {}

  private val isBannerVisibleStateFlow = MutableStateFlow(true)
  val bannerContentProviderFlow: Flow<BannerContentProvider?> = isBannerVisibleStateFlow.map { if (it) createBannerContentProvider() else null }

  private val configFiletype: FileType = getFileTypeByFilename("settings.kts")

  override fun unselected() {
    if (!configEditorDeferred.isCompleted) return
    scope.launch(QodanaDispatchers.Ui) {
      IntentionsUI.getInstance(project).hideForEditor(configEditorDeferred.await())
    }
  }

  private fun createEditorDeferred(): Deferred<Editor> {
    val deferred = CompletableDeferred<Editor>()
    scope.launch(QodanaDispatchers.Default) {
      var editorToRelease: Editor? = null
      var psiFile: PsiFile? = null
      try {
        val inMemoryDocument = createInMemoryDocument(project, defaultConfigurationText(), "settings.kts")
        val newEditor = createEditor(project, inMemoryDocument, configFiletype)
        editorToRelease = newEditor
        readAction {
          psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editorToRelease.document)
        }
        psiFile?.let { DaemonCodeAnalyzer.getInstance(project).setHighlightingEnabled(it, false) }
        deferred.complete(newEditor)
        awaitCancellation()
      }
      finally {
        withContext(QodanaDispatchers.UiAnyModality + NonCancellable) {
          editorToRelease?.let {editor ->
            psiFile?.let { DaemonCodeAnalyzer.getInstance(project).setHighlightingEnabled(it, true) }
            if (!editor.isDisposed) EditorFactory.getInstance().releaseEditor(editor)
          }
        }
      }
    }
    return deferred
  }

  private suspend fun defaultConfigurationText(): String {
    val branches = projectVcsDataProvider.ciRelevantBranches()
    val defaultBranch = if ("master" in branches) "refs/heads/master" else "refs/heads/main"
    val otherBranches = branches.map { "refs/heads/$it" } - defaultBranch
    val otherBranchesText = otherBranches.joinToString(separator = "\n", postfix = "\n") { "    $it" }

    val originUrl = projectVcsDataProvider.originUrl()
    val vcsName = if (originUrl != null) "$originUrl#$defaultBranch" else null

    val linter = getDslLinter()

    val stringToFilterOut = UUID.randomUUID().toString()
    val baselineText = getSarifBaseline(project)?.let { "additionalQodanaArguments = \"--baseline $it\"" } ?: stringToFilterOut

    @Suppress("UnnecessaryVariable")
    @Language("kotlin")
    val dslText = """
      import jetbrains.buildServer.configs.kotlin.*
      import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
      import jetbrains.buildServer.configs.kotlin.buildSteps.qodana
      import jetbrains.buildServer.configs.kotlin.triggers.vcs
      import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
      
      version = "2022.10"
      
      project {
        vcsRoot(ProjectVcsRoot)
        buildType(Build)
      }
      
      object Build : BuildType({
        name = "Build"
        vcs {
          root(ProjectVcsRoot)
        }
        steps {
          qodana {
            name = "Qodana Step"
            reportAsTests = true
            linter = $linter {}
            $baselineText
          }
        }
        triggers {
          vcs {}
        }
        features {
          perfmon {}
        }
      })
      
      object ProjectVcsRoot : GitVcsRoot({
        name = "$vcsName"
        url = "$originUrl"
        branch = "$defaultBranch"
        branchSpec = ""${'"'}

      """.trimIndent().lineSequence().filter { !it.contains(stringToFilterOut) }.joinToString("\n") + otherBranchesText + """
        ""${'"'}.trimIndent()
      })
      """.trimIndent()
    return dslText
  }

  private fun getDslLinter(): String {
    val unknownLinter = "<linter>"
    if (application.isUnitTestMode) {
      return unknownLinter
    }
    return when (ApplicationInfo.getInstance().build.productCode) {
      "IU" -> "jvm"
      "IC" -> "jvmCommunity"
      "PY" -> "python"
      "PS" -> "php"
      "WS" -> "javascript"
      else -> unknownLinter
    }
  }

  override suspend fun isCIPresentInProject(): Boolean {
    return (projectNioPath.resolve(".teamcity").exists())
  }

  private fun createBannerContentProvider(): BannerContentProvider {
    val text = QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.teamcity.text")
    val getTokenAction = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.get.token")) {
      openBrowserWithCurrentQodanaCloudFrontend()
    }
    val howToAddTokenToGitLabCi = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.how.add.token")) {
      BrowserUtil.browse("https://www.jetbrains.com/help/qodana/teamcity.html#Forward+reports+to+Qodana+Cloud")
    }
    return BannerContentProvider(text, listOf(getTokenAction, howToAddTokenToGitLabCi), onClose = {
      isBannerVisibleStateFlow.value = false
    })
  }
}