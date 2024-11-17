package org.jetbrains.qodana.ui.ci.providers.github

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import icons.QodanaIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import org.jetbrains.qodana.ui.ci.SetupCIProvider
import org.jetbrains.qodana.ui.ci.SetupCIProviderFactory
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import javax.swing.Icon
import javax.swing.JComponent
import kotlin.io.path.Path

internal const val DEFAULT_GITHUB_WORKFLOW_FILENAME = "qodana_code_quality.yml"
internal const val GITHUB_WORKFLOWS_DIR = ".github/workflows"
internal val GITHUB_WORKFLOWS_EXTENSIONS = setOf("yml", "yaml")

class SetupGitHubActionsProviderFactory : SetupCIProviderFactory {
  override fun createSetupCIProvider(project: Project, dialogScope: CoroutineScope, projectVcsDataProvider: ProjectVcsDataProvider): SetupCIProvider? {
    val projectNioPath = project.guessProjectDir()?.toNioPath() ?: project.basePath?.let { Path(it) } ?: return null
    val viewModel = SetupGitHubActionsViewModel(projectNioPath, project, dialogScope, projectVcsDataProvider)

    return object : SetupCIProvider.Available {
      override val viewModel: SetupCIViewModel = viewModel

      override val viewFlow: Flow<JComponent>
        get() {
          return flow {
            coroutineScope {
              emit(SetupGitHubActionsView(this, viewModel).getView())
              awaitCancellation()
            }
          }
        }

      override val text: String get() = QodanaBundle.message("qodana.add.to.ci.github.actions")

      override val icon: Icon get() = QodanaIcons.Icons.CI.GitHub

      override val nextButtonText get() = QodanaBundle.message("qodana.run.wizard.finish.ci.button")
    }
  }
}