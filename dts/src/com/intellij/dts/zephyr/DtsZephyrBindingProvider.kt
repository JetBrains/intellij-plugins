package com.intellij.dts.zephyr

import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsString
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.readText
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

@Service(Service.Level.PROJECT)
class DtsZephyrBindingProvider(val project: Project) {
    companion object {
        fun of(project: Project): DtsZephyrBindingProvider = project.service()

        private const val DEFAULT_BINDING_PATH = "bindings/zephyr.yaml"

        private val logger = Logger.getInstance(DtsZephyrBindingProvider::class.java)
    }

    private val yaml = Yaml(SafeConstructor(LoaderOptions()))

    private var bindingsHash: Int? = null
    private var bindings: Map<String, YamlMap>? = null

    private val defaultBinding: YamlMap by lazy {
        try {
            val stream = javaClass.classLoader.getResourceAsStream(DEFAULT_BINDING_PATH)
                ?: throw IOException("resource stream was null")

            val text = stream.bufferedReader().use { it.readText() }

            loadBinding(text, "default") ?: emptyYaml
        } catch (e: IOException) {
            logger.error("failed to load default binding: $e")
            emptyYaml
        }
    }

    private fun loadBinding(binding: String, name: String): YamlMap? {
        try {
            val map = yaml.load<Map<*, *>>(binding)
            return YamlMap(map)
        } catch (e: ClassCastException) {
            logger.debug("unexpected yaml format: $name")
        } catch (e: YAMLException) {
            logger.debug("not a valid yaml file: $name")
        }

        return null
    }

    private fun loadBindings(dir: VirtualFile): Map<String, YamlMap> {
        val bindings = mutableMapOf<String, YamlMap>()

        val visitor = object : VirtualFileVisitor<Any>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.isDirectory || file.extension != "yaml") return true

                loadBinding(file.readText(), file.name)?.let {
                    bindings[file.nameWithoutExtension] = it
                }

                return true
            }
        }
        VfsUtilCore.visitChildrenRecursively(dir, visitor)

        return bindings
    }

    private fun getBindings(): Map<String, YamlMap>? {
        val zephyrProvider = DtsZephyrProvider.of(project)

        if (bindingsHash != zephyrProvider.modificationHash) {
            bindings = zephyrProvider.getBindingsDir()?.let(::loadBindings)
        }

        return bindings
    }

    private fun getBinding(name: String): YamlMap? {
        val bindings = getBindings() ?: return null
        val key = name.removeSuffix(".yaml")

        return bindings[key]
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
     * Invokes the callback for the matching binding and all included bindings.
     * The search ends if the callback returns null or if there are no more
     * matching bindings.
     *
     * The first binding will always be the default binding.
     */
    private fun iterateBindings(compatible: List<String>, callback: (YamlMap) -> Unit) {
        callback(defaultBinding)

        val frontier = Stack<String>()
        compatible.reversed().forEach(frontier::push)

        while (!frontier.empty()) {
            val binding = getBinding(frontier.pop()) ?: continue
            callback(binding)

            getIncludes(binding).forEach(frontier::push)
        }
    }

    private fun doBuildBinding(builder: DtsZephyrBinding.Builder, yaml: YamlMap) {
        yaml.readString("description")?.let(builder::setDescription)

        yaml.readMap("properties")?.let { properties ->
            for ((name, property) in properties.iterateMap()) {
                property.readString("description")?.let { builder.setPropertyDescription(name, it) }
            }
        }

        yaml.readMap("child-binding")?.let { binding ->
            doBuildBinding(builder.getChildBuilder(), binding)

            // child bindings can have includes
            iterateBindings(getIncludes(binding)) {
                doBuildBinding(builder.getChildBuilder(), it)
            }
        }
    }

    private fun getCompatibleStrings(node: DtsNode): List<String> {
        val property = node.dtsProperties.firstOrNull { it.dtsName == "compatible" } ?: return emptyList()
        return property.dtsValues.filterIsInstance<DtsString>().map { it.dtsParse() }
    }


    /**
     * Builds a binding for a specific node.
     *
     * If the node has a compatible property the binding will be built for the
     * matching string. If there is no matching binding this method searches
     * the parents of the node for matching child bindings.
     */
    fun buildBinding(node: DtsNode): DtsZephyrBinding? {
        val compatible = getCompatibleStrings(node).firstOrNull { getBinding(it) != null }

        return if (compatible != null) {
            val builder = DtsZephyrBinding.Builder(compatible)
            iterateBindings(listOf(compatible)) { doBuildBinding(builder, it) }

            builder.build()
        } else {
            val parentBinding = DtsTreeUtil.findParentNode(node)?.let(::buildBinding)
            parentBinding?.child
        }
    }

    /**
     * Builds the default binding.
     */
    fun buildDefaultBinding(): DtsZephyrBinding {
        val builder = DtsZephyrBinding.Builder("default")
        doBuildBinding(builder, defaultBinding)

        return builder.build()
    }
}