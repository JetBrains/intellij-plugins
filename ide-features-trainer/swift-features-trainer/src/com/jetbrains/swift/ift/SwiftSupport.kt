// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.swift.ift

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.wm.ToolWindowAnchor
import training.lang.AbstractLangSupport
import training.util.getFeedbackLink
import java.util.*

internal class SwiftSupport : AbstractLangSupport() {
  override val primaryLanguage: String
    get() = "Swift"

  override val langCourseFeedback get() = getFeedbackLink(this, true)

  override val defaultProductName: String = "AppCode"

  override val projectResourcePath: String =
    "learnProjects/" + ApplicationNamesInfo.getInstance().fullProductName.lowercase(Locale.getDefault()) + "_swift/LearnProjectSwift"

  override fun cleanupBeforeLessons(project: Project) {
    // Do nothing for Swift
  }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {
  }

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override fun getToolWindowAnchor() = ToolWindowAnchor.RIGHT
}