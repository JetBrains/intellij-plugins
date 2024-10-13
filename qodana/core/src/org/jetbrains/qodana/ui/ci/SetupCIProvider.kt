package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import javax.swing.Icon
import javax.swing.JComponent

interface SetupCIProviderFactory {
  companion object {
    val EP = ExtensionPointName<SetupCIProviderFactory>("org.intellij.qodana.setupCIProviderFactory")
  }

  fun createSetupCIProvider(project: Project, dialogScope: CoroutineScope, projectVcsDataProvider: ProjectVcsDataProvider): SetupCIProvider?
}

sealed interface SetupCIProvider {
  val text: @NlsContexts.Label String

  val icon: Icon

  interface Available : SetupCIProvider {
    val viewModel: SetupCIViewModel

    val viewFlow: Flow<JComponent>

    val nextButtonText: String
  }

  interface Unavailable : SetupCIProvider {
    val tooltipText: String

    val helpPageText: String

    val helpPageLink: String
  }
}