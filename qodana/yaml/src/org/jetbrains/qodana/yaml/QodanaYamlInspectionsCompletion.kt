package org.jetbrains.qodana.yaml

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence


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
      QODANA_PROFILE_GROUP_INSPECTIONS, QODANA_PROFILE_INSPECTION -> {
        getAllInspectionsAsync(file.project).map(::InspectionLookupElement)
      }
      QODANA_PROFILE_GROUP_GROUPS, QODANA_PROFILE_INSPECTION_GROUP -> {
        basicGroups() + declaredGroups(file) + categoryGroups(getAllInspectionsAsync(file.project))
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

  private fun basicGroups(): List<QodanaLookupElement> = listOf(
    QodanaLookupElement("ALL", QodanaBundle.message("qodana.yaml.completion.group.all")),
    QodanaLookupElement("GLOBAL", QodanaBundle.message("qodana.yaml.completion.group.global")),
    QodanaLookupElement("LOCAL", QodanaBundle.message("qodana.yaml.completion.group.local")),
  )

  private fun declaredGroups(file: YAMLFile): List<QodanaLookupElement> {
    val groups = YAMLUtil.getQualifiedKeyInFile(file, "profile", "groups")?.value as? YAMLSequence
    return groups?.items.orEmpty()
      .mapNotNull { (it.value as? YAMLMapping)?.getKeyValueByKey("groupId")?.valueText }
      .filter { it.isNotBlank() }
      .distinct()
      .map { QodanaLookupElement(it, QodanaBundle.message("qodana.yaml.completion.group.declared")) }
  }

  private fun categoryGroups(inspections: List<InspectionToolWrapper<*, *>>): List<QodanaLookupElement> {
    val inspectionCounts = mutableMapOf<String, Int>()
    for (inspection in inspections) {
      val path = inspection.groupPath.map { it.replace("/", "_") }
      for (depth in 1..path.size) {
        val categoryId = path.subList(0, depth).joinToString("/")
        inspectionCounts.merge(categoryId, 1, Int::plus)
      }
    }
    return inspectionCounts.map { [categoryId, count] ->
      CategoryLookupElement(categoryId, QodanaBundle.message("qodana.yaml.completion.group.category", count))
    }
  }
}

private class CategoryLookupElement(categoryId: String, description: String) : QodanaLookupElement("category:$categoryId", description) {
  override fun handleInsert(context: InsertionContext) {
    val document = context.document
    val start = context.startOffset
    if (start > 0 && document.charsSequence[start - 1] in "\"'") return
    val tail = context.tailOffset
    document.insertString(tail, "\"")
    document.insertString(start, "\"")
    context.editor.caretModel.moveToOffset(tail + 2)
  }
}

internal class InspectionLookupElement(val tool: InspectionToolWrapper<*, *>, profileName: String? = null) : QodanaLookupElement(
  tool.shortName,
  if (profileName != null)
    "${tool.groupDisplayPath()} [profile: ${profileName}]"
  else
    tool.groupDisplayPath()
)