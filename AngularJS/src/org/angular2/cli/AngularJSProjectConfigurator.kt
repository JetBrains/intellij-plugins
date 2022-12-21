// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.cli

import com.intellij.ide.projectView.actions.MarkRootActionBase
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectConfigurator

/**
 * @author Dennis.Ushakov
 */
class AngularJSProjectConfigurator : DirectoryProjectConfigurator {
  override fun configureProject(project: Project, baseDir: VirtualFile, moduleRef: Ref<Module>, isProjectCreatedWithWizard: Boolean) {
    val module = moduleRef.get() ?: return

    val cliJson = AngularCliUtil.findCliJson(baseDir)
    val model = ModuleRootManager.getInstance(module).modifiableModel
    val entry = MarkRootActionBase.findContentEntry(model, baseDir)
    if (entry != null && cliJson != null) {
      excludeDefault(baseDir, entry)
      ApplicationManager.getApplication().runWriteAction { model.commit() }
      project.save()
      AngularCliUtil.createRunConfigurations(project, baseDir)
    }
    else {
      model.dispose()
    }
  }

  companion object {

    fun excludeDefault(baseDir: VirtualFile, entry: ContentEntry) {
      entry.addExcludeFolder(baseDir.url + "/dist")
      entry.addExcludeFolder(baseDir.url + "/tmp")
    }
  }
}
