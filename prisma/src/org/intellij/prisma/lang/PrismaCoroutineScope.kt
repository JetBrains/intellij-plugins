// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class PrismaCoroutineScope(val coroutineScope: CoroutineScope) {
  companion object {
    fun get(project: Project): CoroutineScope = project.service<PrismaCoroutineScope>().coroutineScope

    suspend fun getAsync(project: Project): CoroutineScope = project.serviceAsync<PrismaCoroutineScope>().coroutineScope
  }
}
