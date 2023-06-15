// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.javascript.nodejs.library.ScanningFileListener
import com.intellij.javascript.nodejs.library.ScanningFileListenerContributor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.libraries.nuxt.NUXT_OUTPUT_FOLDER

class NuxtFolderScanningListenerContributor: ScanningFileListenerContributor {
  override fun register(registrar: ScanningFileListenerContributor.Registrar) {
    registrar.registerFileListener(NUXT_OUTPUT_FOLDER, true, object : ScanningFileListener {
      override fun fileFound(project: Project, file: VirtualFile) {
        NuxtFolderManager.getInstance(project).addIfMissing(file)
      }
    })
  }
}