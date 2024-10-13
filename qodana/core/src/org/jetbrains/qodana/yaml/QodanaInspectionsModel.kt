package org.jetbrains.qodana.yaml

import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.psi.util.childrenOfType
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProjectInspectionProfileManager
import org.jetbrains.yaml.psi.*

internal typealias Inspection = InspectionToolWrapper<*, *>

class InspectionDescriptor(
  val tool: Inspection,
  val projectPath: String = "/"
)

internal class QodanaInspectionsModel(
  val profileName: String? = null,
  val profileInspections: List<Inspection> = emptyList(),
  private val includedInspectionDescriptors: List<InspectionDescriptor> = emptyList(),
  private val excludedInspectionDescriptors: List<InspectionDescriptor> = emptyList()
) {
  val includedInspections: List<Inspection> get() = includedInspectionDescriptors.map(InspectionDescriptor::tool)
  val excludedInspections: List<Inspection> get() = excludedInspectionDescriptors.map(InspectionDescriptor::tool)

  companion object {
    private val DEFAULT_PROFILE = System.getProperty("qodana.default.profile") ?: "qodana.recommended"

    fun fromYaml(file: YAMLFile, allInspections: List<Inspection>): QodanaInspectionsModel {
      val yamlDocument = file.documents.firstOrNull() ?: return QodanaInspectionsModel()
      val mapping = yamlDocument
                      .childrenOfType<YAMLMapping>()
                      .firstOrNull() ?: return QodanaInspectionsModel()


      val profileName = mapping
                          .getKeyValueByKey("profile")
                          ?.value
                          ?.childrenOfType<YAMLKeyValue>()
                          ?.firstOrNull()
                          ?.valueText
                        ?: DEFAULT_PROFILE

      val profileInspections: List<InspectionToolWrapper<*, *>> = QodanaProjectInspectionProfileManager
                                                                    .getInstance(file.project)
                                                                    .getQodanaProfile(profileName)
                                                                    ?.tools
                                                                    ?.filter { it.isEnabled }
                                                                    ?.mapNotNull { tool -> allInspections.find { it.shortName == tool.shortName } }
                                                                  ?: emptyList()

      fun findInspectionList(title: String): List<InspectionDescriptor> {
        return mapping
          .getKeyValueByKey(title)
          ?.let { it.value as? YAMLSequence }
          ?.childrenOfType<YAMLSequenceItem>()
          ?.flatMap { it.childrenOfType<YAMLMapping>() }
          ?.mapNotNull {
            val name = it.getKeyValueByKey("name")?.valueText ?: ""
            val path = it.getKeyValueByKey("path")?.valueText ?: "/"

            allInspections
              .find { insp -> insp.id == name }
              ?.let { inspection ->
                InspectionDescriptor(inspection, path)
              }
          }
          .orEmpty()
      }

      val includedInspections = findInspectionList("include")
      val excludedInspections = findInspectionList("exclude")

      return QodanaInspectionsModel(profileName, profileInspections, includedInspections, excludedInspections)
    }
  }
}