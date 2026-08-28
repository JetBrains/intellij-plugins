// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.starter

import com.intellij.openapi.application.ModernApplicationStarter
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.diagnostic.logger
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionApplicationFactory
import org.jetbrains.qodana.staticAnalysis.inspections.runner.runReportingTerminalFailure
import org.jetbrains.qodana.util.QodanaMessageReporter
import kotlin.system.exitProcess

internal class QodanaApplicationStarter : ModernApplicationStarter() {
  override fun premain(args: List<String>) {
    logger<QodanaApplicationStarter>().info("Command line arguments: $args")
  }

  /**
   * Nothing above this frame ends the process — the platform returns from the starter and leaves the application
   * container running — so both ways of leaving have to be here. A completed run gets the orderly shutdown, which
   * defers to the UI thread to run arbitrary shutdown activities, saves settings, disposes projects and reports the
   * closing statistics. A failed one must not: that path can
   * decline to exit at all (`ApplicationImpl.destructApplication` returns without calling `System.exit` when project
   * disposal fails), which on an exhausted heap is how a failed linter hangs instead of reporting.
   */
  override suspend fun start(args: List<String>) {
    val exitCode = runReportingTerminalFailure(QodanaMessageReporter.DEFAULT) {
      QodanaInspectionApplicationFactory().getApplication(args.subList(1, args.size)).startup()
    }
    if (exitCode == 0) ApplicationManagerEx.getApplicationEx().exit(true, true)
    else exitProcess(exitCode)
  }
}
