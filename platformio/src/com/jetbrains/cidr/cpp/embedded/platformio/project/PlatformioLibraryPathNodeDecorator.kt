package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.components.service
import com.intellij.util.PlatformIcons
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService

class PlatformioLibraryPathNodeDecorator : ProjectViewNodeDecorator {
  override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
    if (node is PsiDirectoryNode) {
      val project = node.project
      if (project.service<PlatformioWorkspace>().isInitialized) {
        val path = node.virtualFile?.path
        if (path != null) {
          val librariesPaths = project.service<PlatformioService>().librariesPaths
          val libName = librariesPaths[path]
          if (!libName.isNullOrBlank()) {
            data.locationString = path
            data.presentableText = libName
            data.setIcon(PlatformIcons.LIBRARY_ICON)
          }
        }
      }
    }
  }
}
