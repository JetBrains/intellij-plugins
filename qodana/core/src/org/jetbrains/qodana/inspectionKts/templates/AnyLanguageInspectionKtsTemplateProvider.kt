package org.jetbrains.qodana.inspectionKts.templates

import com.intellij.icons.AllIcons
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle

private class AnyLanguageInspectionKtsTemplateProvider : InspectionKtsTemplate.Provider {
  override fun template() = InspectionKtsTemplate(
    InspectionKtsTemplate.UiDescriptor(
      name = QodanaBundle.message("inspectionkts.template.any.language.local.inspection"),
      icon = AllIcons.FileTypes.Unknown,
      id = "ANY_LANGUAGE",
      weight = -10
    ),
    ::templateAnyLanguageInspectionKts
  )
}

private class AnyLanguageGlobalInspectionKtsTemplateProvider : InspectionKtsTemplate.Provider {
  override fun template() = InspectionKtsTemplate(
    InspectionKtsTemplate.UiDescriptor(
      name = QodanaBundle.message("inspectionkts.template.any.language.global.inspection"),
      icon = AllIcons.FileTypes.Unknown,
      id = "ANY_LANGUAGE_GLOBAL",
      weight = -5
    ),
    ::templateEmptyGlobalInspectionKts
  )
}

private fun templateAnyLanguageInspectionKts(filename: String): String {
  @Language("kotlin")
  val imports = "import com.intellij.psi.*"

  @Language("kotlin")
  val topComment = """
    /**
     * This is an auto-generated template of custom inspection
     * It doesn't report anything, you need to implement your casts and additional logic in [localInspection]
     * and call [inspection.registerProblem] to report result from inspection
     *
     * The inspection is applied automatically and executed on-fly: to see the inspection results, open the file in the editor
     */
  """.trimIndent()

  @Language("kotlin")
  val inspectionVisitorContent = """
    val anyLanguageInspectionTemplate = localInspection { psiFile, inspection ->
        // obtain here some concrete children nodes of PsiFile and implement your inspection logic, examples:
        // val javaClasses = psiFile.descendantsOfType<PsiClass>
        // val javaMethods = psiFile.descendantsOfType<PsiMethods> 
        // val jsVariables = psiFile.descendantsOfType<JSVariable>
        
        // call inspection.registerProblem(element, "message") to report a problem from inspection
    }
   """.trimIndent()

  return standardLocalInspectionKtsTemplateContent(
    filename,
    imports = imports,
    topComment = topComment,
    inspectionVisitorContent = inspectionVisitorContent,
    inspectionToolVariableName = "anyLanguageInspectionTemplate",
  )
}

private fun templateEmptyGlobalInspectionKts(filename: String): String {
  val inspectionName = templateInspectionKtsFilenameToInspectionName(filename)
  val inspectionId = templateInspectionKtsFilenameToInspectionId(filename)

  @Language("kotlin")
  val imports = "import com.intellij.psi.*"

  @Language("kotlin")
  val topComment = """
    /**
     * This is an auto-generated template of custom global inspection
     * It doesn't report anything, you need to implement your logic in [globalInspection]
     * and call [inspection.registerProblem] to report result from inspection
     *
     * The global inspections are executed ONLY in full project analysis: in Qodana Analysis or Inspect Code...
     * 
     * How to run Qodana Locally: https://www.jetbrains.com/help/qodana/quick-start.html#quickstart-run-in-ide 
     */
  """.trimIndent()

  @Language("kotlin")
  val inspectionVisitorContent = """
    val anyLanguageInspectionTemplate = globalInspection { inspection ->
        // obtain project files: (example)
        val aJava: PsiFile? = inspection.findPsiFileByRelativeToProjectPath("relative/to/project/path/A.java")
        val bJava: PsiFile? = inspection.findPsiFileByRelativeToProjectPath("relative/to/project/path/B.java")
    
        // obtain here some concrete nodes of PsiFile and implement your inspection logic, examples:
        // val javaClasses = aJava.descendantsOfType<PsiClass>
        // val javaMethods = aJava.descendantsOfType<PsiMethods>
        // val jsVariables = aJava.descendantsOfType<JSVariable>
        
        // call inspection.registerProblem(element, "message") to report a problem from inspection
        // or inspection.registerProblem("message") to report a project-wide problem
    }
  """.trimIndent()
  @Language("kotlin")
  val content = """
import org.intellij.lang.annotations.Language
${imports.trim()}

${topComment.trim()}

${InspectionKtsTemplateKDoc.htmlDescription().trim()}
${standardInspectionKtsHtmlDescription().trim()}

${InspectionKtsTemplateKDoc.visitor().trim()}
${inspectionVisitorContent.trim()}

// You can define multiple inspections in one .inspection.kts file 
listOf(
    InspectionKts(
        id = "$inspectionId", ${InspectionKtsTemplateKDoc.inspectionId()}
        globalTool = anyLanguageInspectionTemplate,
        name = "$inspectionName", ${InspectionKtsTemplateKDoc.inspectionName()}
        htmlDescription = htmlDescription,
        level = HighlightDisplayLevel.WARNING,
    )
    // ...
)
  """.trimIndent()

  return content
}