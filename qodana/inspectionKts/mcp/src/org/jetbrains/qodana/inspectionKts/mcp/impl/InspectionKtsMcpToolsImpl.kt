@file:Suppress("RedundantSuspendModifier")

package org.jetbrains.qodana.inspectionKts.mcp.impl

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.mcpserver.util.resolveInProject
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.elementType
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.inspectionKts.KtsInspectionsManager
import org.jetbrains.qodana.inspectionKts.examples.InspectionKtsExample
import org.jetbrains.qodana.inspectionKts.fileFactory.CustomPsiFileFactory
import org.jetbrains.qodana.inspectionKts.mcp.InspectionKtsRunResult
import org.jetbrains.qodana.inspectionKts.mcp.InspectionProblem
import org.jetbrains.qodana.inspectionKts.runInspectionOnPsiFile
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import kotlin.io.path.createTempFile
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

  val psiFile = edtWriteAction {
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
  // Step 1: Compile the inspection.kts
  val tempFile = createTempFile("mcp-inspection", ".inspection.kts")
  tempFile.writeText(inspectionKtsCode)

  val compiledFile = KtsInspectionsManager.getInstance(project).doCompileInspectionKtsFile(tempFile)

  when (compiledFile) {
    is InspectionKtsFileStatus.Cancelled -> {
      return InspectionKtsRunResult(
        compilationSuccess = false,
        compilationStatus = "Inspection compilation was cancelled"
      )
    }
    is InspectionKtsFileStatus.Compiling -> {
      return InspectionKtsRunResult(
        compilationSuccess = false,
        compilationStatus = "Inspection is still compiling (unexpected state)"
      )
    }
    is InspectionKtsFileStatus.Error -> {
      return InspectionKtsRunResult(
        compilationSuccess = false,
        compilationStatus = compiledFile.exception.message ?: "Unknown compilation error",
        compilationErrorDetails = compiledFile.exception.stackTraceToString()
      )
    }
    is InspectionKtsFileStatus.Compiled -> {
      val inspection = compiledFile.inspections.inspections.firstOrNull()
      if (inspection == null) {
        return InspectionKtsRunResult(
          compilationSuccess = false,
          compilationStatus = "No inspection created after compilation"
        )
      }

      val localTool = (inspection as? DynamicInspectionDescriptor.Local)?.tool
      if (localTool == null) {
        return InspectionKtsRunResult(
          compilationSuccess = false,
          compilationStatus = "Compiled inspection is not a local inspection tool"
        )
      }


      val filePath = project.resolveInProject(contextPath, true)
      val psiFile = CustomPsiFileFactory.createOrFindPsiFile(project, filePath, targetFileContent)
                    ?: return InspectionKtsRunResult(
                      compilationSuccess = true,
                      inspectionResultMessage = "No PSI file found for $contextPath"
                    )
      val problemDescriptors = runInspectionOnPsiFile(localTool, psiFile)

      val problems = readAction {
        problemDescriptors.map { descriptor ->
          val lineNumber = computeLineNumber(psiFile, descriptor.psiElement)
          InspectionProblem(
            message = descriptor.descriptionTemplate,
            lineNumber = lineNumber,
            highlightType = descriptor.highlightType.name,
            startOffset = descriptor.psiElement?.textRange?.startOffset,
            endOffset = descriptor.psiElement?.textRange?.endOffset,
            elementText = descriptor.psiElement?.text?.take(100)
          )
        }
      }

      val inspectionResultMessage = if (problems.isEmpty()) "Inspection found no problems"
      else "Inspection found ${problems.size} problems"

      return InspectionKtsRunResult(
        compilationSuccess = true,
        inspectionResultMessage = inspectionResultMessage,
        foundProblems = problems
      )
    }
  }
}


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
