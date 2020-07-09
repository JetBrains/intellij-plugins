// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import icons.FeaturesTrainerIcons
import training.actions.ChooseProgrammingLanguageForLearningAction
import training.lang.LangManager
import training.learn.interfaces.Lesson
import training.learn.lesson.LessonListener
import training.learn.lesson.LessonManager
import training.learn.lesson.LessonProcessor
import training.learn.lesson.XmlLesson
import training.learn.lesson.kimpl.ApplyTaskLessonContext
import training.learn.lesson.kimpl.DocumentationModeTaskContext
import training.learn.lesson.kimpl.KLesson
import training.ui.views.LanguageChoosePanel
import training.ui.views.LearnPanel
import training.ui.views.ModulesPanel
import training.util.useNewLearningUi

private const val IDE_FEATURES_TRAINER_TOOLBAR = "IdeFeaturesTrainerToolbar"

class LearnToolWindow internal constructor(val project: Project, private val wholeToolWindow: ToolWindow) : SimpleToolWindowPanel(true, true), DataProvider {
  val parentDisposable: Disposable = wholeToolWindow.disposable

  private var scrollPane: JBScrollPane
  var learnPanel: LearnPanel? = null
    private set
  private val modulesPanel: ModulesPanel = ModulesPanel(this)

  private var newUiLearnScrollPane: JBScrollPane? = null
  // If an user want to show all steps we save current "progress" here
  private var hiddenLearnPanel: LearnPanel? = null

  init {
    reinitViewsInternal()
    scrollPane = if (LangManager.getInstance().isLangUndefined()) {
      JBScrollPane(LanguageChoosePanel(this))
    }
    else {
      JBScrollPane(modulesPanel)
    }
    setContent(scrollPane)
    if (useNewLearningUi) {
      toolbar = createMenuToolBar().component
      val lessonListener = object : LessonListener {
        override fun lessonPassed(lesson: Lesson) {
          reinitViews()
        }
      }

      LessonManager.instance.addLessonListener(lessonListener)
      Disposer.register(parentDisposable, Disposable {
        LessonManager.instance.removeLessonListener(lessonListener)
        if (LearningUiManager.activeToolWindow == this) {
          LearningUiHighlightingManager.clearHighlights()
        }
      })
      wholeToolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
        override fun contentRemoved(event: ContentManagerEvent) {
          if (LearningUiManager.activeToolWindow == this@LearnToolWindow) {
            if (hiddenLearnPanel == null) {
              LessonManager.instance.stopLesson()
            }
          }
          learnPanel = null
        }
      })
    }
  }

  private fun reinitViewsInternal() {
    if (!useNewLearningUi) {
      learnPanel = LearnPanel(this)
    }
    modulesPanel.updateMainPanel()
  }

  fun setMeSelected() {
    val contentManager = wholeToolWindow.contentManager
    val myTab = contentManager.contents.firstOrNull { it.component == this } ?: return
    contentManager.setSelectedContent(myTab, true)
  }

  fun setLearnPanel(lesson: Lesson, documentationMode: Boolean = false) {
    if (useNewLearningUi) {
      if (!documentationMode) {
        hiddenLearnPanel = null
      }
      val contentManager = wholeToolWindow.contentManager
      contentManager.contents.filter { it.component != this }.forEach {
        contentManager.removeContent(it, true)
      }
      learnPanel = LearnPanel(this, lesson, documentationMode)
      val lessonToolWindowTab = object: SimpleToolWindowPanel(true, true) {
        init {
          setContent(JBScrollPane(learnPanel).also { newUiLearnScrollPane = it })
        }
      }
      val lessonTab = contentManager.factory.createContent(lessonToolWindowTab, lesson.name, false)
      lessonTab.isCloseable = true
      lessonToolWindowTab.toolbar = createLessonToolBar().component
      contentManager.addContent(lessonTab)
      contentManager.setSelectedContent(lessonTab, true)
    }
    else {
      scrollPane.setViewportView(learnPanel)
      scrollPane.revalidate()
      scrollPane.repaint()
    }
  }

  fun setModulesPanel() {
    modulesPanel.updateMainPanel()
    scrollPane.setViewportView(modulesPanel)
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  fun setChooseLanguageView() {
    scrollPane.setViewportView(LanguageChoosePanel(this))
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  fun updateScrollPane() {
    scrollPane.viewport.revalidate()
    scrollPane.viewport.repaint()
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  fun reinitViews() {
    reinitViewsInternal()
    updateScrollPane()
  }

  private fun createMenuToolBar(): ActionToolbar {
    val group = DefaultActionGroup()

    group.add(object : AnAction(FeaturesTrainerIcons.FeatureTrainer) {
      override fun actionPerformed(e: AnActionEvent) {
        System.err.println("click!")
      }

      override fun update(e: AnActionEvent) {
        e.presentation.text = "Continue Learning"
      }
    })

    group.add(ChooseProgrammingLanguageForLearningAction(this))
    group.add(ActionManager.getInstance().getAction("LearningDocumentationModeAction"))
    group.add(ActionManager.getInstance().getAction("ResetLearningProgressAction"))
    return ActionManager.getInstance().createActionToolbar(IDE_FEATURES_TRAINER_TOOLBAR, group, true)
  }

  private fun createLessonToolBar(): ActionToolbar {
    val group = DefaultActionGroup()
    group.add(ActionManager.getInstance().getAction("RestartLessonAction"))
    group.add(ActionManager.getInstance().getAction("PreviousLessonAction"))
    group.add(ActionManager.getInstance().getAction("NextLessonAction"))
    group.add(ActionManager.getInstance().getAction("learn.next.lesson"))
    group.add(ActionManager.getInstance().getAction("LearningDocumentationModeAction"))

    group.add(object : AnAction(AllIcons.Toolwindows.ToolWindowFavorites) {
      override fun actionPerformed(e: AnActionEvent) {
        System.err.println("click!")
      }
    })
    group.add(object : AnAction(AllIcons.Actions.ShortcutFilter) {
      override fun actionPerformed(e: AnActionEvent) {
        System.err.println("click!")
      }
    })
    return ActionManager.getInstance().createActionToolbar(IDE_FEATURES_TRAINER_TOOLBAR, group, true)
  }

  fun openInDocumentationMode(lesson: Lesson) {
    setLearnPanel(lesson, true)
    learnPanel?.setLessonName(lesson.name)
    learnPanel?.setModuleName(lesson.module.name)
    learnPanel?.modulePanel?.init(lesson)
    if (lesson is KLesson) {
      lesson.lessonContent(ApplyTaskLessonContext(DocumentationModeTaskContext(project)))
    }
    else if (lesson is XmlLesson) {
      LessonProcessor.process(project, lesson, null, true)
    }
  }

  fun showSteps() {
    val lesson = learnPanel?.lesson ?: return
    hiddenLearnPanel = learnPanel
    openInDocumentationMode(lesson)
  }

  fun restoreLesson() {
    if (hiddenLearnPanel == null) return
    learnPanel = hiddenLearnPanel
    hiddenLearnPanel = null
    newUiLearnScrollPane?.setViewportView(learnPanel)
  }
}
