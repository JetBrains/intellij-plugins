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
import com.intellij.openapi.vfs.*
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.YAMLException
import java.io.IOException

@Service(Service.Level.PROJECT)
class DtsZephyrBindingProvider(val project: Project) {
  companion object {
    fun of(project: Project): DtsZephyrBindingProvider = project.service()

    fun bindingFor(node: DtsNode, fallbackBinding: Boolean = true): DtsZephyrBinding? {
      val provider = of(node.project)

      val nodeBinding = node.dtsSearch(forward = false, callback = provider::buildBinding)
      if (nodeBinding != null || !fallbackBinding) return nodeBinding

      return provider.buildFallbackBinding()
    }

    fun bindingFor(project: Project, compatible: String): DtsZephyrBinding? {
      return of(project).buildBinding(compatible)
    }

    fun bindingFor(property: DtsProperty): DtsZephyrPropertyBinding? {
      val parent = DtsTreeUtil.parentNode(property) ?: return null
      return bindingFor(parent)?.properties?.get(property.dtsName)
    }

    private const val BUNDLED_BINDINGS_PATH = "bindings"
    private const val DEFAULT_BINDING_NAME = "default"
    private const val FALLBACK_BINDING_NAME = "fallback"

    private val logger = Logger.getInstance(DtsZephyrBindingProvider::class.java)
  }

  private val yaml = Yaml(SafeConstructor(LoaderOptions()))

  private val provider: DtsZephyrProvider by lazy { DtsZephyrProvider.of(project) }

  /**
   * Parser for bindings. Recreated if the zephyr provider is modified.
   */
  private val bindingParser: DtsZephyrBindingParser by cached(provider::modificationCount) {
    val sources = provider.getBindingsDir()?.let(::loadBindings) ?: emptyMap()
    DtsZephyrBindingParser(sources, bundledBindingSources[DEFAULT_BINDING_NAME])
  }

  /**
   * Parser for bundled bindings.
   */
  private val bundledBindingParser: DtsZephyrBindingParser by lazy {
    DtsZephyrBindingParser(bundledBindingSources, null)
  }

  /**
   * Lazily loads all bundled yaml files. Located in: resources/bindings
   */
  private val bundledBindingSources: Map<String, DtsZephyrBindingParser.Source> by lazy {
    val folderUrl = javaClass.classLoader.getResource(BUNDLED_BINDINGS_PATH)
    if (folderUrl == null) {
      logger.error("failed to load bundled bindings folder url")
      return@lazy emptyMap()
    }

    val folder = VfsUtil.findFileByURL(folderUrl)
    if (folder == null) {
      logger.error("failed to load bundled bindings folder")
      return@lazy emptyMap()
    }

    try {
      buildMap {
        for (file in folder.children) {
          val name = file.nameWithoutExtension

          val binding = loadBinding(file.readText(), name)

          if (binding != null) {
            put(name, DtsZephyrBindingParser.Source(null, binding))
          }
          else {
            logger.error("failed to load bundled binding: $name")
          }
        }
      }
    }
    catch (e: IOException) {
      logger.error("failed to load bundled bindings: $e")
      emptyMap()
    }
  }

  private fun loadBinding(binding: String, name: String): Map<*, *>? {
    try {
      return synchronized(yaml) {
        yaml.load(binding)
      }
    }
    catch (e: ClassCastException) {
      logger.debug("unexpected yaml format: $name")
    }
    catch (e: YAMLException) {
      logger.debug("not a valid yaml file: $name")
    }
    catch (e: Exception) {
      logger.debug("unknown exception from yaml parser", e)
    }

    return null
  }

  private fun loadBindings(dir: VirtualFile): Map<String, DtsZephyrBindingParser.Source> {
    val bindings = mutableMapOf<String, DtsZephyrBindingParser.Source>()

    val visitor = object : VirtualFileVisitor<Any>() {
      override fun visitFile(file: VirtualFile): Boolean {
        if (file.isDirectory || file.extension != "yaml") return true

        loadBinding(file.readText(), file.name)?.let {
          bindings[file.nameWithoutExtension] = DtsZephyrBindingParser.Source(file.path, it)
        }

        return true
      }
    }
    VfsUtilCore.visitChildrenRecursively(dir, visitor)

    return bindings
  }

  /**
   * Builds a binding for a specific compatible string.
   */
  fun buildBinding(compatible: String): DtsZephyrBinding? = bindingParser.parse(compatible)

  /**
   * Builds a binding for a specific node.
   *
   * If the node has a compatible property the binding will be built for the
   * matching string. If there is no matching binding this method searches
   * the parents of the node for matching child bindings.
   *
   * If there is a bundled binding for the node and no compatible binding, the
   * bundled binding will be used. A list of bundled bindings can be found
   * here: [DtsBundledBindings]
   *
   * Resolves references automatically.
   */
  fun buildBinding(node: DtsNode): DtsZephyrBinding? {
    val compatibleStrings = node.getDtsCompatibleStrings()
    if (compatibleStrings.isNotEmpty()) {
      return compatibleStrings.firstNotNullOfOrNull(::buildBinding)
    }

    val bundledBinding = DtsBundledBindings.findBindingForNode(node)
    if (bundledBinding != null) return buildBundledBinding(bundledBinding)

    val parentBinding = DtsTreeUtil.findParentNode(node)?.let(::buildBinding)
    return parentBinding?.child
  }

  private fun buildBundledBinding(name: String): DtsZephyrBinding? = bundledBindingParser.parse(name)

  /**
   * Builds a bundled binding.
   */
  fun buildBundledBinding(binding: DtsBundledBindings): DtsZephyrBinding? = buildBundledBinding(binding.nodeName)

  /**
   * Builds the bundled fallback binding which contains the standard
   * documentation from the specification for known properties.
   */
  fun buildFallbackBinding(): DtsZephyrBinding? = buildBundledBinding(FALLBACK_BINDING_NAME)

  /**
   * List of all available bindings. Does not include bundled bindings since
   * they are matched on the node name and not the compatible string.
   */
  fun buildAllBindings(): Collection<DtsZephyrBinding> = bindingParser.parseAll()
}