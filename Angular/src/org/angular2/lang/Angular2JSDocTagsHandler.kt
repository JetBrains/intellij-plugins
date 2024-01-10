// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang

import com.intellij.lang.javascript.psi.jsdoc.JSDocCustomTagDefinition
import com.intellij.lang.javascript.psi.jsdoc.JSDocCustomTagsHandler
import com.intellij.lang.javascript.psi.jsdoc.JSDocCustomTagsHandler.JSDocBlockTagQuickDocBuilder
import com.intellij.lang.javascript.psi.jsdoc.JSDocTag
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagDefinition

class Angular2JSDocTagsHandler : JSDocCustomTagsHandler {
  override fun getCustomTagsDefinitions(): List<JSDocTagDefinition> = listOf(
    NG_MODULE_TAG, PUBLIC_API, USAGE_NOTES
  )

  override fun handleBlockTag(tag: JSDocTag, quickDocBuilder: JSDocBlockTagQuickDocBuilder): Boolean {
    if (tag.tagDefinition == USAGE_NOTES) {
      val descr = tag.getDescriptionText(quickDocBuilder)
      if (!descr.isNullOrBlank()) {
        quickDocBuilder.appendDescription("<h3>Usage Notes</h3>\n")
        quickDocBuilder.appendDescription(descr)
      }
      return true
    }
    return false
  }

  private val NG_MODULE_TAG = JSDocCustomTagDefinition(
    "NgModule",
    label = "NgModule"
  )

  private val PUBLIC_API = JSDocCustomTagDefinition(
    "PublicApi",
    label = "Public API",
    hasDescription = false,
  )

  private val USAGE_NOTES = JSDocCustomTagDefinition(
    "UsageNotes"
  )

}