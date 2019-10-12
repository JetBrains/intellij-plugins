// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.msg

import com.intellij.grazie.GrazieConfig
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

interface GrazieStateLifecycle {
  companion object {
    val topic = Topic.create("grazie_state_lifecycle_topic", GrazieStateLifecycle::class.java)
    val publisher by lazy { ApplicationManager.getApplication().messageBus.syncPublisher(topic) }
  }

  /** Initialize Grazie with passed state */
  fun init(state: GrazieConfig.State, project: Project) {}

  /** Update state of object */
  fun update(prevState: GrazieConfig.State, newState: GrazieConfig.State, project: Project) {}
}
