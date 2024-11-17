package org.jetbrains.qodana.inspectionKts.js

import com.intellij.icons.AllIcons
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import org.jetbrains.qodana.inspectionKts.templates.standardLocalInspectionKtsTemplateContent
import org.jetbrains.qodana.inspectionKts.templates.templateInspectionKtsFilenameToInspectionName

internal class JsInspectionKtsTemplateProvider : InspectionKtsTemplate.Provider {
  companion object {
    const val ID = "JAVASCRIPT"
  }

  override fun template(): InspectionKtsTemplate {
    return InspectionKtsTemplate(
      uiDescriptor = InspectionKtsTemplate.UiDescriptor(
        name = QodanaBundle.message("inspectionkts.template.js.local.inspection"),
        icon = AllIcons.FileTypes.JavaScript,
        id = ID
      ),
      ::templateJsInspectionKts
    )
  }
}

private fun templateJsInspectionKts(filename: String): String {
  val inspectionName = templateInspectionKtsFilenameToInspectionName(filename)

  @Language("kotlin")
  val imports = """
    import com.intellij.lang.javascript.psi.JSFunction
    import com.intellij.lang.javascript.psi.JSVariable
  """.trimIndent()

  @Language("kotlin")
  val topComment = """
    /**
     * This is an auto-generated template JavaScript custom inspection
     * Reports all local variables inside all functions
     *
     * The inspection is applied automatically and executed on-fly: to see the inspection results, open the JavaScript file in the editor
     */
  """.trimIndent()

  @Language("kotlin")
  val inspectionVisitorContent = """
    val everyVariableInFunctionInspection = localInspection { psiFile, inspection ->
        // take js functions in file
        val functions = psiFile.descendantsOfType<JSFunction>()
    
        functions.forEach { function: JSFunction ->
            // take all local variables inside the function
            val variables = function.descendantsOfType<JSVariable>()
            variables.forEach { variable: JSVariable ->
                // declaration type – const/val/var, if none – ignore
                val variableDeclarationKeyword = variable.statement?.varKeyword ?: return@forEach
                // variable type
                val variableType = variable.resolveJsType()
                val message = "This is a variable ${'$'}{variable.name} in function ${'$'}{function.name} of type ${'$'}variableType declared as ${'$'}variableDeclarationKeyword. ${inspectionName}"
                inspection.registerProblem(variable.nameIdentifier ?: variable, message)
            }
        }
    }
  """.trimIndent()

  return standardLocalInspectionKtsTemplateContent(
    filename,
    imports = imports,
    topComment = topComment,
    inspectionVisitorContent = inspectionVisitorContent,
    inspectionToolVariableName = "everyVariableInFunctionInspection",
  )
}