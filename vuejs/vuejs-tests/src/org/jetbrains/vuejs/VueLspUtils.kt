// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.lang.typescript.lsp.TypeScriptGoLspClientDescriptor
import com.intellij.lang.typescript.lsp.TypeScriptGoLspIntegrationProvider
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspClientDescriptor
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.impl.LspClientImpl
import com.intellij.platform.lsp.impl.LspClientManagerImpl
import com.intellij.testFramework.PlatformTestUtil.dispatchAllEventsInIdeEventQueue

internal object VueLspUtils {

  fun waitTypeScriptGoLspServerInit(
    project: Project,
  ) {
    triggerLspServerInit(
      project = project,
      providerClass = TypeScriptGoLspIntegrationProvider::class.java,
      descriptor = TypeScriptGoLspClientDescriptor(project),
    )
  }

  /**
   * TODO: move to common
   * original - [org.angular2.Angular2TestCase.triggerLspServerInit]
   */
  private fun triggerLspServerInit(
    project: Project,
    providerClass: Class<out LspIntegrationProvider>,
    descriptor: LspClientDescriptor,
  ) {
    val getServer = {
      LspClientManager.getInstance(project)
        .getClients(providerClass)
        .firstOrNull()
        .let { it as? LspClientImpl }
    }

    val state = getServer()?.state
    if (state == null) {
      LspClientManagerImpl.getInstanceImpl(project)
        .ensureClientStarted(providerClass, descriptor)
    }
    else {
      return
    }
    val isEDT = ApplicationManager.getApplication().isDispatchThread
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < 4000) {
      val state = getServer()?.state
      if (state != null)
        return
      if (isEDT) {
        dispatchAllEventsInIdeEventQueue()
      }
      Thread.sleep(10)
    }

    throw IllegalStateException("Server didn't initialize in 4000 ms")
  }
}
