// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk

class JavaScriptLangSupport : AbstractLangSupport() {

  companion object {
    @JvmStatic
    val lang: String = "JavaScript"
  }

  override val primaryLanguage: String
    get() = lang

  override val defaultProjectName: String = "LearnJavaScriptProject"

  override val defaultProductName: String = "WebStorm"

  override val langCourseFeedback: String?
    get() = """
        Read <a href="http://blog.jetbrains.com/webstorm/2020/04/learning-plugin-for-webstorm/">this</a> blog post to learn more about how to get the most out of this training.<br><br>
        Have an idea how to make the training experience better? Then please complete <a href="https://forms.gle/EhBiJmN5R638htFv9">this</a> short survey so we can improve the training for you and other WebStorm users.""".trimIndent()

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {
  }

  override fun checkSdk(sdk: Sdk?, project: Project) {}
}