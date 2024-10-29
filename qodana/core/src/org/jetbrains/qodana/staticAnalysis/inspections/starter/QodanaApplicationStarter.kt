// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.staticAnalysis.inspections.starter

import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.openapi.application.ModernApplicationStarter
import com.intellij.openapi.diagnostic.logger
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionApplication
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionApplicationFactory
import kotlin.system.exitProcess

private class QodanaApplicationStarter : ModernApplicationStarter() {
  override fun premain(args: List<String>) {
    logger<QodanaApplicationStarter>().info("Command line arguments: $args")
  }

  override suspend fun start(args: List<String>) {
    buildQodanaApplication(args).startup()
  }

  private suspend fun buildQodanaApplication(args: List<String>): QodanaInspectionApplication {
    try {
      return QodanaInspectionApplicationFactory().getApplication(args.subList(1, args.size))
    }
    catch (e: InspectionApplicationException) {
      System.err.println(e.message)
      exitProcess(1)
    }
    catch (e: Exception) {
      e.printStackTrace() // workaround for IDEA-289086
      exitProcess(1)
    }
  }
}
