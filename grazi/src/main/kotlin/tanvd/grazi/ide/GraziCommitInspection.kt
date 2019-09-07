package tanvd.grazi.ide

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.message.BaseCommitMessageInspection
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle

class GraziCommitInspection : BaseCommitMessageInspection() {
    companion object : GraziStateLifecycle {
        private val grazi: LocalInspectionTool by lazy { GraziInspection() }

        override fun init(state: GraziConfig.State, project: Project) {
            with(CommitMessageInspectionProfile.getInstance(project)) {
                if (state.enabledCommitIntegration) {
                    addTool(project, LocalInspectionToolWrapper(GraziCommitInspection()), emptyMap())
                    setToolEnabled("GraziCommit", true, project)
                } else {
                    if (getToolsOrNull("GraziCommit", project) != null) setToolEnabled("GraziCommit", false, project)
                    //TODO-tanvd how to remove tool?
                }
            }
        }

        override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
            if (prevState.enabledCommitIntegration == newState.enabledCommitIntegration) return

            init(newState, project);
        }
    }

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.find("TYPO") ?: HighlightDisplayLevel.WARNING

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = grazi.buildVisitor(holder, isOnTheFly)

    override fun getDisplayName() = "Grazi proofreading inspection for VCS"
}
