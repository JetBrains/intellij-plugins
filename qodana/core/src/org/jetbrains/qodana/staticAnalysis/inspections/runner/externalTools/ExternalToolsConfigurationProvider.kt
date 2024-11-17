package org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools

import com.intellij.codeInspection.ex.InspectListener
import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.openapi.extensions.ExtensionPointName.Companion.create
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

/**
 * Aimed to provide pre- and post- external tools' configuration activies, such as launching, configuring and clean-up, that
 * needed to be executed after external tools execution.
 */
interface ExternalToolsConfigurationProvider {
  /**
   * Return the array of job descriptors that will be used to track the progress of external tool configuration
   */
  fun announceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor>
  /**
   * Pre external tools-run configuration activities that could be required in order to execute one or more external tool.
   * For example, being used in order to configure Rider and launch SWEA, whose results will be used by other external tools.
   */
  suspend fun performPreRunActivities(context: QodanaGlobalInspectionContext)

  /**
   * Post external tools-run configuration activities that could be required in order to clean up after external tool run.
   * For example, being used in order to reset Rider configuration settings.
   */
  suspend fun performPostRunActivities(context: QodanaGlobalInspectionContext)

  companion object {
    val EP_NAME = create<ExternalToolsConfigurationProvider>("org.intellij.qodana.externalToolsConfigurationProvider")
    suspend fun runPreRunActivities(context: QodanaGlobalInspectionContext, eventPublisher: InspectListener) {
      com.intellij.codeInspection.ex.reportToQodanaWhenActivityFinished(
        eventPublisher,
        "EXTERNAL_TOOLS_CONFIGURATION",
        context.project
      ) {
        for (provider in EP_NAME.extensionList) {
          provider.performPreRunActivities(context)
        }
      }
    }

    suspend fun runPostRunActivities(context: QodanaGlobalInspectionContext) {
      for (provider in EP_NAME.extensionList) {
        provider.performPostRunActivities(context)
      }
    }

    fun runAnnounceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor> {
      return EP_NAME.extensionList.map { it.announceJobDescriptors(context) }
        .flatMap { it.asSequence() }
        .toTypedArray()
    }
  }
}