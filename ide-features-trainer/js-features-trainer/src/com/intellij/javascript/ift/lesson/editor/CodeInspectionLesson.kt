// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.lesson.editor

import com.intellij.icons.AllIcons
import com.intellij.javascript.ift.JsLessonsBundle
import com.intellij.javascript.ift.lesson.setLanguageLevel
import com.intellij.lang.javascript.JavaScriptBundle
import training.dsl.LessonContext
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson
import training.learn.js.textAtCaretEqualsTo
import training.learn.js.textOnLine

class CodeInspectionLesson
  : KLesson("The Power of Code Inspections", JsLessonsBundle.message("js.editor.code.inspection.title")) {

  private val sample = parseLessonSample("""
        function listBookAuthors(books) {
            let listOfAuthors = [];
            books.forEach(function () {
                if (!listOfAuthors.includes(book.author)) {
                    listOfAuthors.push(book.author);
                }
            });
            return listOfAuthors;
        }

        let myBooks = [
            {title: 'Harry Potter', author: 'J. K. Rowling'},
            {title: 'Lord of the Rings', author: 'J. R. R. Tolkien'},
            {title: 'The Hobbit', author: 'J. R. R. Tolkien'}
        ];
        listBookAuthors(myBooks);
        """.trimIndent())


  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(sample)
        task("GotoNextError") {
          text(JsLessonsBundle.message("js.editor.code.inspection.intro", action(it)))
          trigger(it) {
            editor.caretModel.logicalPosition.line == 3 && textAtCaretEqualsTo("book")
          }
        }
        task("ShowIntentionActions") {
          text(JsLessonsBundle.message("js.editor.code.inspection.show.intentions.1",
                                       action("GotoNextError")))
          text(JsLessonsBundle.message("js.editor.code.inspection.show.intentions.2",
                                       code("book"), code("book"), action(it)))
          before { caret(editor.document.getLineEndOffset(3) - 11) }
          
          //handle simple alt+enter and alt+enter for the error tooltip 
          trigger { id -> id == it || id == "com.intellij.codeInsight.daemon.impl.DaemonTooltipWithActionRenderer\$addActionsRow$2"}
        }
        task {
          
          
          text(JsLessonsBundle.message("js.editor.code.inspection.run.intention", strong(JavaScriptBundle.message("javascript.fix.create.parameter", "book")), action("EditorEnter")))
          stateCheck {
            textOnLine(2, "books.forEach(function (book) {")
          }
        }
        task("ShowIntentionActions") {
          text(JsLessonsBundle.message("js.editor.code.inspection.checkmark", icon(AllIcons.General.InspectionsOK),
                                     code("function"), action(it)))
          stateCheck {
            val line = editor.caretModel.logicalPosition.line
            line == 2 && textAtCaretEqualsTo("function")
          }
          trigger(it)
        }
        task {
          text(JsLessonsBundle.message("js.editor.code.inspection.make.shorter",
                                     strong(JavaScriptBundle.message("js.convert.to.arrow.function")), action("EditorEnter")))
          stateCheck {
            textOnLine(2, "books.forEach(book => {")
          }
        }
        text(JsLessonsBundle.message("js.editor.code.inspection.next", LessonUtil.rawEnter()))
      }
    }
  override val sampleFilePath = "codeInspection.js"
}