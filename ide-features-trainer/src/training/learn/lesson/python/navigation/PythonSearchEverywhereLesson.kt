// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.navigation

import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.search.EverythingGlobalScope
import com.intellij.psi.search.ProjectScope
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.testGuiFramework.util.Modifier
import com.intellij.testGuiFramework.util.Shortcut
import com.intellij.ui.components.fields.ExtendableTextField
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil
import training.ui.LearningUiHighlightingManager
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

class PythonSearchEverywhereLesson(module: Module)
  : KLesson("Search everywhere", LessonsBundle.message("python.search.everywhere.lesson.name"), module, "Python") {
  override val lessonContent: LessonContext.() -> Unit = {
    caret(0)

    val shift = KeyEvent.getKeyModifiersText(InputEvent.SHIFT_MASK)
    actionTask("SearchEverywhere") {
      val shortcut = "<shortcut>$shift</shortcut>"
      LessonsBundle.message("python.search.everywhere.invoke.search.everywhere", LessonUtil.actionName(it), shortcut)
    }
    task("cae") {
      text(LessonsBundle.message("python.search.everywhere.type.prefixes", code("cache"), code("extension"), code(it)))
      stateCheck { checkWordInSearch(it) }
      test {
        Thread.sleep(500)
        type(it)
      }
    }

    actionTask("QuickImplementations") {
      test {
        //Filter just one item
        GuiTestUtil.shortcut(Key.HOME)
        type("f")
        Thread.sleep(500)
      }
      LessonsBundle.message("python.search.everywhere.preview", code("FragmentCacheExtension"), action(it))
    }

    task {
      text(LessonsBundle.message("python.search.everywhere.navigate.to.class", LessonUtil.rawEnter(), code("FragmentCacheExtension")))
      stateCheck {
        FileEditorManager.getInstance(project).selectedEditor?.file?.name.equals("cache_extension.py")
      }
      test {
        GuiTestUtil.shortcut(Key.DOWN)
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }

    actionTask("GotoClass") {
      // Try to add (class tab in <strong>Search Everywhere</strong>)
      LessonsBundle.message("python.search.everywhere.goto.class", action(it))
    }

    task("nodevisitor") {
      text(LessonsBundle.message("python.search.everywhere.type.node.visitor", code(it)))
      stateCheck { checkWordInSearch(it) }
      test { type(it) }
    }

    task {
      triggerByUiComponentAndHighlight { _: ActionButtonWithText -> true }
    }

    task(EverythingGlobalScope.getNameText()) {
      text(LessonsBundle.message("python.search.everywhere.use.all.places",
                                 strong(ProjectScope.getProjectFilesScopeName()), strong(it)))
      stateCheck {
        (previous.ui as? ActionButtonWithText)?.accessibleContext?.accessibleName == it
      }
      test {
        GuiTestUtil.shortcut(Shortcut(HashSet(setOf(Modifier.ALT)), Key.P))
      }
    }

    actionTask("QuickJavaDoc") {
      LearningUiHighlightingManager.clearHighlights()
      LessonsBundle.message("python.search.everywhere.quick.documentation", action(it))
    }

    task {
      text(LessonsBundle.message("python.search.everywhere.finish", action("GotoSymbol"), action("GotoFile")))
    }

    if (TaskTestContext.inTestMode) task {
      stateCheck { focusOwner is EditorComponentImpl }
      test {
        GuiTestUtil.shortcut(Key.ESCAPE)
        GuiTestUtil.shortcut(Key.ESCAPE)
      }
    }
  }

  private fun TaskRuntimeContext.checkWordInSearch(expected: String): Boolean =
    (focusOwner as? ExtendableTextField)?.text?.toLowerCase() == expected.toLowerCase()

  override val existedFile = "src/jinja2/__init__.py"
}
