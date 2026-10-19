package com.intellij.deno.roots

import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoUtil
import com.intellij.deno.entities.DenoEntity
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesWorkspaceModelNode
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesWorkspaceModelNodesProvider
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.virtualFile

class DenoExternalLibrariesWorkspaceModelNodesProvider : ExternalLibrariesWorkspaceModelNodesProvider<DenoEntity> {
  override fun getWorkspaceClass(): Class<DenoEntity> = DenoEntity::class.java

  override fun createNode(entity: DenoEntity, project: Project, settings: ViewSettings?): AbstractTreeNode<*>? {
    if (!useWorkspaceModel()) return null
    val roots = listOfNotNull(entity.denoTypes, entity.depsFile, entity.npmFolder).mapNotNull { it.virtualFile }
    if (roots.isEmpty()) return null
    return ExternalLibrariesWorkspaceModelNode(project, roots, emptyList(), DenoBundle.message("deno.library.name"),
                                               DenoUtil.getDefaultDenoIcon(), settings)
  }
}
