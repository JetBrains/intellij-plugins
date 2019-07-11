package tanvd.grazi.ide.init

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.codeInspection.ex.modifyAndCommitProjectProfile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.util.Consumer
import com.intellij.vcs.commit.CommitMessageInspectionProfile
import com.intellij.vcs.commit.CommitMessageSpellCheckingInspection
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.GraziCommitInspection
import tanvd.grazi.ide.GraziLifecycle

open class GraziProjectInit : StartupActivity, DumbAware {

    companion object : GraziLifecycle {
        override fun init() {
            super.init()
        }

        override fun reset() {
            super.reset()
        }
    }

    override fun runActivity(project: Project) {
        with(CommitMessageInspectionProfile.getInstance(project)) {
            addTool(project, LocalInspectionToolWrapper(GraziCommitInspection()), emptyMap())
            enableTool("GraziCommit", project)

            if (GraziConfig.state.enabledSpellcheck) {
                disableToolByDefault(listOf(getTool(CommitMessageSpellCheckingInspection::class.java).shortName), project)
            } else {
                enableToolsByDefault(listOf(getTool(CommitMessageSpellCheckingInspection::class.java).shortName), project)
            }
        }

        if (!ApplicationManager.getApplication().isUnitTestMode && GraziConfig.state.enabledSpellcheck) {
            modifyAndCommitProjectProfile(project, Consumer {
                it.disableToolByDefault(listOf(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME), project)
            })
        }
    }
}
