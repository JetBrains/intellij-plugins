/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.watchers.consumers

import com.intellij.plugins.watcher.config.BackgroundTaskConsumer
import com.intellij.plugins.watcher.model.TaskOptions
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.TerraformFileType

abstract class TerraformToolTaskConsumer : BackgroundTaskConsumer() {
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
