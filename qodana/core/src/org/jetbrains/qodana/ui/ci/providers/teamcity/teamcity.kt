package org.jetbrains.qodana.ui.ci.providers.teamcity

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLayeredPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.update.UiNotifyConnector
import icons.QodanaIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import org.jetbrains.qodana.ui.ci.QodanaCopyFloatingToolbar
import org.jetbrains.qodana.ui.ci.SetupCIProvider
import org.jetbrains.qodana.ui.ci.SetupCIProviderFactory
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import org.jetbrains.qodana.ui.ci.providers.bannerWithEditorComponent
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLayeredPane
import kotlin.io.path.Path

class SetupTeamcityDslProviderFactory : SetupCIProviderFactory {
  override fun createSetupCIProvider(project: Project, dialogScope: CoroutineScope, projectVcsDataProvider: ProjectVcsDataProvider): SetupCIProvider? {
    val projectNioPath = project.guessProjectDir()?.toNioPath() ?: project.basePath?.let { Path(it) } ?: return null
    val viewModel = SetupTeamcityDslViewModel(projectNioPath, project, dialogScope, projectVcsDataProvider)

    return object : SetupCIProvider.Available {
      override val viewModel: SetupCIViewModel = viewModel

      override val viewFlow: Flow<JComponent> = flow {
        coroutineScope {
          emit(setupTeamcityDslView(this, viewModel))
          awaitCancellation()
        }
      }

      override val nextButtonText: String = QodanaBundle.message("qodana.run.wizard.finish.ci.button.ok")

      override val text: String get() = QodanaBundle.message("qodana.add.to.ci.teamcity")

      override val icon: Icon get() = QodanaIcons.Icons.CI.TeamCity
    }
  }
}

private fun setupTeamcityDslView(scope: CoroutineScope, viewModel: SetupTeamcityDslViewModel): DialogPanel {
  val mainPanel = bannerWithEditorComponent(
    scope,
    viewModel.bannerContentProviderFlow,
    flow {
      emit(viewModel.configEditorDeferred.await())
    },
    viewModel.project,
  )

  return panel {
    row {
      val labelComponent = label("").component
      labelComponent.font = JBFont.h2()

      scope.launch(QodanaDispatchers.Ui) {
        val editor = viewModel.configEditorDeferred.await()
        UiNotifyConnector.doWhenFirstShown(editor.component) { setNewLayout(editor) }
        labelComponent.text = QodanaBundle.message("qodana.add.to.ci.teamcity.snippet")
      }
    }
    row {
      text(QodanaBundle.message("qodana.add.to.ci.teamcity.text"))
    }.bottomGap(BottomGap.SMALL)
    row {
      text(QodanaBundle.message("qodana.add.to.ci.teamcity.tip"))
    }.bottomGap(BottomGap.SMALL)
    row {
      cell(mainPanel)
        .resizableColumn()
        .align(Align.FILL)
    }.resizableRow().bottomGap(BottomGap.SMALL)
  }
}

private fun setNewLayout(editor: Editor) {
  if (editor !is EditorImpl) return
  val panelComponents = editor.component.components
  val newToolbar = QodanaCopyFloatingToolbar(editor)
  val layeredPane = panelComponents.filterIsInstance<JBLayeredPane>().firstOrNull() as? JLayeredPane ?: return
  layeredPane.add(newToolbar, JLayeredPane.POPUP_LAYER as Any)
}