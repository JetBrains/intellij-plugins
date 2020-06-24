// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.actions

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.ide.startup.StartupManagerEx
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import training.lang.LangManager
import training.lang.LangSupport
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.NewLearnProjectUtil
import training.learn.exceptons.*
import training.learn.interfaces.Lesson
import training.learn.interfaces.ModuleType
import training.learn.lesson.LessonManager
import training.learn.lesson.LessonProcessor
import training.learn.lesson.XmlLesson
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContextImpl
import training.learn.lesson.kimpl.LessonExecutor
import training.learn.lesson.listeners.NextLessonListener
import training.learn.lesson.listeners.StatisticLessonListener
import training.project.ProjectUtils
import training.ui.LearnToolWindowFactory
import training.ui.LearningUiManager
import training.util.findLanguageByID
import java.awt.FontFormatException
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.concurrent.ExecutionException

/**
 * @author Sergey Karashevich
 */
class OpenLessonAction(val lesson: Lesson) : AnAction(lesson.name) {

  override fun actionPerformed(e: AnActionEvent) {
    val whereToStartLessonProject = e.getData(PROJECT_WHERE_TO_OPEN_DATA_KEY)
    val project = e.project

    if (project != null && whereToStartLessonProject != null) {
      LOG.debug("${project.name}: Action performed -> openLesson(${lesson.name})")
      openLesson(whereToStartLessonProject, lesson)
    }
    else {
      //in case of starting from Welcome Screen
      // Or user activated `Open Lesson` action manually.
      LOG.debug("${project?.name}: Action performed -> openLearnToolWindowAndShowModules(${lesson.name}))")
      openLearnProjectFromWelcomeScreen(project, lesson)
    }
  }

  @Synchronized
  @Throws(BadModuleException::class, BadLessonException::class, IOException::class, FontFormatException::class, InterruptedException::class,
          ExecutionException::class, LessonIsOpenedException::class)
  private fun openLesson(projectWhereToStartLesson: Project, lesson: Lesson) {
    LOG.debug("${projectWhereToStartLesson.name}: start openLesson method")
    try {
      // Stop the current lesson (if any)
      LessonManager.instance.stopLesson()

      val activeToolWindow = LearningUiManager.activeToolWindow
      if (activeToolWindow != null && activeToolWindow.project != projectWhereToStartLesson) {
        // maybe we need to add some confirmation dialog?
        activeToolWindow.setModulesPanel()
      }
      LearningUiManager.activeToolWindow = LearnToolWindowFactory.learnWindowPerProject[projectWhereToStartLesson]

      val langSupport = LangManager.getInstance().getLangSupport() ?: throw Exception("Language for learning plugin is not defined")
      if (lesson.isOpen) throw LessonIsOpenedException(lesson.name + " is opened")

      val vf: VirtualFile?
      var learnProject = LearningUiManager.learnProject
      if (learnProject != null && langSupport.defaultProjectName != learnProject.name) {
        learnProject = null // We are in the project from another course
      }
      LOG.debug("${projectWhereToStartLesson.name}: trying to get cached LearnProject ${learnProject != null}")
      if (learnProject == null) learnProject = findLearnProjectInOpenedProjects(langSupport)
      LOG.debug("${projectWhereToStartLesson.name}: trying to find LearnProject in opened projects ${learnProject != null}")
      if (learnProject != null) LearningUiManager.learnProject = learnProject

      if (lesson.module.moduleType == ModuleType.SCRATCH) {
        LOG.debug("${projectWhereToStartLesson.name}: scratch based lesson")
        CourseManager.instance.checkEnvironment(projectWhereToStartLesson)
        vf = getScratchFile(projectWhereToStartLesson, lesson, langSupport.filename)
      }
      else { //if this file should be opened in LearnProject
        //0. learnProject == null but this project is LearnProject then just getFileInLearnProject
        if ((learnProject == null || learnProject.isDisposed) && projectWhereToStartLesson.name == langSupport.defaultProjectName) {
          LOG.debug(
            "${projectWhereToStartLesson.name}: 0. learnProject is null but the current project (${projectWhereToStartLesson.name}) is LearnProject then just getFileInLearnProject")
          LearningUiManager.learnProject = projectWhereToStartLesson
          vf = getFileInLearnProject(lesson)
          //1. learnProject == null and current project has different name then initLearnProject and register post startup open lesson
        }
        else if (learnProject == null || learnProject.isDisposed) {
          LOG.debug("${projectWhereToStartLesson.name}: 1. learnProject is null or disposed")
          initLearnProject(projectWhereToStartLesson) {
            LOG.debug("${projectWhereToStartLesson.name}: 1. ... LearnProject has been started")
            openLessonWhenLearnProjectStart(lesson, it)
            LOG.debug("${projectWhereToStartLesson.name}: 1. ... open lesson when learn project has been started")
          }
          return
          //2. learnProject != null and learnProject is disposed then reinitProject and getFileInLearnProject
        }
        else if (learnProject.isOpen && projectWhereToStartLesson != learnProject) {
          LOG.debug("${projectWhereToStartLesson.name}: 3. LearnProject is opened but not focused. Ask user to focus to LearnProject")
          askSwitchToLearnProjectBack(learnProject, projectWhereToStartLesson)
          return
          //4. learnProject != null and learnProject is opened and focused getFileInLearnProject
        }
        else if (learnProject.isOpen && projectWhereToStartLesson == learnProject) {
          LOG.debug("${projectWhereToStartLesson.name}: 4. LearnProject is the current project")
          vf = getFileInLearnProject(lesson)
        }
        else {
          throw Exception("Unable to start Learn project")
        }
      }
      if (vf == null) return  //if user aborts opening lesson in LearnProject or Virtual File couldn't be computed

      LOG.debug("${projectWhereToStartLesson.name}: VirtualFile for lesson has been created/found")
      val currentProject =
        if (lesson.module.moduleType != ModuleType.SCRATCH) LearningUiManager.learnProject!!.also {
          // close all tabs in the currently opened learning project
          ProjectUtils.closeAllEditorsInProject(it)
        }
        else projectWhereToStartLesson

      if (lesson.module.moduleType != ModuleType.SCRATCH || LearningUiManager.learnProject == projectWhereToStartLesson) {
        // do not change view environment for scratch lessons in user project
        hideOtherViews(projectWhereToStartLesson)
      }

      LOG.debug("${projectWhereToStartLesson.name}: Add listeners to lesson")
      addNextLessonListenerIfNeeded(currentProject, lesson)
      addStatisticLessonListenerIfNeeded(currentProject, lesson)

      //open next lesson if current is passed
      LOG.debug("${projectWhereToStartLesson.name}: Set lesson view")
      LearningUiManager.activeToolWindow?.setLearnPanel(lesson)
      LOG.debug("${projectWhereToStartLesson.name}: XmlLesson onStart()")
      lesson.onStart()

      //to start any lesson we need to do 4 steps:
      //1. open editor or find editor
      LOG.debug("${projectWhereToStartLesson.name}: PREPARING TO START LESSON:")
      LOG.debug("${projectWhereToStartLesson.name}: 1. Open or find editor")
      var textEditor: TextEditor? = null
      if (FileEditorManager.getInstance(projectWhereToStartLesson).isFileOpen(vf)) {
        val editors = FileEditorManager.getInstance(projectWhereToStartLesson).getEditors(vf)
        for (fileEditor in editors) {
          if (fileEditor is TextEditor) {
            textEditor = fileEditor
          }
        }
      }
      if (textEditor == null) {
        val editors = FileEditorManager.getInstance(projectWhereToStartLesson).openFile(vf, true, true)
        for (fileEditor in editors) {
          if (fileEditor is TextEditor) {
            textEditor = fileEditor
          }
        }
      }
      if (textEditor!!.editor.isDisposed) throw Exception("Editor is already disposed!")

      //2. set the focus on this editor
      //FileEditorManager.getInstance(project).setSelectedEditor(vf, TextEditorProvider.getInstance().getEditorTypeId());
      LOG.debug("${projectWhereToStartLesson.name}: 2. Set the focus on this editor")
      FileEditorManager.getInstance(projectWhereToStartLesson).openEditor(OpenFileDescriptor(projectWhereToStartLesson, vf), true)

      //4. Process lesson
      LOG.debug("${projectWhereToStartLesson.name}: 4. Process lesson")
      when (lesson) {
        is XmlLesson -> LessonProcessor.process(projectWhereToStartLesson, lesson, textEditor.editor)
        is KLesson -> processDslLesson(lesson, textEditor, projectWhereToStartLesson)
      }

    }
    catch (noSdkException: NoSdkException) {
      Messages.showMessageDialog(projectWhereToStartLesson,
                                 LearnBundle.message("dialog.noSdk.message", LangManager.getInstance().getLanguageDisplayName()),
                                 LearnBundle.message("dialog.noSdk.title"), Messages.getErrorIcon())
      if (ProjectSettingsService.getInstance(projectWhereToStartLesson).chooseAndSetSdk() != null) openLesson(projectWhereToStartLesson,
                                                                                                              lesson)
    }
    catch (noSdkException: InvalidSdkException) {
      Messages.showMessageDialog(projectWhereToStartLesson,
                                 LearnBundle.message("dialog.noSdk.message", LangManager.getInstance().getLanguageDisplayName()),
                                 LearnBundle.message("dialog.noSdk.title"), Messages.getErrorIcon())
      if (ProjectSettingsService.getInstance(projectWhereToStartLesson).chooseAndSetSdk() != null) openLesson(projectWhereToStartLesson,
                                                                                                              lesson)
    }
    catch (e: Exception) {
      LOG.error(e)
    }

  }

  private fun processDslLesson(lesson: KLesson, textEditor: TextEditor, projectWhereToStartLesson: Project) {
    val executor = LessonExecutor(lesson, textEditor.editor, projectWhereToStartLesson)
    val lessonContext = LessonContextImpl(executor)
    LessonManager.instance.initDslLesson(textEditor.editor, lesson, executor)
    lesson.lessonContent(lessonContext)
    executor.processNextTask(0)
  }

  private fun hideOtherViews(project: Project) {
    ApplicationManager.getApplication().invokeLater {
      val windowManager = ToolWindowManager.getInstance(project)
      val declaredFields = ToolWindowId::class.java.declaredFields
      for (field in declaredFields) {
        if (Modifier.isStatic(field.modifiers) && field.type == String::class.java) {
          val id = field.get(null) as String
          windowManager.getToolWindow(id)?.hide(null)
        }
      }
    }
  }

  private fun addNextLessonListenerIfNeeded(currentProject: Project, lesson: Lesson) {
    val lessonListener = NextLessonListener(currentProject)
    if (!lesson.lessonListeners.any { it is NextLessonListener })
      lesson.addLessonListener(lessonListener)
  }

  private fun addStatisticLessonListenerIfNeeded(currentProject: Project, lesson: Lesson) {
    val statLessonListener = StatisticLessonListener(currentProject)
    if (!lesson.lessonListeners.any { it is StatisticLessonListener })
      lesson.addLessonListener(statLessonListener)
  }

  private fun openLearnProjectFromWelcomeScreen(projectToClose: Project?, lessonToOpen: Lesson) {
    initLearnProject(projectToClose) { project ->
      if (project.isOpen) {
        showModules(project)
        openLessonWhenLearnProjectStart(lessonToOpen, project)
      }
      else {
        StartupManager.getInstance(project).registerPostStartupActivity {
          hideOtherViews(project)
          showModules(project)
          openLessonWhenLearnProjectStart(lessonToOpen, project)
        }
      }
    }
  }

  private fun showModules(project: Project) {
    val toolWindowManager = ToolWindowManager.getInstance(project)
    toolWindowManager.getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW)?.show(null)
  }

  private fun openLessonWhenLearnProjectStart(lesson: Lesson, myLearnProject: Project) {
    fun _openLesson() {
      val toolWindowManager = ToolWindowManager.getInstance(myLearnProject)
      val learnToolWindow = toolWindowManager.getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW)
      if (learnToolWindow != null) {
        learnToolWindow.show(null)
        openLesson(myLearnProject, lesson)
      }
    }

    val startupManager = StartupManager.getInstance(myLearnProject)
    if (startupManager is StartupManagerEx && startupManager.postStartupActivityPassed()) {
      _openLesson()
    }
    else {
      startupManager.registerPostStartupActivity {
        _openLesson()
      }
    }
  }

  @Throws(IOException::class)
  private fun getScratchFile(project: Project, lesson: Lesson, filename: String): VirtualFile? {
    var vf: VirtualFile? = null
    val languageByID = findLanguageByID(lesson.lang)
    if (CourseManager.instance.mapModuleVirtualFile.containsKey(lesson.module)) {
      vf = CourseManager.instance.mapModuleVirtualFile[lesson.module]
      ScratchFileService.getInstance().scratchesMapping.setMapping(vf, languageByID)
    }
    if (vf == null || !vf.isValid) {
      //while module info is not stored

      //find file if it is existed
      vf = ScratchFileService.getInstance().findFile(ScratchRootType.getInstance(), filename, ScratchFileService.Option.existing_only)
      if (vf != null) {
        FileEditorManager.getInstance(project).closeFile(vf)
        ScratchFileService.getInstance().scratchesMapping.setMapping(vf, languageByID)
      }

      if (vf == null || !vf.isValid) {
        vf = ScratchRootType.getInstance().createScratchFile(project, filename, languageByID, "")
        assert(vf != null)
      }
      CourseManager.instance.registerVirtualFile(lesson.module, vf!!)
    }
    return vf
  }

  private fun askSwitchToLearnProjectBack(learnProject: Project, currentProject: Project) {
    Messages.showInfoMessage(currentProject,
                             LearnBundle.message("dialog.askToSwitchToLearnProject.message", learnProject.name),
                             LearnBundle.message("dialog.askToSwitchToLearnProject.title"))
  }

  @Throws(IOException::class)
  private fun getFileInLearnProject(lesson: Lesson): VirtualFile {
    val function = object : Computable<VirtualFile> {

      override fun compute(): VirtualFile {
        val learnProject = LearningUiManager.learnProject!!

        val existedFile = lesson.existedFile ?: lesson.module.primaryLanguage.projectSandboxRelativePath
        val manager = ProjectRootManager.getInstance(learnProject)
        if (existedFile != null) {
          val root = manager.contentRoots[0]
          return root.findFileByRelativePath(existedFile)!!
        }

        val fileName = lesson.fileName

        var lessonVirtualFile: VirtualFile? = null
        var roots = manager.contentSourceRoots
        if (roots.isEmpty()) {
          roots = manager.contentRoots
        }
        for (file in roots) {
          if (file.name == fileName) {
            lessonVirtualFile = file
            break
          }
          else {
            lessonVirtualFile = file.findChild(fileName)
            if (lessonVirtualFile != null) {
              break
            }
          }
        }
        if (lessonVirtualFile == null) {
          lessonVirtualFile = roots[0].createChildData(this, fileName)
        }

        CourseManager.instance.registerVirtualFile(lesson.module, lessonVirtualFile)
        return lessonVirtualFile
      }
    }

    val vf = ApplicationManager.getApplication().runWriteAction(function)
    assert(vf is VirtualFile)
    return vf
  }

  private fun initLearnProject(projectToClose: Project?, postInitCallback: (learnProject: Project) -> Unit) {
    val langSupport = LangManager.getInstance().getLangSupport() ?: throw Exception("Language for learning plugin is not defined")
    //if projectToClose is open
    findLearnProjectInOpenedProjects(langSupport)?.let {
      postInitCallback(it)
      return
    }

    if (!ApplicationManager.getApplication().isUnitTestMode && projectToClose != null)
      if (!NewLearnProjectUtil.showDialogOpenLearnProject(projectToClose))
        return //if user abort to open lesson in a new Project
    try {
      NewLearnProjectUtil.createLearnProject(projectToClose, langSupport) { learnProject ->
        langSupport.applyToProjectAfterConfigure().invoke(learnProject)
        LearningUiManager.learnProject = learnProject
        postInitCallback(learnProject)
      }
    }
    catch (e: IOException) {
      LOG.error(e)
    }
  }

  private fun findLearnProjectInOpenedProjects(langSupport: LangSupport): Project? {
    val openProjects = ProjectManager.getInstance().openProjects
    return openProjects.firstOrNull { it.name == langSupport.defaultProjectName }
  }

  companion object {
    val PROJECT_WHERE_TO_OPEN_DATA_KEY = DataKey.create<Project>("PROJECT_WHERE_TO_OPEN_DATA_KEY")
    private val LOG = logger<OpenLessonAction>()
  }
}