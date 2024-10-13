package org.jetbrains.qodana.inspectionKts.kotlin

import icons.KotlinBaseResourcesIcons
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import org.jetbrains.qodana.inspectionKts.templates.standardLocalInspectionKtsTemplateContent
import org.jetbrains.qodana.inspectionKts.templates.templateInspectionKtsFilenameToInspectionName

private class KotlinInspectionKtsTemplateProvider : InspectionKtsTemplate.Provider {
  companion object {
    const val ID = "KOTLIN"
  }

  override fun template(): InspectionKtsTemplate {
    return InspectionKtsTemplate(
      uiDescriptor = InspectionKtsTemplate.UiDescriptor(
        name = QodanaBundle.message("inspectionkts.template.kotlin.local.inspection"),
        icon = KotlinBaseResourcesIcons.Kotlin,
        id = ID
      ),
      ::templateKotlinInspectionKts
    )
  }
}

private fun templateKotlinInspectionKts(filename: String): String {
  val inspectionName = templateInspectionKtsFilenameToInspectionName(filename)

  @Language("kotlin")
  val imports = """
    import org.jetbrains.kotlin.psi.KtClass
    import org.jetbrains.kotlin.psi.KtNamedFunction
    import org.jetbrains.kotlin.psi.KtProperty
  """.trimIndent()

  @Language("kotlin")
  val topComment = """
    /**
     * This is an auto-generated template Kotlin custom inspection
     * Reports all local variables inside all functions
     * Ignores methods of classes inheriting from class with some FQN
     *
     * The inspection is applied automatically and executed on-fly: to see the inspection results, open the Kotlin file in the editor
     */
  """.trimIndent()

  @Language("kotlin")
  val inspectionVisitorContent = """
    val variableInFunctionInspection = localInspection { psiFile, inspection ->
        // skip kts files
        val isKts = psiFile.name.endsWith(".kts")
        if (isKts) return@localInspection

        // get all functions in not ignored classes
        val functions = psiFile.descendantsOfType<KtNamedFunction>()
            .filter { function -> !isInIgnoredClass(function) }

        functions.forEach { function: KtNamedFunction ->
            // get all variables declared in function
            val variables = function.descendantsOfType<KtProperty>()
            variables.forEach { variable: KtProperty ->
                // get variable's type FQN
                val variableTypeFqn = analyze(variable) {
                    variable.getReturnKtType().expandedClassSymbol?.getFQN() ?: return@forEach
                }
                val declarationText = if (variable.isVar) "var" else "val"
                val message = "This is a variable ${'$'}{variable.name} in function ${'$'}{function.name} of type ${'$'}variableTypeFqn declared as ${'$'}declarationText. $inspectionName"
                inspection.registerProblem(variable, message)
            }
        }
    }

    // skip function in classes which inherit from class with some FQN
    fun isInIgnoredClass(function: KtNamedFunction): Boolean {
        if (function.isTopLevel) return false

        val fqnToIgnore = "ignored.fully.qualified.name"
        val containingClass = function.parent?.parent as? KtClass ?: return false
        analyze(containingClass) {
            // check this class
            val classSymbol = containingClass.getClassOrObjectSymbol() ?: return false
            if (classSymbol.getFQN() == fqnToIgnore) {
                return true
            }

            // check supers
            return classSymbol.superTypes.any { superType ->
                val superClassSymbol = superType.expandedClassSymbol ?: return@any false
                superClassSymbol.getFQN() == fqnToIgnore
            }
        }
    }
  """.trimIndent()

  return standardLocalInspectionKtsTemplateContent(
    filename,
    imports = imports,
    topComment = topComment,
    inspectionVisitorContent = inspectionVisitorContent,
    inspectionToolVariableName = "variableInFunctionInspection",
  )
}