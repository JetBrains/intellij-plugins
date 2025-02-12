// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.watchers.consumers

import com.intellij.plugins.watcher.config.BackgroundTaskConsumer
import com.intellij.plugins.watcher.model.TaskOptions
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.TerraformFileType

abstract class TfToolTaskConsumer : BackgroundTaskConsumer() {
  override fun isAvailable(file: PsiFile): Boolean {
    return false
  }

  companion object {

    fun createDefaultOptions(): TaskOptions {
      val options = TaskOptions()
      options.output = ""
      options.isImmediateSync = false
      options.exitCodeBehavior = TaskOptions.ExitCodeBehavior.ERROR
      options.fileExtension = TerraformFileType.defaultExtension
      // Unnecessary
      // options.setEnvData(EnvironmentVariablesData.create(new HashMap<String,String>(), true));
      // 2017.3
      // options.setRunOnExternalChanges(false);
      return options
    }
  }
}
