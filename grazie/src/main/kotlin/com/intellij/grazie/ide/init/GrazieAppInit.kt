// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.init

import com.intellij.application.subscribe
import com.intellij.ide.ApplicationInitializedListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.grazie.ide.GrazieCommitInspection
import com.intellij.grazie.ide.GrazieInspection
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.language.LangDetector
import com.intellij.grazie.language.LangTool
import com.intellij.grazie.spellcheck.GrazieSpellchecker

class GrazieAppInit : ApplicationInitializedListener {
  override fun componentsInitialized() {
    GrazieStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangTool)
    GrazieStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangDetector)
    GrazieStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GrazieSpellchecker)
    GrazieStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GrazieCommitInspection)
    GrazieStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GrazieInspection)
  }
}
