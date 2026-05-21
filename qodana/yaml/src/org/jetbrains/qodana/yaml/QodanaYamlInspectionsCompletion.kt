package org.jetbrains.qodana.yaml

import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jetbrains.yaml.psi.YAMLFile


class QodanaYamlInspectionsCompletion : QodanaYamlCompletionContributorBase() {
  override suspend fun variantsForKey(key: String, file: YAMLFile, prefix: String): List<QodanaLookupElement> {
    val allInspectionsDefault = listOf(QodanaLookupElement("All", "All inspections"))

    return when (key) {
      QODANA_INSPECTION_INCLUDE_NAME, QODANA_INSPECTION_EXCLUDE_NAME -> {
        val allInspections = getAllInspectionsAsync(file.project)
        val model = QodanaInspectionsModel.fromYaml(file, allInspections)

        if (key == QODANA_INSPECTION_INCLUDE_NAME)
          allInspectionsDefault + allInspections.map(::InspectionLookupElement)
        else
          allInspectionsDefault +
          model.profileInspections.map { InspectionLookupElement(it, model.profileName) } +
          model.includedInspections.map(::InspectionLookupElement)
      }
      else -> emptyList()
    }
  }

  private suspend fun getAllInspectionsAsync(project: Project): List<InspectionToolWrapper<*, *>> {
    return withContext(Dispatchers.IO) {
      async {
        runInterruptible {
          getAllInspections(project)
        }
      }
    }.await()
  }
}

internal class InspectionLookupElement(val tool: InspectionToolWrapper<*, *>, profileName: String? = null) : QodanaLookupElement(
  tool.shortName,
  if (profileName != null)
    "${tool.groupDisplayPath()} [profile: ${profileName}]"
  else
    tool.groupDisplayPath()
)