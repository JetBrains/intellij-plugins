package com.intellij.dts.zephyr.binding

import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.getDtsCompatibleStrings
import com.intellij.dts.lang.psi.getDtsPath
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.util.containers.MultiMap

private fun getBindingWithParent(
  bindings: MultiMap<String, DtsZephyrBinding>,
  compatibleStrings: List<String>,
  parentBinding: DtsZephyrBinding,
): DtsZephyrBinding? {
  if (compatibleStrings.isEmpty()) {
    return parentBinding.child
  }

  for (compatible in compatibleStrings) {
    val found = bindings[compatible]

    // first look for matching bus
    val forBus = found.filter { parentBinding.buses.contains(it.onBus) }
    if (forBus.isNotEmpty()) return forBus.first()

    // fallback to binding without bus
    val noBus = found.filter { it.onBus == null }
    if (noBus.isNotEmpty()) return noBus.first()
  }

  return null
}

private fun getBindingFallback(
  bindings: MultiMap<String, DtsZephyrBinding>,
  compatibleStrings: List<String>,
): DtsZephyrBinding? {
  for (compatible in compatibleStrings) {
    val found = bindings[compatible]
    if (found.isEmpty()) continue

    // first look for binding without bus
    val noBus = found.filter { it.onBus == null }
    if (noBus.isNotEmpty()) return noBus.first()

    // fallback to any binding
    return found.first()
  }

  return null
}

private fun searchBundledBinding(node: DtsNode): DtsZephyrBinding? {
  val name = node.getDtsPath()?.nameWithoutUnit() ?: return null
  return DtsZephyrBundledBindings.getInstance().getBinding(name)
}

fun searchBinding(bindings: MultiMap<String, DtsZephyrBinding>, node: DtsNode, compatible: List<String>): DtsZephyrBinding? {
  val parentBinding = DtsTreeUtil.findParentNode(node)?.let { parent ->
    searchBinding(bindings, parent, parent.getDtsCompatibleStrings())
  }

  val binding = if (parentBinding == null) {
    getBindingFallback(bindings, compatible)
  }
  else {
    getBindingWithParent(bindings, compatible, parentBinding)
  }

  return binding ?: searchBundledBinding(node)
}
