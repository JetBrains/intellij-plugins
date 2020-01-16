// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import training.project.ProjectUtils

class JavaScriptLangSupport : AbstractLangSupport() {

  companion object {
    @JvmStatic
    val lang: String = "JavaScript"
  }

  override val primaryLanguage: String
    get() = lang

  override val langCourseFeedback: String?
    get() = """Have an idea how to make the training experience better? Then please complete <a href="https://forms.gle/EhBiJmN5R638htFv9">this</a> short survey so we can improve the training for you and other WebStorm users."""

  override fun importLearnProject(): Project? {
    TODO("not implemented")
  }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {
  }

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    return ProjectUtils.importOrOpenProject(
      "/learnProjects/javascript/LearnJavaScriptProject",
      "LearnJavaScriptProject",
      javaClass.classLoader
    )
  }
}