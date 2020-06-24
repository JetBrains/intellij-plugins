// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.lang

import com.intellij.ide.impl.NewProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl
import com.intellij.openapi.roots.impl.LanguageLevelProjectExtensionImpl
import com.intellij.openapi.util.Computable
import com.intellij.pom.java.LanguageLevel
import training.learn.exceptons.InvalidSdkException
import training.learn.exceptons.NoSdkException
import training.util.JdkSetupUtil

/**
 * @author Sergey Karashevich
 */
class JavaLangSupport : AbstractLangSupport() {
  override val primaryLanguage: String = "JAVA"

  override val defaultProductName: String = "IDEA"

  override fun applyProjectSdk(sdk: Sdk, project: Project) {
    val applySdkAction = {
      ApplicationManager.getApplication().runWriteAction { NewProjectUtil.applyJdkToProject(project, sdk) }
    }
    CommandProcessor.getInstance().executeCommand(project, applySdkAction, null, null)
  }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = { newProject ->
    //Set language level for LearnProject
    LanguageLevelProjectExtensionImpl.getInstanceImpl(newProject).currentLevel = LanguageLevel.JDK_1_6
  }

  //Java SDK and project configuration staff
  override fun getSdkForProject(project: Project): Sdk? {
    return if (ApplicationManager.getApplication().isUnitTestMode)
      ApplicationManager.getApplication().runWriteAction(Computable<Sdk> { getJavaSdk() })
    else
      getJavaSdk()
  }

  private fun getJavaSdk(): Sdk {

    //check for stored jdk
    val jdkList = getJdkList()
    if (jdkList.isNotEmpty()) {
      jdkList
        .filter { JavaSdk.getInstance().getVersion(it) != null && JavaSdk.getInstance().getVersion(it)!!.isAtLeast(JavaSdkVersion.JDK_1_6) }
        .forEach { return it }
    }

    //if no predefined jdks -> add bundled jdk to available list and return it
    val javaSdk = JavaSdk.getInstance()

    val bundleList = JdkSetupUtil.findJdkPaths().bundles
    //we believe that Idea has at least one bundled jdk
    val jdkBundle = bundleList.first()
    val jdkBundleLocation = JdkSetupUtil.getJavaHomePath(jdkBundle)
    val jdkName = "JDK_" + jdkBundle.bundleVersion.toString()
    val newJdk = javaSdk.createJdk(jdkName, jdkBundleLocation, false)

    val foundJdk = ProjectJdkTable.getInstance().findJdk(newJdk.name, newJdk.sdkType.name)
    if (foundJdk == null) ApplicationManager.getApplication().runWriteAction { ProjectJdkTable.getInstance().addJdk(newJdk) }

    ApplicationManager.getApplication().runWriteAction {
      val modifier = newJdk.sdkModificator
      JavaSdkImpl.attachJdkAnnotations(modifier)
      modifier.commitChanges()
    }

    return newJdk
  }

  override fun checkSdk(sdk: Sdk?, project: Project) {
    val sdkTypeId = sdk?.sdkType
    if (sdkTypeId is JavaSdk) {
      val version = sdkTypeId.getVersion(sdk)
      if (version != null && !version.isAtLeast(JavaSdkVersion.JDK_1_6)) {
        throw InvalidSdkException("Please use at least JDK 1.6 or IDEA SDK with corresponding JDK")
      }
    }
    else {
      throw NoSdkException()
    }
  }

  private fun getJdkList(): MutableList<Sdk> {
    val type = JavaSdk.getInstance()
    val allJdks = ProjectJdkTable.getInstance().allJdks
    return allJdks.filterTo(mutableListOf()) { isCompatibleJdk(it, type) }
  }

  private fun isCompatibleJdk(projectJdk: Sdk, type: SdkType?) = (type == null || projectJdk.sdkType === type)

  companion object {
    @JvmStatic
    val lang: String = "JAVA"
  }
}
