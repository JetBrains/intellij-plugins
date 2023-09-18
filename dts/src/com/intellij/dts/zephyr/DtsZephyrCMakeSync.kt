package com.intellij.dts.zephyr

import com.intellij.dts.settings.DtsSettings
import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetListener
import com.intellij.execution.ExecutionTargetManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.CMakeSettingsListener
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget

class DtsZephyrCMakeSync(val project: Project) : CMakeSettingsListener, DtsSettings.ChangeListener, ExecutionTargetListener {
    companion object {
        private const val zephyrRootFlagName = "-DZEPHYR_BASE:PATH"
        private const val zephyrBoardFlagName = "-DBOARD"
    }

    private fun getFlagValue(options: List<String>, name: String): String? {
        val index = CMakeSettings.findFlagIndex(options, name)
        if (index < 0) return null

        return CMakeSettings.getFlagValue(options[index])
    }

    private fun findBoard(rootPath: String?, boardName: String?): ZephyrBoard? {
        if (rootPath == null || boardName == null) return null

        val root = LocalFileSystem.getInstance().findFileByPath(rootPath)
        for (board in DtsZephyrRoot.getAllBoards(root)) {
            if (board.name == boardName) return board
        }

        return null
    }

    private fun findActiveProfile(
        profiles: List<CMakeSettings.Profile>,
        target: ExecutionTarget
    ): CMakeSettings.Profile? {
        val enabled = profiles.filter { it.enabled }

        return if (target is CMakeBuildProfileExecutionTarget) {
            enabled.firstOrNull { it.name == target.profileName }
        } else {
            enabled.firstOrNull()
        }
    }

    private fun syncSettings(settings: DtsSettings, profile: CMakeSettings.Profile) {
        if (!settings.zephyrCMakeSync) return

        val options = CMakeSettings.getOptionsList(profile.generationOptions)

        val rootPath = getFlagValue(options, zephyrRootFlagName)
        val boardName = getFlagValue(options, zephyrBoardFlagName)
        val board = findBoard(rootPath, boardName)

        // do not update settings if nothing changed, prevents infinite loop
        if (settings.zephyrRoot == rootPath && settings.zephyrBoard == board) return

        ApplicationManager.getApplication().invokeLater {
            settings.update {
                if (rootPath != null) zephyrRoot = rootPath
                if (board != null) zephyrBoard = board.marshal()
            }
        }
    }

    override fun profilesChanged(old: List<CMakeSettings.Profile>, current: List<CMakeSettings.Profile>) {
        val profile = findActiveProfile(
            current,
            ExecutionTargetManager.getActiveTarget(project),
        ) ?: return

        syncSettings(DtsSettings.of(project), profile)
    }

    override fun settingsChanged(settings: DtsSettings) {
        val profile = findActiveProfile(
            CMakeSettings.getInstance(project).profiles,
            ExecutionTargetManager.getActiveTarget(project),
        ) ?: return

        syncSettings(settings, profile)
    }

    override fun activeTargetChanged(newTarget: ExecutionTarget) {
        val profile = findActiveProfile(
            CMakeSettings.getInstance(project).profiles,
            newTarget,
        ) ?: return

        syncSettings(DtsSettings.of(project), profile)
    }
}