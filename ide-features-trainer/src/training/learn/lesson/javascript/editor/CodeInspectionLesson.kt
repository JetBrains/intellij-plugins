// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.editor

import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.JavaScriptBundle
import training.lang.JavaScriptLangSupport
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textAtCaretEqualsTo
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class CodeInspectionLesson(module: Module)
  : KLesson("The Power of Code Inspections", LessonsBundle.message("js.editor.code.inspection.title"), module, JavaScriptLangSupport.lang) {

  val sample = parseLessonSample("""
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
          text(LessonsBundle.message("js.editor.code.inspection.intro", action(it)))
          trigger(it) {
            editor.caretModel.logicalPosition.line == 3 && textAtCaretEqualsTo("book")
          }
        }
        task("ShowIntentionActions") {
          text(LessonsBundle.message("js.editor.code.inspection.show.intentions",
                                     action("GotoNextError"), code("book"), code("book"), action(it)))
          before { caret(editor.document.getLineEndOffset(3) - 11) }
          
          //handle simple alt+enter and alt+enter for the error tooltip 
          trigger { id -> id == it || id == "com.intellij.codeInsight.daemon.impl.DaemonTooltipWithActionRenderer\$addActionsRow$2"}
        }
        task {
          
          
          text(LessonsBundle.message("js.editor.code.inspection.run.intention", strong(JavaScriptBundle.message("javascript.fix.create.parameter", "book")), action("EditorEnter")))
          stateCheck {
            textOnLine(2, "books.forEach(function (book) {")
          }
        }
        task("ShowIntentionActions") {
          text(LessonsBundle.message("js.editor.code.inspection.checkmark", icon(AllIcons.General.InspectionsOK),
                                     code("function"), action(it)))
          stateCheck {
            val line = editor.caretModel.logicalPosition.line
            line == 2 && textAtCaretEqualsTo("function")
          }
          trigger(it)
        }
        task {
          text(LessonsBundle.message("js.editor.code.inspection.make.shorter",
                                     strong(JavaScriptBundle.message("js.convert.to.arrow.function")), action("EditorEnter")))
          stateCheck {
            textOnLine(2, "books.forEach(book => {")
          }
        }
        task {
          text(LessonsBundle.message("js.editor.code.inspection.next", action("learn.next.lesson")))
        }
      }
    }
  override val existedFile = "codeInspection.js"
}