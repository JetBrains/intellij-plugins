// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.basic

import com.intellij.psi.tree.IElementType
import com.jetbrains.python.PyTokenTypes
import training.learn.interfaces.Module
import training.learn.lesson.general.SingleLineCommentLesson
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class PythonCommentLesson(module: Module) : SingleLineCommentLesson(module, "Python") {
  override val commentElementType: IElementType
    get() = PyTokenTypes.END_OF_LINE_COMMENT

  override val sample: LessonSample
    get() = parseLessonSample("""
      for i in range(5):    # for each number i in range 0-4. range(5) function returns list [0, 1, 2, 3, 4]
          print(i)          # this line is executed 5 times. First time i equals 0, then 1, ...
      
      primes = [2, 3, 5, 7]   # create new list
      
      for prime in primes:
          print(prime)
    """.trimIndent())
}
