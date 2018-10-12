package training.learn.lesson.kimpl

import training.lang.LangSupport
import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.interfaces.ModuleType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

open class KModule(override val name: String, override val moduleType: ModuleType ): Module {


  private val myLessons by lazy {
    return@lazy this::class.declaredFunctions.filter { it.visibility == KVisibility.PUBLIC }.map { it.call(this)
    as KLesson}
  }

  override var lessons: MutableList<Lesson>
    get() {
      return myLessons.toMutableList()
    }
    set(value) {

    }


  override val sanitizedName: String
    get() = name

  override var id: String?
    get() = name
    set(value) {}

  override val description: String = "Empty description of Kotlin Module"

  override fun filterLessonByLang(langSupport: LangSupport): MutableList<Lesson> {
    return lessons.asSequence().filter { langSupport.acceptLang(it.lang) }.toMutableList()
  }

  override fun giveNotPassedLesson(): Lesson? {
    return lessons.asSequence().filter { !it.passed }.firstOrNull()
  }

  override fun giveNotPassedAndNotOpenedLesson(): Lesson? {
    return lessons.asSequence().filter { !it.passed && !it.isOpen }.firstOrNull()
  }

  override fun hasNotPassedLesson(): Boolean {
    return lessons.asSequence().any { !it.passed }
  }

  override fun update() {
    //do nothing here
  }

}