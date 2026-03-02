package com.intellij.dts.ide

import com.intellij.dts.settings.DtsSettings
import com.intellij.dts.zephyr.DtsZephyrProvider
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.ui.LayeredIcon
import com.intellij.ui.treeStructure.ProjectViewUpdateCause
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DtsProjectViewDecorator(private val project: Project): ProjectViewNodeDecorator {
  override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
    val settings = DtsSettings.of(project)
    if (settings.zephyrCMakeSync) return

    val zephyr = DtsZephyrProvider.of(project)
    val board = zephyr.board ?: return

    val file = node.virtualFile ?: return
    if (file.nameWithoutExtension != board.name) return

    val icon = data.getIcon(false) ?: return
    val layeredIcon = LayeredIcon.create(icon, AllIcons.Modules.SourceRootFileLayer)

    data.setIcon(layeredIcon)
  }

  class DtsSettingsListener(private val project: Project, private val scope: CoroutineScope): DtsSettings.ChangeListener {
    override fun settingsChanged(settings: DtsSettings) {
      scope.launch(Dispatchers.EDT) {
        ProjectView.getInstance(project).refresh(ProjectViewUpdateCause.PLUGIN_DTS)
      }
    }
  }
}
