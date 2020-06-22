// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.completion

import com.intellij.application.options.CodeStyle
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textAfterOffset
import training.learn.lesson.javascript.textBeforeOffset
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class CodeEditingLesson(module: Module) : KLesson("Code Editing Tips and Tricks", module, "HTML") {

  val sample = parseLessonSample(""" 
        <!doctype html><html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Multiple selections</title>
</head>
<body>
<table>
  <tr>
      <td>Jackson</td>
     <td>Eve</td>
    <td>24</td>
  </tr>
  <tr>
    <td>First name</td>
    <td>Last name</td>
    <td>Age</td>
  </tr>
</table>
</body>
  </html>


        """.trimIndent())


  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareRuntimeTask {
          CodeStyle.getSettings(project).AUTODETECT_INDENTS = false
        }
        prepareSample(sample)
        task("ReformatCode") {
          text("Let's go over some tips and tricks that can help you edit code a lot faster. For starters, there's no need to manually fix code formatting with WebStorm. Reformat the code with ${action(it)}.")
          trigger(it)
        }
        waitBeforeContinue(500)
        caret(232)
        task("EditorSelectWord") {
          caret(232)
          text("That’s it! Now let’s see how to quickly select pieces of code in a file. Press ${action(it)} a few times to fully select the second tr element (lines 14-18).")
          trigger(it) {
            val selectionEnd = editor.selectionModel.selectionEnd
            val selectionStart = editor.selectionModel.selectionStart
            val selectionEndLineNumber = editor.document.getLineNumber(selectionEnd)
            val selectionStartLineNumber = editor.document.getLineNumber(selectionStart)
            textBeforeOffset(selectionEnd, "</tr>") && textAfterOffset(selectionStart, "<tr>") && selectionEndLineNumber == 17 && selectionStartLineNumber == 13
          }
        }
        task("MoveStatementUp") {
          text("Now that you've selected the code, you can (un)comment it out (<action>CommentByBlockComment</action>), delete it (<action>EditorDeleteToWordStart</action>), or shrink the selection (<action>EditorUnSelectWord</action>).\n" +
            "Another thing you can do is move this code up or down the file. Let’s move it up with ${action(it)}")
          trigger(it)
        }
        task {
          text("Next up is multi-caret editing. Use it to save a bunch of time as you modify code in several spots at once. Place the caret inside the first <strong>td</strong> tag (line 10). Then select all <strong>td</strong> tags inside the same tr element (lines 10-12): press <action>SelectNextOccurrence</action> six times until all the necessary tags are selected. \n" +
            "Let's replace <strong>td</strong> with <strong>th</strong> and hit <action>EditorEscape</action> to exit the multi-caret mode.")
          stateCheck {
            textOnLine(9, "<th>") &&
            textOnLine(10, "<th>") &&
            textOnLine(11, "<th>")
          }
          trigger("EditorEscape")
        }
        task("CommentByLineComment") {
          text("Finally, let’s quickly try the most popular line actions, such as duplicate line (<action>EditorDuplicate</action>), delete line (<action>EditorDeleteLine</action>), or comment it out (<action>CommentByLineComment</action>). Use <action>EditorDuplicate</action> to duplicate the selected line now. Then hit <action>EditorDeleteLine</action> and <action>CommentByLineComment</action> to try the other line actions. ")
          trigger(it)
        }
        task {
          text("That's it for this lesson. Click the button below to start the next one or use  ${action("learn.next.lesson")}.")
        }
      }
    }
  override val existedFile = "index.html"
}