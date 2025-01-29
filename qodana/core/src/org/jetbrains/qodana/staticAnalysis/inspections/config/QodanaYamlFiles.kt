package org.jetbrains.qodana.staticAnalysis.inspections.config

import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile

data class QodanaYamlFiles(
  val effectiveQodanaYaml: Path?,
  val localQodanaYaml: Path?,
  val qodanaConfigJson: Path?,
) {
  companion object {
    const val EFFECTIVE_QODANA_YAML_FILENAME: String = "effective.qodana.yaml"
    const val LOCAL_QODANA_YAML_FILENAME: String = "qodana.yaml"
    const val QODANA_CONFIG_JSON_FILENAME: String = "qodana-config.json"

    fun fromConfigDir(configDirectory: Path): QodanaYamlFiles {
      if (!configDirectory.exists()) {
        throw QodanaException("Config directory '${configDirectory}' must exist exist")
      }

      if (!configDirectory.isDirectory()) {
        throw QodanaException("Config directory '${configDirectory}' must be a directory")
      }

      return QodanaYamlFiles(
        effectiveQodanaYaml = configDirectory.resolve(EFFECTIVE_QODANA_YAML_FILENAME).existingOrNull()?.also { it.validate() },
        localQodanaYaml = configDirectory.resolve(LOCAL_QODANA_YAML_FILENAME).existingOrNull()?.also { it.validate() },
        qodanaConfigJson = configDirectory.resolve(QODANA_CONFIG_JSON_FILENAME).existingOrNull()?.also { it.validate() },
      )
    }

    fun noConfigDir(localQodanaYaml: Path): QodanaYamlFiles {
      localQodanaYaml.validate()

      return QodanaYamlFiles(
        effectiveQodanaYaml = localQodanaYaml,
        localQodanaYaml = localQodanaYaml,
        qodanaConfigJson = null
      )
    }

    fun noFiles(): QodanaYamlFiles {
      return QodanaYamlFiles(
        effectiveQodanaYaml = null,
        localQodanaYaml = null,
        qodanaConfigJson = null
      )
    }
  }
}

private fun Path.existingOrNull() = takeIf { it.exists() }

private fun Path.validate() {
  if (!this.exists()) {
    throw QodanaException("Configuration file '${this}' doesn't exist")
  }

  if (!this.isRegularFile()) {
    throw QodanaException("Configuration file '${this}' must be regular file")
  }

  if (!this.isReadable()) {
    throw QodanaException("Qodana doesn't have enough privileges to read configuration file '${this}'")
  }
}