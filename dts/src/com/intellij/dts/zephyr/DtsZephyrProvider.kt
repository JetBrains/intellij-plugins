package com.intellij.dts.zephyr

import com.intellij.dts.settings.DtsSettings
import com.intellij.dts.util.Either
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import java.io.File

@Service(Service.Level.PROJECT)
class DtsZephyrProvider(val project: Project) : Disposable {
    companion object {
        private val boardsPath = "boards"
        private val bindingsPath = "dts/bindings"

        private val includePaths = listOf("include", "dts", "dts/common")

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
        messageBus.subscribe(VirtualFileManager.VFS_CHANGES, BulkVirtualFileListenerAdapter(object : VirtualFileListener {
                override fun fileCreated(event: VirtualFileEvent) = fileSystemTracker.incModificationCount()

                override fun fileDeleted(event: VirtualFileEvent) = fileSystemTracker.incModificationCount()

                override fun fileMoved(event: VirtualFileMoveEvent) = fileSystemTracker.incModificationCount()
            })
        )
    }

    fun validateRoot(root: VirtualFile): Boolean {
        if (!root.isDirectory) return false

        // check if all expected directories exist
        for (expectedDirectory in listOf(boardsPath, bindingsPath) + includePaths) {
            val directory = root.findDirectory(expectedDirectory)
            if (directory == null || !directory.exists() || directory.children.isEmpty()) return false
        }

        return true
    }

    fun searchRoot(): VirtualFile? {
        val candidates = mutableListOf<VirtualFile>()

        val visitor = object : VirtualFileVisitor<Any>(limit(2)) {
            override fun visitFile(file: VirtualFile): Boolean {
                if (validateRoot(file)) {
                    candidates.add(file)
                    return false
                }

                return true
            }
        }

        for (module in project.modules) {
            val rootManger = ModuleRootManager.getInstance(module)

            for (root in rootManger.contentRoots) {
                VfsUtilCore.visitChildrenRecursively(root, visitor)
            }
        }

        // TODO: search for best match zephyr directory

        return candidates.firstOrNull()
    }

    private fun getRootSource(): Either<String, Pair<Long, Long>> {
        val path = settings.zephyrRoot

        return if (path == null) {
            Either.Right(Pair(fileSystemTracker.modificationCount, rootManager.modificationCount))
        } else {
            Either.Left(path)
        }
    }

    private fun getRootDir(): VirtualFile? {
        val source = getRootSource()

        if (rootSource == source) return root
        rootSource = source

        val newRoot = source.fold({ path ->
            // TODO: add folder as module root if the folder is currently not part of the project
            LocalFileSystem.getInstance().findFileByIoFile(File(path))
        }, {
            searchRoot()
        })

        if (newRoot != root) {
            rootTracker.incModificationCount()
            root = newRoot
        }

        return root
    }

    private fun getSubDir(path: String): VirtualFile? {
        val zephyr = getRootDir() ?: return null
        return zephyr.findDirectory(path)
    }

    fun getBoardDir(): VirtualFile? {
        val board = settings.zephyrBoard ?: return null
        val arch = settings.zephyrArch ?: return null

        return getSubDir("$boardsPath/$arch/$board")
    }

    fun getIncludeDirs(): List<VirtualFile> {
        val includes = sequence {
            yieldAll(includePaths)
            settings.zephyrArch?.let { yield("dts/$it") }
        }

        return includes.mapNotNull(::getSubDir).toList()
    }

    fun getBindingsDir(): VirtualFile? {
        return getSubDir(bindingsPath)
    }

    override fun dispose() {}
}