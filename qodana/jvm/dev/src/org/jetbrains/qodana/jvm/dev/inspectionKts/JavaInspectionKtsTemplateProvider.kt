package org.jetbrains.qodana.jvm.dev.inspectionKts

import com.intellij.icons.AllIcons
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import org.jetbrains.qodana.inspectionKts.templates.standardLocalInspectionKtsTemplateContent
import org.jetbrains.qodana.inspectionKts.templates.templateInspectionKtsFilenameToInspectionName

class JavaInspectionKtsTemplateProvider : InspectionKtsTemplate.Provider {
  override fun template(): InspectionKtsTemplate {
    return InspectionKtsTemplate(
      uiDescriptor = InspectionKtsTemplate.UiDescriptor(
        name = QodanaBundle.message("inspectionkts.template.java.local.inspection"),
        icon = AllIcons.FileTypes.Java,
        id = "JAVA"
      ),
      ::templateJavaInspectionKts
    )
  }
}

private fun templateJavaInspectionKts(filename: String): String {
  val inspectionName = templateInspectionKtsFilenameToInspectionName(filename)

  @Language("kotlin")
  val imports = """
    import com.intellij.psi.PsiClass
    import com.intellij.psi.PsiLocalVariable
    import com.intellij.psi.PsiMethod
  """.trimIndent()

  @Language("kotlin")
  val topComment = """
    /**
     * This is an auto-generated template Java custom inspection
     * Reports all local variables inside all class methods
     *
     * The inspection is applied automatically and executed on-fly: to see the inspection results, open the Java file in the editor
     * 
     * In this example, the inspection algorithm is the following:
     *   1. Take all classes in file
     *   2. If the class is not an interface, take all its declared methods, otherwise ignore
     *   3. For each declared method, take all nodes that correspond to local variables and are descendants of method node 
     *   4. Reports variables' name and type
     */
  """.trimIndent()

  @Language("kotlin")
  val inspectionVisitorContent = """
    val everyLocalVariableInMethodInspection = localInspection { psiFile, inspection ->
        // take all classes declared in file: all PsiClass children of PsiFile node (root)
        val classes = psiFile.descendantsOfType<PsiClass>()
        
        classes.forEach { javaClass: PsiClass ->
            // ignore interfaces
            if (javaClass.isInterface) {
                return@forEach
            }
    
            // take all declared methods of a class
            javaClass.methods.forEach { method: PsiMethod ->
                // take all local variables inside method
                val localVariables = method.descendantsOfType<PsiLocalVariable>()
    
                // for each local variable, report its name and type as a problem
                localVariables.forEach { variable: PsiLocalVariable ->
                    val variableType = variable.type.canonicalText
                    val message = "This is a variable ${'$'}{variable.name} in method ${'$'}{method.name} of type ${'$'}variableType. $inspectionName"
                    inspection.registerProblem(variable, message)
                }
            }
        }
    }
  """.trimIndent()

  return standardLocalInspectionKtsTemplateContent(
    filename,
    imports = imports,
    topComment = topComment,
    inspectionVisitorContent = inspectionVisitorContent,
    inspectionToolVariableName = "everyLocalVariableInMethodInspection",
  )
}