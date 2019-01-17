package training

import com.intellij.testGuiFramework.fixtures.RadioButtonFixture
import com.intellij.testGuiFramework.framework.Timeouts
import com.intellij.testGuiFramework.impl.*
import org.fest.swing.fixture.ContainerFixture
import org.fest.swing.timing.Timeout
import org.junit.Test
import training.commands.kotlin.TaskContext
import training.learn.CourseManager
import training.learn.interfaces.Lesson
import training.learn.lesson.LessonListener
import java.awt.Container
import java.util.concurrent.CompletableFuture
import javax.swing.JRadioButton
import kotlin.concurrent.thread
import kotlin.test.assertTrue


/**
 *  Use next gradle task to invoke test:
 *  gradle -Dtest.single=SelectLessonTest clean test -Didea.gui.test.alternativeIdePath="<path_to_installed_IDE>"
 */
class RubyLessonsGuiTestCase : GuiTestCase() {
  @Test
  fun rubyNavigationLessonTest() {
    val courseManager = CourseManager.instance
    openLearnProject()
    ideFrame {
      waitForBackgroundTasksToFinish()
      toolwindow(id = "Learn") {
        content {
          for (module in courseManager.modules) {
            if (module.lessons.isEmpty()) continue
            linkLabel(module.name).click()
            for (lesson in module.lessons) {
              val doneOrTimeout = lessonCompleteFuture(lesson)
              TaskContext.inTestMode = true
              linkLabel(lesson.name).click()
              assertTrue(doneOrTimeout.get(), "lesson " + lesson.name + " should be passed")
              System.err.println("Passed " + lesson.name)
            }
            linkLabel("All Topics").click()
          }
        }
      }
    }
  }

  private fun lessonCompleteFuture(lesson: Lesson): CompletableFuture<Boolean> {
    val doneOrTimeout = CompletableFuture<Boolean>()
    val timeoutThread = thread(name = "GUI lesson test timeout") {
      try {
        Thread.sleep(10000)
        doneOrTimeout.complete(false)
      }
      catch (e: InterruptedException) {
        // ignore it
      }
   }
    lesson.addLessonListener(object : LessonListener {
      override fun lessonStarted(lesson: Lesson) {
        //do nothing
      }

      override fun lessonPassed(lesson: Lesson) {
        doneOrTimeout.complete(true)
        timeoutThread.interrupt()
      }

      override fun lessonClosed(lesson: Lesson) {
        //do nothing
      }

      override fun lessonNext(lesson: Lesson) {
        //do nothing
      }
    })
    return doneOrTimeout
  }

  private fun openLearnProject() {
    CourseManager.instance.showGotMessage = false
    welcomeFrame {
      actionLink("Learn IntelliJ IDEA").click()
      dialog {
        radioButtonContainingText("Ruby").select()
        button("Start Learning").click()
      }
    }
  }

  private fun <C : Container> ContainerFixture<C>.radioButtonContainingText(
      labelText: String,
      ignoreCase: Boolean = false,
      timeout: Timeout = Timeouts.defaultTimeout): RadioButtonFixture {
    val radioButton: JRadioButton = findComponentWithTimeout(timeout) { it.isShowing && it.isVisible && it.text.contains(labelText, ignoreCase) }
    return RadioButtonFixture(robot(), radioButton)
  }
}