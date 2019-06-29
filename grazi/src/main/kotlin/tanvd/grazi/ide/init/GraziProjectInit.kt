package tanvd.grazi.ide.init

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.codeInspection.ex.modifyAndCommitProjectProfile
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.util.Consumer
import com.intellij.vcs.commit.CommitMessageInspectionProfile
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.ide.GraziCommitInspection

open class GraziProjectInit : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        with(CommitMessageInspectionProfile.getInstance(project)) {
            addTool(project, LocalInspectionToolWrapper(GraziCommitInspection()), emptyMap())
            enableTool("GraziCommit", project)
        }

        if (GraziConfig.state.enabledSpellcheck && !GraziPlugin.isTest) {
            modifyAndCommitProjectProfile(project, Consumer {
                it.disableToolByDefault(listOf(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME), project)
            })
        }
    }
}
