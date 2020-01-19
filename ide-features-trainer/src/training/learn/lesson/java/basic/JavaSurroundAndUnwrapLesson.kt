// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.java.basic

import training.learn.interfaces.Module
import training.learn.lesson.general.SurroundAndUnwrapLesson
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class JavaSurroundAndUnwrapLesson(module: Module) : SurroundAndUnwrapLesson(module, "JAVA") {
  override val sample: LessonSample = parseLessonSample("""
    class SurroundAndUnwrapDemo {
        public static void main(String[] args) {
            <select>System.out.println("Surround and Unwrap me!");</select>
        }
    }
  """.trimIndent())

  override val surroundItems = arrayOf("try", "catch", "finally")

  override val lineShiftBeforeUnwrap = -2
}
