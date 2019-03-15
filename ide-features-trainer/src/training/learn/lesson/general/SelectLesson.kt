package training.learn.lesson.general

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample

class SelectLesson(module: Module, lang: String, private val sample: LessonSample) :
    KLesson("Select", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)

      actionTask("EditorNextWordWithSelection") {
        "Place the caret before any word. Press ${action(it)} to move the caret to the next word and select everything in between."
      }
      actionTask("EditorSelectWord") {
        "Press ${action(it)} to extend the selection to the next code block."
      }
      actionTask("EditorSelectWord") {
        "Try increasing your selection with ${action(it)} until your whole file is selected."
      }
      actionTask("EditorUnSelectWord") {
        "${action(it)} shrinks selection. Try pressing it."
      }
      actionTask("\$SelectAll") {
        "Now select the whole file instantly with ${action(it)}."
      }
      task {
        text("Awesome! Click the button below to start the next lesson, or use ${action("learn.next.lesson")}.")
      }
    }

}