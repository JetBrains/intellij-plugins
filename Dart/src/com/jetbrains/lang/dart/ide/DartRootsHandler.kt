// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide

import com.google.gson.JsonObject
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.util.SmartList
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.URLUtil
import com.jetbrains.lang.dart.ide.toolingDaemon.DartToolingDaemonService
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil
import java.util.concurrent.Callable

@Service(Service.Level.PROJECT)
class DartRootsHandler private constructor(private val project: Project) : Disposable {
  private var myIncludedRoots: List<String> = SmartList()

  fun updateRoots() {
    ReadAction.nonBlocking(
      Callable<List<String>?> { calcIncludedDartRoots() })
      .coalesceBy(this)
      .finishOnUiThread(ModalityState.nonModal()
      ) { includedRoots: List<String>? ->
        if (includedRoots != null) {
          val params = JsonObject()
          params.addProperty("roots", includedRoots.toString());
          val dtdService = DartToolingDaemonService.getInstance(project)
          dtdService.ready.thenRun {
            dtdService.sendRequest("FileSystem.setIDEWorkspaceRoots", params, true) { response ->
              println("received response")
              println(response)
            }
          }
        }
      }
      .submit(AppExecutorUtil.getAppExecutorService())
  }

  private fun calcIncludedDartRoots(): List<String>? {
    val sdk = DartSdk.getDartSdk(project) ?: return null

    val newIncludedRoots: MutableList<String> = SmartList()

    for (module in DartSdkLibUtil.getModulesWithDartSdkEnabled(project)) {
      for (contentEntry in ModuleRootManager.getInstance(module).contentEntries) {
        val contentEntryUrl = contentEntry.url
        if (contentEntryUrl.startsWith(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR)) {
          newIncludedRoots.add(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(contentEntryUrl)))
        }
      }
    }

    if (myIncludedRoots == newIncludedRoots) {
      return null
    }

    myIncludedRoots = newIncludedRoots
    return newIncludedRoots
  }

  override fun dispose() {
    TODO("Not yet implemented")
  }
}