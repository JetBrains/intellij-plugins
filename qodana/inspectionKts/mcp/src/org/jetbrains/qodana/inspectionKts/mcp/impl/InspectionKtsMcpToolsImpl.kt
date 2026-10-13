@file:Suppress("RedundantSuspendModifier")

package org.jetbrains.qodana.inspectionKts.mcp.impl

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.diagnostic.rethrowControlFlowException
import com.intellij.mcpserver.util.resolveInProject
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.elementType
import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.inspectionKts.core.KtsInspectionsManager
import org.jetbrains.qodana.inspectionKts.core.runInspectionOnPsiFile
import org.jetbrains.qodana.inspectionKts.examples.InspectionKtsExample
import org.jetbrains.qodana.inspectionKts.fileFactory.CustomPsiFileFactory
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsBatchRunResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsCompileResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsExampleRequest
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsFileResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsProjectRunResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsRunResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionProblem
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import java.io.IOException
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration

private val ANY_LANGUAGE_TEMPLATES = setOf(
  "ANY_LANGUAGE_GLOBAL",
  "ANY_LANGUAGE",
)

private val IGNORED_EXAMPLES = setOf(
  "JSON and YAML"
)

internal suspend fun generatePsiTreeImpl(project: Project, code: String, language: String): String {
  val fileExtension = when (language.lowercase()) {
    "java" -> "java"
    "kotlin", "kt" -> "kt"
    else -> return "Error: Unsupported language '$language'. Supported: Java, Kotlin"
  }

  val psiFile = readAction {
    val fileType = FileTypeRegistry.getInstance().getFileTypeByExtension(fileExtension)
    PsiFileFactory.getInstance(project).createFileFromText("Placeholder.$fileExtension", fileType, code)
  }

  return generatePsiTreeText(psiFile)
}

internal suspend fun generateInspectionKtsExamplesImpl(language: String, includeAdditionalExamples: Boolean): String {
  val languageUpper = language.uppercase()
  val languageTemplates = ANY_LANGUAGE_TEMPLATES + languageUpper

  val templateBlocks = InspectionKtsTemplate.Provider.templates()
    .filter { it.uiDescriptor.id in languageTemplates }
    .map { it.templateContent("AnExampleFileName.ReplaceMe") }
    .joinToString(separator = "\n") { content ->
      """
      <Example>
      $content
      </Example>
      """.trimIndent()
    }

  val additionalBlocks = if (includeAdditionalExamples) {
    InspectionKtsExample.Provider
      .examples()
      .filter { it.text !in IGNORED_EXAMPLES }
      .map {
        val content = it.resourceUrl.readText()
        val comment = "// Only Code of localInspection { ... }\n"
        comment + content
      }
      .joinToString("\n") { content ->
        """
        <Example>
        $content
        </Example>
        """.trimIndent()
      }
  }
  else ""

  return buildString {
    appendLine("<Examples>")
    appendLine(templateBlocks)
    if (additionalBlocks.isNotBlank()) {
      appendLine(additionalBlocks)
    }
    append("</Examples>")
  }
}

internal fun generateInspectionKtsApiImpl(language: String, wrapInTags: Boolean): String {
  val langName = when (language.lowercase()) {
    "java" -> "Java"
    "kotlin", "kt" -> "Kotlin"
    else -> return "Error: Unsupported language '$language'. Supported: Java, Kotlin"
  }

  val resourcePath = "apiClasses/classes$langName.txt"
  val classes = object {}.javaClass.classLoader.getResource(resourcePath)?.readText()
                ?: return "Error: Cannot find API documentation for $langName at $resourcePath"

  return if (wrapInTags) {
    """
    <API>
    <api.kt>
    $classes
    </api.kt>
    </API>
    """.trimIndent()
  }
  else {
    classes
  }
}

internal suspend fun runInspectionKtsImpl(
  project: Project,
  inspectionKtsCode: String,
  contextPath: String,
  targetFileContent: String?,
): InspectionKtsRunResult {
  return when (val compilation = compileInspection(project, inspectionKtsCode)) {
    is Compilation.Failed -> compilation.result.toRunResult()
    is Compilation.Ready -> {
      val filePath = project.resolveInProject(contextPath, true)
      val psiFile = CustomPsiFileFactory.createOrFindPsiFile(project, filePath, targetFileContent)
                    ?: return InspectionKtsRunResult(
                      compilationSuccess = true,
                      inspectionResultMessage = "No PSI file found for $contextPath"
                    )
      val problems = inspectFile(compilation.tool, psiFile)
      InspectionKtsRunResult(
        compilationSuccess = true,
        inspectionResultMessage = if (problems.isEmpty()) "Inspection found no problems" else "Inspection found ${problems.size} problems",
        foundProblems = problems,
      )
    }
  }
}

internal suspend fun compileInspectionKtsImpl(project: Project, inspectionKtsCode: String): InspectionKtsCompileResult =
  when (val compilation = compileInspection(project, inspectionKtsCode)) {
    is Compilation.Failed -> compilation.result
    is Compilation.Ready -> compilation.result
  }

internal suspend fun runInspectionKtsExamplesImpl(
  project: Project,
  inspectionKtsCode: String,
  examples: List<InspectionKtsExampleRequest>,
): InspectionKtsBatchRunResult = when (val compilation = compileInspection(project, inspectionKtsCode)) {
  is Compilation.Failed -> InspectionKtsBatchRunResult(compilation.result)
  is Compilation.Ready -> InspectionKtsBatchRunResult(
    compilation = compilation.result,
    files = examples.map { example -> inspectExample(project, compilation, example) },
  )
}

@OptIn(ExperimentalPathApi::class)
internal suspend fun runInspectionKtsProjectImpl(
  project: Project,
  inspectionKtsCode: String,
  timeout: Duration,
): InspectionKtsProjectRunResult = when (val compilation = compileInspection(project, inspectionKtsCode)) {
  is Compilation.Failed -> InspectionKtsProjectRunResult(compilation.result)
  is Compilation.Ready -> {
    val projectRoot = project.basePath?.let(Path::of)?.toAbsolutePath()?.normalize()
                      ?: error("The opened project has no base path")
    val outputDirectory = createTempDirectory("mcp-inspection-analysis")
    try {
      val config = QodanaConfig.fromYaml(projectRoot, outputDirectory)
      val results = project.service<DynamicInspectionRunnerParallel>()
        .runSingle(compilation.tool, config, outputDirectory, timeout)
      InspectionKtsProjectRunResult(compilation.result, results.toFileResults(projectRoot))
    }
    catch (e: Exception) {
      rethrowControlFlowException(e)
      InspectionKtsProjectRunResult(
        compilation.result,
        listOf(InspectionKtsFileResult(path = projectRoot.toString(), executionError = e.message ?: e.javaClass.simpleName)),
      )
    }
    finally {
      try {
        outputDirectory.deleteRecursively()
      }
      catch (e: IOException) {
        fileLogger().warn("Cannot delete the MCP inspection output at $outputDirectory", e)
      }
    }
  }
}

private fun List<Result>.toFileResults(projectRoot: Path): List<InspectionKtsFileResult> =
  mapNotNull { result -> result.toLocatedProblem(projectRoot) }
    .groupBy({ it.path }, { it.problem })
    .toSortedMap()
    .map { (path, problems) -> InspectionKtsFileResult(path = path, foundProblems = problems) }

private fun Result.toLocatedProblem(projectRoot: Path): LocatedProblem? {
  val physicalLocation = locations.firstOrNull()?.physicalLocation ?: return null
  val path = physicalLocation.artifactLocation?.uri ?: return null
  val region = physicalLocation.region
  return LocatedProblem(
    path = projectRelativePath(projectRoot, path),
    problem = InspectionProblem(
      message = message?.text ?: "Inspection problem",
      lineNumber = region?.startLine ?: -1,
      highlightType = level?.toString()?.uppercase() ?: "WARNING",
      startOffset = null,
      endOffset = null,
      elementText = region?.snippet?.text?.take(100),
    ),
  )
}

private fun projectRelativePath(projectRoot: Path, path: String): String {
  return try {
    val normalized = Path.of(path).normalize()
    if (normalized.isAbsolute && normalized.startsWith(projectRoot)) projectRoot.relativize(normalized).toString()
    else normalized.toString()
  }
  catch (_: InvalidPathException) {
    path
  }
}

private data class LocatedProblem(val path: String, val problem: InspectionProblem)

private suspend fun inspectExample(
  project: Project,
  compilation: Compilation.Ready,
  example: InspectionKtsExampleRequest,
): InspectionKtsFileResult {
  val root = Path.of(example.projectPath).toAbsolutePath().normalize()
  val source = root.resolve(example.targetFilePath).normalize()
  if (!source.startsWith(root) || !source.isRegularFile()) {
    return InspectionKtsFileResult(example.id, source.toString(), executionError = "Target source file does not exist")
  }
  return try {
    val psiFile = CustomPsiFileFactory.createOrFindPsiFile(project, source, source.readText())
                  ?: return InspectionKtsFileResult(example.id, source.toString(), executionError = "No PSI file found")
    InspectionKtsFileResult(example.id, source.toString(), inspectFile(compilation.tool, psiFile))
  }
  catch (e: Exception) {
    InspectionKtsFileResult(example.id, source.toString(), executionError = e.message ?: e.javaClass.simpleName)
  }
}

private suspend fun inspectFile(
  tool: com.intellij.codeInspection.LocalInspectionTool,
  psiFile: com.intellij.psi.PsiFile,
): List<InspectionProblem> {
  val descriptors = runInspectionOnPsiFile(tool, psiFile)
  return readAction {
    descriptors.map { descriptor ->
      InspectionProblem(
        message = descriptor.descriptionTemplate,
        lineNumber = computeLineNumber(psiFile, descriptor.psiElement),
        highlightType = descriptor.highlightType.name,
        startOffset = descriptor.psiElement?.textRange?.startOffset,
        endOffset = descriptor.psiElement?.textRange?.endOffset,
        elementText = descriptor.psiElement?.text?.take(100),
      )
    }
  }
}

private suspend fun compileInspection(project: Project, code: String): Compilation {
  val tempFile = createTempFile("mcp-inspection", ".inspection.kts")
  return try {
    tempFile.writeText(code)
    when (val status = KtsInspectionsManager.getInstance(project).doCompileInspectionKtsFile(tempFile)) {
      is InspectionKtsFileStatus.Cancelled -> Compilation.Failed(
        InspectionKtsCompileResult(false, "Inspection compilation was cancelled")
      )
      is InspectionKtsFileStatus.Compiling -> Compilation.Failed(
        InspectionKtsCompileResult(false, "Inspection is still compiling (unexpected state)")
      )
      is InspectionKtsFileStatus.Error -> Compilation.Failed(
        InspectionKtsCompileResult(false, status.exception.message ?: "Unknown compilation error", status.exception.stackTraceToString())
      )
      is InspectionKtsFileStatus.Compiled -> {
        val inspections = status.inspections.inspections.filterIsInstance<DynamicInspectionDescriptor.Local>()
        if (inspections.size != 1) {
          Compilation.Failed(
            InspectionKtsCompileResult(false, "Expected exactly one local inspection, found ${inspections.size}")
          )
        }
        else {
          val tool = inspections.single().tool
          Compilation.Ready(
            tool,
            InspectionKtsCompileResult(
              compilationSuccess = true,
              inspectionId = tool.shortName,
              inspectionName = tool.displayName,
              inspectionDescription = tool.staticDescription,
            ),
          )
        }
      }
    }
  }
  finally {
    tempFile.deleteIfExists()
  }
}

private sealed interface Compilation {
  data class Ready(
    val tool: com.intellij.codeInspection.LocalInspectionTool,
    val result: InspectionKtsCompileResult,
  ) : Compilation

  data class Failed(val result: InspectionKtsCompileResult) : Compilation
}

private fun InspectionKtsCompileResult.toRunResult(): InspectionKtsRunResult = InspectionKtsRunResult(
  compilationSuccess = compilationSuccess,
  compilationStatus = compilationStatus,
  compilationErrorDetails = compilationErrorDetails,
)

private suspend fun generatePsiTreeText(element: PsiElement, level: Int = 0): String {
  return readAction {
    val nodeType = getNodeTypeDescription(element) ?: return@readAction ""
    val childrenInfo = getChildrenInfo(element)
    val indentation = "  ".repeat(level)

    val currentLine = "$indentation$nodeType$childrenInfo\n"

    buildString {
      append(currentLine)

      val children = element.children.toList()
      for (child in children) {
        append(generatePsiTreeTextSync(child, level + 1))
      }
    }
  }
}

private fun generatePsiTreeTextSync(element: PsiElement, level: Int): String {
  val nodeType = getNodeTypeDescription(element) ?: return ""
  val childrenInfo = getChildrenInfo(element)
  val indentation = "  ".repeat(level)

  val currentLine = "$indentation$nodeType$childrenInfo\n"

  return buildString {
    append(currentLine)

    val children = element.children.toList()
    for (child in children) {
      append(generatePsiTreeTextSync(child, level + 1))
    }
  }
}

private fun getNodeTypeDescription(element: PsiElement): String? {
  return when {
    element.javaClass.simpleName == "LeafPsiElement" -> {
      val elementType = element.elementType
      "${elementType?.javaClass?.simpleName}($elementType)"
    }
    else -> element.javaClass.simpleName
  }
}

private fun getChildrenInfo(element: PsiElement): String {
  val hasChildren = element.children.isNotEmpty()
  val hasDirectChildren = element.firstChild != null

  return if (!hasDirectChildren && hasChildren) {
    " -> children retrieved with node.children()"
  }
  else {
    ""
  }
}

private fun computeLineNumber(psiFile: PsiElement, element: PsiElement?): Int {
  if (element == null) return -1
  val doc = psiFile.containingFile?.viewProvider?.document ?: return -1
  val start = element.textRange?.startOffset ?: return -1
  val zeroBased = doc.getLineNumber(start)
  return zeroBased + 1
}
