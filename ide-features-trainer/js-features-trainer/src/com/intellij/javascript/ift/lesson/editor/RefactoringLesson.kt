// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.lesson.editor

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.idea.ActionsBundle
import com.intellij.javascript.ift.JavaScriptLangSupport
import com.intellij.javascript.ift.JsLessonsBundle
import com.intellij.javascript.ift.lesson.setLanguageLevel
import com.intellij.refactoring.RefactoringBundle
import com.intellij.ui.components.JBList
import training.dsl.LessonContext
import training.dsl.LessonUtil
import training.dsl.dropMnemonic
import training.dsl.parseLessonSample
import training.learn.course.KLesson
import training.learn.js.textAtCaretEqualsTo
import training.learn.js.textOnLine

class RefactoringLesson
  : KLesson("Refactorings in a Nutshell", JsLessonsBundle.message("js.editor.refactorings.title"), JavaScriptLangSupport.lang) {

  val sample = parseLessonSample(""" 
        function listBookAuthors(<caret>books) {
            let listOfAuthors = [];
            books.forEach(book => {
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
        task("Refactorings.QuickListPopupAction") {
          val quickListPopup = ActionsBundle.message("group.RefactoringMenu.text").dropMnemonic() + " > " +
                               ActionsBundle.message("action.Refactorings.QuickListPopupAction.text")
          text(JsLessonsBundle.message("js.editor.refactorings.this",
                                     "https://www.jetbrains.com/help/webstorm/refactoring-source-code.html#ws_supported_refactorings",
                                     code("books"), action(it), strong(quickListPopup)))
          stateCheck {
            textAtCaretEqualsTo("books")
          }
          trigger(it)
        }

        task("RenameElement") {
          text(JsLessonsBundle.message("js.editor.refactorings.rename", strong(RefactoringBundle.message("refactor.this.title")), strong(ActionsBundle.message("action.RenameAction.text"))))
          trigger(it) {
            textAtCaretEqualsTo("books")
          }
        }

        task {
          text(
            JsLessonsBundle.message("js.editor.refactorings.rename.apply", code("books"), code("listOfBooks"), action("EditorEnter")))
          stateCheck {
            textOnLine(0, "function listBookAuthors(listOfBooks) {") &&
            TemplateManager.getInstance(project).getActiveTemplate(editor) == null
          }
        }
        task("IntroduceVariable") {
          text(JsLessonsBundle.message("js.editor.refactorings.shortcut", code("author"), action(it)))
          stateCheck {
            textAtCaretEqualsTo("author")
          }
          trigger(it)
        }
        task {
          text(
            JsLessonsBundle.message("js.editor.refactoring.select.expression", code("author"), code("book.author"), action("EditorEnter")))
          stateCheck {
            focusOwner is JBList<*> && (focusOwner as JBList<*>).model.getElementAt(0).toString() == "NO"
          }
        }
        task {
          text(JsLessonsBundle.message("js.editor.refactoring.replace", code("let"), code("author")))
          stateCheck {
            textOnLine(3, "let author = book.author;") &&
            textOnLine(4, "if (!listOfAuthors.includes(author)) {") &&
            textOnLine(5, "listOfAuthors.push(author);") &&
            TemplateManager.getInstance(project).getActiveTemplate(editor) == null
          }
        }
        text(
          JsLessonsBundle.message("js.editor.refactorings.next",
                                "https://resources.jetbrains.com/storage/products/webstorm/docs/WebStorm_ReferenceCard.pdf",
                                strong(RefactoringBundle.message("refactor.this.title")),
                                LessonUtil.rawEnter()))
      }
    }
  override val existedFile = "refactoring.js"
}