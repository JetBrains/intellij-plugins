/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk

/**
 * @author Sergey Karashevich
 */
class MockLangSupport(override val primaryLanguage: String) : AbstractLangSupport() {
  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun importLearnProject(): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun checkSdk(sdk: Sdk?, project: Project) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit {
    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}
