package com.intellij.openRewrite

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.MavenDependencyUtil

private const val OPEN_REWRITE_VERSION = "8.16.0"
private const val OPEN_REWRITE_CORE = "org.openrewrite:rewrite-core:$OPEN_REWRITE_VERSION"

class OpenRewriteProjectDescriptor : DefaultLightProjectDescriptor() {
  private val libraries = mutableMapOf(
    OPEN_REWRITE_CORE to DependencyScope.COMPILE,
  )

  override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
    super.configureModule(module, model, contentEntry)
    configureModel(model)
  }

  private fun configureModel(model: ModifiableRootModel) {
    for ((mavenId, scope) in libraries) {
      MavenDependencyUtil.addFromMaven(model, mavenId, false, scope)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as OpenRewriteProjectDescriptor

    return libraries == other.libraries
  }

  override fun hashCode(): Int {
    return libraries.hashCode()
  }
}