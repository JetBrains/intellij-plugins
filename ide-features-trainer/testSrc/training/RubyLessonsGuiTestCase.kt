package training

import com.intellij.testGuiFramework.fixtures.ComponentFixture
import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.fixtures.RadioButtonFixture
import com.intellij.testGuiFramework.framework.Timeouts
import com.intellij.testGuiFramework.impl.GuiTestCase
import com.intellij.testGuiFramework.impl.actionLink
import com.intellij.testGuiFramework.impl.findComponentWithTimeout
import com.intellij.testGuiFramework.impl.linkLabel
import org.fest.swing.fixture.ContainerFixture
import org.fest.swing.timing.Timeout
import org.junit.Test
import training.commands.kotlin.TaskContext
import training.lang.LangManager
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

      for (module in courseManager.modules) {
        if (module.lessons.isEmpty()) continue
        linkAtLearnPanel { linkLabel(module.name).click() }
        for (lesson in module.lessons) {
          val doneOrTimeout = lessonCompleteFuture(lesson)
          TaskContext.inTestMode = true
          linkAtLearnPanel { linkLabel(lesson.name).click() }
          assertTrue(doneOrTimeout.get(), "lesson " + lesson.name + " should be passed")
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
      LangManager.getInstance().loadState(LangManager.State("ruby"))
      actionLink("Learn IntelliJ IDEA").click()
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