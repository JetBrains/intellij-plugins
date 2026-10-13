@file:Suppress("RedundantSuspendModifier")

package org.jetbrains.qodana.inspectionKts.mcp.impl

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.mcpserver.util.resolveInProject
import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.inspectionKts.core.KtsInspectionsManager
import org.jetbrains.qodana.inspectionKts.examples.InspectionKtsExample
import org.jetbrains.qodana.inspectionKts.fileFactory.CustomPsiFileFactory
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsBatchRunResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsCompileResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsExampleRequest
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsFileResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsProjectRunResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsRunResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionProblem
import org.jetbrains.qodana.inspectionKts.core.runInspectionOnPsiFile
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

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

internal suspend fun runInspectionKtsProjectImpl(
  project: Project,
  inspectionKtsCode: String,
): InspectionKtsProjectRunResult = when (val compilation = compileInspection(project, inspectionKtsCode)) {
  is Compilation.Failed -> InspectionKtsProjectRunResult(compilation.result)
  is Compilation.Ready -> {
    val projectRoot = project.basePath?.let(Path::of)?.toAbsolutePath()?.normalize()
                      ?: error("The opened project has no base path")
    val files = readAction {
      buildList {
        ProjectRootManager.getInstance(project).fileIndex.iterateContent { file ->
          if (!file.isDirectory && file.extension?.lowercase() in SOURCE_EXTENSIONS) add(file)
          true
        }
      }
    }
    val results = files.sortedBy { it.path }.mapNotNull { file ->
      val psiFile = readAction { PsiManager.getInstance(project).findFile(file) } ?: return@mapNotNull null
      val path = runCatching { projectRoot.relativize(Path.of(file.path)).toString() }.getOrDefault(file.path)
      try {
        InspectionKtsFileResult(path = path, foundProblems = inspectFile(compilation.tool, psiFile))
      }
      catch (e: Exception) {
        InspectionKtsFileResult(path = path, executionError = e.message ?: e.javaClass.simpleName)
      }
    }
    InspectionKtsProjectRunResult(compilation.result, results)
  }
}

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

private val SOURCE_EXTENSIONS = setOf("java", "kt")


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
