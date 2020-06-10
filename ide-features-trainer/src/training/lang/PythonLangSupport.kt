// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.lang

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.templates.github.ZipUtil
import com.intellij.util.download.DownloadableFileService
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.sdk.PyDetectedSdk
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.flavors.PythonSdkFlavor
import com.jetbrains.python.sdk.flavors.VirtualEnvSdkFlavor
import java.io.File
import java.nio.file.Files

/**
 * @author Sergey Karashevich
 */
class PythonLangSupport : AbstractLangSupport() {
  override val defaultProjectName = "PyCharmLearningProject"

  override val primaryLanguage = "Python"

  override val defaultProductName: String = "PyCharm"

  override fun getSdkForProject(project: Project): Sdk? {
    //find registered python SDKs
    var pySdk: Sdk? = ProjectJdkTable.getInstance().allJdks.find { sdk -> sdk.sdkType is PythonSdkType && isNoOlderThan27(sdk) }

    //register first detected SDK
    if (pySdk == null) {
      val sdkList: List<Sdk> = detectPySdks()
      pySdk = sdkList.firstOrNull() ?: return null
      ApplicationManager.getApplication().runWriteAction { ProjectJdkTable.getInstance().addJdk(pySdk) }
    }
    return pySdk
  }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {}

  override fun checkSdk(sdk: Sdk?, project: Project) {
  }

  //detect sdk with version 2.7 and higher
  private fun detectPySdks(): List<Sdk> {
    val model = ProjectSdksModel()
    model.reset(null)
    val sdkHomes = mutableListOf<String>()
    sdkHomes.addAll(VirtualEnvSdkFlavor.getInstance().suggestHomePaths(null))
    PythonSdkFlavor.getApplicableFlavors()
      .filter { it !is VirtualEnvSdkFlavor }
      .forEach { sdkHomes.addAll(it.suggestHomePaths(null)) }
    sdkHomes.sort()
    return SdkConfigurationUtil.filterExistingPaths(PythonSdkType.getInstance(), sdkHomes, model.sdks)
      .mapTo(mutableListOf(), ::PyDetectedSdk).filter { sdk -> isNoOlderThan27(sdk) }
  }

  private fun isNoOlderThan27(sdk: Sdk) =
    PythonSdkFlavor.getFlavor(sdk)?.getLanguageLevel(sdk)?.isAtLeast(LanguageLevel.PYTHON27) ?: false

  override fun blockProjectFileModification(project: Project, file: VirtualFile): Boolean {
    return file.path != "${project.basePath}${VfsUtilCore.VFS_SEPARATOR_CHAR}${projectSandboxRelativePath}"
  }

  override val installRemoteProject: ((File) -> Unit)? = { projectDirectory ->
    val service = DownloadableFileService.getInstance()
    val zipName = "project.zip"
    val url = "https://github.com/pallets/jinja/archive/2.11.1.zip"
    val fileDescription = service.createFileDescription(url, zipName)
    val downloader = service.createDownloader(listOf(fileDescription), zipName)
    val tempDir = Files.createTempDirectory("IFT-temp")
    val files = downloader.download(tempDir.toFile())
    if (files.size != 1) {
      error("Cannot download $url into $tempDir/$zipName")
    }
    val zipFile = files[0].first
    ZipUtil.unzipWithProgressSynchronously(null, "Unzip demo project", zipFile, projectDirectory, true)
  }

  override val projectSandboxRelativePath = "app/sandbox.rb"
}
