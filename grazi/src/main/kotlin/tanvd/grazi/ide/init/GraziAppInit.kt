package tanvd.grazi.ide.init

import com.intellij.application.subscribe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.project.DumbAware
import tanvd.grazi.ide.GraziInspection
import tanvd.grazi.ide.GraziLifecycle
import tanvd.grazi.language.LangTool
import tanvd.grazi.spellcheck.GraziSpellchecker

class GraziAppInit : BaseComponent, DumbAware {
    override fun initComponent() {
        GraziLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziSpellchecker)
        GraziLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangTool)
        GraziLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziInspection)

        GraziLifecycle.publisher.init()
    }
}
