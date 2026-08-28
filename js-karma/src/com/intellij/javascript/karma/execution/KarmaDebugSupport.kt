package com.intellij.javascript.karma.execution

import com.intellij.openapi.extensions.ExtensionPointName

interface KarmaDebugSupport {
  fun dropLocationCache(runConfiguration: KarmaRunConfiguration)

  companion object {
    private val EP = ExtensionPointName.create<KarmaDebugSupport>("com.intellij.javascript.karma.debugSupport")

    @JvmStatic
    fun dropLocationCacheIfAvailable(runConfiguration: KarmaRunConfiguration) {
      EP.extensionList.forEach { it.dropLocationCache(runConfiguration) }
    }
  }
}
