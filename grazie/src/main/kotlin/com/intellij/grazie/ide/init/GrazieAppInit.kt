// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.init

import com.intellij.grazie.ide.GrazieCommitInspection
import com.intellij.grazie.ide.GrazieInspection
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.jlanguage.LangDetector
import com.intellij.grazie.jlanguage.LangTool
import com.intellij.ide.ApplicationInitializedListener
import com.intellij.openapi.application.ApplicationManager

class GrazieAppInit : ApplicationInitializedListener {
  override fun componentsInitialized() {
    val connection = ApplicationManager.getApplication().messageBus.connect()
    connection.subscribe(GrazieStateLifecycle.topic, LangTool)
    connection.subscribe(GrazieStateLifecycle.topic, LangDetector)
    connection.subscribe(GrazieStateLifecycle.topic, GrazieCommitInspection)
    connection.subscribe(GrazieStateLifecycle.topic, GrazieInspection)
  }
}
