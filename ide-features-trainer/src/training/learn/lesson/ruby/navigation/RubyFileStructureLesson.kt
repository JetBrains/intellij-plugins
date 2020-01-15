// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.navigation

import com.intellij.ide.dnd.aware.DnDAwareTree
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.speedSearch.SpeedSearchSupply
import training.commands.kotlin.TaskContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class RubyFileStructureLesson(module: Module) : KLesson("File structure", module, "ruby") {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      caret(0)

      actionTask("FileStructurePopup") {
        "A large source file can be difficult to read and navigate, sometimes you only need an overview of the file." +
        "Use ${action(it)} to see the file structure."
      }
      task("ch") {
        text("Type <code>$it</code> to see elements that contain the word <strong>$it</strong>.")
        stateCheck { checkWordInSearch(it) }
        test {
          ideFrame {
            waitComponent(DnDAwareTree::class.java, "FileStructurePopup")
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

  private fun TaskContext.checkWordInSearch(expected: String): Boolean {
    val focusOwner = focusOwner
    if (focusOwner is DnDAwareTree && focusOwner.javaClass.name.contains("FileStructurePopup")) {
      val supply = SpeedSearchSupply.getSupply(focusOwner)
      return supply?.enteredPrefix == expected
    }
    return false
  }

  override val existedFile: String
    get() = "lib/active_support/core_ext/date/calculations.rb"
}
