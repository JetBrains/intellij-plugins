@file:Suppress("FunctionName", "unused", "RedundantSuspendModifier")

package org.jetbrains.qodana.inspectionKts.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.Serializable
import org.jetbrains.qodana.inspectionKts.mcp.impl.generateInspectionKtsApiImpl
import org.jetbrains.qodana.inspectionKts.mcp.impl.generateInspectionKtsExamplesImpl
import org.jetbrains.qodana.inspectionKts.mcp.impl.generatePsiTreeImpl
import org.jetbrains.qodana.inspectionKts.mcp.impl.compileInspectionKtsImpl
import org.jetbrains.qodana.inspectionKts.mcp.impl.runInspectionKtsImpl
import org.jetbrains.qodana.inspectionKts.mcp.impl.runInspectionKtsExamplesImpl
import org.jetbrains.qodana.inspectionKts.mcp.impl.runInspectionKtsProjectImpl

/**
 * MCP Toolset for InspectionKts-related tools.
 * Provides tools for generating PSI trees, fetching inspection examples, and retrieving API documentation.
 */
class InspectionKtsMcpToolset : McpToolset {
  override fun displayName(): String = InspectionKtsMcpBundle.message("toolset.display.name.inspectionKts")

  override fun displayDescription(toolName: String): String? = InspectionKtsMcpBundle.message("tool.description.$toolName")

  @McpTool
  @McpDescription(
    """
    Creates a PSI tree for provided Java or Kotlin code and returns it as indented text.
    Use this tool to understand the PSI structure of code snippets when writing inspections.
    The output shows element types and their hierarchy, with hints about when node.children() is needed.
    """
  )
  suspend fun generate_psi_tree(
    @McpDescription("Source code snippet to parse")
    code: String,
    @McpDescription("Programming language: 'Java' or 'Kotlin'")
    language: String,
  ): String {
    val project = currentCoroutineContext().project
    return generatePsiTreeImpl(project, code, language)
  }

  @McpTool
  @McpDescription(
    """
    Returns example inspection.kts templates for the target language to guide code generation.
    Provides XML-wrapped examples showing how to write inspections using the InspectionKts API.
    """
  )
  suspend fun generate_inspection_kts_examples(
    @McpDescription("Target language for examples: 'Java', 'Kotlin', or 'Any' (default)")
    language: String = "Any",
    @McpDescription("If true, includes additional curated examples besides templates")
    includeAdditionalExamples: Boolean = true,
  ): String {
    return generateInspectionKtsExamplesImpl(language, includeAdditionalExamples)
  }

  @McpTool
  @McpDescription(
    """
    Returns the Inspection KTS API documentation for the target language.
    Provides available classes and functions that can be used when writing inspection.kts files.
    """
  )
  suspend fun generate_inspection_kts_api(
    @McpDescription("Target language: 'Java' or 'Kotlin'")
    language: String,
    @McpDescription("If true, wraps the API content in <API> and <api.kt> tags")
    wrapInTags: Boolean = true,
  ): String {
    return generateInspectionKtsApiImpl(language, wrapInTags)
  }

  @McpTool
  @McpDescription(
    """
    Compiles an inspection.kts script and runs it against a target file.
    Returns compilation errors if any, or the list of problems found by the inspection.
    Use this tool to test inspection.kts scripts during development.
    """
  )
  suspend fun run_inspection_kts(
    @McpDescription("The inspection.kts script content to compile and run")
    inspectionKtsCode: String,
    @McpDescription("Relative path of the target file inside project to analyze (e.g., 'src/my/package/Example.kt'")
    contextPath: String,
    @McpDescription("The content of the target file to analyze. If not provided, the file must exist in the project.")
    targetFileContent: String? = null,
  ): InspectionKtsRunResult {
    val project = currentCoroutineContext().project
    return runInspectionKtsImpl(project, inspectionKtsCode, contextPath, targetFileContent)
  }

  @McpTool
  @McpDescription("Compiles inspection.kts code and returns generic inspection metadata without executing it.")
  suspend fun compile_inspection_kts(
    @McpDescription("The inspection.kts script content to compile") inspectionKtsCode: String,
  ): InspectionKtsCompileResult {
    val project = currentCoroutineContext().project
    return compileInspectionKtsImpl(project, inspectionKtsCode)
  }

  @McpTool
  @McpDescription(
    "Compiles inspection.kts code once and executes it on the target source file in each supplied test project."
  )
  suspend fun run_inspection_kts_examples(
    @McpDescription("The inspection.kts script content to compile and execute") inspectionKtsCode: String,
    @McpDescription("Generic test-project paths and the source file to inspect in each project")
    examples: List<InspectionKtsExampleRequest>,
  ): InspectionKtsBatchRunResult {
    val project = currentCoroutineContext().project
    return runInspectionKtsExamplesImpl(project, inspectionKtsCode, examples)
  }

  @McpTool
  @McpDescription(
    "Compiles inspection.kts code and executes it on every Java and Kotlin source file in the currently opened project."
  )
  suspend fun run_inspection_kts_project(
    @McpDescription("The inspection.kts script content to compile and execute") inspectionKtsCode: String,
  ): InspectionKtsProjectRunResult {
    val project = currentCoroutineContext().project
    return runInspectionKtsProjectImpl(project, inspectionKtsCode)
  }
}

@Serializable
data class InspectionKtsCompileResult(
  val compilationSuccess: Boolean,
  val compilationStatus: String? = null,
  val compilationErrorDetails: String? = null,
  val inspectionId: String? = null,
  val inspectionName: String? = null,
  val inspectionDescription: String? = null,
)

@Serializable
data class InspectionKtsExampleRequest(
  val id: String,
  val projectPath: String,
  val targetFilePath: String,
)

@Serializable
data class InspectionKtsFileResult(
  val id: String? = null,
  val path: String,
  val foundProblems: List<InspectionProblem> = emptyList(),
  val executionError: String? = null,
)

@Serializable
data class InspectionKtsBatchRunResult(
  val compilation: InspectionKtsCompileResult,
  val files: List<InspectionKtsFileResult> = emptyList(),
)

@Serializable
data class InspectionKtsProjectRunResult(
  val compilation: InspectionKtsCompileResult,
  val files: List<InspectionKtsFileResult> = emptyList(),
)

@Serializable
data class InspectionKtsRunResult(
  val compilationSuccess: Boolean,
  val compilationStatus: String? = null,
  val compilationErrorDetails: String? = null,
  val inspectionResultMessage: String? = null,
  val foundProblems: List<InspectionProblem> = emptyList(),
)

@Serializable
data class InspectionProblem(
  val message: String,
  val lineNumber: Int,
  val highlightType: String,
  val startOffset: Int?,
  val endOffset: Int?,
  val elementText: String?,
)
