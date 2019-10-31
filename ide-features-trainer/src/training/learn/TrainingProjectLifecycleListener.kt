/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.impl.ProjectLifecycleListener
import com.intellij.openapi.util.io.FileUtil
import training.lang.LangManager

class TrainingProjectLifecycleListener : ProjectLifecycleListener {
  override fun projectComponentsInitialized(project: Project) {
    val langSupport = LangManager.getInstance().getLangSupport() ?: return
    if (FileUtil.pathsEqual(project.basePath, NewLearnProjectUtil.projectFilePath(langSupport))) {
      langSupport.setProjectListeners(project)
    }
  }

  init {
    ApplicationManager.getApplication().messageBus.connect().subscribe(ProjectLifecycleListener.TOPIC, this)
  }

}