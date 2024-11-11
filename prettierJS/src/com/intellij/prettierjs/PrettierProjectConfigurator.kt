// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectConfigurator

class PrettierProjectConfigurator : DirectoryProjectConfigurator.AsyncDirectoryProjectConfigurator() {
  override suspend fun configure(project: Project, baseDir: VirtualFile, moduleRef: Ref<Module>, isProjectCreatedWithWizard: Boolean) {
    project.service<PrettierConfiguratorService>().configure(baseDir)
  }
}

