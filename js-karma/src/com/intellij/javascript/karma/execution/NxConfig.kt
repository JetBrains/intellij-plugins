// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution

import com.google.gson.JsonParser
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.webcore.util.JsonUtil
import java.io.IOException

internal class NxConfig(private val myConfig: VirtualFile) {
  fun getProjectName(): String? {
    try {
      return doParseProjectName()
    }
    catch (e: Exception) {
      LOG.info("Failed to find project in " + myConfig.path, e)
      return null
    }
  }

  @Throws(IOException::class)
  private fun doParseProjectName(): String? {
    if (!myConfig.isValid) return null
    val text = JSLinterConfigFileUtil.loadActualText(myConfig)
    val rootObj = JsonParser.parseString(text).asJsonObject
    return JsonUtil.getChildAsString(rootObj, "name")
  }

  companion object {
    private val LOG: Logger = logger<NxConfig>()

    fun findNxConfig(project: Project, contextDirectory: VirtualFile): NxConfig? {
      val config = JSProjectUtil.findFileUpToContentRoot(project, contextDirectory, "project.json")
      return if (config != null) NxConfig(config) else null
    }
  }
}
