package org.jetbrains.qodana.inspectionKts.core

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.codeInspection.ex.DynamicInspectionsProvider
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.util.InternalInspectionsProvider
import java.nio.file.Path

/**
 * The system property naming a directory of `.inspection.kts` files whose inspections Qodana runs as internal ones: their results
 * go to a run property of the SARIF report instead of its results, and affect nothing else.
 */
internal const val INTERNAL_INSPECTIONS_KTS_DIR_PROPERTY: String = "qodana.internal.inspections.kts.dir"

/** Keeps the internal inspections apart from FlexInspect's own in the platform, e.g. in the statistics. */
internal const val INTERNAL_INSPECTIONS_PROVIDER_NAME: String = "FlexInspectInternal"

/**
 * Registers the inspections of [INTERNAL_INSPECTIONS_KTS_DIR_PROPERTY], which [KtsInternalInspectionsProvider] names to Qodana as
 * internal ones. Files that failed to compile are left out silently.
 */
internal class KtsInternalDynamicInspectionsProvider : DynamicInspectionsProvider {
  override fun inspections(project: Project): Flow<Set<DynamicInspectionDescriptor>> {
    return KtsInspectionsManager.getInstance(project).internalKtsInspectionsFlow.compiledInspectionsFlow()
  }

  override fun name(): String = INTERNAL_INSPECTIONS_PROVIDER_NAME
}

/** Names the inspections registered by [KtsInternalDynamicInspectionsProvider]. */
internal class KtsInternalInspectionsProvider : InternalInspectionsProvider {
  override fun internalInspectionIds(project: Project): Set<String> {
    return KtsInspectionsManager.getInstance(project).internalKtsInspectionsFlow.value.orEmpty()
      .filterIsInstance<InspectionKtsFileStatus.Compiled>()
      .flatMap { it.inspections.inspections }
      .mapTo(LinkedHashSet()) { it.toolWrapper.shortName }
  }
}

/** The directory of [INTERNAL_INSPECTIONS_KTS_DIR_PROPERTY], or `null` when the property is not set. */
internal fun internalInspectionsKtsDirectory(): Path? =
  System.getProperty(INTERNAL_INSPECTIONS_KTS_DIR_PROPERTY)?.takeIf { it.isNotBlank() }?.let { Path.of(it).toAbsolutePath().normalize() }
