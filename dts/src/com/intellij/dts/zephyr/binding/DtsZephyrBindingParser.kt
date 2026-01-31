package com.intellij.dts.zephyr.binding

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.asSafely
import com.intellij.util.containers.MultiMap
import java.util.Stack

data class BindingSource(val files: Map<String, BindingFile>, val default: BindingFile?)

private data class Filter(val allowedProperties: List<String>?, val blockedProperties: List<String>?, val childFilter: Filter?)

private data class Include(val name: String, val filter: Filter)

private val emptyFilter = Filter(null, null, null)

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

private fun findSource(source: BindingSource, name: String): BindingFile? {
  return source.files[name.removeSuffix(".yaml")]
}

private fun iterateIncludes(source: BindingSource, file: BindingFile, callback: (BindingFile, Filter) -> Unit) {
  val frontier = Stack<Include>()
  readIncludes(file.data).forEach(frontier::push)

  while (!frontier.empty()) {
    val include = frontier.pop()
    val includeFile = findSource(source, include.name) ?: continue

    callback(includeFile, include.filter)

    readIncludes(includeFile.data).forEach(frontier::push)
  }
}

private fun doBuildBinding(builder: DtsZephyrBinding.Builder, source: BindingSource, file: BindingFile, filter: Filter) {
  readValue<String>(file.data, "description")?.let(builder::setDescription)
  readValue<Boolean>(file.data, "undeclared-properties")?.let(builder::setAllowUndeclaredProperties)
  readValue<String>(file.data, "on-bus")?.let(builder::setOnBus)

  // bus can eiter be a string or a list of strings
  readValue<String>(file.data, "bus")?.let { builder.setBusses(listOf(it)) }
  readList<String>(file.data, "bus")?.let(builder::setBusses)

  readMap(file.data, "properties")?.let { properties ->
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

  readMap(file.data, "child-binding")?.let { data ->
    buildBinding(
      builder.getChildBuilder(),
      source,
      BindingFile(null, data),
      filter.childFilter ?: emptyFilter,
    )
  }
}

private fun buildBinding(builder: DtsZephyrBinding.Builder, source: BindingSource, file: BindingFile, filter: Filter) {
  file.path?.let(builder::setPath)

  source.default?.let { doBuildBinding(builder, source, it, emptyFilter) }
  doBuildBinding(builder, source, file, filter)

  iterateIncludes(source, file) { includeFile, includeFilter ->
    doBuildBinding(builder, source, includeFile, includeFilter)
  }
}

fun parseExternalBindings(source: BindingSource): MultiMap<String, DtsZephyrBinding> {
  val bindings: MultiMap<String, DtsZephyrBinding> = MultiMap.create()

  for (file in source.files.values) {
    ProgressManager.checkCanceled()

    val compatible = readValue<String>(file.data, "compatible") ?: continue
    val builder = DtsZephyrBinding.Builder().setCompatible(compatible)

    buildBinding(builder, source, file, emptyFilter)

    bindings.putValue(compatible, builder.build())
  }

  return bindings
}

fun parseBundledBindings(source: BindingSource): Map<String, DtsZephyrBinding> {
  val bindings: MutableMap<String, DtsZephyrBinding> = mutableMapOf()

  for ((name, file) in source.files.entries) {
    val builder = DtsZephyrBinding.Builder()

    buildBinding(builder, source, file, emptyFilter)

    bindings[name] = builder.build()
  }

  return bindings
}