// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.init

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.msg.GrazieStateLifecycle

open class GrazieProjectInit : StartupActivity, DumbAware {
  override fun runActivity(project: Project) {
    GrazieStateLifecycle.publisher.init(GrazieConfig.get(), project)
  }
}

