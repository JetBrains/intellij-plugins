package org.angularjs

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

public open class AngularJSTemplatesProvider(): DefaultLiveTemplatesProvider {
    public override fun getDefaultLiveTemplateFiles(): Array<String>? {
        return array<String>("liveTemplates/AngularJS")
    }
    public override fun getHiddenLiveTemplateFiles(): Array<String>? {
        return null
    }
}
