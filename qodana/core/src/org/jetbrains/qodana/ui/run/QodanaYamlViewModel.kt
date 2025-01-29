package org.jetbrains.qodana.ui.run

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import java.nio.file.Path

interface QodanaYamlViewModel {
  class YamlState(
    val document: Document,
    val editor: Editor,
    val physicalFile: Path?,
  ) {
    val isPhysical: Boolean
      get() = physicalFile != null
  }

  sealed interface ParseResult {
    class Valid(val yamlConfig: QodanaYamlConfig, val parsedText: String? = null) : ParseResult

    class Error(val message: @NlsContexts.DialogMessage String) : ParseResult
  }

  val project: Project

  val yamlStateFlow: StateFlow<YamlState?>

  val yamlValidationErrorFlow: Flow<ParseResult.Error?>

  fun parseQodanaYaml(): Deferred<ParseResult?>

  fun writeQodanaYamlIfNeeded(): Deferred<Path?>
}