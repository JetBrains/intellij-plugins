package com.intellij.openRewrite.run.before

import com.intellij.execution.BeforeRunTask
import com.intellij.java.library.MavenCoordinates
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

internal const val DEFAULT_BEFORE_RUN_VERSION = "0.0.1-SNAPSHOT"
private const val UNKNOWN = "unknown"

class OpenRewriteInstallBeforeRunTask : BeforeRunTask<OpenRewriteInstallBeforeRunTask>(INSTALL_BEFORE_RUN_TASK_KEY),
                                        PersistentStateComponent<OpenRewriteInstallBeforeRunTask.State> {
  private var state = State()
  var scratchFileUrl: String?
    get() = state.scratchFileUrl
    set(value) {
      state.scratchFileUrl = value
    }
  var groupId: String?
    get() = state.groupId
    set(value) {
      state.groupId = value
    }
  var artifactId: String?
    get() = state.artifactId
    set(value) {
      state.artifactId = value
    }
  var version: String?
    get() = state.version
    set(value) {
      state.version = value
    }

  fun getScratchVirtualFile(): VirtualFile? {
    val fileUrl = scratchFileUrl ?: return null
    val path = FileUtil.toSystemIndependentName(fileUrl)
    return if (path.isNotEmpty()) LocalFileSystem.getInstance().findFileByPath(path) else null
  }

  fun getCoordinates(): MavenCoordinates {
    val id: String by lazy { getId() }
    return MavenCoordinates(state.groupId ?: id, state.artifactId ?: id, state.version ?: DEFAULT_BEFORE_RUN_VERSION)
  }

  internal fun getId(): String {
    val fileName = scratchFileUrl?.substringAfterLast("/")?.substringAfterLast('\\') ?: return UNKNOWN
    val fileNameWithoutExtension = fileName.substringBeforeLast(".")
    return fileNameWithoutExtension
      .replaceFirstChar { if (it.isUpperCase()) it.lowercase() else it.toString() }
      .flatMap { char ->
        if (char.isUpperCase()) listOf('-', char.lowercaseChar()) else listOf(char)
      }
      .joinToString("")
  }

  override fun loadState(state: State) {
    state.resetModificationCount()
    this.state = state
  }

  override fun getState(): State = state

  class State : BaseState() {
    var scratchFileUrl: String? by string()
    var groupId: String? by string()
    var artifactId: String? by string()
    var version: String? by string()
  }
}