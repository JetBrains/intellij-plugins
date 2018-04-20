// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.pubServer

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.sdk.DartSdkUtil
import java.io.File
import java.util.concurrent.ExecutionException

private val LOG = logger<DartWebdev>()

object DartWebdev {
  var activated = false

  fun useWebdev(sdk: DartSdk?): Boolean {
    if (sdk == null) return false
    val sdkVersion = sdk.version
    if (StringUtil.compareVersionNumbers(sdkVersion, "2") < 0) return false
    if (sdkVersion.startsWith("2.0.0-dev.")) {
      val devVersion = sdkVersion.substring("2.0.0-dev.".length)
      return StringUtil.compareVersionNumbers(devVersion, "50") >= 0
    }
    return true
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

      CapturingProcessHandler(command).runProcessWithProgressIndicator(indicator, 60 * 1000, false)
    }

    try {
      val processOutput = ProgressManager.getInstance()
        .runProcessWithProgressSynchronously<ProcessOutput, ExecutionException>(process,
                                                                                "Activating Package 'webdev'",
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