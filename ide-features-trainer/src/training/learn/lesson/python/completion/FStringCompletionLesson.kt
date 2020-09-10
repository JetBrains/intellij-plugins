// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.completion

import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil
import training.learn.lesson.kimpl.LessonUtil.checkExpectedStateOfEditor
import training.learn.lesson.kimpl.parseLessonSample

class FStringCompletionLesson(module: Module)
  : KLesson("completion.f.string", LessonsBundle.message("python.f.string.completion.lesson.name"), module, "Python") {
  private val template = """
    import sys
    
    class Car:
        def __init__(self, speed=0):
            self.speed = speed
            self.odometer = 0
            self.time = 0
        def say_state(self):
            print("I'm going kph!".format(self.speed))
        def accelerate(self):
            self.speed += 5
        def brake(self):
            self.speed -= 5
        def step(self):
            self.odometer += self.speed
            self.time += 1
        def average_speed(self):
            return self.odometer / self.time
    if __name__ == '__main__':
        my_car_show_distance = sys.argv[1]
        my_car = Car()
        print("I'm a car!")
        while True:
            action = input("What should I do? [A]ccelerate, [B]rake, "
                     "show [O]dometer, or show average [S]peed?").upper()
            if action not in "ABOS" or len(action) != 1:
                print("I don't know how to do that")
            if my_car_show_distance == "yes":
                print(<f-place>"The car has driven <caret> kilometers")
    """.trimIndent() + '\n'

  private val completionItem = "my_car"

  private val sample = parseLessonSample(template.replace("<f-place>", ""))

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    task("\${my") {
      text(LessonsBundle.message("python.f.string.completion.type.prefix", LessonUtil.productName, code(it)))
      runtimeText {
        val prefixTyped = checkExpectedStateOfEditor(sample) { change ->
          "\${my_car".startsWith(change) && change.startsWith(it)
        } == null
        if (prefixTyped) LessonsBundle.message("python.f.string.completion.invoke.manually", action("CodeCompletion")) else null
      }
      triggerByListItemAndHighlight(highlightBorder = false) { item ->
        item.toString().contains(completionItem)
      }
      proposeRestore {
        checkExpectedStateOfEditor(sample) { change ->
          "\${my_car".startsWith(change)
        }
      }
      test { type(it) }
    }
    task {
      text(LessonsBundle.message("python.f.string.completion.complete.it", code(completionItem), action("EditorChooseLookupItem")))
      val result = template.replace("<f-place>", "f").replace("<caret>", "\${$completionItem}")
      restoreByUi()
      stateCheck {
        editor.document.text == result
      }
      test { GuiTestUtil.shortcut(Key.ENTER) }
    }
    task {
      text(LessonsBundle.message("python.f.string.completion.result.message"))
    }
  }
}
