/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.lang

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.sdk.PyDetectedSdk
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.flavors.PythonSdkFlavor
import com.jetbrains.python.sdk.flavors.VirtualEnvSdkFlavor
import training.learn.exceptons.InvalidSdkException
import training.learn.exceptons.NoSdkException

/**
 * @author Sergey Karashevich
 */
class PythonLangSupport : AbstractLangSupport() {
  
  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun importLearnProject(): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override val primaryLanguage: String
    get() = "python"


  override fun getSdkForProject(project: Project): Sdk {
    //find registered python SDKs
    var pySdk: Sdk? = ProjectJdkTable.getInstance().allJdks.find { sdk -> sdk.sdkType is PythonSdkType && isNoOlderThan27(sdk)}

    //register first detected SDK
    if (pySdk == null) {
      val sdkList: List<Sdk> = detectPySdks()
      pySdk = sdkList.firstOrNull() ?: throw NoSdkException("Python")
      ApplicationManager.getApplication().runWriteAction { ProjectJdkTable.getInstance().addJdk(pySdk) }
    }
    return pySdk
  }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {}

  override fun checkSdk(sdk: Sdk?, project: Project) {
    if (sdk?.sdkType is PythonSdkType) {
      if (!isNoOlderThan27(sdk)) {
        throw InvalidSdkException("Please use at least JDK 1.6 or IDEA SDK with corresponding JDK")
      }
    }
    else {
      throw NoSdkException()
    }
  }

  //detect sdk with version 2.7 and higher
  private fun detectPySdks(): List<Sdk> {
    val model = ProjectSdksModel()
    model.reset(null)
    val sdkHomes = mutableListOf<String>()
    sdkHomes.addAll(VirtualEnvSdkFlavor.INSTANCE.suggestHomePaths(null))
    PythonSdkFlavor.getApplicableFlavors()
        .filter { it !is VirtualEnvSdkFlavor }
        .forEach { sdkHomes.addAll(it.suggestHomePaths(null)) }
    sdkHomes.sort()
    return SdkConfigurationUtil.filterExistingPaths(PythonSdkType.getInstance(), sdkHomes, model.sdks).mapTo(mutableListOf(), ::PyDetectedSdk).filter { sdk -> isNoOlderThan27(sdk) }
  }

  private fun isNoOlderThan27(sdk: Sdk) = PythonSdkFlavor.getFlavor(sdk)!!.getLanguageLevel(sdk).isAtLeast(LanguageLevel.PYTHON27)

}
