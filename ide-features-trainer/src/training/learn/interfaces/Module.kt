package training.learn.interfaces

import training.lang.LangSupport

interface Module {

  var lessons: MutableList<Lesson>

  val sanitizedName: String

  var id: String?

  val name: String

  val moduleType: ModuleType

  val description: String?

  fun filterLessonByLang(langSupport: LangSupport): MutableList<Lesson>

  fun giveNotPassedLesson(): Lesson?

  fun giveNotPassedAndNotOpenedLesson(): Lesson?

  fun hasNotPassedLesson(): Boolean

  fun update()

  fun addLesson(lesson: Lesson) {
    if (lessons.any { it.id == lesson.id && it.hashCode() == lesson.hashCode()}) return // do not add lesson twice
    lesson.module = this
    lessons.add(lesson)
  }

}