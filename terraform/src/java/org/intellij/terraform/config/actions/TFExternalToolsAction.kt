/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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
package org.intellij.terraform.config.actions

import com.intellij.execution.ExecutionException
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ExceptionUtil
import org.intellij.terraform.config.TerraformConstants
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.hcl.HCLFileType
import org.jetbrains.annotations.Nls

abstract class TFExternalToolsAction : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
    if (project == null || file == null || !isAvailableOnFile(file, true, true)) {
      e.presentation.isEnabled = false
      return
    }
    e.presentation.isEnabled = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    val file = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
    assert(project != null)
    val title = StringUtil.notNullize(e.presentation.text)

    val module = ModuleUtilCore.findModuleForFile(file, project!!)
    try {
      invoke(project, module, title, file)
    }
    catch (ex: ExecutionException) {
      error(title, project, ex)
      LOG.error(ex)
    }
  }

  @Throws(ExecutionException::class)
  abstract fun invoke(project: Project,
                      module: Module?,
                      title: @Nls String,
                      virtualFile: VirtualFile)

  companion object {
    private val LOG = Logger.getInstance(TFExternalToolsAction::class.java)

    private fun error(title: @Nls String, project: Project, ex: Exception?) {
      val message = if (ex == null) "" else ExceptionUtil.getUserStackTrace(ex, LOG)
      TerraformConstants.EXECUTION_NOTIFICATION_GROUP.createNotification(title, message, NotificationType.ERROR).notify(project)
    }

    fun isAvailableOnFile(file: VirtualFile, checkDirChildren: Boolean, onlyTerraformFileType: Boolean): Boolean {
      if (!file.isInLocalFileSystem) return false
      if (file.isDirectory) {
        if (!checkDirChildren) return false
        val children = file.children
        if (children != null) {
          for (child in children) {
            if (isAvailableOnFile(child, false, onlyTerraformFileType)) return true
          }
        }
        return false
      }

      return if (onlyTerraformFileType)
        FileTypeRegistry.getInstance().isFileOfType(file, TerraformFileType)
      else FileTypeRegistry.getInstance().isFileOfType(file, HCLFileType) ||
           FileTypeRegistry.getInstance().isFileOfType(file, TerraformFileType)
    }
  }
}
