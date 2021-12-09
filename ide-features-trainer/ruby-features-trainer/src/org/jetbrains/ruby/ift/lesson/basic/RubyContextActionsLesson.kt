package org.jetbrains.ruby.ift.lesson.basic

import org.jetbrains.plugins.ruby.RBundle
import training.dsl.LessonSample
import training.dsl.parseLessonSample
import training.learn.lesson.general.ContextActionsLesson

class RubyContextActionsLesson : ContextActionsLesson() {
  override val sample: LessonSample = parseLessonSample("""
    def inc(value)
      result = value + 1
      <caret>result
    end
    
    def intention_example(a, b)
      if a
        1
      elsif b
        1
      else
        2
      end
    end
    
    intention_example(true, false)
  """.trimIndent())

  override val warningQuickFix: String = RBundle.message("inspection.unnecessary.return.value.remove.fix")
  override val warningPossibleArea: String = "result\n"

  override val intentionText: String = RBundle.message("ruby.intentions.merge.sequential.ifs")
  override val intentionCaret: String = "if"
  override val intentionPossibleArea: String = intentionCaret
}