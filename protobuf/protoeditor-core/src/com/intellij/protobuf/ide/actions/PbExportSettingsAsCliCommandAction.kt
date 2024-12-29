package com.intellij.protobuf.ide.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.protobuf.ide.PbIdeBundle
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.ide.settings.computeDeterministicImportPaths
import com.intellij.protobuf.ide.settings.getOrComputeImportPathsForAllImportStatements
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.io.URLUtil
import java.awt.datatransfer.StringSelection

internal class PbExportSettingsAsCliCommandAction : DumbAwareAction(
  PbIdeBundle.messagePointer("action.export.as.cli.argument.name"),
  AllIcons.Actions.Copy
) {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      {
        CopyPasteManager.getInstance().setContents(StringSelection(
          joinImportPathsIntoCliArgument(project, PROTOC_PATH_ARGUMENT)
        ))
      },
      PbIdeBundle.message("action.export.as.cli.argument.progress.title"),
      true,
      project)
  }

  companion object {
    private const val PROTOC_PATH_ARGUMENT = "--proto_path"

    fun joinImportPathsIntoCliArgument(project: Project, pathArgumentName: String): String {

      val argumentWithWhiteSpaces = " $pathArgumentName "
      return retrieveUnescapedImportPaths(project)
        .joinToString(prefix = argumentWithWhiteSpaces, separator = argumentWithWhiteSpaces)
    }

    private fun retrieveImportUrls(project: Project): Sequence<String> {
      return computeDeterministicImportPaths(project, PbProjectSettings.getInstance(project))
        .mapNotNull(PbProjectSettings.ImportPathEntry::getLocation)
        .plus(getOrComputeImportPathsForAllImportStatements(project))
    }

    private fun retrieveUnescapedImportPaths(project: Project): Sequence<String> {
      return retrieveImportUrls(project)
        .map(URLUtil::extractPath)
        .map { if (it.endsWith("!/")) it else it.trim('/') }
        .map(FileUtil::toSystemDependentName)
        .map(ParametersListUtil::escape)
        .distinct()
    }
  }
}