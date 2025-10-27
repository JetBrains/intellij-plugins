package org.jetbrains.qodana.staticAnalysis.inspections.config

import org.jetbrains.annotations.TestOnly

data class QodanaProfileYamlConfig(
  val name: String = "",
  val path: String = "",

  val base: BaseProfile = BaseProfile(),

  val groups: List<InspectionGroup> = emptyList(),
  val inspections: List<InspectionConfig> = emptyList()
) {
  data class BaseProfile(
    val name: String = "",
    val path: String = ""
  )

  data class InspectionGroup(
    val groupId: String = "Unknown",
    val inspections: List<String> = emptyList(),
    val groups: List<String> = emptyList()
  )

  data class InspectionConfig(
    val inspection: String? = null,
    val group: String? = null,
    val enabled: Boolean? = null,
    val severity: String? = null,
    val ignore: List<String> = emptyList(),
    val options: Map<String, String>? = null
  )
}

data class QodanaProfileConfig(
  val base: QodanaProfileYamlConfig.BaseProfile = QodanaProfileYamlConfig.BaseProfile(),

  val groups: List<QodanaProfileYamlConfig.InspectionGroup> = emptyList(),
  val inspections: List<QodanaProfileYamlConfig.InspectionConfig> = emptyList()
) {
  companion object {
    @TestOnly
    fun named(name: String): QodanaProfileConfig {
      return QodanaProfileConfig(
        base = QodanaProfileYamlConfig.BaseProfile(name),
        groups = emptyList(),
        inspections = emptyList()
      )
    }

    @TestOnly
    fun fromPath(path: String): QodanaProfileConfig {
      return QodanaProfileConfig(
        base = QodanaProfileYamlConfig.BaseProfile(path = path),
        groups = emptyList(),
        inspections = emptyList()
      )
    }

    fun fromYaml(yaml: QodanaProfileYamlConfig, profileNameFromCli: String, profilePathFromCli: String): QodanaProfileConfig {
      val profileName = when {
        profileNameFromCli.isNotBlank() -> profileNameFromCli
        yaml.name.isNotBlank() -> yaml.name
        yaml.base.name.isNotBlank() -> yaml.base.name
        else -> ""
      }
      val profilePath = when {
        profilePathFromCli.isNotBlank() -> profilePathFromCli
        yaml.path.isNotBlank() -> yaml.path
        yaml.base.path.isNotBlank() -> yaml.base.path
        else -> ""
      }
      val base = QodanaProfileYamlConfig.BaseProfile(name = profileName, path = profilePath)
      return QodanaProfileConfig(base, yaml.groups, yaml.inspections)
    }
  }
}