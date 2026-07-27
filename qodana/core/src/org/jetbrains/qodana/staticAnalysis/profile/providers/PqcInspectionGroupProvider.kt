package org.jetbrains.qodana.staticAnalysis.profile.providers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.inspectionProfile.InspectionGroupProvider
import com.intellij.codeInspection.inspectionProfile.YamlInspectionGroup
import com.intellij.openapi.diagnostic.logger

private const val PQC_GROUPS_RESOURCE = "/qodana-profiles/.idea/inspectionProfiles/qodana.pqc.groups.yaml"

private val LOG = logger<PqcInspectionGroupProvider>()

private val MAPPER by lazy {
  YAMLMapper().registerKotlinModule()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

class PqcInspectionGroupProvider : InspectionGroupProvider {

  private val groups: Map<String, YamlInspectionGroup> by lazy { loadGroups() }

  override fun findGroup(groupId: String): YamlInspectionGroup? = groups[groupId]

  private fun loadGroups(): Map<String, YamlInspectionGroup> {
    return try {
      val raw = javaClass.getResourceAsStream(PQC_GROUPS_RESOURCE)?.use { stream ->
        MAPPER.readValue(stream, PqcGroupsFileRaw::class.java)
      }
      if (raw == null) {
        LOG.warn("PQC groups resource not found on classpath: $PQC_GROUPS_RESOURCE")
        return emptyMap()
      }
      val result = LinkedHashMap<String, YamlInspectionGroup>()

      val nested = InspectionGroupProvider { id -> result[id] }
      for ((id, inspections, groups) in raw.groups) {
        if (id.isEmpty()) continue
        result[id] = if (groups.isNotEmpty()) {
          PqcCompositeGroup(id, groups, nested)
        }
        else {
          PqcLeafGroup(id, inspections.toHashSet())
        }
      }
      result
    }
    catch (e: Exception) {
      LOG.warn("Failed to load PQC inspection groups from $PQC_GROUPS_RESOURCE", e)
      emptyMap()
    }
  }
}

private data class PqcGroupsFileRaw(
  val groups: List<PqcGroupRaw> = emptyList(),
)

private data class PqcGroupRaw(
  val groupId: String = "",
  val inspections: List<String> = emptyList(),
  val groups: List<String> = emptyList(),
)

private class PqcLeafGroup(
  override val groupId: String,
  private val inspections: Set<String>,
) : YamlInspectionGroup {
  override fun includesInspection(tool: InspectionToolWrapper<*, *>): Boolean = tool.shortName in inspections
}

private class PqcCompositeGroup(
  override val groupId: String,
  private val groupRules: List<String>,
  private val groupProvider: InspectionGroupProvider,
) : YamlInspectionGroup {
  override fun includesInspection(tool: InspectionToolWrapper<*, *>): Boolean {
    for (groupRule in groupRules.asReversed()) {
      if (groupRule.isEmpty()) continue
      val refId = groupRule.removePrefix("!")
      val ref = groupProvider.findGroup(refId)
      if (ref != null && ref.includesInspection(tool)) {
        return refId == groupRule
      }
    }
    return false
  }
}
