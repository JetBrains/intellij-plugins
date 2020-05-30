// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.util

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.ui.UIUtil
import training.lang.LangManager
import training.lang.LangSupport
import training.learn.CourseManager
import training.learn.NewLearnProjectUtil
import training.learn.lesson.LessonManager
import training.learn.lesson.LessonStateManager
import training.ui.UiManager
import java.awt.Point
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.swing.Icon

fun createNamedSingleThreadExecutor(name: String): ExecutorService =
  Executors.newSingleThreadExecutor(ThreadFactoryBuilder().setNameFormat(name).build())

fun findLanguageByID(id: String): Language? {
  val effectiveId = if (id.toLowerCase() == "cpp") {
    "ObjectiveC"
  }
  else {
    id
  }
  return Language.findLanguageByID(effectiveId)
}

fun editorPointForBalloon(myEditor: Editor): Point {
  val offset = myEditor.caretModel.currentCaret.offset
  val position = myEditor.offsetToVisualPosition(offset)
  return myEditor.visualPositionToXY(position)
}

fun createBalloon(text: String): Balloon = createBalloon(text, 3000)
fun createBalloon(text: String, delay: Long): Balloon =
  JBPopupFactory.getInstance()
    .createHtmlTextBalloonBuilder(text, null, UIUtil.getToolTipBackground(), null)
    .setHideOnClickOutside(true)
    .setCloseButtonEnabled(true)
    .setHideOnKeyOutside(true)
    .setAnimationCycle(0)
    .setFadeoutTime(delay)
    .createBalloon()

val featureTrainerMode: TrainingMode
  get() =
    @Suppress("InvalidBundleOrProperty")
    when (Registry.stringValue("ide.features.trainer.mode")) {
      "public-demo" -> TrainingMode.DEMO
      "development" -> TrainingMode.DEVELOPMENT
      "" -> TrainingMode.NORMAL
      else -> TrainingMode.NORMAL
    }

const val trainerPluginConfigName: String = "ide-features-trainer.xml"

val featureTrainerVersion: String by lazy {
  val featureTrainerPluginId = PluginManagerCore.getPluginByClassName(CourseManager::class.java.name)
  PluginManagerCore.getPlugin(featureTrainerPluginId)?.version ?: "UNKNOWN"
}

val isFeatureTrainerSnapshot: Boolean by lazy {
  featureTrainerVersion.contains("SNAPSHOT")
}

fun createAnAction(icon: Icon, action: (AnActionEvent) -> Unit): AnAction {
  return object : AnAction(icon) {
    override fun isDumbAware() = true
    override fun actionPerformed(e: AnActionEvent) {
      action(e)
    }
  }
}

fun clearTrainingProgress() {
  LessonStateManager.resetPassedStatus()
  UiManager.setLanguageChooserView()
}

fun resetPrimaryLanguage(activeLangSupport: LangSupport): Boolean {
  val old = LangManager.getInstance().getLangSupport()
  if (activeLangSupport != old) {
    LangManager.getInstance().updateLangSupport(activeLangSupport)
    UiManager.setModulesView()
    LessonManager.instance.stopLesson()
    return true
  }
  return false
}

fun findLanguageSupport(project: Project): LangSupport? {
  val langSupport = LangManager.getInstance().getLangSupport() ?: return null
  if (FileUtil.pathsEqual(project.basePath, NewLearnProjectUtil.projectFilePath(langSupport))) {
    return langSupport
  }
  return null
}
