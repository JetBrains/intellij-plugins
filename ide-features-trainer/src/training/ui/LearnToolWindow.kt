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
import icons.FeaturesTrainerIcons
import training.actions.ChooseProgrammingLanguageForLearningAction
import training.lang.LangManager
import training.learn.interfaces.Lesson
import training.learn.lesson.LessonListener
import training.learn.lesson.LessonManager
import training.ui.views.LanguageChoosePanel
import training.ui.views.LearnPanel
import training.ui.views.ModulesPanel
import training.util.useNewLearningUi

private const val IDE_FEATURES_TRAINER_TOOLBAR = "IdeFeaturesTrainerToolbar"

class LearnToolWindow internal constructor(val project: Project,
                                           private val wholeToolWindow: ToolWindow,
                                           parentDisposable: Disposable
) : SimpleToolWindowPanel(true, true), DataProvider {
  private var scrollPane: JBScrollPane
  var learnPanel: LearnPanel? = null
    private set
  private val modulesPanel: ModulesPanel = ModulesPanel(this)

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
      })
    }
  }

  private fun reinitViewsInternal() {
    if (!useNewLearningUi) {
      learnPanel = LearnPanel(this)
    }
    modulesPanel.updateMainPanel()
  }

  fun setLearnPanel(lesson: Lesson) {
    if (useNewLearningUi) {
      val contentManager = wholeToolWindow.contentManager
      contentManager.contents.filter { it.component != this }.forEach {
        contentManager.removeContent(it, true)
      }
      learnPanel = LearnPanel(this)
      val lessonToolWindowTab = object: SimpleToolWindowPanel(true, true) {
        init {
          setContent(JBScrollPane(learnPanel))
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
    group.add(ActionManager.getInstance().getAction("ResetLearningProgressAction"))
    return ActionManager.getInstance().createActionToolbar(IDE_FEATURES_TRAINER_TOOLBAR, group, true)
  }

  private fun createLessonToolBar(): ActionToolbar {
    val group = DefaultActionGroup()
    group.add(ActionManager.getInstance().getAction("RestartLessonAction"))
    group.add(ActionManager.getInstance().getAction("PreviousLessonAction"))
    group.add(ActionManager.getInstance().getAction("NextLessonAction"))

    group.add(ActionManager.getInstance().getAction("learn.next.lesson"))
    group.add(object : AnAction(AllIcons.Actions.ListFiles) {
      override fun actionPerformed(e: AnActionEvent) {
        System.err.println("click!")
      }
    })
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
}
