// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.completion

import training.dsl.parseLessonSample
import training.learn.lesson.general.CompletionWithTabLesson

class RubyCompletionWithTabLesson :
  CompletionWithTabLesson("goodbye") {

  override val sample = parseLessonSample("""class DemoClass
  def hello
    puts 'Hello'
  end

  def goodbye
    puts 'Goodbye'
  end
end

# @param demo [DemoClass]
def say_something(demo)
  demo.<caret>hello
end
""".trimIndent())
}