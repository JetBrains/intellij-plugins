package com.intellij.lang.javascript.linter.eslint

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.SemVer
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.vfs.VfsUtil

internal data class ESLintFlatConfigData(
  val flatConfigMode: Boolean,
  val flatConfigFile: VirtualFile?,
) {
  companion object {
    @JvmStatic
    fun determineFlatConfig(
      project: Project,
      specifiedConfigFile: VirtualFile?,
      packageVersion: SemVer?,
      workingDirectory: VirtualFile,
    ): ESLintFlatConfigData {
      return if (specifiedConfigFile != null) {
        determineConfigFromSpecifiedFile(specifiedConfigFile, packageVersion)
      }
      else {
        determineConfigFromProject(project, packageVersion, workingDirectory)
      }
    }

    private fun determineConfigFromSpecifiedFile(
      specifiedConfigFile: VirtualFile,
      packageVersion: SemVer?,
    ): ESLintFlatConfigData {
      val isCustomFlatConfigFile = !EslintUtil.isCustomLegacyConfigFileName(specifiedConfigFile.name)
      val flatConfigMode = EslintUtil.isUseFlatConfigMode(packageVersion, isCustomFlatConfigFile)

      return ESLintFlatConfigData(flatConfigMode = flatConfigMode, flatConfigFile = specifiedConfigFile)
    }

    private fun determineConfigFromProject(
      project: Project,
      packageVersion: SemVer?,
      workingDirectory: VirtualFile,
    ): ESLintFlatConfigData {
      val flatConfigFile = findFirstFlatConfigFile(project, workingDirectory)
      val flatConfigMode = EslintUtil.isUseFlatConfigMode(packageVersion, flatConfigFile != null)

      return ESLintFlatConfigData(flatConfigMode = flatConfigMode, flatConfigFile = flatConfigFile)
    }

    private fun findFirstFlatConfigFile(
      project: Project,
      workingDirectory: VirtualFile,
    ): VirtualFile? {
      var foundFlatConfigFile: VirtualFile? = null

      JSProjectUtil.processDirectoriesUpToContentRoot(project, workingDirectory) { dir ->
        val flatConfigs = VfsUtil.getChildren(dir) { child ->
          EslintUtil.isFlatConfigFileName(child.name)
        }
        if (flatConfigs.isNotEmpty()) {
          foundFlatConfigFile = flatConfigs.first()
          false
        }
        else {
          true
        }
      }

      return foundFlatConfigFile
    }
  }
}
