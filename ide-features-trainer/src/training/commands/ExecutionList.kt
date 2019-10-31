package training.commands

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.jdom.Element
import training.learn.interfaces.Lesson
import java.util.*

class ExecutionList(val elements: Queue<Element>, val lesson: Lesson, val project: Project) { val editor: Editor
    get() = FileEditorManager.getInstance(project).selectedTextEditor!!
}
