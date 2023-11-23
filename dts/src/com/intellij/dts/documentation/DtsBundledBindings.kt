package com.intellij.dts.documentation

import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.getDtsPath
import com.intellij.dts.zephyr.binding.DtsZephyrBinding
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.openapi.util.NlsSafe

enum class DtsBundledBindings(val nodeName: @NlsSafe String) {
  ALIASES("aliases"),
  CHOSEN("chosen"),
  CPUS("cpus"),
  MEMORY("memory"),
  RESERVED_MEMORY("reserved-memory");

  fun build(provider: DtsZephyrBindingProvider): DtsZephyrBinding? {
    return provider.buildBundledBinding(this)
  }

  companion object {
    fun findBindingForNode(node: DtsNode): DtsBundledBindings? {
      val name = node.getDtsPath()?.nameWithoutUnit() ?: return null
      return entries.firstOrNull { it.nodeName == name }
    }
  }
}