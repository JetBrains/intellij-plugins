package training.learn.lesson

import com.intellij.openapi.project.Project
import training.learn.CourseManager
import training.learn.Module
import training.learn.exceptons.BadLessonException
import training.learn.exceptons.BadModuleException
import training.learn.exceptons.LessonIsOpenedException
import training.learn.exceptons.NoProjectException
import training.learn.log.LessonLog
import java.awt.Dimension
import java.awt.FontFormatException
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by karashevich on 29/01/15.
 */
data class Lesson(val scenario: Scenario, val lang: String, val module: Module?) {

  var lessonListeners: ArrayList<LessonListener> = ArrayList<LessonListener>()
    private set
  var statistic = ArrayList<Pair<String, Long>>()
  var exerciseCount: Short = 0
    private set
  var passed: Boolean = false
  var isOpen: Boolean = false
    private set
  val name: String = scenario.name
  val id: String = scenario.id

  /*Log lesson metrics*/
  val lessonLog: LessonLog = LessonLog(this)

  init {
    lessonListeners = ArrayList<LessonListener>()
    passed = LessonStateManager.getStateFromBase(id) == LessonState.PASSED
  }

  @Deprecated("")
  @Throws(IOException::class, FontFormatException::class, LessonIsOpenedException::class)
  fun open(infoPanelDimension: Dimension) {
    //init infoPanel, check that Lesson has not opened yet
    if (isOpen) throw LessonIsOpenedException(this.name + "is opened")
    onStart()
    isOpen = true
  }


  @Throws(NoProjectException::class, BadLessonException::class, ExecutionException::class, LessonIsOpenedException::class, IOException::class, FontFormatException::class, InterruptedException::class, BadModuleException::class)
  fun open() {
    var currentProject = CourseManager.instance.currentProject
    if (currentProject == null) {
      currentProject = CourseManager.instance.learnProject
    }
    if (currentProject == null) throw NoProjectException()
    CourseManager.instance.openLesson(currentProject, this)
  }

  @Throws(NoProjectException::class, BadLessonException::class, ExecutionException::class, LessonIsOpenedException::class, IOException::class, FontFormatException::class, InterruptedException::class, BadModuleException::class)
  fun open(projectWhereToOpenLesson: Project) {
    CourseManager.instance.openLesson(projectWhereToOpenLesson, this)
  }

  fun close() {
    isOpen = false
    onClose()
  }

  //Listeners
  fun addLessonListener(lessonListener: LessonListener) {
    lessonListeners.add(lessonListener)
  }

  fun removeLessonListener(lessonListener: LessonListener) {
    if (lessonListeners.contains(lessonListener)) lessonListeners.remove(lessonListener)

  }

  fun onStart() {
    lessonLog.log("Lesson started")
    exerciseCount = 0
    statistic.add(Pair("started", System.currentTimeMillis()))
    lessonLog.resetCounter()
    lessonListeners.forEach { it.lessonStarted(this) }
  }

  private fun onItemPassed() {
    statistic.add(Pair("passed item #" + exerciseCount, System.currentTimeMillis()))
    exerciseCount++
  }

  private fun onClose() {
    lessonListeners.clear()
    statistic.add(Pair("closed", System.currentTimeMillis()))

  }

  //call onPass handlers in lessonListeners
  private fun onPass() {
    lessonLog.log("Lesson passed")
    statistic.add(Pair("finished", System.currentTimeMillis()))
    lessonListeners.forEach { it.lessonPassed(this) }

  }

  @Throws(BadLessonException::class, ExecutionException::class, IOException::class, FontFormatException::class, InterruptedException::class, BadModuleException::class, LessonIsOpenedException::class)
  fun onNextLesson() {
    lessonListeners.forEach { it.lessonNext(this) }
  }

  fun pass() {
    passed = true
    LessonStateManager.setPassed(this)
    onPass()
  }

  fun passItem() {
    onItemPassed()
  }

  internal object EditorParameters {
    val PROJECT_TREE = "projectTree"
  }

  override fun equals(other: Any?): Boolean {
    return other != null && other is Lesson && other.name == this.name
  }

  override fun hashCode(): Int {
    var result = scenario.hashCode()
    result = 31 * result + id.hashCode()
    result = 31 * result + name.hashCode()
    return result
  }

}
