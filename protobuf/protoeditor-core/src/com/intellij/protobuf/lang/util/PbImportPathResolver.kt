package com.intellij.protobuf.lang.util

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.protobuf.lang.PbFileType
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

internal object PbImportPathResolver {
  fun findSuitableImportPaths(importStatement: String, project: Project): List<String> {
    val relativeProtoPath = FileUtil.toSystemIndependentName(importStatement)
    return DumbService.getInstance(project).runReadActionInSmartMode(Computable {
      FileTypeIndex.getFiles(PbFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        .asSequence()
        .map { FileUtil.toSystemIndependentName(it.url) }
        .filter { it.endsWith(relativeProtoPath) }
        .map { it.removeSuffix(relativeProtoPath).removeSuffix("/") }
        .filter { it.isNotBlank() }
        .distinct()
        .toList()
    })
  }
}