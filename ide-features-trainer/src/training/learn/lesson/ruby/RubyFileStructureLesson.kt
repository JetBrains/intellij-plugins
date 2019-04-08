package training.learn.lesson.ruby

import com.intellij.ide.dnd.aware.DnDAwareTree
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.framework.Timeouts
import com.intellij.testGuiFramework.impl.waitUntilFound
import com.intellij.testGuiFramework.util.Key
import training.commands.kotlin.TaskContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import java.util.concurrent.CompletableFuture
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel

class RubyFileStructureLesson(module: Module) : KLesson("File structure", module, "ruby") {
  private val LOG = Logger.getInstance(this.javaClass)

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      caret(0)

      actionTask("FileStructurePopup") {
        "A large source file can be difficult to read and navigate, sometimes you only need an overview of the file." +
            "Use ${action(it)} to see the file structure."
      }
      task("ch") {
        val listener = StructureLessonListener(it)
        text("Type <code>$it</code> to see elements that contain the word <strong>$it</strong>.")
        listener.complete = stateCheck { checkWordInSearch(listener) }
        test {
          ideFrame {
            waitUntilFound(null, DnDAwareTree::class.java, Timeouts.seconds02) { popup ->
              popup.javaClass.name.contains("FileStructurePopup") && popup.isShowing
            }
          }
          type(it)
        }
      }
      task {
        text("Press <strong>Enter</strong> to jump to the selected item.")
        stateCheck { focusOwner is EditorComponentImpl }
        test { GuiTestUtil.shortcut(Key.ENTER) }
      }
      task("ActivateStructureToolWindow") {
        text("The IDE can also show you the file structure as a tool window. Open it with ${action(it)}.")
        stateCheck { focusOwner?.javaClass?.name?.contains("StructureViewComponent") ?: false }
        test { actions(it) }
      }
    }

  private fun TaskContext.checkWordInSearch(listener: StructureLessonListener): Boolean {
    val focusOwner = focusOwner
    if (focusOwner is DnDAwareTree && focusOwner.javaClass.name.contains("FileStructurePopup")) {
      val model = focusOwner.model
      if (listener.model != model) {
        listener.model = model
        model.addTreeModelListener(listener)
      }
    }
    return false
  }

  override val existedFile: String
    get() = "lib/active_support/core_ext/date/calculations.rb"

  private inner class StructureLessonListener(val expected: String) : TreeModelListener {
    @Volatile
    var model: TreeModel? = null
    @Volatile
    var complete: CompletableFuture<Boolean>? = null

    override fun treeNodesInserted(event: TreeModelEvent?) {
      checkCompleteness()
    }

    override fun treeStructureChanged(event: TreeModelEvent?) {
      checkCompleteness()
    }

    override fun treeNodesChanged(event: TreeModelEvent?) {
      checkCompleteness()
    }

    override fun treeNodesRemoved(event: TreeModelEvent?) {
      checkCompleteness()
    }

    private fun checkCompleteness() {
      try {
        model?.let {
          val filter = getLastFilter(it)
          if (expected == filter) {
            complete?.complete(true)
          }
        }
      } catch (e: NoSuchFieldException) {
        exceptionCase(e)
      } catch (e: SecurityException) {
        exceptionCase(e)
      }
    }

    private fun exceptionCase(e: Exception) {
      LOG.warn("${e.javaClass} : ${e.message}")
      complete?.complete(true)
    }
  }

  companion object {
    // A very dark magic implementation!
    private fun getLastFilter(model: TreeModel): String? {
      //m.model.structure.myFilter.myLastFilter
      val f = model.javaClass.getDeclaredField("model")
      f.isAccessible = true
      val modelField = f.get(model)
      val f2 = modelField.javaClass.getDeclaredField("structure")
      f2.isAccessible = true
      val structure = f2.get(modelField)
      val f3 = structure.javaClass.getDeclaredField("myFilter")
      f3.isAccessible = true
      val myFilter = f3.get(structure)
      val f4 = myFilter.javaClass.getDeclaredField("myLastFilter")
      f4.isAccessible = true
      val myLastFilter = f4.get(myFilter)
      return myLastFilter as String?
    }
  }
}
