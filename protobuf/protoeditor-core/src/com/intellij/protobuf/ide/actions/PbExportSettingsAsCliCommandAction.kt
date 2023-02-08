package com.intellij.protobuf.ide.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.protobuf.ide.PbIdeBundle
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.ui.AnActionButton
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.io.URLUtil
import java.awt.datatransfer.StringSelection

class PbExportSettingsAsCliCommandAction : AnActionButton(
  { PbIdeBundle.message("action.export.as.cli.argument.name") },
  AllIcons.Actions.Copy
) {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    CopyPasteManager.getInstance().setContents(StringSelection(joinImportPathsIntoCliArgument(project, PROTOC_PATH_ARGUMENT)))
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  companion object {
    private const val PROTOC_PATH_ARGUMENT = "--proto_path"

    fun joinImportPathsIntoCliArgument(project: Project, pathArgumentName: String): String {

      val argumentWithWhiteSpaces = " $pathArgumentName "
      return retrieveUnescapedImportPaths(project)
        .joinToString(prefix = argumentWithWhiteSpaces, separator = argumentWithWhiteSpaces, transform = ParametersListUtil::escape)
    }

    private fun retrieveImportUrls(project: Project): Sequence<String> {
      return PbProjectSettings.getInstance(project)
        .importPathEntries
        .asSequence()
        .mapNotNull(PbProjectSettings.ImportPathEntry::getLocation)
    }

    private fun retrieveUnescapedImportPaths(project: Project): Sequence<String> {
      return retrieveImportUrls(project)
        .map(URLUtil::extractPath)
        .map(FileUtil::toSystemDependentName)
    }
  }
}