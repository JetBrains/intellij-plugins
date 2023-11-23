package com.intellij.dts.zephyr

import com.intellij.dts.documentation.DtsBundledBindings
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsRefNode
import com.intellij.dts.lang.psi.DtsString
import com.intellij.dts.lang.psi.getDtsReferenceTarget
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.util.cached
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
import java.util.*

@JvmInline
private value class YamlMap(private val map: Map<*, *>) {
    fun readMap(key: String): YamlMap? {
        val result = map[key] as? Map<*, *> ?: return null
        return YamlMap(result)
    }

    fun readMapList(key: String): List<YamlMap>? {
        val result = map[key] as? List<*> ?: return null
        return result.filterIsInstance<Map<*, *>>().map { YamlMap(it) }
    }

    fun iterateMap(): Iterable<Map.Entry<String, YamlMap>> {
        return map.entries.mapNotNull { (key, value) ->
            if (key is String && value is Map<*, *>) {
                object : Map.Entry<String, YamlMap> {
                    override val key: String = key
                    override val value: YamlMap = YamlMap(value)
                }
            } else null
        }
    }

    fun readString(key: String): String? {
        return map[key] as? String
    }

    fun readStringList(key: String): List<String>? {
        val result = map[key] as? List<*> ?: return null
        return result.filterIsInstance<String>()
    }
}

private val emptyYaml = YamlMap(emptyMap<Any, Any>())

private data class BindingSource(val path: String, val data: YamlMap)

@Service(Service.Level.PROJECT)
class DtsZephyrBindingProvider(val project: Project) {
    companion object {
        fun of(project: Project): DtsZephyrBindingProvider = project.service()

        fun bindingFor(node: DtsNode, fallbackToDefault: Boolean = true): DtsZephyrBinding? {
            val provider = of(node.project)

            val nodeBinding = DtsTreeUtil.search(node.containingFile, node, provider::buildBinding)
            if (nodeBinding != null || !fallbackToDefault) return nodeBinding

            return provider.buildDefaultBinding()
        }

        fun bindingFor(project: Project, compatible: String): DtsZephyrBinding? {
            return of(project).buildBinding(compatible)
        }

        private const val BUNDLED_BINDINGS_PATH = "bindings"
        private const val DEFAULT_BINDING_NAME = "default"

        private val logger = Logger.getInstance(DtsZephyrBindingProvider::class.java)
    }

    private val yaml = Yaml(SafeConstructor(LoaderOptions()))

    private val provider: DtsZephyrProvider by lazy { DtsZephyrProvider.of(project) }

    /**
     * Cache for bindings, invalidated if the zephyr provider is modified. Backed
     * by [bindingsYaml].
     */
    private val bindings: MutableMap<String, DtsZephyrBinding> by cached(provider::modificationCount) {
        mutableMapOf()
    }

    /**
     * Cache for binding yaml files, invalidated if the zephyr provider is modified.
     */
    private val bindingsYaml: Map<String, BindingSource> by cached(provider::modificationCount) {
        provider.getBindingsDir()?.let(::loadBindings) ?: emptyMap()
    }

    /**
     * Cache for bundled bindings. Backed by [bundledBindingsYaml].
     */
    private val bundledBindings: MutableMap<String, DtsZephyrBinding> = mutableMapOf()

    /**
     * Lazily loads all bundled yaml files. Located in: resources/bindings
     */
    private val bundledBindingsYaml: Map<String, YamlMap> by lazy {
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
                    put(name, binding ?: emptyYaml)

                    if (binding == null) {
                        logger.error("failed to load bundled binding: $name")
                    }
                }
            }
        } catch (e: IOException) {
            logger.error("failed to load bundled bindings: $e")
            emptyMap()
        }
    }

    private fun loadBinding(binding: String, name: String): YamlMap? {
        try {
            return synchronized(yaml) {
                val map = yaml.load<Map<*, *>>(binding)
                YamlMap(map)
            }
        } catch (e: ClassCastException) {
            logger.debug("unexpected yaml format: $name")
        } catch (e: YAMLException) {
            logger.debug("not a valid yaml file: $name")
        }

        return null
    }

    private fun loadBindings(dir: VirtualFile): Map<String, BindingSource> {
        val bindings = mutableMapOf<String, BindingSource>()

        val visitor = object : VirtualFileVisitor<Any>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.isDirectory || file.extension != "yaml") return true

                loadBinding(file.readText(), file.name)?.let {
                    bindings[file.nameWithoutExtension] = BindingSource(file.path, it)
                }

                return true
            }
        }
        VfsUtilCore.visitChildrenRecursively(dir, visitor)

        return bindings
    }

    private fun getBinding(name: String): BindingSource? {
        val key = name.removeSuffix(".yaml")
        return bindingsYaml[key]
    }

    /**
     * Returns a list of includes based on the provided binding. Includes in a
     * binding can be defined in one of three ways:
     *
     * 1. String literal
     * include: "file.yaml"
     *
     * 2. A string list
     * include: ["file1.yaml", "file2.yaml"]
     *
     * 3. A mapping (allowlist is ignored)
     * include:
     *   - name: file1.yaml
     *     property-allowlist:
     *       - prop1
     *       - prop2
     *   - name: file2.yaml
     */
    private fun getIncludes(binding: YamlMap): List<String> {
        // first case
        binding.readString("include")?.let { return listOf(it) }

        // second case
        binding.readStringList("include")?.takeIf { it.isNotEmpty() }?.let { return it }

        // third case
        binding.readMapList("include")?.let { return it.mapNotNull { map -> map.readString("name") } }

        return emptyList()
    }

    /**
     * Invokes the callback for the binding itself and all included bindings.
     * Additionally, the callback is also invoked for the bundled default
     * binding.
     */
    private fun iterateBindings(compatible: List<String>, callback: (YamlMap) -> Unit) {
        bundledBindingsYaml[DEFAULT_BINDING_NAME]?.let(callback)

        val frontier = Stack<String>()
        compatible.reversed().forEach(frontier::push)

        while (!frontier.empty()) {
            val binding = getBinding(frontier.pop())?.data ?: continue
            callback(binding)

            getIncludes(binding).forEach(frontier::push)
        }
    }

    private fun doBuildPropertyBinding(builder: DtsZephyrPropertyBinding.Builder, yaml: YamlMap) {
        yaml.readString("description")?.let(builder::setDescription)
        yaml.readString("type")?.let(builder::setType)
    }

    private fun doBuildBinding(builder: DtsZephyrBinding.Builder, yaml: YamlMap, resolveChildIncludes: Boolean) {
        yaml.readString("description")?.let(builder::setDescription)

        yaml.readMap("properties")?.let { properties ->
            for ((name, property) in properties.iterateMap()) {
                doBuildPropertyBinding(builder.getPropertyBuilder(name), property)
            }
        }

        yaml.readMap("child-binding")?.let { binding ->
            doBuildBinding(builder.getChildBuilder(), binding, resolveChildIncludes)

            if (resolveChildIncludes) {
                iterateBindings(getIncludes(binding)) {
                    doBuildBinding(builder.getChildBuilder(), it, resolveChildIncludes = true)
                }
            }
        }
    }

    private fun getCompatibleStrings(node: DtsNode): List<String> {
        val property = node.dtsProperties.firstOrNull { it.dtsName == "compatible" } ?: return emptyList()
        return property.dtsValues.filterIsInstance<DtsString>().map { it.dtsParse() }
    }

    /**
     * Builds a binding for a specific compatible string.
     */
    fun buildBinding(compatible: String): DtsZephyrBinding? {
        val source = getBinding(compatible) ?: return null

        return bindings.computeIfAbsent(compatible) {
            val builder = DtsZephyrBinding.Builder(compatible)
            iterateBindings(listOf(compatible)) { doBuildBinding(builder, it, resolveChildIncludes = true) }

            builder.setPath(source.path)

            builder.build()
        }
    }

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
        if (node is DtsRefNode) {
            return node.getDtsReferenceTarget()?.let(::buildBinding)
        }

        val compatible = getCompatibleStrings(node).firstOrNull { getBinding(it) != null }
        if (compatible != null) return buildBinding(compatible)

        val bundledBinding = DtsBundledBindings.findBindingForNode(node)
        if (bundledBinding != null) return buildBundledBinding(bundledBinding)

        val parentBinding = DtsTreeUtil.findParentNode(node)?.let(::buildBinding)
        return parentBinding?.child
    }

    private fun buildBundledBinding(name: String): DtsZephyrBinding = bundledBindings.computeIfAbsent(name) {
        val binding = bundledBindingsYaml[name] ?: return@computeIfAbsent DtsZephyrBinding.empty

        val builder = DtsZephyrBinding.Builder(null, isBundled = true)
        doBuildBinding(builder, binding, resolveChildIncludes = false)

        builder.build()
    }

    /**
     * Builds a bundled binding.
     */
    fun buildBundledBinding(binding: DtsBundledBindings): DtsZephyrBinding = buildBundledBinding(binding.nodeName)

    /**
     * Builds the default binding which contains the standard documentation from
     * the specification for known properties.
     */
    fun buildDefaultBinding(): DtsZephyrBinding = buildBundledBinding(DEFAULT_BINDING_NAME)

    /**
     * List of all available bindings. Does not include bundled bindings since
     * they are matched on the node name and not the compatible string.
     */
    fun buildAllBindings(): List<DtsZephyrBinding> = bindingsYaml.keys.mapNotNull(::buildBinding)
}