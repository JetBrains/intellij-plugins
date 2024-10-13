package org.jetbrains.qodana.staticAnalysis

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.SourceFolder
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.java.JpsJavaExtensionService

suspend fun markGenFolderAsGeneratedSources(module: Module): SourceFolder {
  val rootManager = ModuleRootManager.getInstance(module).modifiableModel
  val contentRoot = rootManager.contentEntries[0]
  val generatedProperties = JpsJavaExtensionService.getInstance().createSourceRootProperties("", true)
  val genFolder = contentRoot.addSourceFolder(contentRoot.url + "/gen", JavaSourceRootType.SOURCE, generatedProperties)
  writeAction { rootManager.commit() }
  return genFolder
}