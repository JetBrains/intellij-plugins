// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.lang

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.wm.ToolWindowAnchor

/**
 * @author Sergey Karashevich
 */
class SwiftSupport : AbstractLangSupport() {
  override val primaryLanguage: String
    get() = "swift"

  override val defaultProductName: String = "AppCode"

  override val projectResourcePath: String =
    "/learnProjects/" + ApplicationNamesInfo.getInstance().fullProductName.toLowerCase() + "_swift/LearnProjectSwift"

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {
  }

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override fun getToolWindowAnchor(): ToolWindowAnchor {
    return ToolWindowAnchor.RIGHT
  }
}