package tanvd.grazi.ide

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.vcs.commit.BaseCommitMessageInspection
import com.intellij.vcs.commit.CommitMessageInspectionProfile
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle


class GraziCommitInspection : BaseCommitMessageInspection() {
    companion object : GraziStateLifecycle {
        val graziInspection: LocalInspectionTool by lazy { GraziInspection() }

        override fun init(state: GraziConfig.State, project: Project) {
            with(CommitMessageInspectionProfile.getInstance(project)) {
                addTool(project, LocalInspectionToolWrapper(GraziCommitInspection()), emptyMap())
                enableTool("GraziCommit", project)
            }
        }
    }

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.find("TYPO") ?: HighlightDisplayLevel.WARNING

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return graziInspection.buildVisitor(holder, isOnTheFly)
    }

    override fun getDisplayName() = "Grazi proofreading inspection for VCS"
}
