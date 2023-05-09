package com.intellij.deno.roots

import com.intellij.deno.DenoBundle
import com.intellij.deno.entities.DenoEntity
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesWorkspaceModelNode
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesWorkspaceModelNodesProvider
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.workspaceModel.ide.virtualFile

class DenoExternalLibrariesWorkspaceModelNodesProvider : ExternalLibrariesWorkspaceModelNodesProvider<DenoEntity> {
  override fun getWorkspaceClass(): Class<DenoEntity> = DenoEntity::class.java

  override fun createNode(entity: DenoEntity, project: Project, settings: ViewSettings?): AbstractTreeNode<*>? {
    if (!useWorkspaceModel()) return null
    if (entity.denoTypes == null && entity.depsFile == null) return null
    return ExternalLibrariesWorkspaceModelNode(project,
                                               listOfNotNull(entity.denoTypes?.virtualFile, entity.depsFile?.virtualFile),
                                               DenoBundle.message("deno.library.name"), settings)
  }
}