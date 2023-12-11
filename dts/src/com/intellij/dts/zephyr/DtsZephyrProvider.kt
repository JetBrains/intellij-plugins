package com.intellij.dts.zephyr

import com.intellij.dts.settings.DtsSettings
import com.intellij.dts.util.Either
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import java.io.File

@Service(Service.Level.PROJECT)
class DtsZephyrProvider(val project: Project) : Disposable.Default {
  companion object {
    fun of(project: Project): DtsZephyrProvider = project.service()
  }

  private val settings by lazy { DtsSettings.of(project) }
  private val rootManager by lazy { ProjectRootManager.getInstance(project) }

  private val fileSystemTracker = SimpleModificationTracker()
  private val rootTracker = SimpleModificationTracker()

  /**
   * Stores the path to the current zephyr root. The root is either determined
   * by the path specified by the user in the [DtsSettings] or inferred
   * automatically if the path is empty.
   */
  private var root: VirtualFile? = null

  /**
   * Stores how the current root was determined. Left stores the path specified
   * in the settings and right stores the state of the file system when the
   * root was searched for.
   */
  private var rootSource: Either<String, Pair<Long, Long>>? = null

  /**
   * Checks if the root dir needs to change and increases if the root dir
   * changes.
   *
   * Could interact with the file system.
   */
  val modificationCount: Long
    get() {
      getRootDir()

      return rootTracker.modificationCount
    }

  init {
    val messageBus = project.messageBus.connect(this)
    messageBus.subscribe(
      VirtualFileManager.VFS_CHANGES,
      BulkVirtualFileListenerAdapter(
        object : VirtualFileListener {
          override fun fileCreated(event: VirtualFileEvent) = fileSystemTracker.incModificationCount()

          override fun fileDeleted(event: VirtualFileEvent) = fileSystemTracker.incModificationCount()

          override fun fileMoved(event: VirtualFileMoveEvent) = fileSystemTracker.incModificationCount()
        }
      ),
    )
  }

  private fun getRootSource(): Either<String, Pair<Long, Long>> {
    val path = settings.zephyrRoot

    return if (path == null) {
      Either.Right(Pair(fileSystemTracker.modificationCount, rootManager.modificationCount))
    }
    else {
      Either.Left(path)
    }
  }

  private fun getRootDir(): VirtualFile? {
    val source = getRootSource()

    if (rootSource == source) return root
    rootSource = source

    val newRoot = source.fold(
      { path ->
        LocalFileSystem.getInstance().findFileByIoFile(File(path))
      },
      {
        DtsZephyrRoot.searchForRoot(project)
      },
    )

    if (newRoot != root) {
      rootTracker.incModificationCount()
      root = newRoot
    }

    return root
  }

  fun getBoardDir(): VirtualFile? = settings.zephyrBoard?.virtualFile

  fun getBindingsDir(): VirtualFile? = DtsZephyrRoot.getBindingsDir(getRootDir())

  fun getIncludeDirs(): List<VirtualFile> = DtsZephyrRoot.getIncludeDirs(getRootDir(), settings.zephyrBoard)
}