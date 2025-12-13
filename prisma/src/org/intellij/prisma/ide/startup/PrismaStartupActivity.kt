// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.intellij.prisma.ide.config.PrismaConfigManager

class PrismaStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    PrismaConfigManager.getInstanceAsync(project) // init the listener to load config on file opening
  }
}
