package org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools

import com.intellij.codeInspection.ex.InspectListener
import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName.Companion.create
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.impl.ExtensionPointImpl
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.util.application
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.XmlProblem
import java.util.function.Consumer

/**
 * Wrapper for external tool analyzer for Qodana. That analyzer wraps an external tool, supporting generation of one or more
 * inspections. Provides an interface that makes those internal inspections profile-aware for Qodana.
 */
interface ExternalToolsProvider {
  /**
   * Return the list of supported tools with their name, group, description, severity and availability
   */
  fun describeTools(project: Project?): List<ExternalInspectionDescriptor>

  /**
   * Return the array of job descriptors that will be used to track the progress of external tool
   */
  fun announceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor>

  /**
   * Execute external tool
   * @param consumer - should be used to consume issue from external tool
   */
  suspend fun runTools(context: QodanaGlobalInspectionContext, consumer: Consumer<ExternalToolIssue>)

  companion object {
    val EXTERNAL_TOOLS_EP = create<ExternalToolsProvider>("org.intellij.qodana.externalToolsProvider")
    private val LOG = Logger.getInstance(ExternalToolsProvider::class.java)

    fun runAnnounceToolProvider(project: Project?): List<ExternalInspectionToolWrapper> {
      val ep = EXTERNAL_TOOLS_EP.point as ExtensionPointImpl<ExternalToolsProvider>
      val tools: MutableList<ExternalInspectionToolWrapper> = ArrayList()
      ep.processUnsortedWithPluginDescriptor { provider, descriptor ->
        tools.addAll(instantiateTools(provider, descriptor, project))
      }
      return tools
    }

    fun runAnnounceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor> {
      return filterEnabledProviders(getEnabledToolNames(context), context.project).map { it.announceJobDescriptors(context) }
        .flatMap { it.asSequence() }
        .toTypedArray()
    }

    suspend fun runExternalToolsProviders(context: QodanaGlobalInspectionContext, eventPublisher: InspectListener) {
      com.intellij.codeInspection.ex.reportToQodanaWhenActivityFinished(
        eventPublisher,
        "EXTERNAL_TOOLS_EXECUTION",
        context.project
      ) {
        val enabledTools = getEnabledToolNames(context)
        for (provider in filterEnabledProviders(enabledTools, context.project)) {
          try {
            provider.runTools(context) { issue: ExternalToolIssue ->
              if (!enabledTools.contains(issue.inspectionId)) return@runTools
              val element = issue.toElement()
              if (issue.file != null) {
                val virtualFile = VirtualFileManager.getInstance().findFileByUrl(issue.file)
                val tools = context.effectiveProfile.getToolsOrNull(issue.inspectionId, context.project)
                if (virtualFile != null && tools != null) {
                  application.runReadAction {
                    if (tools.getEnabledTool(PsiManager.getInstance(context.project).findFile(virtualFile), true) != null) {
                      context.consumer.consume(listOf(element).map { XmlProblem(it) }, issue.inspectionId)
                    }
                  }
                }
              }
              else {
                context.consumer.consume(listOf(element).map { XmlProblem(it) }, issue.inspectionId)
              }
            }
          }
          catch (e: ProcessCanceledException) {
            throw e
          }
          catch (e: IndexNotReadyException) {
            throw e
          }
          catch (e: Throwable) {
            LOG.error(e)
          }
        }
      }
    }

    private fun instantiateTools(provider: ExternalToolsProvider, pluginDescriptor: PluginDescriptor, project: Project?): List<ExternalInspectionToolWrapper> {
      return provider.describeTools(project).map { ExternalInspectionToolWrapper(it, pluginDescriptor) }
    }

    private fun filterEnabledProviders(enabledToolNames: Set<String>, project: Project): Array<ExternalToolsProvider> {
      return EXTERNAL_TOOLS_EP.extensionList.filter { e -> e.describeTools(project).any { x -> enabledToolNames.contains(x.shortName) } }.toTypedArray()
    }

    private fun getEnabledToolNames(context: QodanaGlobalInspectionContext): Set<String> {
      return context.effectiveProfile.tools.filter { it.isEnabled }.map { it.shortName }.toSet()
    }
  }
}