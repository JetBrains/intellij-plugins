// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.wm.ToolWindowAnchor
import training.project.ProjectUtils

class JavaScriptLangSupport : AbstractLangSupport() {

  companion object {
    @JvmStatic
    val lang: String = "JavaScript"
  }

  override val primaryLanguage: String
    get() = lang;


  override fun importLearnProject(): Project? {
    TODO("not implemented")
  }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {
  }

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    return ProjectUtils.importOrOpenProject("/learnProjects/javascript/LearnJavaScriptProject", "LearnJavaScriptProject")
  }

  override fun getToolWindowAnchor(): ToolWindowAnchor {
    return ToolWindowAnchor.LEFT
  }

}