package com.intellij.dts.zephyr.binding

import com.intellij.dts.api.dtsSearch
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.getDtsCompatibleStrings
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.zephyr.DtsZephyrProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class DtsZephyrBindingProvider(val project: Project) {
  companion object {
    fun of(project: Project): DtsZephyrBindingProvider = project.service()

    fun bindingFor(node: DtsNode, fallbackBinding: Boolean = true): DtsZephyrBinding? {
      val provider = of(node.project)

      val nodeBinding = node.dtsSearch(forward = false, callback = provider::getBinding)
      if (nodeBinding != null || !fallbackBinding) return nodeBinding

      return DtsZephyrBundledBindings.getInstance().getBinding(DtsZephyrBundledBindings.FALLBACK_BINDING)
    }

    fun bindingFor(property: DtsProperty): DtsZephyrPropertyBinding? {
      val parent = DtsTreeUtil.parentNode(property) ?: return null
      return bindingFor(parent)?.properties?.get(property.dtsName)
    }
  }

  private val provider: DtsZephyrProvider get() = DtsZephyrProvider.of(project)


  fun getBindings(compatible: String): Collection<DtsZephyrBinding> = provider.bindings[compatible]

  /**
   * Gets a binding for a specific node from a set of compatible strings.
   *
   * Searches the compatible strings and returns the first binding that was
   * found.

   * If there is a bundled binding for the node and no compatible binding, the
   * bundled binding will be used. A list of bundled bindings can be found
   * here: [DtsZephyrBundledBindings.NODE_BINDINGS]
   *
   * Resolves references automatically.
   */
  fun getBinding(node: DtsNode, compatible: List<String>): DtsZephyrBinding? = searchBinding(provider.bindings, node, compatible)

  /**
   * Gets a binding for a specific node. Class [getBinding] with the compatible
   * strings found in the compatible property of the node.
   */
  fun getBinding(node: DtsNode): DtsZephyrBinding? = getBinding(node, node.getDtsCompatibleStrings())

  /**
   * List of all available bindings. Does not include bundled bindings since
   * they are matched on the node name and not the compatible string.
   */
  fun getAllBindings(): Collection<DtsZephyrBinding> = provider.bindings.values()
}