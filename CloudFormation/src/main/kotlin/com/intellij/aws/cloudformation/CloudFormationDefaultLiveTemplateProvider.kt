package com.intellij.aws.cloudformation

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

class CloudFormationDefaultLiveTemplateProvider : DefaultLiveTemplatesProvider {
  override fun getDefaultLiveTemplateFiles(): Array<String> {
    return arrayOf("/liveTemplates/cloudformation")
  }

  override fun getHiddenLiveTemplateFiles(): Array<String>? {
    return null
  }
}
