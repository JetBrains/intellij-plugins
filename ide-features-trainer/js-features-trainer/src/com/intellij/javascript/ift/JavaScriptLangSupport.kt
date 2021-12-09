// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import training.dsl.LessonUtil
import training.lang.AbstractLangSupport
import training.project.ReadMeCreator
import training.util.getFeedbackLink

class JavaScriptLangSupport : AbstractLangSupport() {

  companion object {
    @JvmStatic
    val lang: String = "JavaScript"
  }

  override val primaryLanguage: String
    get() = lang

  override val contentRootDirectoryName: String = "LearnJavaScriptProject"

  override val defaultProductName: String = "WebStorm"

  override val langCourseFeedback get() = getFeedbackLink(this, true)
  
  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {
  }

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override val readMeCreator: ReadMeCreator = object : ReadMeCreator() {
    override val iftDescription = JsLessonsBundle.message("js.readme.description", LessonUtil.productName)
    override val howToUseHeader = JsLessonsBundle.message("js.readme.usage.header")
    private val feedbackHeader = JsLessonsBundle.message("js.readme.feedback.header")

    override fun createReadmeMdText(): String = """
## $welcomeHeader

$iftDescription

### $howToUseHeader

$toolWindowDescription

$experiencedUsersRemark

### $shortcutsHeader

$shortcutProblemDescription

$bugTrackerRemark

### $exitHeader

$exitOptionsDescription

### $feedbackHeader

$feedbackRequest
""" // do not use trim because derived implementations will need to follow the current indent
  }

}