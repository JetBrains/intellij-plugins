// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.util

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.testGuiFramework.fixtures.ComponentFixture
import com.intellij.testGuiFramework.fixtures.CustomToolWindowFixture
import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.impl.GuiTestCase
import com.intellij.testGuiFramework.impl.linkLabel
import org.jetbrains.concurrency.AsyncPromise
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskTestContext
import training.learn.CourseManager
import training.learn.interfaces.Lesson
import training.learn.lesson.LessonListener
import training.learn.lesson.kimpl.KLesson
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class LearningLessonsAutoExecutor(val project: Project, private val progress: ProgressIndicator) {
  fun execute() {
    with(TaskTestContext.guiTestCase) {
      runAllModules()
    }
  }

  private fun GuiTestCase.runAllModules() {
    val courseManager = CourseManager.instance
    ideFrame {
      for (module in courseManager.modules) {
        progress.checkCanceled()
        if (module.lessons.isEmpty()) continue
        linkAtLearnPanel { linkLabel(module.name).click() }
        for (lesson in module.lessons) {
          if (lesson !is KLesson) continue
          for (i in 1..2) {
            try {
              executeLesson(lesson, this)
              break
            }
            catch (e: TimeoutException) {
            }
          }
          if (!lesson.passed) {
            throw IllegalStateException("Can't pass lesson " + lesson.name)
          }
        }
        linkAtLearnPanel { linkLabel("All Topics").click() }
      }
    }
  }

  private fun GuiTestCase.executeLesson(lesson: Lesson, ideFrameFixture: IdeFrameFixture) {
    val lessonPromise = AsyncPromise<Boolean>()
    lesson.addLessonListener(object : LessonListener {
      override fun lessonPassed(lesson: Lesson) {
        lessonPromise.setResult(true)
      }
    })
    progress.checkCanceled()
    TaskContext.inTestMode = true
    linkAtLearnPanel { ideFrameFixture.linkLabel(lesson.name).click() }
    //testActionsExecutor.run()
    val passedStatus = lessonPromise.blockingGet(4, TimeUnit.SECONDS)
    if (passedStatus == null || !passedStatus) {
      throw IllegalStateException("Can't pass lesson " + lesson.name)
    }
    System.err.println("Passed " + lesson.name)
    TaskContext.inTestMode = false
  }

  private fun GuiTestCase.linkAtLearnPanel(link: () -> ComponentFixture<*, *>) {
    DumbService.getInstance(project).waitForSmartMode()
    ideFrame {
      with(CustomToolWindowFixture("Learn", this)) {
        content {
          link()
        }
      }
    }
  }

  companion object {
    fun runLessons(project: Project) {
      runBackgroundableTask("Run all lessons", project) {
        try {
          val learningLessonsAutoExecutor = LearningLessonsAutoExecutor(project, it)
          learningLessonsAutoExecutor.execute()
        }
        finally {
          TaskContext.inTestMode = false
        }
      }
    }
  }
}