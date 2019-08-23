package tanvd.grazi.ide.init

import com.intellij.application.subscribe
import com.intellij.ide.ApplicationInitializedListener
import com.intellij.openapi.application.ApplicationManager
import tanvd.grazi.ide.GraziCommitInspection
import tanvd.grazi.ide.GraziInspection
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.LangDetector
import tanvd.grazi.language.LangTool
import tanvd.grazi.spellcheck.GraziSpellchecker
import tanvd.grazi.utils.LangToolInstrumentation

class GraziAppInit : ApplicationInitializedListener {
    override fun componentsInitialized() {
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangTool)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangDetector)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziSpellchecker)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziCommitInspection)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziInspection)

        LangToolInstrumentation.reloadEnglish()
    }
}
