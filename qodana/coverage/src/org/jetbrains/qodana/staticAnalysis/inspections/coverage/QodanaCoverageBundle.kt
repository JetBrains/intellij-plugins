package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.coverage.CoverageSuite
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.rt.coverage.data.ProjectData

/**
 * Stores manually constructed coverage data.
 *
 * As Qodana does not read original coverage reports and also may filter ProjectData, we cannot use CoverageProvider directly,
 * as it requires a physical file. However, the CoverageSuite has only soft reference to coverage data, so if it is garbage collected,
 * we will not be able to construct it again. This class stores the constructed report instead
 */
class QodanaCoverageBundle(
  suite: CoverageSuite,
  initialData: ProjectData,
) : CoverageSuitesBundle(suite) {

  private var currentData: ProjectData? = initialData

  init {
    suite.setCoverageData(initialData)
  }

  override fun getCoverageData(): ProjectData? {
    val data = currentData ?: return null
    for (s in suites) {
      if (s.getCoverageData(null) !== data) {
        s.setCoverageData(data)
      }
    }
    return data
  }

  override fun setCoverageData(projectData: ProjectData?) {
    currentData = projectData
    for (s in suites) {
      s.setCoverageData(projectData)
    }
  }
}
