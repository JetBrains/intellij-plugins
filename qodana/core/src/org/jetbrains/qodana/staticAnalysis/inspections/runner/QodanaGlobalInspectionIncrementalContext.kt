package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.ex.EnabledInspectionsProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.ui.content.ContentManager
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaInspectionVetoer
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.InspectionToolScopeExtender
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaScopeExtenderProvider
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * Instantiated in the case of scoped-like scripts for incremental analysis.
 * Contains additional metadata:
 * - changed lines of code for coverage
 * - scope extended files to scope extender mappings for more deep PR analysis needs
 */
class QodanaGlobalInspectionIncrementalContext(
  project: Project,
  contentManager: NotNullLazyValue<out ContentManager>,
  config: QodanaConfig,
  outputPath: Path,
  profile: QodanaProfile,
  qodanaRunScope: CoroutineScope,
  coverageStatisticsData: CoverageStatisticsData,
  val scopeExtended: Map<VirtualFile, Set<String>>,
  private val changedPaths: Set<Path>,
) : QodanaGlobalInspectionContext(project, contentManager, config, outputPath, profile, qodanaRunScope, coverageStatisticsData) {
  private val extenderCache = ConcurrentHashMap<String, InspectionToolScopeExtender?>()
  private val vetoCache = ConcurrentHashMap<String, Boolean>()

  override fun shouldSkip(inspectionId: String, file: PsiFile, wrappers: EnabledInspectionsProvider.ToolWrappers): Boolean {
    return isVetoed(inspectionId) || !isInExtendedScope(inspectionId, file.virtualFile) && profileState.shouldSkip(inspectionId, file, wrappers)
  }

  private fun isInExtendedScope(inspectionId: String, file: VirtualFile): Boolean {
    if (!QodanaRegistry.isScopeExtendingEnabled) return false
    val extender = extenderCache.computeIfAbsent(inspectionId) { QodanaScopeExtenderProvider.getExtender(it) } ?: return false
    val fileExtenders = scopeExtended[file] ?: return false
    return extender.name in fileExtenders
  }

  private fun isVetoed(inspectionId: String): Boolean {
    return vetoCache.computeIfAbsent(inspectionId) { QodanaInspectionVetoer.isVetoed(it, changedPaths, project) }
  }
}