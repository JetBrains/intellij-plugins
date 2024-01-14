// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:JvmName("RevealUsageTriggerCollector")

package org.jetbrains.appcode.reveal

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector

internal object RevealUsageTriggerCollector : CounterUsagesCollector() {
  override fun getGroup(): EventLogGroup = GROUP

  private val GROUP = EventLogGroup("appcode.reveal", 2)

  @JvmField
  val INSTALL_ON_DEVICE = GROUP.registerEvent("installOnDevice")
  @JvmField
  val INJECT = GROUP.registerEvent("inject")
  @JvmField
  val SHOW_IN_REVEAL = GROUP.registerEvent("showInReveal")
}