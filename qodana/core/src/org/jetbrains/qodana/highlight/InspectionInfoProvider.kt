package org.jetbrains.qodana.highlight

import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.jetbrains.qodana.sarif.model.ReportingDescriptor
import com.jetbrains.qodana.sarif.model.Tool
import com.jetbrains.qodana.sarif.model.ToolComponent
import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.sarif.SUPPRESS_TOOL_ID_PARAMETER
import org.jetbrains.qodana.staticAnalysis.sarif.textFormat.markdownToHtml

class InspectionInfoProvider(
  private val project: Project,
  private val inspectionsIdsWithoutToolsToDescription: Map<String, String>,
  private val inspectionIdsToCategory: Map<String, String>,
  private val inspectionIdsToName: Map<String, String>,
  private val inspectionsIdsToSuppressToolIds: Map<String, String>
) {
  companion object {
    fun create(project: Project, inspectionsIds_: List<String>, tools: List<Tool>): InspectionInfoProvider {
      val inspectionIds = inspectionsIds_.toSet()
      val toolsComponents = (tools.mapNotNull { it.driver } + tools.flatMap { it.extensions ?: emptyList() }).toSet()
      val rules = toolsComponents.allRulesMatchingInspectionsIds(inspectionIds).toList()

      val inspectionsToolsProvider = InspectionsToolsProvider(project)
      val inspectionIdsWithoutTools = inspectionIds.filter {
        inspectionsToolsProvider.getInspectionToolById(it) == null
      }.toSet()
      val inspectionIdsWithoutToolsToDescription = createInspectionsDescriptionsMap(
        rules = toolsComponents.allRulesMatchingInspectionsIds(inspectionIdsWithoutTools)
      )
      val inspectionIdsToCategory = createInspectionsCategoriesMap(rules, tools.flatMap { it.driver?.taxa ?: emptyList() })
      val inspectionIdsToName = createInspectionsNamesMap(rules)
      val inspectionIdsToSuppressToolId = createInspectionsSuppressToolIdsMap(rules)

      return InspectionInfoProvider(
        project,
        inspectionIdsWithoutToolsToDescription,
        inspectionIdsToCategory,
        inspectionIdsToName,
        inspectionIdsToSuppressToolId
      )
    }
  }

  @Nls
  fun getName(inspectionId: String): String? {
    return inspectionIdsToName[inspectionId]
  }

  @Nls
  fun getCategory(inspectionId: String): String? {
    return inspectionIdsToCategory[inspectionId]
  }

  @InspectionMessage
  fun getDescription(inspectionId: String): String? {
    return inspectionsIdsWithoutToolsToDescription[inspectionId]
           ?: InspectionsToolsProvider(project).getInspectionToolById(inspectionId)?.loadDescription()
  }

  fun getSuppressIdByInspection(inspectionId: String): String? {
    return inspectionsIdsToSuppressToolIds[inspectionId]
           ?: InspectionsToolsProvider(project).getInspectionToolById(inspectionId)?.tool?.suppressId
  }
}

private class InspectionsToolsProvider(private val project: Project) {
  private val platformProjectInspectionProfile: InspectionProfileImpl by lazy {
    InspectionProfileManager.getInstance(project).currentProfile
  }

  private val qodanaInspectionProfile: InspectionProfileImpl by lazy {
    QodanaInspectionProfileManager.getInstance(project).currentProfile
  }

  fun getInspectionToolById(inspectionId: String) : InspectionToolWrapper<*, *>? {
    return sequence {
      yield(platformProjectInspectionProfile)
      yield(qodanaInspectionProfile)
    }.mapNotNull { it.getInspectionTool(inspectionId, project) }.firstOrNull()
  }
}

private fun createInspectionsDescriptionsMap(rules: Sequence<ReportingDescriptor>): Map<String, String> {
  return rules
    .mapNotNull { rule ->
      val multiformatMessageString = rule.fullDescription ?: return@mapNotNull null
      val description = multiformatMessageString.markdown?.let { markdownToHtml(it) } ?: multiformatMessageString.text ?: return@mapNotNull null
      rule.id to description
    }
    .toMap()
}

private fun createInspectionsCategoriesMap(rules: List<ReportingDescriptor>, taxa: List<ReportingDescriptor>): Map<String, String> {
  return rules
    .mapNotNull { rule ->
      val categoryId = rule.relationships?.firstOrNull()?.target?.id ?: return@mapNotNull null
      val category = taxa.firstOrNull { it.id == categoryId }?.name ?: return@mapNotNull null

      rule.id to category
    }
    .toMap()
}

private fun createInspectionsNamesMap(rules: List<ReportingDescriptor>): Map<String, String> {
  return rules
    .mapNotNull { rule ->
      val name = rule.shortDescription?.text ?: return@mapNotNull null
      rule.id to name
    }
    .toMap()
}

private fun createInspectionsSuppressToolIdsMap(rules: List<ReportingDescriptor>): Map <String, String> {
  return rules.mapNotNull { rule ->
    val suppressToolId = rule.defaultConfiguration?.parameters?.get(SUPPRESS_TOOL_ID_PARAMETER) as? String ?: return@mapNotNull null
    rule.id to suppressToolId
  }.toMap()
}

fun Set<ToolComponent>.allRulesMatchingInspectionsIds(inspectionsIds: Set<String>): Sequence<ReportingDescriptor> {
  return this.asSequence()
    .flatMap { it.rules?.filterNotNull() ?: emptyList() }
    .filter { it.id in inspectionsIds }
}