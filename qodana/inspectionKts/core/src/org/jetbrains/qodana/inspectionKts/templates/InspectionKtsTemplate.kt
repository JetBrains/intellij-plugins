package org.jetbrains.qodana.inspectionKts.templates

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.NlsContexts
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_DIRECTORY
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_EXTENSION
import java.util.*
import javax.swing.Icon

class InspectionKtsTemplate(
  val uiDescriptor: UiDescriptor,
  val templateContent: (filename: String) -> String,
) {
  class UiDescriptor(
    @NlsContexts.ListItem val name: String,
    val icon: Icon,
    val id: String,
    val weight: Int = 0
  )

  interface Provider {
    companion object {
      private val EP_NAME = ExtensionPointName<Provider>("org.intellij.qodana.inspectionKtsTemplateProvider")

      fun templates(): List<InspectionKtsTemplate> {
        return EP_NAME.extensionList.map { it.template() }
      }
    }

    fun template(): InspectionKtsTemplate
  }
}

fun templateInspectionKtsFilenameToInspectionName(filename: String): String {
  return "Template custom inspection $INSPECTIONS_KTS_DIRECTORY/$filename.$INSPECTIONS_KTS_EXTENSION"
}

private val NON_WORD_REGEX by lazy {
  Regex("\\W")
}

fun templateInspectionKtsFilenameToInspectionId(filename: String): String {
  return filename
    .split(NON_WORD_REGEX)
    .joinToString(separator = "") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
}

fun standardLocalInspectionKtsTemplateContent(
  filename: String,
  imports: String,
  topComment: String,
  htmlDescription: String = standardInspectionKtsHtmlDescription(),
  inspectionVisitorContent: String,
  inspectionToolVariableName: String,
  htmlDescriptionVariableName: String = "htmlDescription"
): String {
  val inspectionName = templateInspectionKtsFilenameToInspectionName(filename)
  val inspectionId = templateInspectionKtsFilenameToInspectionId(filename)

  @Language("kotlin")
  val content = """
import org.intellij.lang.annotations.Language
${imports.trim()}

${topComment.trim()}

${InspectionKtsTemplateKDoc.htmlDescription().trim()}
${htmlDescription.trim()}

${InspectionKtsTemplateKDoc.visitor().trim()}
${inspectionVisitorContent.trim()}

// You can define multiple inspections in one .inspection.kts file 
listOf(
    InspectionKts(
        id = "$inspectionId", ${InspectionKtsTemplateKDoc.inspectionId()}
        localTool = $inspectionToolVariableName,
        name = "$inspectionName", ${InspectionKtsTemplateKDoc.inspectionName()}
        htmlDescription = $htmlDescriptionVariableName,
        level = HighlightDisplayLevel.WARNING,
    )
    // ...
)
  """.trimIndent()

  return content
}

internal fun standardInspectionKtsHtmlDescription(): String {
  // red code is FP
  val tripleQuote = "\"\"\""
  @Language("kotlin")
  val content = """
    @Language("HTML")
    val htmlDescription = $tripleQuote
        <html>
        <body>
            HTML description of custom inspection
        </body>
        </html>
    ${tripleQuote}.trimIndent()
  """.trimIndent()
  return content
}

object InspectionKtsTemplateKDoc {
  fun htmlDescription(): String {
    @Language("JAVA")
    val content = """
    /**
     * Full HTML description of inspection: Describe here motivation, examples, etc.
     */
    """.trimIndent()
    return content
  }

  fun visitor(): String {
    @Language("kotlin")
    val content = """
    /**
     * Inspection operates with file's PSI tree: to see the PSI tree of a file, open the PSI Viewer 
     * PSI tree is an AST representing file's source code with PsiFile is a root node 
     * You can traverse the tree/call API methods of specific PsiElements to retrieve some data
    
     * You can use the following utility methods to traverse the PSI tree from given PsiElement:
     * * PsiElement.getChildren() – all children nodes
     * * PsiElement.descendants() – all children nodes recursively (with children's children)
     * * PsiElement.descendantsOfType<...>() – all children of specified type recursively
     * * PsiElement.getParent() – parent node 
     * * PsiElement.parents(withSelf = false) – all parent nodes recursively
     * * PsiElement.siblings(forward = true, withSelf = false) – all forward siblings: nodes with the same parent located after this element
     * * PsiElement.siblings(forward = false, withSelf = false) – all backward siblings: nodes with the same parent located before this element
     
     * Call `inspection.findPsiFileByRelativeToProjectPath(String)` to get other PSI file 
     * Call `inspection.registerProblem(PsiElement, String)` function to report a problem from inspection
     * 
     * See the PSI Viewer for available APIs and PSI tree structure
     * Invoke "Open PSI Viewer" in the banner above or select "Tools | View PSI structure of Current File..." from the top menu
     * 
     * How to debug the inspection: call inspection.registerProblem(PsiElement, "your debug message") and see highlighting in the editor, also check PSI viewer
     */
     """.trimIndent()
    return content
  }

  fun inspectionId(): String {
    return "// inspection id (used in qodana.yaml)"
  }

  fun inspectionName(): String {
    return "// Inspection name, displayed in UI"
  }
}