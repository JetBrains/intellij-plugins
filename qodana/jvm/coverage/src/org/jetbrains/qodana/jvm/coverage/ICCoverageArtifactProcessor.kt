package org.jetbrains.qodana.jvm.coverage

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.JavaCoverageEngine
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiManager
import com.intellij.psi.util.ClassUtil
import com.intellij.rt.coverage.data.ProjectData
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coverage.CHANGED_LINES_ARTIFACT_ID
import org.jetbrains.qodana.coverage.ChangedLinesMetaDataArtifact
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.coverage.readChangedLinesPayload
import org.jetbrains.qodana.registry.QodanaRegistry.openCoveragePackageLength
import org.jetbrains.qodana.registry.QodanaRegistry.openCoverageSmartFilteringEnabled
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.CoverageCloudArtifactsProcessor
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageBundle
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.filterClassLinesByAllowed

class ICCoverageArtifactProcessor: CoverageCloudArtifactsProcessor {
  override suspend fun process(artifacts: Map<String, ReportMetadata>, project: Project): CoverageSuitesBundle? {
    val artifact = (artifacts[JVM_COVERAGE] as? CoverageMetaDataArtifact) ?: return null
    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(JavaCoverageEngine::class.java)
    val runner = CoverageCloudArtifactsProcessor.getCoverageRunner(artifact.path) ?: return null
    if (!runner.acceptsCoverageEngine(engine)) return null

    val fileProvider = ICCoverageFileProvider(artifact.path)
    val changedLinesArtifact = artifacts[CHANGED_LINES_ARTIFACT_ID] as? ChangedLinesMetaDataArtifact
    val noFiltersSuite = engine.createCoverageSuite(artifact.id, project, runner, fileProvider, -1) ?: return null
    val rawData = withContext(QodanaDispatchers.IO) {
      noFiltersSuite.getCoverageData(CoverageDataManager.getInstance(project))
    } ?: return null

    // report for incremental analysis
    if (changedLinesArtifact != null) {
      val payload = withContext(QodanaDispatchers.IO) { readChangedLinesPayload(changedLinesArtifact.path) }
      val fqnAllowed = if (payload != null && payload.files.isNotEmpty()) buildFqnAllowedLines(payload.files, project) else emptyMap()
      if (fqnAllowed.isNotEmpty()) {
        val topLevelInclude = fqnAllowed.keys.asSequence().map { it.substringBefore('$') }.toSet().toTypedArray()
        val suite = engine.createSuite(runner, artifact.id, fileProvider, topLevelInclude, null, -1,
                                       false, true, false, project) ?: return null
        return QodanaCoverageBundle(suite, filterClassLinesByAllowed(rawData, fqnAllowed))
      }
    }

    if (openCoverageSmartFilteringEnabled) {
      val smartSuite = engine.createSuite(runner, artifact.id, fileProvider, coverageFilterComputation(rawData), null, -1,
                                          false, true, false, project) ?: return null
      return CoverageSuitesBundle(smartSuite)
    }

    return CoverageSuitesBundle(noFiltersSuite)
  }

  private fun coverageFilterComputation(covData: ProjectData): Array<String> {
    return covData.classes
      .keys
      .asSequence()
      .map { (it.split('.').dropLast(1).take(openCoveragePackageLength) + "*").joinToString(".") }
      .toSet()
      .toTypedArray<String>()
  }

  private suspend fun buildFqnAllowedLines(
    changedFiles: Map<String, Set<Int>>,
    project: Project,
  ): Map<String, Set<Int>> = readAction {
    val projectDir = project.guessProjectDir() ?: return@readAction emptyMap()
    val psiManager = PsiManager.getInstance(project)
    buildMap {
      for ((rel, lines) in changedFiles) {
        val vFile = projectDir.findFileByRelativePath(FileUtil.toSystemIndependentName(rel)) ?: continue
        val psiFile = psiManager.findFile(vFile) as? PsiClassOwner ?: continue
        for (topClass in psiFile.classes) {
          putClassAndNested(topClass, lines)
        }
      }
    }
  }

  private fun MutableMap<String, Set<Int>>.putClassAndNested(psiClass: PsiClass, lines: Set<Int>) {
    val fqn = ClassUtil.getJVMClassName(psiClass) ?: return
    put(fqn, lines)
    for (inner in psiClass.innerClasses) {
      putClassAndNested(inner, lines)
    }
  }
}