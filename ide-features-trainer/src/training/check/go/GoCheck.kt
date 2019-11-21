package training.check.go

import com.goide.psi.GoFile
import com.goide.psi.GoFunctionDeclaration
import com.goide.psi.GoMethodDeclaration
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import training.check.Check

abstract class GoCheck : Check {

  protected lateinit var project: Project
  protected lateinit var editor: Editor

  override fun set(project: Project, editor: Editor) {
    this.project = project
    this.editor = editor
  }

  override fun before() {}

  protected val goFile: GoFile?
    get() {
      val document = editor.document
      val manager = PsiDocumentManager.getInstance(project)
      return manager.getPsiFile(document) as? GoFile
    }

  protected fun GoFile.findFunction(name: String): GoFunctionDeclaration? = functions.find { it.name == name }

  protected fun GoFile.findMethod(name: String): GoMethodDeclaration? = methods.find { it.name == name }

}

