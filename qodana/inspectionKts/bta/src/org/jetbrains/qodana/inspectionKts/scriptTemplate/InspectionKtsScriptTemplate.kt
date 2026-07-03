package org.jetbrains.qodana.inspectionKts.scriptTemplate

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.resultField

/**
 * Name of the field the compiler generates to hold the script's last-expression value (its result).
 * Set as the script definition's [resultField] so the BTA executor can read the result back by this
 * exact name instead of scanning the generated class's fields.
 */
const val INSPECTION_KTS_RESULT_FIELD_NAME: String = "__inspectionKtsResult__"

/**
 * Script template for `*.inspection.kts` files compiled via the Kotlin Build Tools API.
 * Default imports are intentionally empty here; they are injected per-script at the BTA call site by
 * prepending `import` lines to the script source, keeping `InspectionKtsDefaultImportProvider` the
 * single source of truth. The compilation classpath is likewise supplied to BTA separately, so this
 * template needs no `jvm { dependencies(...) }` block.
 */
@KotlinScript(
  fileExtension = "inspection.kts",
  compilationConfiguration = InspectionKtsScriptCompilationConfiguration::class,
)
abstract class InspectionKtsScriptTemplate

object InspectionKtsScriptCompilationConfiguration : ScriptCompilationConfiguration({
  defaultImports.put(emptyList())
  resultField.put(INSPECTION_KTS_RESULT_FIELD_NAME)
  ide {
    acceptedLocations.put(listOf(ScriptAcceptedLocation.Project))
  }
})
