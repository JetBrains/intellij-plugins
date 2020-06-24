/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training

import com.intellij.testGuiFramework.fixtures.ComponentFixture
import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.impl.GuiTestCase
import com.intellij.testGuiFramework.impl.actionLink
import com.intellij.testGuiFramework.impl.linkLabel
import org.junit.Test
import training.commands.kotlin.TaskTestContext
import training.lang.LangManager
import training.learn.CourseManager
import training.learn.lesson.LessonManager
import training.util.BlockingExecutor

/**
 *  Use next gradle task to invoke test:
 *  gradle -Dtest.single=SelectLessonTest clean test -Didea.gui.test.alternativeIdePath="<path_to_installed_IDE>"
 */
class RubyLessonsGuiTestCase : GuiTestCase() {
  private val testActionsExecutor = BlockingExecutor()

  @Test
  fun runAllRubyLessons() {
    LessonManager.externalTestActionsExecutor = testActionsExecutor

    val courseManager = CourseManager.instance
    openLearnProject()
    ideFrame {
      for (module in courseManager.modules) {
        if (module.lessons.isEmpty()) continue
        linkAtLearnPanel { linkLabel(module.name).click() }
        for (lesson in module.lessons) {
          TaskTestContext.inTestMode = true
          linkAtLearnPanel { linkLabel(lesson.name).click() }
          testActionsExecutor.run()
          if (!lesson.passed) {
            throw IllegalStateException("Can't pass lesson " + lesson.name)
          }
          System.err.println("Passed " + lesson.name)
          TaskTestContext.inTestMode = false
        }
        linkAtLearnPanel { linkLabel("All Topics").click() }
      }
    }
  }

  private fun IdeFrameFixture.linkAtLearnPanel(link: () -> ComponentFixture<*, *>) {
    //Thread.sleep(200)
    waitForBackgroundTasksToFinish()
    toolwindow(id = "Learn") {
      content {
        link()
      }
    }
  }

  private fun openLearnProject() {
    welcomeFrame {
      LangManager.getInstance().loadState(LangManager.State("ruby"))
      actionLink("Learn IntelliJ IDEA").click()
    }
  }
}