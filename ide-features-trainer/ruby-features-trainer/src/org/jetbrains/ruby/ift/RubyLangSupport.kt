// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift

import com.intellij.CommonBundle
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.VersionComparatorUtil
import org.jetbrains.plugins.ruby.RBundle
import org.jetbrains.plugins.ruby.ruby.RModuleUtil
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType
import org.jetbrains.plugins.ruby.ruby.sdk.RubyVersionUtil
import org.jetbrains.plugins.ruby.version.management.SdkRefresher
import training.FeaturesTrainerIcons
import training.lang.AbstractLangSupport
import training.learn.LearnBundle
import training.learn.exceptons.InvalidSdkException
import training.learn.exceptons.NoSdkException
import training.project.ReadMeCreator
import training.util.getFeedbackLink

internal class RubyLangSupport : AbstractLangSupport() {
  private val rubyProjectName: String
    get() = "RubyMineLearningProject"

  override val scratchFileName: String = "Learning.rb"

  override val langCourseFeedback get() = getFeedbackLink(this, false)

  override val readMeCreator = ReadMeCreator()

  override fun checkSdk(sdk: Sdk?, project: Project) {
    if (project.name != rubyProjectName) return

    if (sdk == null) {
      throw NoSdkException()
    }
    if (sdk.sdkType !is RubySdkType) {
      throw InvalidSdkException(LearnBundle.message("dialog.message.jdk.ruby.required"))
    }
    val rubyVersion = RubyVersionUtil.getShortVersion(
      sdk.versionString ?: throw InvalidSdkException(LearnBundle.message("dialog.message.jdk.ruby.version.missed")))
    if (VersionComparatorUtil.compare(rubyVersion, "2.3.0") < 0) {
      throw InvalidSdkException(LearnBundle.message("dialog.message.jdk.ruby.version.required"))
    }
  }

  override fun getSdkForProject(project: Project, selectedSdk: Sdk?): Sdk? {
    return try {
      super.getSdkForProject(project, selectedSdk)
    }
    catch (e: NoSdkException) {
      SdkRefresher.refreshAll()
      try {
        super.getSdkForProject(project, selectedSdk)
      }
      catch (e: NoSdkException) {
        showSdkNotFoundDialog(project)
        null
      }
    }
  }

  private fun showSdkNotFoundDialog(project: Project) {
    val decision = Messages.showOkCancelDialog(project,
                                               RubyLessonsBundle.message(
                                                 "ruby.sdk.not.found.dialog.message"),
                                               RubyLessonsBundle.message("ruby.sdk.not.found.dialog.title"),
                                               RubyLessonsBundle.message("ruby.sdk.not.found.dialog.ok"),
                                               CommonBundle.getCancelButtonText(),
                                               FeaturesTrainerIcons.PluginIcon)
    if (decision == Messages.OK) {
      ShowSettingsUtil.getInstance().showSettingsDialog(project, RBundle.message("ruby.ide.sdk.configurable.name"))
    }
  }

  override fun applyProjectSdk(sdk: Sdk, project: Project) {
    super.applyProjectSdk(sdk, project)
    RModuleUtil.getInstance().changeModuleSdk(sdk, project.module)
  }

  override val contentRootDirectoryName: String
    get() = rubyProjectName

  override val primaryLanguage: String
    get() = "ruby"

  override val defaultProductName: String = "RubyMine"

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = { }

  override fun blockProjectFileModification(project: Project, file: VirtualFile): Boolean = true

  private val Project.module: Module
    get() {
      val modules = ModuleManager.getInstance(this).modules
      assert(modules.size == 1)
      return modules[0]
    }

  override val sampleFilePath = "src/sandbox.rb"

  override fun isSdkConfigured(project: Project): Boolean = ProjectRootManager.getInstance(project).projectSdk?.sdkType is RubySdkType
}