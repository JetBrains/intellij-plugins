// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.framework.FrameworkType
import com.intellij.framework.detection.DetectedFrameworkDescription
import com.intellij.framework.detection.FileContentPattern
import com.intellij.framework.detection.FrameworkDetectionContext
import com.intellij.framework.detection.FrameworkDetector
import com.intellij.ide.projectView.actions.MarkRootActionBase
import com.intellij.json.JsonFileType
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.StandardPatterns
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileContent
import org.angular2.lang.Angular2Bundle

/**
 * @author Dennis.Ushakov
 */
class AngularJSFrameworkDetector : FrameworkDetector(AngularJSFramework.ID) {

  override fun getFileType(): FileType {
    return JsonFileType.INSTANCE
  }

  override fun createSuitableFilePattern(): ElementPattern<FileContent> {
    return FileContentPattern.fileContent().withName(
      StandardPatterns.string().with(object : PatternCondition<String>("cli-json-name") {
        override fun accepts(s: String, context: ProcessingContext): Boolean {
          return AngularCliUtil.isAngularJsonFile(s)
        }
      })
    ).with(object : PatternCondition<FileContent>("notLibrary") {
      override fun accepts(content: FileContent, context: ProcessingContext): Boolean {
        return !JSLibraryUtil.isProbableLibraryFile(content.file)
      }
    })
  }

  override fun detect(newFiles: Collection<VirtualFile>,
                      context: FrameworkDetectionContext): List<DetectedFrameworkDescription> {
    return if (newFiles.isNotEmpty() && !isConfigured(newFiles, context.project)) {
      listOf(AngularCLIFrameworkDescription(newFiles))
    }
    else emptyList()
  }

  private fun isConfigured(files: Collection<VirtualFile>, project: Project?): Boolean {
    if (project == null) return false

    for (file in files) {
      val module = ModuleUtilCore.findModuleForFile(file, project)
      if (module != null) {
        for (root in ModuleRootManager.getInstance(module).excludeRootUrls) {

          if (root == file.parent.url + "/tmp") {
            return true
          }
        }
      }
    }
    return false
  }

  override fun getFrameworkType(): FrameworkType {
    return AngularJSFramework.INSTANCE
  }

  private inner class AngularCLIFrameworkDescription(private val myNewFiles: Collection<VirtualFile>) : DetectedFrameworkDescription() {

    override fun getRelatedFiles(): Collection<VirtualFile> {
      return myNewFiles
    }

    override fun getSetupText(): String {
      return Angular2Bundle.message("angular.description.angular-cli")
    }

    override fun getDetector(): FrameworkDetector {
      return this@AngularJSFrameworkDetector
    }

    override fun setupFramework(modifiableModelsProvider: ModifiableModelsProvider, modulesProvider: ModulesProvider) {
      for (module in modulesProvider.modules) {
        val model = modifiableModelsProvider.getModuleModifiableModel(module)
        val item = myNewFiles.firstOrNull()
        val entry = if (item != null) MarkRootActionBase.findContentEntry(model, item) else null
        if (entry == null) {
          modifiableModelsProvider.disposeModuleModifiableModel(model)
          continue
        }
        AngularJSProjectConfigurator.excludeDefault(item!!.parent, entry)
        modifiableModelsProvider.commitModuleModifiableModel(model)
        for (vf in myNewFiles) {
          AngularCliUtil.createRunConfigurations(module.project, vf.parent)
        }
      }
    }

    override fun equals(other: Any?): Boolean {
      return other is AngularCLIFrameworkDescription && myNewFiles == other.myNewFiles
    }

    override fun hashCode(): Int {
      return myNewFiles.hashCode()
    }
  }
}
