package org.jetbrains.qodana.jvm.java.migrate

object MigrateClassesTestUtils {

  private fun <K : Any, V : Any> MutableMap<K, V>.notNull(key: K, value: V?) {
    if (value != null) put(key, value)
  }

  fun paramMap(includeMapping: String?, vararg mappings: Map<String, String>) = buildMap<String, Any> {
    notNull("include-mapping", includeMapping)
    put("mapping", mappings.toList())
  }

  fun mapping(oldName: String? = null, newName: String? = null, type: String? = null, recursive: String? = null) = buildMap {
    notNull("old-name", oldName)
    notNull("new-name", newName)
    notNull("type", type)
    notNull("recursive", recursive)
  }

}