// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.navigation

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.components.fields.ExtendableTextField
import training.commands.kotlin.TaskRuntimeContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class RubyClassSearchLesson(module: Module) : KLesson("Class Search", module, "ruby") {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      caret(0)

      actionTask("GotoClass") {
        "Try to find a class with ${action(it)}"
      }
      task("date") {
        text("Type <code>$it</code> to see classes that contain the word <strong>$it</strong>.")
        stateCheck { checkWordInSearch(it) }
        test { type(it) }
      }
      task("datebe") {
        text("You can search for a class by part of its name. Type <code>be</code> (the search string will be <code>$it</code>) " +
             "to see classes that contain the words <strong>date</strong> and <strong>be</strong>.")
        stateCheck { checkWordInSearch(it) }
        test { type("be") }
      }
      task("QuickImplementations") {
        text("To check the selected class before navigating to it, you can use ${action(it)} to see its quick definition.")
        trigger(it)
        test { actions(it) }
      }
      task {
        text("Suppose you are looking for ${code("DateAndTimeBehavior")}." +
             "Choose it and then press <strong>Enter</strong> to navigate.")
        stateCheck {
          FileEditorManager.getInstance(project).selectedEditor?.file?.name.equals("date_and_time_behavior.rb")
        }
        test {
          GuiTestUtil.shortcut(Key.DOWN)
          GuiTestUtil.shortcut(Key.ENTER)
        }
      }
    }

  private fun TaskRuntimeContext.checkWordInSearch(expected: String): Boolean =
    (focusOwner as? ExtendableTextField)?.text?.toLowerCase() == expected.toLowerCase()

  override val existedFile: String
    get() = "lib/active_support.rb"
}
