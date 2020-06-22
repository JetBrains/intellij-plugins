// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.completion

import training.lang.JavaScriptLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textBeforeCaret
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class BasicCompletionLesson(module: Module) : KLesson("The Nuts and Bolts of Code Completion", module, JavaScriptLangSupport.lang) {

  val sample = parseLessonSample("""
        let favoriteAnimals = ['dog', 'cat', 'unicorn'];
        
        function pickAnimal(arr) {
            const rnd = arr.length * Math.random();
            return arr[<caret>];
        }
        
        console.log();
        """.trimIndent())


  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(sample)

        caret(136)
        task("EditorChooseLookupItem") {
          text("WebStorm is full of features that help you write better code and increase your productivity. Let’s start with code completion. It enables you to code faster by completing keywords and symbols from language APIs and project dependencies. Type <strong>M</strong> and hit <action>EditorChooseLookupItem</action> to autocomplete <strong>Math</strong>.")
          trigger(it) {
            textBeforeCaret("Math")
          }
        }

        task("EditorChooseLookupItem") {
          text("So, code completion shows context-aware suggestions as you type. To add one of these suggestions, you can use <action>EditorEnter</action> like we just did, or press ${action("EditorTab")} to replace an existing item. Now add a <strong>.</strong> after <strong>Math</strong>, then type <strong>f</strong> and autocomplete the <strong>floor</strong> method with <action>EditorEnter</action>.")
          trigger(it) {
            textBeforeCaret("Math.floor(")
          }
        }

        task("QuickJavaDoc") {
          text("The tooltip (<action>ParameterInfo</action>) we’ve got after placing the caret inside <strong>()</strong> lets you quickly look up the names of parameters in methods and functions. In some situations, you may want to review more detailed documentation. Let’s do it now by pressing ${action(it)}.")
          stateCheck {
            val line = editor.caretModel.logicalPosition.line
            val column = editor.caretModel.logicalPosition.column
            line == 4 && column in 20..26
          }
          trigger(it)
        }
        task {
          text("This is how you can look up JavaScript documentation right in WebStorm. Now add <strong>rnd</strong> inside <strong>()</strong> to continue.")
          stateCheck {
            textBeforeCaret("Math.floor(rnd")
          }
        }
        task("EditorChooseLookupItem") {
          text("Finally, let’s complete the <strong>console.log</strong> statement. Place the caret inside <strong>()</strong> on the line 8 and add <strong>pickAnimal(favoriteAnimals)</strong> using code completion.")
          trigger(it) {
            textBeforeCaret("pickAnimal(favoriteAnimals")
          }
        }
        task {
          text("That’s it for this lesson. To start the next one, click the button below or use ${action("learn.next.lesson")}.")
        }
      }
    }
  override val existedFile = "basicCompletion.js"

}


