package tanvd.grazi.ide.init

import com.intellij.application.subscribe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.project.DumbAware
import tanvd.grazi.ide.GraziInspection
import tanvd.grazi.ide.msg.GraziAppLifecycle
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.LangTool
import tanvd.grazi.spellcheck.GraziSpellchecker

class GraziAppInit : BaseComponent, DumbAware {
    override fun initComponent() {
        GraziAppLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziSpellchecker)
        GraziAppLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangTool)
        GraziAppLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziInspection)

        GraziAppLifecycle.publisher.init()

        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziSpellchecker)
    }
}
