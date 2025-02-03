package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.EnabledInspectionsProvider
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.Notification
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup.Companion.SANITY_FAILURE_NOTIFICATION
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.withKind
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

internal class SanityInspectionGroup(name: String, profile: QodanaInspectionProfile) : NamedInspectionGroup(name, profile) {
  companion object {
    const val SANITY_FAILURE_NOTIFICATION = "sanityFailure"
  }

  override fun createState(context: QodanaGlobalInspectionContext): NamedInspectionGroup.State = State(context)

  private inner class State(context: QodanaGlobalInspectionContext) : NamedInspectionGroup.State(context) {
    private val macroManager = PathMacroManager.getInstance(context.project)

    private val thresholds = Thresholds(context.project, context.config)

    override fun shouldSkip(inspectionId: String, file: PsiFile, wrappers: EnabledInspectionsProvider.ToolWrappers): Boolean {
      // Skip analysis if none of the inspections from the user profile is going to check this file.
      val profile = context.profile
      if (wrappers.allWrappers.none { profile.idToEffectiveGroup[it.shortName] == profile.mainGroup }) return true

      val path = macroManager.collapsePath(file.virtualFile.url)
      val module = ModuleUtilCore.findModuleForFile(file)?.name
      return thresholds.isReached(inspectionId, path, module)
    }

    override fun onConsumeProblem(inspectionId: String, relativePath: String?, module: String?): Boolean {
      val isThresholdReached = thresholds.addProblem(inspectionId, relativePath, module)
      if (isThresholdReached) return false
      return super.onConsumeProblem(inspectionId, relativePath, module)
    }

    override fun onFinish() {
      val sanityNotification = thresholds.constructNotification() ?: return
      context.project.service<RuntimeNotificationCollector>().add(sanityNotification)
    }
  }

  override fun applyConfig(config: QodanaConfig, project: Project, addDefaultExclude: Boolean): SanityInspectionGroup {
    super.applyConfig(config, project, addDefaultExclude)
    val excludeModifiers = config.getExcludeModifiers(addDefaultExclude, project)
    if (excludeModifiers.isEmpty()) return this

    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    val newProfile = QodanaInspectionProfile.clone(profile, "qodana.sanity.profile(base:${profile.name})", profileManager)

    excludeModifiers.forEach { it.updateProfileScopes(newProfile, project, config.projectPath) }
    return SanityInspectionGroup(name, newProfile)
  }
}

/** Tracks the number of found problems, to skip further problems if one of the thresholds (file, module, project) is reached. */
private class Thresholds(val project: Project, config: QodanaConfig) {
  private val problemCounters: Map<String, ProblemCounters>

  init {
    problemCounters = QodanaToolRegistrar.getInstance(project).createTools().associate {
      it.shortName to ProblemCounters(
        project,
        it,
        _maxPerFile = config.fileSuspendThreshold,
        _maxPerModule = config.moduleSuspendThreshold,
        _maxPerProject = config.projectSuspendThreshold
      )
    }
  }


  /**
   * Returns true if sanity threshold per file/module/project for the inspection is reached
   */
  fun addProblem(inspectionId: String, relativePath: String?, module: String?): Boolean {
    val problemCounters = problemCounters[inspectionId] ?: return false
    return problemCounters.addProblem(relativePath, module)
  }

  fun isReached(inspectionId: String, relativePath: String?, module: String?): Boolean {
    val problemCounters = problemCounters[inspectionId] ?: return false
    return problemCounters.isThresholdReached(relativePath, module)
  }

  fun constructNotification(): Notification? {
    val skippedInspections = problemCounters
      // ignore file filter because we want to report notification only in the case when sanity problems count is high
      // see discussion in IJ-CR-122309
      .filter { it.value.wasProjectThresholdReached() || it.value.wasAnyModuleThresholdReached() }
      .map { "\"${it.value.inspectionToolWrapper.displayName}\"" }

    if (skippedInspections.isEmpty()) {
      return null
    }

    return Notification()
      .withLevel(Notification.Level.ERROR)
      .withTimeUtc(Instant.now())
      .withMessage(
        Message()
          .withText(QodanaBundle.message("sanity.suspend.inspection.notification.message", skippedInspections.size, skippedInspections.joinToString(", ")))
      )
      .withKind(SANITY_FAILURE_NOTIFICATION)
  }
}

@VisibleForTesting
internal class ProblemCounters(
  val project: Project,
  val inspectionToolWrapper: InspectionToolWrapper<*, *>,
  _maxPerFile: Int,
  _maxPerModule: Int,
  _maxPerProject: Int
) {
  private val perFile = ConcurrentHashMap<FileAndModule, Int>()
  private val perModule = ConcurrentHashMap<String, Int>()
  private val perProject = AtomicInteger(0)

  private val maxPerFile: Int = _maxPerFile.inftyIfNotPositive()
  private val maxPerModule: Int = _maxPerModule.inftyIfNotPositive()
  private val maxPerProject: Int = _maxPerProject.inftyIfNotPositive()

  /**
   * Returns true if problem reached threshold
   */
  fun addProblem(relativePath: String?, module: String?): Boolean {
    val currentPerFile = if (relativePath != null) {
      perFile.compute(FileAndModule(relativePath, module ?: "")) { _, v -> (v ?: 0) + 1 } ?: -1
    } else -1
    if (currentPerFile > maxPerFile) {
      return true
    }

    val currentPerModule = if (module != null) {
      perModule.compute(module) { _, v -> (v ?: 0) + 1 } ?: -1
    } else -1
    if (currentPerModule > maxPerModule) {
      return true
    }

    val currentPerProject = perProject.incrementAndGet()

    return currentPerFile >= maxPerFile || currentPerModule >= maxPerModule || currentPerProject >= maxPerProject
  }

  fun isThresholdReached(relativePath: String?, module: String?): Boolean {
    if (perProject.get() >= maxPerProject)
      return true

    if (module != null && (perModule[module] ?: 0) >= maxPerModule)
      return true

    if (relativePath != null && (perFile[FileAndModule(relativePath, module ?: "")] ?: 0) >= maxPerFile)
      return true

    return false
  }

  fun wasAnyModuleThresholdReached(): Boolean {
    return perModule.any { it.value >= maxPerModule }
  }

  fun wasProjectThresholdReached(): Boolean {
    return perProject.get() >= maxPerProject
  }
}

private data class FileAndModule(val file: String, val module: String)

private fun Int.inftyIfNotPositive(): Int {
  if (this <= 0) return Int.MAX_VALUE
  return this
}