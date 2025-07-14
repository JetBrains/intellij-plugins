// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.cli

import com.intellij.javascript.library.exclude.JsExcludeEntity
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectConfigurator
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

/**
 * @author Dennis.Ushakov
 */
class Angular2ProjectConfigurator : DirectoryProjectConfigurator {
  override fun configureProject(project: Project, baseDir: VirtualFile, moduleRef: Ref<Module>, isProjectCreatedWithWizard: Boolean) {
    val cliJson = AngularCliUtil.findCliJson(baseDir)
    if (cliJson == null) return

    val workspaceModel = project.workspaceModel
    val virtualFileUrlManager = workspaceModel.getVirtualFileUrlManager()
    val excludes = defaultAngularExcludes(baseDir.toVirtualFileUrl(virtualFileUrlManager))

    WriteAction.run<Throwable> {
      workspaceModel.updateProjectModel("create default angular excludes") { storage ->
        for (exclude in excludes) {
          storage.addEntity(JsExcludeEntity(exclude, JsExcludeEntity.MyEntitySource))
        }
      }
    }
    AngularCliUtil.createRunConfigurations(project, baseDir)
  }
}

private fun defaultAngularExcludes(baseDir: VirtualFileUrl): List<VirtualFileUrl> {
  return listOf(baseDir.append("dist"), baseDir.append("tmp"))
}

fun ContentEntry.addDefaultAngularExcludes(baseDir: VirtualFile) {
  addExcludeFolder(baseDir.url + "/dist")
  addExcludeFolder(baseDir.url + "/tmp")
}