// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general.completion

import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.LessonUtil
import javax.swing.JList

abstract class BasicCompletionLessonBase(module: Module, lang: String) : KLesson("Basic Completion", module, lang) {
  protected abstract val sample1: LessonSample
  protected abstract val sample2: LessonSample

  protected abstract val item1StartToType: String

  protected abstract val item1CompletionPrefix: String
  protected open val item1CompletionSuffix: String = ""

  private val item1Completion: String
    get() = item1CompletionPrefix + item1CompletionSuffix

  protected abstract val item2Completion: String

  protected open val item2Inserted: String
    get() = item2Completion

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      val result1 = LessonUtil.insertIntoSample(sample1, item1Completion)
      prepareSample(sample1)
      task {
        text("By default, the IDE proposes completion for your code instantly. Start typing <code>$item1Completion</code> right where " +
             "the caret is, and you will see the Lookup Menu with matching suggestions.")
        triggerByListItemAndHighlight(highlightBorder = false, highlightInside = false) { // no highlighting
          it.toString().contains(item1Completion)
        }
        proposeRestore {
          LessonUtil.checkExpectedStateOfEditor(editor, sample1) {
            val change = if (it.endsWith(item1CompletionSuffix)) it.subSequence(0, it.length - item1CompletionSuffix.length) else it
            item1CompletionPrefix.substring(0, item1CompletionPrefix.length - 2).startsWith(change)
          }
        }
        test {
          GuiTestUtil.typeText(item1StartToType)
        }
      }
      task {
        text("Continue typing <code>$item1Completion</code> unless it become the first item.")
        stateCheck {
          (previous.ui as? JList<*>)?.let {
            isTheFirstVariant(it)
          } ?: false
        }
        restoreByUi()
      }
      task {
        text("Now just press <action>EditorEnter</action> to complete this statement.")
        trigger("EditorChooseLookupItem") {
          editor.document.text == result1
        }
        restoreState {
          (previous.ui as? JList<*>)?.takeIf { it.isShowing }?.let {
            !isTheFirstVariant(it)
          } ?: true
        }
        test {
          GuiTestUtil.shortcut(Key.ENTER)
        }
      }
      waitBeforeContinue(500)
      val result2 = LessonUtil.insertIntoSample(sample2, item2Inserted)
      prepareSample(sample2)
      task("CodeCompletion") {
        text("To activate Basic Completion explicitly, press ${action(it)}.")
        trigger(it)
        triggerByListItemAndHighlight { item ->
          item.toString().contains(item2Completion)
        }
        proposeRestore {
          LessonUtil.checkExpectedStateOfEditor(editor, sample2) { change ->
            change.isEmpty()
          }
        }
        test {
          actions(it)
        }
      }
      task {
        text("Select <code>$item2Completion</code> and press <action>EditorEnter</action>.")
        stateCheck {
          editor.document.text == result2
        }
        restoreByUi()
        test {
          ideFrame {
            jListContains(item2Completion).item(item2Completion).doubleClick()
          }
        }
      }
    }

  private fun isTheFirstVariant(it: JList<*>) =
    it.model.size >= 1 && it.model.getElementAt(0).toString().contains(item1Completion)
}