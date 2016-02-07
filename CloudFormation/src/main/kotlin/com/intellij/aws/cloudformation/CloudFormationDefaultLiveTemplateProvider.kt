package com.intellij.aws.cloudformation

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import org.jetbrains.annotations.NonNls

class CloudFormationDefaultLiveTemplateProvider : DefaultLiveTemplatesProvider {

  override fun getDefaultLiveTemplateFiles(): Array<String> {
    return DEFAULT_TEMPLATES
  }

  override fun getHiddenLiveTemplateFiles(): Array<String>? {
    return null
  }

  companion object {
    @NonNls private val DEFAULT_TEMPLATES = arrayOf("/liveTemplates/cloudformation")
  }
}
