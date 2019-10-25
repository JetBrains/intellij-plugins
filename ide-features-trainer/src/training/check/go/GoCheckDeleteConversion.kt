package training.check.go

import com.goide.psi.GoFile
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import training.check.Check

class GoCheckDeleteConversion : Check {

  private lateinit var project: Project
  private lateinit var editor: Editor

  override fun set(project: Project, editor: Editor) {
    this.project = project
    this.editor = editor
  }

  override fun before() {}

  override fun check(): Boolean {
    val document = editor.document
    val manager = PsiDocumentManager.getInstance(project)
    val file = manager.getPsiFile(document) as? GoFile ?: return false
    val block = file.functions.find { it.name == "main" }?.block ?: return false
    return block.text == "{\n" +
            "\t_ = ioutil.WriteFile(\"./out.txt\", getData(), 0644)\n" +
            "}"
  }

}