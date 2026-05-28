package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.coverage.CoverageSuiteListener
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.view.CoverageViewManager
import com.intellij.openapi.project.Project

/**
 * Activates the Coverage tool window when a [QodanaCoverageBundle] finishes loading.
 *
 * The platform [com.intellij.coverage.view.CoverageViewSuiteListener] only auto-activates the tool
 * window when every suite is backed by a [com.intellij.coverage.DefaultCoverageFileProvider]. Qodana
 * cloud processors deliberately use custom providers, so the platform gate prevents activation.
 */
internal class QodanaCoverageToolWindowActivator(
  private val project: Project,
  private val targetBundle: CoverageSuitesBundle,
) : CoverageSuiteListener {

  override fun coverageDataCalculated(bundle: CoverageSuitesBundle) {
    if (bundle !== targetBundle) return
    val viewManager = CoverageViewManager.getInstance(project)
    val view = viewManager.getView(bundle) ?: return
    viewManager.activateToolwindow(view)
  }
}
