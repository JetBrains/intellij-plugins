package org.jetbrains.qodana.jvm.java.migrate

import com.intellij.refactoring.migration.MigrationManager
import com.intellij.refactoring.migration.MigrationMap
import com.intellij.refactoring.migration.MigrationMapEntry
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.script.UnvalidatedParameters
import org.jetbrains.qodana.staticAnalysis.script.optional

data class MigrationParameters(
  val included: String?,
  val mappings: List<MigrationMapEntry>
) {
  companion object {
    private fun fail(reason: String): Nothing = throw QodanaException(reason)
    private fun <T> Map<String, T>.req(key: String) =
      get(key) ?: fail("Missing required mapping property '$key'")

    fun fromParameters(params: UnvalidatedParameters): MigrationParameters {
      val included = params.optional<String>("include-mapping")?.takeUnless(String::isBlank)
      val mappings = params.optional<List<Map<String, String>>>("mapping")
        .orEmpty()
        .map {
          MigrationMapEntry().apply {
            oldName = it.req("old-name")
            newName = it.req("new-name")
            type = when (val t = it["type"]) {
              null, "class" -> MigrationMapEntry.CLASS
              "package" -> MigrationMapEntry.PACKAGE
              else -> fail("Unknown mapping type $t - only 'class' or 'package' are applicable")
            }
            if (type == MigrationMapEntry.PACKAGE) {
              isRecursive = it.req("recursive").toBoolean()
            }
          }

        }
      return if (included == null && mappings.isEmpty()) {
        fail("Require either 'include-mapping' or a non-empty list 'mapping'")
      }
      else {
        MigrationParameters(included, mappings)
      }
    }
  }

  fun resolveMap(migrationManager: MigrationManager): MigrationMap {
    val map = if (included == null) {
      MigrationMap()
    }
    else {
      migrationManager.findMigrationMap(included)?.cloneMap() ?: fail("Cannot find migration $included - Is the required plugin installed?")
    }

    map.name = when {
      map.name == null -> QodanaBundle.message("script.migrate.classes.custom.migration")
      mappings.isEmpty() -> map.name
      else -> QodanaBundle.message("script.migrate.classes.modified.migration", map.name)
    }
    mappings.forEach(map::addEntry)

    return map
  }
}