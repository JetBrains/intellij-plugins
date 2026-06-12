package com.intellij.openRewrite.recipe

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

private const val OPEN_REWRITE_PREFIX = "rewrite-"
private val OPEN_REWRITE_ARTIFACTS = listOf("core", "gradle", "groovy", "hcl",
                                            "java", "java-8", "java-11", "java-17", "java-21", "java-tck", "java-test",
                                            "json", "maven", "properties", "probuf", "test", "xml", "yaml")

internal class OpenRewriteDefaultLibraryResolver : OpenRewriteLibraryResolver {
  override fun resolveDependencies(virtualFile: VirtualFile?, version: String, project: Project): List<String> =
    OPEN_REWRITE_ARTIFACTS.map { "$OPEN_REWRITE_GROUP_ID:$OPEN_REWRITE_PREFIX$it:$version" }
}