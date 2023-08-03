package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.PlatformIcons
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService

class PlatformioSourcesStructureProvider : TreeStructureProvider {

  private fun detectSubfolder(folder: String, subfolder: String): Boolean {
    if (folder == subfolder) return true
    return subfolder.substringAfter(folder).startsWith('/')
  }

  override fun modify(parent: AbstractTreeNode<*>,
                      children: MutableCollection<AbstractTreeNode<*>>,
                      settings: ViewSettings?): MutableCollection<AbstractTreeNode<*>> {
    if (parent is ProjectViewModuleNode) {
      val project = parent.project
      if (project.service<PlatformioWorkspace>().isInitialized) {
        val librariesPaths = project.service<PlatformioService>().librariesPaths
        val result = mutableListOf<AbstractTreeNode<*>>()
        val libNodes = mutableMapOf<String, LibraryNode>()
        children.forEach { child ->
          var libName: String? = null
          val path = child.asSafely<PsiDirectoryNode>()?.virtualFile?.path
          if (path != null) {
            libName = librariesPaths.entries.firstOrNull { detectSubfolder(it.key, path) }?.value
          }
          if (libName != null) {
            val libNode = libNodes.getOrPut(libName) { LibraryNode(project, libName) }
            libNode.children.add(child)
            result.add(libNode)
          }
          else {
            result.add(child)
          }
        }
        return result
      }
    }
    return children
  }

  inner class LibraryNode(project: Project, name: String) : AbstractTreeNode<String>(project, name) {

    private val children: MutableCollection<AbstractTreeNode<*>> = mutableListOf()
    override fun getChildren(): MutableCollection<AbstractTreeNode<*>> = children

    override fun update(presentation: PresentationData) {
      presentation.setIcon(PlatformIcons.LIBRARY_ICON)
      presentation.presentableText = value
    }
  }
}

