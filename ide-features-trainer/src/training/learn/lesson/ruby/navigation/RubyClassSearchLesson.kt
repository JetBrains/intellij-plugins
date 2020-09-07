// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.navigation

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.components.fields.ExtendableTextField
import training.commands.kotlin.TaskRuntimeContext
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil

class RubyClassSearchLesson(module: Module)
  : KLesson("Class Search", LessonsBundle.message("ruby.class.search.lesson.name"), module, "ruby") {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      caret(0)

      actionTask("GotoClass") {
        LessonsBundle.message("ruby.class.search.invoke.goto.class", action(it))
      }
      task("date") {
        text(LessonsBundle.message("ruby.class.search.type.word", code(it), strong(it)))
        stateCheck { checkWordInSearch(it) }
        test { type(it) }
      }
      task("datebe") {
        text(LessonsBundle.message("ruby.class.search.type.second.prefix", code("be"), code(it), strong("date"), strong("be")))
        stateCheck { checkWordInSearch(it) }
        test { type("be") }
      }
      task("QuickImplementations") {
        text(LessonsBundle.message("ruby.class.search.preview", action(it)))
        trigger(it)
        test { actions(it) }
      }
      task {
        text(LessonsBundle.message("ruby.class.search.navigate.to.target", code("DateAndTimeBehavior"), LessonUtil.rawEnter()))
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
