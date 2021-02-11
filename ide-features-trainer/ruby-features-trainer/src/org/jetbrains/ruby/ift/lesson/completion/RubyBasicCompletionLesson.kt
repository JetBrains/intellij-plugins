// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.completion

import training.learn.lesson.general.completion.BasicCompletionLessonBase
import training.learn.lesson.kimpl.parseLessonSample

class RubyBasicCompletionLesson : BasicCompletionLessonBase("ruby") {
  override val sample1 = parseLessonSample("""
    class AnimalClass
      def speak
        'Hello!'
      end
    end
    
    class Anatomy
    end
    
    class Cat < <caret>
    end
""".trimIndent())

  override val sample2 = parseLessonSample("""
    class AnimalClass
      def speak
        'Hello!'
      end
    end
    
    class Anatomy
    end
    
    class Cat < AnimalClass
      def meow
        'Meow'
      end
    
      def speak
        <caret>
      end
    end
  """.trimIndent())

  override val item1StartToType = "Ani"
  override val item1CompletionPrefix = "AnimalClass"
  override val item2Completion = "meow"
}