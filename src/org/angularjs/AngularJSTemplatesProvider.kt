package org.angularjs
import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider
import com.intellij.openapi.diagnostic.Logger
public open class AngularJSTemplatesProvider() : DefaultLiveTemplatesProvider {

    public override fun getDefaultLiveTemplateFiles(): Array<String>? {
        return array("liveTemplates/AngularJS")
    }
    public override fun getHiddenLiveTemplateFiles(): Array<String>? {
        return null
    }

    {
        LOG?.info("Hello")
    }
    class object {
        private val LOG : Logger? = Logger.getInstance("#com.intellij.codeInsight.template.impl.TemplateSettings")
    }
}