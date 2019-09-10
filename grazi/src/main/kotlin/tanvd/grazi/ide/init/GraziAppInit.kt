// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

class GraziAppInit : ApplicationInitializedListener {
    override fun componentsInitialized() {
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangTool)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), LangDetector)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziSpellchecker)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziCommitInspection)
        GraziStateLifecycle.topic.subscribe(ApplicationManager.getApplication(), GraziInspection)
    }
}
