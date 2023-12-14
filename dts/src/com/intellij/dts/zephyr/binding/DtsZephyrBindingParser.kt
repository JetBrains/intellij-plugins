package com.intellij.dts.zephyr.binding

import com.intellij.util.asSafely
import java.util.*

class DtsZephyrBindingParser(
  private val sources: Map<String, Source>,
  private val defaultSource: Source?,
) {
  companion object {
    private val emptyFilter = Filter(null, null, null)
  }

  data class Source(val path: String?, val data: Map<*, *>)

  private data class Filter(val allowedProperties: List<String>?, val blockedProperties: List<String>?, val childFilter: Filter?)

  private data class Include(val name: String, val filter: Filter)

  private val cache = mutableMapOf<String, DtsZephyrBinding?>()

  private inline fun <reified T : Any> readValue(data: Map<*, *>, key: String): T? {
    return data[key].asSafely<T>()
  }

  private inline fun <reified T : Any> readList(data: Map<*, *>, key: String): List<T>? {
    return data[key]?.asSafely<List<*>>()?.filterIsInstance<T>()
  }

  private fun readMap(data: Map<*, *>, key: String): Map<*, *>? {
    return readValue<Map<*, *>>(data, key)
  }

  private fun readFilter(data: Map<*, *>): Filter {
    return Filter(
      allowedProperties = readList<String>(data, "property-allowlist"),
      blockedProperties = readList<String>(data, "property-blocklist"),
      childFilter = readMap(data, "child-binding")?.let(::readFilter)
    )
  }

  private fun readInclude(include: Any): Include? {
    // case single string
    if (include is String) {
      return Include(include, emptyFilter)
    }

    // case map with filters
    if (include !is Map<*, *>) return null

    return Include(
      name = readValue<String>(include, "name") ?: return null,
      filter = readFilter(include),
    )
  }

  private fun readIncludes(data: Map<*, *>): List<Include> {
    // case single include
    readValue<String>(data, "include")?.let { include ->
      return listOf(Include(include, emptyFilter))
    }

    // case list of includes (each element can either be a map or string)
    readList<Any>(data, "include")?.let { includes ->
      return includes.mapNotNull(::readInclude)
    }

    return emptyList()
  }

  private fun getSource(name: String): Source? {
    return sources[name.removeSuffix(".yaml")]
  }

  private fun iterateIncludes(source: Source, callback: (Source, Filter) -> Unit) {
    val frontier = Stack<Include>()
    readIncludes(source.data).forEach(frontier::push)

    while (!frontier.empty()) {
      val include = frontier.pop()
      val includeSource = getSource(include.name) ?: continue

      callback(includeSource, include.filter)

      readIncludes(includeSource.data).forEach(frontier::push)
    }
  }

  private fun doBuildBinding(builder: DtsZephyrBinding.Builder, source: Source, filter: Filter) {
    readValue<String>(source.data, "description")?.let(builder::setDescription)
    readValue<Boolean>(source.data, "undeclared-properties")?.let(builder::setAllowUndeclaredProperties)

    readMap(source.data, "properties")?.let { properties ->
      for ((name, property) in properties.entries) {
        if (name !is String || property !is Map<*, *>) continue

        if (filter.allowedProperties != null && !filter.allowedProperties.contains(name)) continue
        if (filter.blockedProperties != null && filter.blockedProperties.contains(name)) continue

        val propertyBuilder = builder.getPropertyBuilder(name)
        readValue<String>(property, "description")?.let(propertyBuilder::setDescription)
        readValue<String>(property, "type")?.let(propertyBuilder::setType)
        readValue<Boolean>(property, "required")?.let(propertyBuilder::setRequired)
        readValue<Any>(property, "default")?.let(propertyBuilder::setDefault)
        readValue<Any>(property, "const")?.let(propertyBuilder::setConst)
        readValue<List<Any>>(property, "enum")?.let(propertyBuilder::setEnum)
      }
    }

    readMap(source.data, "child-binding")?.let { binding ->
      buildBinding(
        builder.getChildBuilder(),
        Source(null, binding),
        filter.childFilter ?: emptyFilter,
      )
    }
  }

  private fun buildBinding(builder: DtsZephyrBinding.Builder, source: Source, filter: Filter) {
    source.path?.let(builder::setPath)

    defaultSource?.let { doBuildBinding(builder, it, emptyFilter) }
    doBuildBinding(builder, source, filter)

    iterateIncludes(source) { includeSource, includeFilter ->
      doBuildBinding(builder, includeSource, includeFilter)
    }
  }

  private fun doParse(name: String): DtsZephyrBinding? {
    val source = getSource(name) ?: return null
    val builder = DtsZephyrBinding.Builder()

    readValue<String>(source.data, "compatible")?.let(builder::setCompatible)
    buildBinding(builder, source, emptyFilter)

    return builder.build()
  }

  @Synchronized
  fun parse(name: String): DtsZephyrBinding? {
    return if (sources.containsKey(name)) {
      cache.computeIfAbsent(name, ::doParse)
    }
    else {
      null
    }
  }

  fun parseAll(): Collection<DtsZephyrBinding> {
    return sources.keys.mapNotNull(::parse)
  }
}