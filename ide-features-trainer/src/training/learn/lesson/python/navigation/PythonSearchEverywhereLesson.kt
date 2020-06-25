// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.navigation

import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.testGuiFramework.util.Modifier
import com.intellij.testGuiFramework.util.Shortcut
import com.intellij.ui.components.fields.ExtendableTextField
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.ui.LearningUiHighlightingManager
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

class PythonSearchEverywhereLesson(module: Module) : KLesson("Search Everywhere", module, "Python") {
  override val lessonContent: LessonContext.() -> Unit = {
    caret(0)

    val shift = KeyEvent.getKeyModifiersText(InputEvent.SHIFT_MASK)
    actionTask("SearchEverywhere") {
      "To open <strong>Search Everywhere</strong> you need to press <shortcut>$shift</shortcut> two times in a row."
    }
    task("cae") {
      text("Suppose you are looking for a class with ${code("cache")} and ${code("extension")} words in the name. " +
           "Type ${code(it)} (prefixes of required words) to the search field.")
      stateCheck { checkWordInSearch(it) }
      test { type(it) }
    }

    actionTask("QuickImplementations") {
      "We found ${code("FragmentCacheExtension")}. " +
      "Now you can preview the found item. Just select it by arrows (or mouse single-click) and press ${action(it)}."
    }

    task {
      text("Press <strong>Enter</strong> to navigate to ${code("FragmentCacheExtension")}.")
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
      "Use ${action(it)} to find class faster or in special places."
    }

    task("nodevisitor") {
      text("Suppose you need some library class responsible for visiting nodes. Type ${code(it)}.")
      stateCheck { checkWordInSearch(it) }
      test { type(it) }
    }

    task {
      triggerByUiComponentAndHighlight { _: ActionButtonWithText -> true }
    }

    task("All Places") {
      text("Now you see a class inside this demo project. " +
           "Lets Switch <strong>Project Files</strong> filter to <strong>$it</strong> and you will see available library variants.")
      stateCheck {
        (previous.ui as? ActionButtonWithText)?.accessibleContext?.accessibleName == it
      }
      test {
        GuiTestUtil.shortcut(Shortcut(HashSet(setOf(Modifier.ALT)), Key.P))
      }
    }

    actionTask("QuickJavaDoc") {
      LearningUiHighlightingManager.clearHighlights()
      "Use ${action(it)} to quickly look at available documentation."
    }

    task {
      text("<strong>Done!</strong> In the same way you can use ${action("GotoSymbol")} to look for a method or global variable " +
           "and use ${action("GotoFile")} to look for a file.")
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
