// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.lesson.editor

import com.intellij.javascript.ift.JavaScriptLangSupport
import com.intellij.javascript.ift.JsLessonsBundle
import com.intellij.javascript.ift.lesson.setLanguageLevel
import training.dsl.LessonContext
import training.dsl.LessonUtil
import training.dsl.parseLessonSample
import training.learn.course.KLesson
import training.learn.js.textBeforeCaret

class BasicCompletionLesson
  : KLesson("The Nuts and Bolts of Code Completion", JsLessonsBundle.message("js.editor.completion.title"), JavaScriptLangSupport.lang) {

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
          text(JsLessonsBundle.message("js.editor.completion.choose.lookup", strong("Ma"), action("EditorChooseLookupItem"),
                                     code("Math")))
          trigger(it) {
            textBeforeCaret("Math")
          }
        }

        task("EditorChooseLookupItem") {
          text(JsLessonsBundle.message("js.editor.completion.choose.method",
                                     action("EditorEnter"), action("EditorTab"), code("."), code("Math"), strong("f"), code("floor")))
          trigger(it) {
            textBeforeCaret("Math.floor(")
          }
        }

        task("QuickJavaDoc") {
          text(
            JsLessonsBundle.message("js.editor.completion.parameter.info",
                                  action("ParameterInfo"), code("()"), action(it)))
          stateCheck {
            val line = editor.caretModel.logicalPosition.line
            val column = editor.caretModel.logicalPosition.column
            line == 4 && column in 20..26
          }
          trigger(it)
        }
        task {
          text(JsLessonsBundle.message("js.editor.completion.add.parameter",
                                     code("rnd"), code("()")))
          stateCheck {
            textBeforeCaret("Math.floor(rnd")
          }
        }
        task("EditorChooseLookupItem") {
          text(
            JsLessonsBundle.message("js.editor.completion.console.log.argument",
                                  code("console.log"), code("()"), code("pickAnimal(favoriteAnimals)")))
          trigger(it) {
            textBeforeCaret("pickAnimal(favoriteAnimals")
          }
        }
        text(JsLessonsBundle.message("js.editor.completion.next", LessonUtil.rawEnter()))
      }
    }
  override val existedFile = "basicCompletion.js"

}


