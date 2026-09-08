// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.util.application
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

internal fun optimizedGraphModificationTracker(
  project: Project,
): ModificationTracker =
  OptimizedGraphModificationTracker(
    project.service<TypeScriptConfigService>().graphModificationTracker,
  )

/**
 * Adapter to current JSImportGraph calculation
 * On BGT we can delegate all responsibility to other trackers (PSI modification, NodeModules modification)
 * On EDT (and 1 time after it) we should use the original tracker
 */
private class OptimizedGraphModificationTracker(
  private val originalTracker: ModificationTracker,
) : ModificationTracker {
  private val lastCallWasEdt = AtomicBoolean(true)
  private val cachedModificationCount = AtomicLong()

  override fun getModificationCount(): Long {
    val isEdt = application.isDispatchThread
    val wasEdt = lastCallWasEdt.getAndSet(isEdt)
    if (isEdt || wasEdt) {
      cachedModificationCount.set(originalTracker.modificationCount)
    }
    return cachedModificationCount.get()
  }
}
