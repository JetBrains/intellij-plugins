// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.internal.statistic.updater.StatisticsValidationUpdatedService
import com.intellij.internal.statistic.updater.updateValidationRules
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.serviceAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/**
 * Runs the update of the statistics schema in the scope of the application.
 *
 * The update can block on a network call that does not respond to cancellation.
 * The caller waits for the result with a timeout, and the update itself can continue after it.
 */
@Service(Service.Level.APP)
internal class QodanaStatisticsSchemaUpdater(private val scope: CoroutineScope) {
  fun startUpdate(): Deferred<Unit> = scope.async(Dispatchers.IO) {
    // Wait for the scheduled update to end, so that the two updates do not run at the same time.
    serviceAsync<StatisticsValidationUpdatedService>().updatedDeferred.join()
    updateValidationRules()
  }
}
