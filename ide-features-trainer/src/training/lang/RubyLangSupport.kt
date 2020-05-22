// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.lang

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.VersionComparatorUtil
import org.jetbrains.plugins.ruby.gem.GemDependency
import org.jetbrains.plugins.ruby.gem.GemInstallUtil
import org.jetbrains.plugins.ruby.gem.gem.GemRunner
import org.jetbrains.plugins.ruby.ruby.RModuleUtil
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType
import org.jetbrains.plugins.ruby.ruby.sdk.RubyVersionUtil
import org.jetbrains.plugins.ruby.version.management.SdkRefresher
import training.learn.exceptons.InvalidSdkException
import training.learn.exceptons.NoSdkException
import java.io.File

class RubyLangSupport : AbstractLangSupport() {
  private val rubyProjectName: String
    get() = "RubyMineLearningProject"

  override fun checkSdk(sdk: Sdk?, project: Project) {
    if (project.name != rubyProjectName) return

    if (sdk == null) {
      throw NoSdkException()
    }
    if (sdk.sdkType !is RubySdkType) {
      throw InvalidSdkException("Selected SDK should be Ruby SDK")
    }
    val rubyVersion = RubyVersionUtil.getShortVersion(
      sdk.versionString ?: throw InvalidSdkException("SDK should have a version"))
    if (VersionComparatorUtil.compare(rubyVersion, "2.3.0") < 0) {
      throw InvalidSdkException("Ruby version should be at least 2.3")
    }
  }

  override fun getSdkForProject(project: Project): Sdk? {
    return try {
      super.getSdkForProject(project)
    }
    catch (e: NoSdkException) {
      SdkRefresher.refreshAll()
      super.getSdkForProject(project)
    }
  }

  override fun applyProjectSdk(sdk: Sdk, project: Project) {
    super.applyProjectSdk(sdk, project)
    RModuleUtil.getInstance().changeModuleSdk(sdk, project.module)
  }

  override val defaultProjectName: String
    get() = rubyProjectName

  override val primaryLanguage: String
    get() = "ruby"

  override val defaultProductName: String = "RubyMine"

  override fun applyToProjectAfterConfigure(): (Project) -> Unit {
    return { project ->
      val tempDirectory = FileUtil.createTempDirectory("bundler_gem", null, true)
      val bundlerGem = File(tempDirectory, "bundler-2.0.1.gem")
      val bundler = RubyLangSupport::class.java.getResourceAsStream("/learnProjects/ruby/gems/bundler-2.0.1.gem")
      FileUtil.writeToFile(bundlerGem, FileUtil.loadBytes(bundler))

      val sdk = ProjectRootManager.getInstance(project).projectSdk!!
      val module = project.module

      GemInstallUtil.installGemsRequirements(sdk,
                                             module,
                                             listOf(GemDependency.any(bundlerGem.absolutePath)),
                                             false, false, false, false, true, null, mutableMapOf())

      GemRunner.bundle(module, sdk, "install", null, null, null, "--local")
    }
  }

  override fun blockProjectFileModification(project: Project, file: VirtualFile): Boolean {
    return file.path != "${project.basePath}${VfsUtilCore.VFS_SEPARATOR_CHAR}$projectSandboxRelativePath"
  }

  private val Project.module: Module
    get() {
      val modules = ModuleManager.getInstance(this).modules
      assert(modules.size == 1)
      return modules[0]
    }

  override val projectSandboxRelativePath = "app/sandbox.rb"
}