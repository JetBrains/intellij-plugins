package training.learn.lesson

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jdom.Element
import training.commands.Command
import training.commands.CommandFactory
import training.commands.ExecutionList
import training.editor.actions.HideProjectTreeAction
import training.util.PerformActionUtil
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


/**
 * Created by karashevich on 30/01/15.
 */
object LessonProcessor {

  val LOG = Logger.getInstance(this.javaClass.canonicalName)

  @get:org.jetbrains.annotations.TestOnly
  var currentExecutionList: training.commands.ExecutionList? = null
    private set

  fun process(project: Project, lesson: XmlLesson, editor: Editor) {

    val myEditorParameters = LessonProcessor.getEditorParameters(lesson.scenario.root)
    val myQueueOfElements = LessonProcessor.createQueueOfCommands(lesson.scenario.root)

    //Initialize lesson in the editor
    LessonManager.getInstance(lesson).initLesson(editor)

    //Prepare environment before execution
    with(LessonProcessor) {
      prepareEnvironment(editor, project, myEditorParameters)
      currentExecutionList = ExecutionList(myQueueOfElements, lesson, project)
      CommandFactory.buildCommand(myQueueOfElements.peek()).execute(currentExecutionList!!)
    }
  }

  private fun createQueueOfCommands(root: Element): BlockingQueue<Element> {
    val commandsQueue = LinkedBlockingQueue<Element>()
    for (rootChild in root.children) {
      //if element is MouseBlocked (blocks all mouse events) than add all children inside it.
      if (LessonProcessor.isMouseBlock(rootChild)) {
        if (rootChild.children != null) {
          commandsQueue.add(rootChild) //add block element
          for (mouseBlockChild in rootChild.children) {
            if (LessonProcessor.isCaretBlock(mouseBlockChild)) {
              if (mouseBlockChild.children != null) {
                commandsQueue.add(mouseBlockChild) //add block element
                commandsQueue += mouseBlockChild.children
                commandsQueue += org.jdom.Element(Command.CommandType.CARETUNBLOCK.toString()) //add unblock element
              }
            }
            else {
              commandsQueue.add(mouseBlockChild) //add inner elements
            }
          }
          commandsQueue.add(Element(Command.CommandType.MOUSEUNBLOCK.toString())) //add unblock element
        }
      }
      else if (LessonProcessor.isCaretBlock(rootChild)) {
        if (rootChild.children != null) {
          commandsQueue.add(rootChild) //add block element
          commandsQueue += rootChild.children
          commandsQueue += Element(Command.CommandType.CARETUNBLOCK.toString()) //add unblock element
        }
      }
      else {
        commandsQueue.add(rootChild)
      }
    }
    return commandsQueue
  }


  private fun getEditorParameters(root: Element): HashMap<String, String> {
    val editorParameters = HashMap<String, String>()
    if (root.getAttribute(XmlLesson.EditorParameters.PROJECT_TREE) != null) {
      editorParameters.put(XmlLesson.EditorParameters.PROJECT_TREE, root.getAttributeValue(XmlLesson.EditorParameters.PROJECT_TREE))
    }
    return editorParameters
  }

  private fun prepareEnvironment(editor: Editor, project: Project, editorParameters: HashMap<String, String>) {
    if (editorParameters.containsKey(XmlLesson.EditorParameters.PROJECT_TREE)) {
      if (ActionManager.getInstance().getAction(HideProjectTreeAction.Companion.myActionId) == null) {
        val hideAction = HideProjectTreeAction()
        ActionManager.getInstance().registerAction(hideAction.actionId, hideAction)
      }
      PerformActionUtil.performAction(HideProjectTreeAction.Companion.myActionId, editor, project)
    }
  }

  private fun isMouseBlock(el: Element): Boolean {
    return el.name.toUpperCase() == Command.CommandType.MOUSEBLOCK.toString()
  }

  private fun isCaretBlock(el: Element): Boolean {
    return el.name.toUpperCase() == Command.CommandType.CARETBLOCK.toString()
  }

}
