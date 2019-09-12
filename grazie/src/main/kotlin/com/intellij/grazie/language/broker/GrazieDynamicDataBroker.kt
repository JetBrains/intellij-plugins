// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.language.broker

import com.intellij.grazie.GrazieDynamic
import org.languagetool.tools.databroker.ResourceDataBroker
import java.io.InputStream
import java.net.URL

object GrazieDynamicDataBroker : ResourceDataBroker {
  override fun getFromResourceDirAsStream(path: String): InputStream? {
    val completePath = getCompleteResourceUrl(path)
    val resourceAsStream = GrazieDynamic.getResourceAsStream(completePath)
    assertNotNull(resourceAsStream, path, completePath)
    return resourceAsStream
  }


  override fun getFromResourceDirAsUrl(path: String): URL? {
    val completePath = getCompleteResourceUrl(path)
    val resource = GrazieDynamic.getResource(completePath)
    assertNotNull(resource, path, completePath)
    return resource
  }

  private fun getCompleteResourceUrl(path: String): String {
    return appendPath(resourceDir, path)
  }

  override fun getFromRulesDirAsStream(path: String): InputStream? {
    val completePath = getCompleteRulesUrl(path)
    val resourceAsStream = GrazieDynamic.getResourceAsStream(completePath)
    assertNotNull(resourceAsStream, path, completePath)
    return resourceAsStream
  }


  override fun getFromRulesDirAsUrl(path: String): URL? {
    val completePath = getCompleteRulesUrl(path)
    val resource = GrazieDynamic.getResource(completePath)
    assertNotNull(resource, path, completePath)
    return resource
  }

  private fun assertNotNull(`object`: Any?, path: String, completePath: String) {
    if (`object` == null) {
      throw RuntimeException("Path $path not found in class path at $completePath")
    }
  }

  private fun getCompleteRulesUrl(path: String): String {
    return appendPath(rulesDir, path)
  }

  private fun appendPath(baseDir: String, path: String): String {
    val completePath = StringBuilder(baseDir)
    if (!this.rulesDir.endsWith("/") && !path.startsWith("/")) {
      completePath.append('/')
    }
    if (this.rulesDir.endsWith("/") && path.startsWith("/") && path.length > 1) {
      completePath.append(path.substring(1))
    }
    else {
      completePath.append(path)
    }
    return completePath.toString()
  }

  override fun resourceExists(path: String): Boolean {
    val completePath = getCompleteResourceUrl(path)
    return GrazieDynamic.getResource(completePath) != null
  }

  override fun ruleFileExists(path: String): Boolean {
    val completePath = getCompleteRulesUrl(path)
    return GrazieDynamic.getResource(completePath) != null
  }

  override fun getResourceDir(): String {
    return ResourceDataBroker.RESOURCE_DIR
  }

  override fun getRulesDir(): String {
    return ResourceDataBroker.RULES_DIR
  }
}
