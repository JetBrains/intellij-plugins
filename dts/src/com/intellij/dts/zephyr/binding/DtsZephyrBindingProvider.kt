package com.intellij.dts.zephyr.binding

import com.intellij.dts.api.dtsSearch
import com.intellij.dts.documentation.DtsBundledBindings
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.getDtsCompatibleStrings
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.util.cached
import com.intellij.dts.zephyr.DtsZephyrProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.containers.MultiMap

@Service(Service.Level.PROJECT)
class DtsZephyrBindingProvider(val project: Project) {
  companion object {
    fun of(project: Project): DtsZephyrBindingProvider = project.service()

    fun bindingFor(node: DtsNode, fallbackBinding: Boolean = true): DtsZephyrBinding? {
      val provider = of(node.project)

      val nodeBinding = node.dtsSearch(forward = false, callback = provider::getBinding)
      if (nodeBinding != null || !fallbackBinding) return nodeBinding

      return provider.getFallbackBinding()
    }

    fun bindingFor(property: DtsProperty): DtsZephyrPropertyBinding? {
      val parent = DtsTreeUtil.parentNode(property) ?: return null
      return bindingFor(parent)?.properties?.get(property.dtsName)
    }

    private const val BUNDLED_BINDINGS_PATH = "bindings"
    private const val DEFAULT_BINDING_NAME = "default"
    private const val FALLBACK_BINDING_NAME = "fallback"

    val logger = Logger.getInstance(DtsZephyrBindingProvider::class.java)
  }

  private val provider: DtsZephyrProvider by lazy { DtsZephyrProvider.of(project) }

  /**
   * Parser for bindings. Recreated if the zephyr provider is modified.
   */
  private val externalBindings: MultiMap<String, DtsZephyrBinding> by cached(provider::modificationCount) {
    val dir = provider.getBindingsDir() ?: return@cached MultiMap.empty()

    val files = loadExternalBindings(dir)
    val default = bundledBindingsSource?.files?.get(DEFAULT_BINDING_NAME)

    parseExternalBindings(BindingSource(files, default))
  }

  /**
   * Parser for bundled bindings.
   */
  private val bundledBindings: Map<String, DtsZephyrBinding> by lazy {
    val source = bundledBindingsSource ?: return@lazy emptyMap()

    parseBundledBindings(source)
  }

  /**
   * Lazily loads all bundled yaml files. Located in: resources/bindings
   */
  private val bundledBindingsSource: BindingSource? by lazy {
    val url = javaClass.classLoader.getResource(BUNDLED_BINDINGS_PATH)
    if (url == null) {
      logger.error("failed to load bundled bindings folder url")
      return@lazy null
    }

    val dir = VfsUtil.findFileByURL(url)
    if (dir == null) {
      logger.error("failed to load bundled bindings folder")
      return@lazy null
    }

    BindingSource(loadBundledBindings(dir), null)
  }


  fun getBindings(compatible: String): Collection<DtsZephyrBinding> = externalBindings.get(compatible)

  private fun getBindingWithParent(compatibleStrings: List<String>, parentBinding: DtsZephyrBinding): DtsZephyrBinding? {
    if (compatibleStrings.isEmpty()) {
      return parentBinding.child
    }

    for (compatible in compatibleStrings) {
      val bindings = externalBindings.get(compatible)

      // first look for matching bus
      val forBus = bindings.filter { parentBinding.buses.contains(it.onBus) }
      if (forBus.isNotEmpty()) return forBus.first()

      // fallback to binding without bus
      val noBus = bindings.filter { it.onBus == null }
      if (noBus.isNotEmpty()) return noBus.first()
    }

    return null
  }

  private fun getBindingFallback(compatibleStrings: List<String>): DtsZephyrBinding? {
    for (compatible in compatibleStrings) {
      val bindings = externalBindings.get(compatible)
      if (bindings.isEmpty()) continue

      // first look for binding without bus
      val noBus = bindings.filter { it.onBus == null }
      if (noBus.isNotEmpty()) return noBus.first()

      // fallback to any binding
      return bindings.first()
    }

    return null
  }

  /**
   * Gets a binding for a specific node from a set of compatible strings.
   *
   * Searches the compatible strings and returns the first binding that was
   * found.
   *
   * If there is a bundled binding for the node and no compatible binding, the
   * bundled binding will be used. A list of bundled bindings can be found
   * here: [DtsBundledBindings]
   *
   * Resolves references automatically.
   */
  fun getBinding(node: DtsNode, compatible: List<String>): DtsZephyrBinding? {
    val parentBinding = DtsTreeUtil.findParentNode(node)?.let(::getBinding)

    val binding = if (parentBinding == null) {
      getBindingFallback(compatible)
    }
    else {
      getBindingWithParent(compatible, parentBinding)
    }
    if (binding != null) return binding

    val bundledBinding = DtsBundledBindings.findBindingForNode(node)
    return bundledBinding?.let(::getBundledBinding)
  }

  /**
   * Gets a binding for a specific node. Class [getBinding] with the compatible
   * strings found in the compatible property of the node.
   */
  fun getBinding(node: DtsNode): DtsZephyrBinding? = getBinding(node, node.getDtsCompatibleStrings())

  private fun getBundledBinding(name: String): DtsZephyrBinding? = bundledBindings[name]

  /**
   * Builds a bundled binding.
   */
  fun getBundledBinding(binding: DtsBundledBindings): DtsZephyrBinding? = getBundledBinding(binding.nodeName)

  /**
   * Builds the bundled fallback binding which contains the standard
   * documentation from the specification for known properties.
   */
  fun getFallbackBinding(): DtsZephyrBinding? = getBundledBinding(FALLBACK_BINDING_NAME)

  /**
   * List of all available bindings. Does not include bundled bindings since
   * they are matched on the node name and not the compatible string.
   */
  fun getAllBindings(): Collection<DtsZephyrBinding> = externalBindings.values()
}