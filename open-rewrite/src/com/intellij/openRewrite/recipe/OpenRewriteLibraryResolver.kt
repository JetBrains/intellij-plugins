package com.intellij.openRewrite.recipe

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface OpenRewriteLibraryResolver {
  companion object {
    private val EP_NAME: ExtensionPointName<OpenRewriteLibraryResolver> =
      ExtensionPointName.create("com.intellij.openRewrite.libraryResolver")

    fun resolveDependencies(virtualFile: VirtualFile?, version: String, project: Project): List<String> {
      for (resolver in EP_NAME.extensionList) {
        val dependencies = resolver.resolveDependencies(virtualFile, version, project)
        if (dependencies.isNotEmpty()) {
          return dependencies
        }
      }
      return emptyList()
    }
  }

  fun resolveDependencies(virtualFile: VirtualFile?, version: String, project: Project): List<String>
}