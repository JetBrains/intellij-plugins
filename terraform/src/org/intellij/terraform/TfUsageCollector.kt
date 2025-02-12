// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.internal.statistic.service.fus.collectors.ProjectUsagesCollector
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.CommonProcessors

internal class TfUsageCollector : ProjectUsagesCollector() {

  private val GROUP = EventLogGroup(
    id = "terraform.project.metrics",
    version = 1,
  )

  private val TERRAGRUNT = GROUP.registerEvent(
    "terragrunt.found", EventFields.Boolean("exists"),
    "if \"terragrunt.hcl\" file exists in the project among other terraform files")

  override fun getGroup(): EventLogGroup = GROUP

  override fun requiresReadAccess(): Boolean = true

  override fun requiresSmartMode(): Boolean = true

  override fun getMetrics(project: Project): Set<MetricEvent> {
    val result = mutableSetOf<MetricEvent>()

    if (hasHCLLanguageFiles(project, FileTypeManager.getInstance().registeredFileTypes.asList())) {
      val terragruntSearch = CommonProcessors.FindFirstProcessor<VirtualFile>()
      FilenameIndex.processFilesByName("terragrunt.hcl", false, GlobalSearchScope.allScope(project), terragruntSearch)
      result.add(TERRAGRUNT.metric(terragruntSearch.isFound))
    }

    return result
  }
}

internal object TfUsageTriggerCollector : CounterUsagesCollector() {

  override fun getGroup(): EventLogGroup = GROUP

  private val GROUP = EventLogGroup(
    id = "terraform.usages",
    version = 1,
  )

  val ODD_FEATURE_USED = GROUP.registerEvent(
    "odd.feature.used", EventFields.String("feature", listOf("ignored-references")),
    "if some legacy feature was used in the project")
}

