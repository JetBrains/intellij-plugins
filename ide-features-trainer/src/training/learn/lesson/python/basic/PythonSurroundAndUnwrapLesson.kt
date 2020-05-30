// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.basic

import training.learn.interfaces.Module
import training.learn.lesson.general.SurroundAndUnwrapLesson
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class PythonSurroundAndUnwrapLesson(module: Module) : SurroundAndUnwrapLesson(module, "Python") {
  override val sample: LessonSample = parseLessonSample("""
    def surround_and_unwrap_demo(debug):
        <select>if debug:
            print("Surround and Unwrap me!")</select>
    
  """.trimIndent())

  override val surroundItems = arrayOf("try", "except")

  override val lineShiftBeforeUnwrap = -2
}
