// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.util.containers.ContainerUtil
import training.actions.OpenLessonAction
import training.lang.LangManager
import training.lang.LangSupport
import training.learn.exceptons.InvalidSdkException
import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.lesson.LessonManager
import training.ui.LearnToolWindowFactory
import training.ui.LearningUiManager
import training.util.isLearningDocumentationMode

class CourseManager internal constructor() {
  val mapModuleVirtualFile = mutableMapOf<Module, VirtualFile>()

  private val allModules: MutableList<Module> = mutableListOf()

  val modules: List<Module>
    get() = LangManager.getInstance().getLangSupport()?.let { filterByLanguage(it) } ?: emptyList()

  val lessonsForModules: List<Lesson>
    get() = modules.map { it.lessons }.flatten()

  init {
    initXmlModules()
  }

  fun clearModules() {
    allModules.clear()
  }

  fun initXmlModules() {
    val trainingModules = TrainingModules.EP_NAME.extensions
    for (modules in trainingModules) {
      val primaryLanguage = LangManager.getInstance().getLangSupportById(modules.language) ?:
                            error("Cannot find primary language support: ${modules.language}")
      val classLoader = modules.loaderForClass
      for (module in modules.children) {
        val moduleFilename = module.xmlPath
        val xmlModule = XmlModule.initModule(moduleFilename, primaryLanguage, classLoader)
                        ?: throw Exception("Unable to init module (is null) from file: $moduleFilename")
        allModules.add(xmlModule)
      }
    }
  }

  //TODO: remove this method or convert XmlModule to a Module
  fun registerVirtualFile(module: Module, virtualFile: VirtualFile) {
    mapModuleVirtualFile[module] = virtualFile
  }

  /**
   * @param projectWhereToOpen -- where to open projectWhereToOpen
   */
  @Synchronized
  fun openLesson(projectWhereToOpen: Project, lesson: Lesson?) {
    LessonManager.instance.stopLesson()
    if (lesson == null) return //todo: remove null lessons
    if (isLearningDocumentationMode(projectWhereToOpen)) {
      if (projectWhereToOpen == LearningUiManager.activeToolWindow?.project) {
        LessonManager.instance.stopLesson()
        LearningUiManager.activeToolWindow = null
      }
      LearnToolWindowFactory.learnWindowPerProject[projectWhereToOpen]?.let { learnToolWindow ->
        learnToolWindow.openInDocumentationMode(lesson)
        return
      }
    }
    val focusOwner = IdeFocusManager.getInstance(projectWhereToOpen).focusOwner
    val parent = DataManager.getInstance().getDataContext(focusOwner)
    val data = mutableMapOf<String, Any?>()
    val openLessonAction = OpenLessonAction(lesson)
    data[OpenLessonAction.PROJECT_WHERE_TO_OPEN_DATA_KEY.name] = projectWhereToOpen
    val context = SimpleDataContext.getSimpleContext(data, parent)
    val event = AnActionEvent.createFromAnAction(openLessonAction, null, "", context)
    ActionUtil.performActionDumbAware(openLessonAction, event)
  }

  /**
   * checking environment to start learning plugin. Checking SDK.

   * @param project where lesson should be started
   * *
   * @throws InvalidSdkException - if project SDK is not suitable for module
   */
  @Throws(InvalidSdkException::class)
  fun checkEnvironment(project: Project) {
    val sdk = ProjectRootManager.getInstance(project).projectSdk
    LangManager.getInstance().getLangSupport()?.checkSdk(sdk, project) ?: throw Exception("Language for learning plugin is not defined")
  }

  fun findLesson(lessonName: String): Lesson? {
    return modules
      .flatMap { it.lessons }
      .firstOrNull { it.name.toUpperCase() == lessonName.toUpperCase() }
  }

  fun getNextNonPassedLesson(currentLesson: Lesson?): Lesson? {
    val lessons = lessonsForModules
    val list = if (currentLesson != null) {
      val index = lessons.indexOf(currentLesson)
      if (index < 0) return null
      lessons.subList(index + 1, lessons.size) + lessons.subList(0, index + 1)
    }
    else {
      lessons
    }
    return list.find { !it.passed }
  }

  fun giveNextModule(currentLesson: Lesson): Module? {
    var nextModule: Module? = null
    val module = currentLesson.module
    val size = modules.size
    if (size == 1) return null

    for (i in 0 until size) {
      if (modules[i] == module) {
        if (i + 1 < size) nextModule = modules[i + 1]
        break
      }
    }
    if (nextModule == null || nextModule.lessons.isEmpty()) return null
    return nextModule
  }

  fun calcLessonsForLanguage(primaryLangSupport: LangSupport): Int {
    return ContainerUtil.concat(filterByLanguage(primaryLangSupport).map { m -> m.lessons }).size
  }

  fun calcPassedLessonsForLanguage(primaryLangSupport: LangSupport): Int {
    return filterByLanguage(primaryLangSupport)
      .flatMap { m -> m.lessons }
      .filter { it.passed }
      .size
  }

  private fun filterByLanguage(primaryLangSupport: LangSupport): List<Module> {
    return allModules.filter { it.primaryLanguage == primaryLangSupport }
  }

  fun getModulesByLanguage(primaryLangSupport: LangSupport): List<Module> = filterByLanguage(primaryLangSupport)

  companion object {
    val instance: CourseManager
      get() = ServiceManager.getService(CourseManager::class.java)
  }

}