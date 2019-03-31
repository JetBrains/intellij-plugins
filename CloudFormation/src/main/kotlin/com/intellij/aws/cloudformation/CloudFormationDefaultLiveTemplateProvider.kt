package com.intellij.aws.cloudformation

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

class CloudFormationDefaultLiveTemplateProvider : DefaultLiveTemplatesProvider {
  override fun getDefaultLiveTemplateFiles(): Array<String> {
    return arrayOf(
        "/liveTemplates/cloudformation_json",
        "/liveTemplates/cloudformation_yaml"
    )
  }

  override fun getHiddenLiveTemplateFiles(): Array<String>? {
    return null
  }
}
