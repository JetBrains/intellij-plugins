package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnFirstLevelMappingNode
import com.intellij.aws.cloudformation.model.CfnMappingValue
import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnSecondLevelMappingNode

object CloudFormationResolve {
  fun getSectionEntities(parsed: CloudFormationParsedFile, section: CloudFormationSection): List<CfnNamedNode> =
      when (section) {
        CloudFormationSection.Conditions -> parsed.root.conditionsNode?.conditions ?: emptyList()
        CloudFormationSection.Mappings -> parsed.root.mappingsNode?.mappings ?: emptyList()
        CloudFormationSection.Resources -> parsed.root.resourcesNode?.resources ?: emptyList()
        CloudFormationSection.Parameters -> parsed.root.parametersNode?.parameters ?: emptyList()
        CloudFormationSection.Outputs -> parsed.root.outputsNode?.properties ?: emptyList()

        CloudFormationSection.Description, CloudFormationSection.FormatVersion,
        CloudFormationSection.Globals, CloudFormationSection.Metadata, CloudFormationSection.Transform -> emptyList()
      }

  fun resolveEntity(parsed: CloudFormationParsedFile, entityName: String, sections: Collection<CloudFormationSection>): CfnNamedNode? =
      sections
          .map { getSectionEntities(parsed, it) }
          .map { entities -> entities.firstOrNull { it.name?.value == entityName } }
          .firstOrNull { it != null }

  fun getEntities(parsed: CloudFormationParsedFile, sections: Collection<CloudFormationSection>): Set<String> =
      sections
          .map { getSectionEntities(parsed, it) }
          .flatMap { entities -> entities.mapNotNull { it.name?.value } }
          .toSet()

  fun resolveResource(parsed: CloudFormationParsedFile, resourceName: String): CfnResourceNode? =
      parsed.root.resourcesNode?.resources?.firstOrNull { it.name?.value == resourceName }

  fun resolveMapping(parsed: CloudFormationParsedFile, mappingName: String): CfnFirstLevelMappingNode? =
      parsed.root.mappingsNode?.mappings?.firstOrNull { it.name?.value == mappingName }

  fun resolveFirstLevelMappingKey(parsed: CloudFormationParsedFile, mappingName: String, topLevelKey: String): CfnSecondLevelMappingNode? {
    val firstLevel = resolveMapping(parsed, mappingName)
    return firstLevel?.firstLevelMapping?.firstOrNull { it.name?.value == topLevelKey }
  }

  fun resolveSecondLevelMappingKey(parsed: CloudFormationParsedFile, mappingName: String, topLevelKey: String, secondLevelKey: String): CfnMappingValue? {
    val firstLevelNode = resolveFirstLevelMappingKey(parsed, mappingName, topLevelKey)
    return firstLevelNode?.secondLevelMapping?.firstOrNull { it.name?.value == secondLevelKey }
  }

  fun getTopLevelMappingKeys(parsed: CloudFormationParsedFile, mappingName: String): List<String>? {
    val mappingElement = resolveMapping(parsed, mappingName)
    return mappingElement?.firstLevelMapping?.mapNotNull { it.name?.value }
  }

  fun getSecondLevelMappingKeys(parsed: CloudFormationParsedFile, mappingName: String, topLevelKey: String): List<String>? {
    val topLevelKeyElement = resolveFirstLevelMappingKey(parsed, mappingName, topLevelKey)
    return topLevelKeyElement?.secondLevelMapping?.mapNotNull { it.name?.value }
  }
}
