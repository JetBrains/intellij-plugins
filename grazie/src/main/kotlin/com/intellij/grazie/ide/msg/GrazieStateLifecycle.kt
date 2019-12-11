// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.msg

import com.intellij.application.subscribe
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.GrazieCommitInspection
import com.intellij.grazie.ide.GrazieInspection
import com.intellij.grazie.jlanguage.LangDetector
import com.intellij.grazie.jlanguage.LangTool
import com.intellij.grazie.spellcheck.GrazieSpellchecker
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic

interface GrazieStateLifecycle {
  companion object {
    val topic = Topic.create("grazie_state_lifecycle_topic", GrazieStateLifecycle::class.java)
    val publisher by lazy { ApplicationManager.getApplication().messageBus.syncPublisher(topic) }

    init {
      topic.subscribe(ApplicationManager.getApplication(), LangTool)
      topic.subscribe(ApplicationManager.getApplication(), LangDetector)
      topic.subscribe(ApplicationManager.getApplication(), GrazieSpellchecker)
      topic.subscribe(ApplicationManager.getApplication(), GrazieCommitInspection)
      topic.subscribe(ApplicationManager.getApplication(), GrazieInspection)
    }
  }

  /** Initialize Grazie with passed state */
  fun init(state: GrazieConfig.State) {}

  /** Update Grazie state */
  fun update(prevState: GrazieConfig.State, newState: GrazieConfig.State) {}
}
