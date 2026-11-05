package org.jetbrains.qodana.util

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

/**
 * Names the inspections that Qodana runs as internal ones: only in its internal group, disabled in all its other groups, on every
 * file in scope. Their results go to a run property of the SARIF report instead of its results, affecting neither the quality
 * gate nor the baseline.
 */
interface InternalInspectionsProvider {
  companion object {
    val EP_NAME: ExtensionPointName<InternalInspectionsProvider> = ExtensionPointName("org.intellij.qodana.internalInspectionsProvider")

    /** The ids of the internal inspections of [project], from all providers. */
    fun internalInspectionIds(project: Project): Set<String> {
      return buildSet {
        EP_NAME.forEachExtensionSafe { addAll(it.internalInspectionIds(project)) }
      }
    }
  }

  /** The ids of the inspections of [project] to run as internal ones, asked once the [DynamicInspectionInitializer]s are done. */
  fun internalInspectionIds(project: Project): Set<String>
}
