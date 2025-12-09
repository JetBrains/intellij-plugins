// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("IO_FILE_USAGE")

package org.jetbrains.idea.perforce.checkout

import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.impl.welcomeScreen.cloneableProjects.CloneableProjectsService.*
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.P4File
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigFields.*
import org.jetbrains.idea.perforce.perforce.connections.PerforceWorkspaceConfigurator.Companion.GITIGNORE_NAME
import org.jetbrains.idea.perforce.perforce.connections.PerforceWorkspaceConfigurator.Companion.P4CONFIG_NAME
import org.jetbrains.idea.perforce.perforce.connections.PerforceWorkspaceConfigurator.Companion.P4IGNORE_NAME
import java.io.File
import java.io.IOException

internal class PerforceCloneTask(
  private val project: Project,
  private val params: PerforceCloneParams,
  private val listener: CheckoutProvider.Listener,
) : CloneTask {

  override fun taskInfo(): CloneTaskInfo {
    return CloneTaskInfo(
      PerforceBundle.message("checkout.progress.title"),
      PerforceBundle.message("checkout.progress.cancel"),
      PerforceBundle.message("checkout.task.title"),
      PerforceBundle.message("checkout.task.tooltip"),
      PerforceBundle.message("checkout.task.failed"),
      PerforceBundle.message("checkout.task.canceled"),
      PerforceBundle.message("checkout.stop.title"),
      PerforceBundle.message("checkout.stop.description", params.server)
    )
  }

  override fun run(indicator: ProgressIndicator): CloneStatus {
    indicator.text = PerforceBundle.message("checkout.progress.syncing", params.directory)

    val targetDir = File(params.directory)
    if (!targetDir.exists()) {
      targetDir.mkdirs()
    }

    try {
      val runner = PerforceCloneRunnerFactory.createRunner(project, params, targetDir)
      val p4File = P4File.create(targetDir)

      val result = runner.sync(p4File, true)

      val stderr = result.stderr
      if (result.exitCode != 0 && stderr.isNotBlank() && !stderr.contains(PerforceRunner.FILES_UP_TO_DATE)) {
        return CloneStatus.FAILURE
      }

      LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetDir)?.refresh(true, true)

      writeP4Config(targetDir)

      listener.directoryCheckedOut(targetDir, PerforceVcs.getKey())
      listener.checkoutCompleted()

      return CloneStatus.SUCCESS
    }
    catch (e: VcsException) {
      val projectPath = FileUtil.toSystemIndependentName(targetDir.absolutePath)
      fileLogger().warn("Cannot clone project $projectPath", e)
      return CloneStatus.FAILURE
    }
  }

  private fun writeP4Config(targetDir: File) {
    val configFile = targetDir.resolve(P4CONFIG_NAME)
    if (configFile.exists()) {
      return
    }

    val configContent = buildString {
      appendLine("${P4PORT.name}=${params.server}")
      appendLine("${P4USER.name}=${params.user}")
      appendLine("${P4CLIENT.name}=${params.client}")
      append("${P4IGNORE.name}=$P4IGNORE_NAME;$GITIGNORE_NAME")
    }

    try {
      FileUtil.writeToFile(configFile, configContent)
    }
    catch (e: IOException) {
      fileLogger().warn("Cannot create p4config in $targetDir", e)
    }
  }
}
