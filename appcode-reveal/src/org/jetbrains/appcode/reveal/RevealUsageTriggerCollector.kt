// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:JvmName("RevealUsageTriggerCollector")

package org.jetbrains.appcode.reveal

import com.intellij.internal.statistic.service.fus.collectors.FUCounterUsageLogger
import com.intellij.openapi.project.Project

fun trigger(project: Project, feature: String) {
  FUCounterUsageLogger.getInstance().logEvent(project, "appcode.reveal", feature)
}