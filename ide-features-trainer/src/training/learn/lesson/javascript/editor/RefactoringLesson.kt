// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.editor

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.idea.ActionsBundle
import com.intellij.ui.components.JBList
import training.lang.JavaScriptLangSupport
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textAtCaretEqualsTo
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.dropMnemonic
import training.learn.lesson.kimpl.parseLessonSample

class RefactoringLesson(module: Module)
  : KLesson("Refactorings in a Nutshell", LessonsBundle.message("js.editor.refactorings.title"), module, JavaScriptLangSupport.lang) {

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
          text(LessonsBundle.message("js.editor.refactorings.this",
                                     "https://www.jetbrains.com/help/webstorm/refactoring-source-code.html#ws_supported_refactorings",
                                     code("books"), action(it), strong(quickListPopup)))
          stateCheck {
            textAtCaretEqualsTo("books")
          }
          trigger(it)
        }

        task("RenameElement") {
          text(LessonsBundle.message("js.editor.refactorings.rename", strong(ActionsBundle.message("action.Refactorings.QuickListPopupAction.text")), strong(ActionsBundle.message("action.RenameAction.text"))))
          trigger(it) {
            textAtCaretEqualsTo("books")
          }
        }

        task {
          text(
            LessonsBundle.message("js.editor.refactorings.rename.apply", code("books"), code("listOfBooks"), action("EditorEnter")))
          stateCheck {
            textOnLine(0, "function listBookAuthors(listOfBooks) {") &&
            TemplateManager.getInstance(project).getActiveTemplate(editor) == null
          }
        }
        task("IntroduceVariable") {
          text(LessonsBundle.message("js.editor.refactorings.shortcut", code("author"), action(it)))
          stateCheck {
            textAtCaretEqualsTo("author")
          }
          trigger(it)
        }
        task {
          text(
            LessonsBundle.message("js.editor.refactoring.select.expression", code("author"), code("book.author"), action("EditorEnter")))
          stateCheck {
            focusOwner is JBList<*> && (focusOwner as JBList<*>).model.getElementAt(0).toString() == "NO"
          }
        }
        task {
          text(LessonsBundle.message("js.editor.refactoring.replace", code("let"), code("author")))
          stateCheck {
            textOnLine(3, "let author = book.author;") &&
            textOnLine(4, "if (!listOfAuthors.includes(author)) {") &&
            textOnLine(5, "listOfAuthors.push(author);") &&
            TemplateManager.getInstance(project).getActiveTemplate(editor) == null
          }
        }
        task {
          text(
            LessonsBundle.message("js.editor.refactorings.next",
                                  "https://resources.jetbrains.com/storage/products/webstorm/docs/WebStorm_ReferenceCard.pdf",
                                  strong(ActionsBundle.message("group.RefactoringMenu.text").dropMnemonic()),
                                  action("learn.next.lesson")))
        }
      }
    }
  override val existedFile = "refactoring.js"
}