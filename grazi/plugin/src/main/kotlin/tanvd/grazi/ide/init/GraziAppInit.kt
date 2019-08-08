package tanvd.grazi.ide.init

import com.intellij.application.subscribe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.project.DumbAware
import tanvd.grazi.EnglishChonker
import tanvd.grazi.ide.GraziCommitInspection
import tanvd.grazi.ide.GraziInspection
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.LangTool
import tanvd.grazi.spellcheck.GraziSpellchecker


class GraziAppInit : BaseComponent, DumbAware {
    override fun initComponent() {
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangTool)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziSpellchecker)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziInspection)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziCommitInspection)

        EnglishChonker.reloadEnglish()
    }
}
