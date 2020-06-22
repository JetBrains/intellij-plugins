// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.completion

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.ui.components.JBList
import training.lang.JavaScriptLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textAtCaretEqualsTo
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class RefactoringLesson(module: Module) : KLesson("Refactorings in a Nutshell", module, JavaScriptLangSupport.lang) {

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
          text("WebStorm has a <a href='https://www.jetbrains.com/help/webstorm/refactoring-source-code.html#ws_supported_refactorings'>number of refactorings</a> that can automatically restructure existing code without changing its behavior across the entire project. Let's look up the list of refactorings available for the <strong>books</strong> parameter. To do this, press ${action(it)} or go to <strong>Refactor > Refactor This</strong> from the main menu.")
          stateCheck {
            textAtCaretEqualsTo("books")
          }
          trigger(it)
        }

        task("RenameElement") {
          text("With <strong>Refactor This</strong>, you don't need to memorize all the refactorings the IDE has, or their shortcuts. Let's click <strong>Rename</strong> to see one of the most popular refactorings in action.")
          trigger(it) {
            textAtCaretEqualsTo("books")
          }
        }

        task {
          text("Rename the <strong>books</strong> parameter to <strong>listOfBooks</strong> and hit <action>EditorEnter</action>. This will apply the changes across all files in the project.")
          stateCheck {
            textOnLine(0, "function listBookAuthors(listOfBooks) {") &&
            TemplateManager.getInstance(project).getActiveTemplate(editor) == null
          }
        }
        task("IntroduceVariable") {
          text("Well done! Let's try refactoring code the other way – by using a shortcut. Place the caret on the <strong>author</strong> property (line 4) and press ${action(it)}.")
          stateCheck {
            textAtCaretEqualsTo("author")
          }
          trigger(it)
        }
        task {
          text("Let's create a new variable, <strong>author</strong>, which will contain <strong>book.author</strong>. Select the <strong>book.author</strong> expression from the list and hit <action>EditorEnter</action>.")
          stateCheck {
            focusOwner is JBList<*> && (focusOwner as JBList<*>).model.getElementAt(0).toString() == "NO"
          }
        }
        task {
          text("Now replace all 2 occurrences with the <strong>let</strong> variable named <strong>author</strong>.")
          stateCheck {
            textOnLine(3, "let author = book.author;") &&
            textOnLine(4, "if (!listOfAuthors.includes(author)) {") &&
            textOnLine(5, "listOfAuthors.push(author);") &&
            TemplateManager.getInstance(project).getActiveTemplate(editor) == null
          }
        }
        task {
          text("We've just explored two ways to refactor code in WebStorm. Print out the <a href=\"https://resources.jetbrains.com/storage/products/webstorm/docs/WebStorm_ReferenceCard.pdf\">keymap reference</a> if you prefer using shortcuts, or simply keep using the <strong>Refactor This</strong> menu. Click the button below to start the next lesson or use ${action("learn.next.lesson")}.")
        }
      }
    }
  override val existedFile = "refactoring.js"
}