// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.ide.impl.ProjectUtil
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.newvfs.events.*
import com.intellij.util.Alarm
import com.intellij.util.ui.update.MergingUpdateQueue
import com.intellij.util.ui.update.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Service
class VolarCoroutineScope(val cs: CoroutineScope) {
  companion object {
    @JvmStatic
    fun get() = service<VolarCoroutineScope>().cs
  }
}

class VolarRestartServiceListener : AsyncFileListener {
  override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
    if (getVolarProjects().isEmpty() || !Registry.`is`("volar.restart.service")) return null

    val toProcess = events.filter { isAcceptableEventType(it) && isAcceptableFile(it) }
    return if (toProcess.isNotEmpty()) {
      object : AsyncFileListener.ChangeApplier {
        override fun afterVfsChange() = service<VolarRestartService>().schedule(toProcess)
      }
    }
    else null

  }


  private fun isAcceptableFile(it: VFileEvent): Boolean {
    return it.file?.isDirectory ?: false ||
           it.path.let { it.endsWith(".vue") || it.endsWith(".ts") }
  }

  private fun isAcceptableEventType(event: VFileEvent) =
    event.getFileSystem() is LocalFileSystem &&
    (event is VFileMoveEvent || event is VFileCopyEvent || event is VFileDeleteEvent || event is VFilePropertyChangeEvent && event.isRename)
}

@Service
class VolarRestartService : Disposable {
  private val queue: MergingUpdateQueue = MergingUpdateQueue("Volar restart service",
                                                             1000, true, null, this, null,
                                                             Alarm.ThreadToUse.POOLED_THREAD)

  fun schedule(events: List<VFileEvent>) {
    queue.queue(Update.create(this) {
      VolarCoroutineScope.get().launch(Dispatchers.EDT) {
        restartVolarServices(events)
      }
    })
  }

  private suspend fun restartVolarServices(events: List<VFileEvent>) {
    val projects = readAction {
      getVolarProjects().filter {
        val fileIndex = ProjectFileIndex.getInstance(it)
        for (event in events) {
          val file = event.file
          if (file != null && file.isValid && fileIndex.isInProject(file) ||
              (file == null || file.isValid) && event is VFileDeleteEvent && hasProjectPath(it, event.path)) return@filter true
        }
        return@filter false
      }
    }

    projects.forEach { project ->
      if (project.isOpen) {
        getVolarService(project)?.restart(false)
      }
    }
  }

  private fun hasProjectPath(project: Project, path: String) = project.basePath?.let { FileUtil.isAncestor(it, path, false) } ?: false


  override fun dispose() {}
}

private fun getVolarProjects() =
  ProjectUtil.getOpenProjects()
    .mapNotNull { if (hasVolarService(it)) it else null }

private fun hasVolarService(project: Project) = getVolarService(project) != null

private fun getVolarService(project: Project) =
  TypeScriptService.getForProject(project) { it.serviceId == "vue" && it.isServiceCreated }
