package org.jetbrains.qodana.staticAnalysis.scopes

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path

/**
 * Allows plugins to veto an inspection for the whole incremental run based on the current changeset.
 */
@ApiStatus.Internal
interface QodanaInspectionVetoer {
  companion object {
    val EP_NAME: ExtensionPointName<QodanaInspectionVetoer> =
      ExtensionPointName.create("org.intellij.qodana.qodanaInspectionVetoer")

    fun isVetoed(inspectionId: String, changedPaths: Set<Path>, project: Project): Boolean {
      return changedPaths.isNotEmpty() && EP_NAME.extensionList.any { vetoer ->
        vetoer.isApplicable(inspectionId) && vetoer.shouldVeto(changedPaths, project)
      }
    }
  }

  fun isApplicable(inspectionId: String): Boolean

  fun shouldVeto(changedPaths: Set<Path>, project: Project): Boolean
}
