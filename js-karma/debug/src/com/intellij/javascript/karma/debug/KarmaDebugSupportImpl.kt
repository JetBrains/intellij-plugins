package com.intellij.javascript.karma.debug

import com.intellij.javascript.debugger.locationResolving.JSLocationResolver
import com.intellij.javascript.karma.execution.KarmaDebugSupport
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.openapi.application.ApplicationManager

internal class KarmaDebugSupportImpl : KarmaDebugSupport {
  override fun dropLocationCache(runConfiguration: KarmaRunConfiguration) {
    ApplicationManager.getApplication().getService(JSLocationResolver::class.java)?.dropCache(runConfiguration)
  }
}
