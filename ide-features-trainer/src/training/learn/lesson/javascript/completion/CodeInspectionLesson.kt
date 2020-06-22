// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.completion

import training.lang.JavaScriptLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textAtCaretEqualsTo
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class CodeInspectionLesson(module: Module) : KLesson("The Power of Code Inspections", module, JavaScriptLangSupport.lang) {

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
          text("As you work in the editor, WebStorm constantly analyzes your code, detects various problems in it, and suggests how it can be improved. The opened file has two highlighted problems on lines 4 and 5. Let’s check what they are by pressing ${action(it)}.")
          trigger(it) {
            editor.caretModel.logicalPosition.line == 3 && textAtCaretEqualsTo("book")
          }
        }
        task("ShowIntentionActions") {
          text("You can also use <strong>F2</strong> to jump from one error to another. Or, you can explore the found problems by hovering over them.\nIn this file, the IDE has located an unresolved variable – one that wasn't defined anywhere in the code. It suggests creating a new one, <strong>book</strong>, as one of the possible fixes, but we need to add a parameter book instead. Place the caret on <strong>book</strong> and hit ${action(it)} to see the full list of fixes.")
          before { caret(editor.document.getLineEndOffset(3) - 11) }
          
          //handle simple alt+enter and alt+enter for the error tooltip 
          trigger { id -> id == it || id == "com.intellij.codeInsight.daemon.impl.DaemonTooltipWithActionRenderer\$addActionsRow$2"}
        }
        task {
          text("Let’s select <strong>Create parameter 'book'</strong> and press <action>EditorEnter</action>.")
          stateCheck {
            textOnLine(2, "books.forEach(function (book) {")
          }
        }
        task("ShowIntentionActions") {
          text("If you now look at the top right-hand corner of the editor, you’ll see a green checkmark (<icon>AllIcons.General.InspectionsOK</icon>) confirming the file has no more problems. However, there’s still a minor detail that can be optimized to make the code shorter. Place the caret on <strong>function</strong> (line 3) and press ${action(it)}.")
          stateCheck {
            val line = editor.caretModel.logicalPosition.line
            line == 2 && textAtCaretEqualsTo("function")
          }
          trigger(it)
        }
        task {
          text("Now let’s make our function expression shorter. Select the <strong>Convert to arrow function</strong> quick-fix and hit <action>EditorEnter</action>.")
          stateCheck {
            textOnLine(2, "books.forEach(book => {")
          }
        }
        task {
          text("That’s it for this lesson. Click the button below to start the next one or use  ${action("learn.next.lesson")}.")
        }
      }
    }
  override val existedFile = "codeInspection.js"
}