// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general.refactorings

import com.intellij.testGuiFramework.impl.jList
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample

class ExtractVariableFromBubbleLesson(module: Module, lang: String, private val sample: LessonSample) : KLesson("Extract Variable", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      task("IntroduceVariable") {
        text("Press ${action(it)} to extract a local variable from the selected expression ${code("i + 1")}.")
        triggerStart("IntroduceVariable")
        test {
          actions(it)
        }
      }

      task {
        text("This code block contains 3 occurrences of the selected expression. " +
             "Choose the second item in the list to replace both of them.")

        stateCheck {
          editor.document.text.split("i + 1").size == 2
        }
        test {
          ideFrame {
            val item = "Replace all 3 occurrences"
            jList(item).clickItem(item)
          }
        }
      }

      actionTask("NextTemplateVariable") {
        "Choose a name for the new variable or leave it as it is. " +
        "Press <strong>Enter</strong> to complete the refactoring."
      }
    }
}
