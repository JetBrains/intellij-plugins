// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object QodanaProjectInfoCollector : CounterUsagesCollector() {

  override fun getGroup() = GROUP

  private val GROUP = EventLogGroup("qodana.project.info", 2)

  private val authors30Field = EventFields.RoundedInt("authors30")
  private val authors60Field = EventFields.RoundedInt("authors60")
  private val authors90Field = EventFields.RoundedInt("authors90")
  private val commits30Field = EventFields.RoundedInt("commits30")
  private val commits60Field = EventFields.RoundedInt("commits60")
  private val commits90Field = EventFields.RoundedInt("commits90")

  private val commitsSummaryEvent = GROUP.registerVarargEvent(
    "vcs.commits.summary",
    authors30Field,
    authors60Field,
    authors90Field,
    commits30Field,
    commits60Field,
    commits90Field
  )

  private val ossLicenseField = EventFields.Boolean("oss_license")
  private val doesHaveOssLicenseEvent = GROUP.registerEvent("does.have.oss.license", ossLicenseField)

  @JvmStatic
  fun logCommitsSummary(
    project: Project,
    authors30: Int,
    authors60: Int,
    authors90: Int,
    commits30: Int,
    commits60: Int,
    commits90: Int
  ) {
    val pairs = listOf(
      authors30Field.with(authors30),
      authors60Field.with(authors60),
      authors90Field.with(authors90),
      commits30Field.with(commits30),
      commits60Field.with(commits60),
      commits90Field.with(commits90)
    )

    commitsSummaryEvent.log(project, pairs)
  }

  @JvmStatic
  fun logAbsentHistorySummary(project: Project) {
    commitsSummaryEvent.log(project)
  }

  @JvmStatic
  fun logOssLicense(project: Project, hasOssLicense: Boolean) {
    doesHaveOssLicenseEvent.log(project, hasOssLicense)
  }
}
