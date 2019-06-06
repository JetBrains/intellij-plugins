package training

import com.intellij.testGuiFramework.fixtures.ComponentFixture
import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.impl.GuiTestCase
import com.intellij.testGuiFramework.impl.actionLink
import com.intellij.testGuiFramework.impl.linkLabel
import org.junit.Test
import training.commands.kotlin.TaskContext
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
  fun rubyNavigationLessonTest() {
    LessonManager.externalTestActionsExecutor = testActionsExecutor

    val courseManager = CourseManager.instance
    openLearnProject()
    ideFrame {
      for (module in courseManager.modules) {
        if (module.lessons.isEmpty()) continue
        linkAtLearnPanel { linkLabel(module.name).click() }
        for (lesson in module.lessons) {
          TaskContext.inTestMode = true
          linkAtLearnPanel { linkLabel(lesson.name).click() }
          testActionsExecutor.run()
          if (!lesson.passed) {
            throw IllegalStateException("Can't pass lesson " + lesson.name)
          }
          System.err.println("Passed " + lesson.name)
          TaskContext.inTestMode = false
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
    CourseManager.instance.showGotMessage = false
    welcomeFrame {
      LangManager.getInstance().loadState(LangManager.State("ruby"))
      actionLink("Learn IntelliJ IDEA").click()
    }
  }
}