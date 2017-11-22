package training.learn.lesson.listeners

import com.intellij.openapi.project.Project
import training.learn.lesson.Lesson
import training.learn.lesson.LessonListenerAdapter
import training.statistic.StatisticBase

class StatisticLessonListener(val project: Project) : LessonListenerAdapter() {

  override fun lessonStarted(lesson: Lesson) {
    StatisticBase.instance.onStartLesson(lesson)
  }

  override fun lessonPassed(lesson: Lesson) {
    StatisticBase.instance.onPassLesson(lesson)
  }
}