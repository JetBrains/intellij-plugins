package org.jetbrains.qodana.staticAnalysis.inspections.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.annotations.VisibleForTesting
import kotlinx.coroutines.runInterruptible
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.reflect.full.declaredMemberProperties

internal val QODANA_CONFIG_FILES: List<String> = listOf(QODANA_YAML_CONFIG_FILENAME, QODANA_YML_CONFIG_FILENAME)

// parsing
object QodanaYamlReader {
  private val parser by lazy {
    YAMLMapper().registerKotlinModule()
      .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  }

  @VisibleForTesting
  val rootProps: Set<String> by lazy {
    val roots = hashSetOf<String>()
    roots.add("dot-net") // deprecated .NET
    roots.add("dotnet") // current .NET, see Rider

    roots.add("ide") // qodana-cli
    roots.add("plugins") // qodana-cli
    roots.add("properties") // qodana-cli
    roots.add("linter") // qodana-cli

    roots.addAll(knownProps)

    roots
  }

  private val knownProps: Set<String> by lazy {
    QodanaYamlConfig::class.declaredMemberProperties.map { it.name }.toSet()
  }

  fun parse(@Language("yaml") yaml: String): Result<QodanaYamlConfig> =
    runCatching {
      val obj = when (val parsed = parser.readTree(yaml)) {
        null, is MissingNode -> parser.createObjectNode() // don't fail on empty input
        is ObjectNode -> parsed
        else -> throw QodanaException("Not a valid qodana.yaml configuration '$yaml'")
      }
      val unknown = mutableListOf<String>()
      val relevantFields = knownProps
      val filteredObject = obj.fieldNames()
        .asSequence()
        .onEach { if (it !in rootProps) unknown += it }
        .filter { it in relevantFields }
        .toList()
        .let(obj::retain)

      if (unknown.isNotEmpty()) throw QodanaException("Unexpected keys in qodana.yaml: $unknown")
      parser.treeToValue(filteredObject, QodanaYamlConfig::class.java)
    }

  @Deprecated(
    "THE LOADED CONFIGURATION IS INVALID: yaml path can be passed as CLI parameter. Also, yaml config must be patched with project path: withAbsoluteProfilePath. " +
    "To obtain config, use project.qodanaAnalysisConfig",
    level = DeprecationLevel.WARNING
  )
  suspend fun load(configPath: Path): Result<QodanaYamlConfig> {
    val text = runInterruptible(StaticAnalysisDispatchers.IO) { configPath.readText() }
    return parse(text)
  }

  fun defaultConfigPath(parentFolder: Path): Path? = QODANA_CONFIG_FILES.map(parentFolder::resolve).filter(Path::exists).firstOrNull()

  @Deprecated(
    "THE LOADED CONFIGURATION IS INVALID: yaml path can be passed as CLI parameter. Also, yaml config must be patched with project path: withAbsoluteProfilePath. " +
    "To obtain config, use project.qodanaAnalysisConfig",
    level = DeprecationLevel.WARNING
  )
  suspend fun loadDefaultConfig(parentFolder: Path): Result<QodanaYamlConfig>? {
    val path = defaultConfigPath(parentFolder) ?: return null
    return load(path)
  }
}
