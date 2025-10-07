// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.actions

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.modules.InstallNodeModuleQuickFix
import com.intellij.lang.javascript.modules.PackageInstaller
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.libraries.nuxt.NUXT_2_13_0
import org.jetbrains.vuejs.libraries.nuxt.NUXT_TYPES_PKG
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtModelManager
import java.io.File

class InstallNuxtTypesAction(val project: Project, val packageJson: VirtualFile, val notification: Notification) :
  DumbAwareAction(JavaScriptBundle.message("node.js.quickfix.install.node.module.with.dev.dependencies.text", NUXT_TYPES_PKG)) {
  override fun actionPerformed(e: AnActionEvent) {
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter ?: return
    val parent = packageJson.parent
    notification.hideBalloon()

    ProgressManager.getInstance().run(object : Task.Backgroundable(project, JavaScriptBundle.message(
      "node.js.quickfix.install.node.module.with.dev.dependencies.text", NUXT_TYPES_PKG), true) {
      override fun run(indicator: ProgressIndicator) {
        val expectedVersion = ReadAction.compute<String?, Throwable> {
          NuxtModelManager.getApplication(project, packageJson)
            ?.nuxtVersion?.let { getMatchingTypesVersion(it) }
        }
        val extraOptions = InstallNodeModuleQuickFix.buildExtraOptions(project, true)
        val listener = InstallNodeModuleQuickFix.createListener(project, packageJson, NUXT_TYPES_PKG)
        PackageInstaller(project, interpreter, NUXT_TYPES_PKG, expectedVersion, File(parent.path), listener, extraOptions)
          .run(indicator)

        VfsUtil.markDirtyAndRefresh(true, false, false, packageJson)
      }
    })
  }

  private fun getMatchingTypesVersion(nuxtVersion: SemVer): String? {
    return "~" + if (nuxtVersion.isGreaterOrEqualThan(NUXT_2_13_0)) {
      "${nuxtVersion.major}.${nuxtVersion.minor}"
    }
    else {
      when (nuxtVersion.minor) {
        9 -> "0.2"
        10 -> "0.5"
        11 -> "0.6"
        12 -> "0.7"
        else -> return null
      }
    } + ".0"
  }

}