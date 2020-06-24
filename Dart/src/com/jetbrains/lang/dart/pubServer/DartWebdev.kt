// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.pubServer

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.lang.dart.DartBundle
import com.jetbrains.lang.dart.ide.actions.DartPubActionBase
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.sdk.DartSdkUtil
import java.io.File
import java.util.concurrent.ExecutionException

private val LOG = logger<DartWebdev>()

object DartWebdev {
  var activated: Boolean = false

  fun useWebdev(sdk: DartSdk?): Boolean {
    if (sdk == null) return false
    val sdkVersion = sdk.version
    return StringUtil.compareVersionNumbers(sdkVersion, "2") >= 0
  }

  /**
   * @return `false` only if explicitly cancelled by user
   */
  fun ensureWebdevActivated(project: Project): Boolean {
    ApplicationManager.getApplication().assertIsDispatchThread()

    if (activated) return true

    val sdk = DartSdk.getDartSdk(project) ?: return false // should't happen, checked before

    val process = {
      val indicator = ProgressManager.getInstance().progressIndicator
      indicator.isIndeterminate = true
      indicator.text2 = "pub global activate webdev"

      val command = GeneralCommandLine()
      command.isRedirectErrorStream = true
      val pubFile = File(DartSdkUtil.getPubPath(sdk.homePath))
      command.exePath = pubFile.path
      command.addParameters("global", "activate", "webdev")
      command.withEnvironment(DartPubActionBase.PUB_ENV_VAR_NAME, DartPubActionBase.getPubEnvValue() + ".webdev.activate")

      CapturingProcessHandler(command).runProcessWithProgressIndicator(indicator, 60 * 1000, false)
    }

    try {
      val processOutput = ProgressManager.getInstance()
        .runProcessWithProgressSynchronously<ProcessOutput, ExecutionException>(process,
                                                                                DartBundle.message(
                                                                                  "progress.title.activating.package.webdev"),
                                                                                true,
                                                                                project)
      if (processOutput.isCancelled) return false
      if (processOutput.isTimeout) {
        // It could be already activated so we want to give a chance and therefore return true here.
        LOG.warn("Webdev activation timed out. Output: " + processOutput.stdout)
        return true
      }
    }
    catch (e: com.intellij.execution.ExecutionException) {
      // It could be already activated so we want to give a chance and therefore return true here.
      LOG.warn("Failed to activate webdev: " + e.message)
      return true
    }

    activated = true
    return true
  }
}