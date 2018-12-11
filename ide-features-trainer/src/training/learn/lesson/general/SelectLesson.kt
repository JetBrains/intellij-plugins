package training.learn.lesson.general

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.START_TAG

class SelectLesson(module: Module, lang: String, private val sample: LessonSample) :
    KLesson("Select", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      task {
        copyCode(sample.text)
        caret(sample.getInfo(START_TAG).startOffset)
      }
      triggerTask("EditorNextWordWithSelection") {
        text("Place the caret before any word. Press ${action(it)} to move the caret to the next word and select everything in between.")
      }
      triggerTask("EditorSelectWord") {
        text("Press ${action(it)} to extend the selection to the next code block.")
      }
      triggerTask("EditorSelectWord") {
        text("Try increasing your selection with ${action(it)} until your whole file is selected.")
      }
      triggerTask("EditorUnSelectWord") {
        text("${action(it)} shrinks selection. Try pressing it.")
      }
      triggerTask("\$SelectAll") {
        text("Now select the whole file instantly with ${action(it)}.")
      }
      complete()
      task {
        text("Awesome! Click the button below to start the next lesson, or use ${action("learn.next.lesson")}.")
      }
    }

}