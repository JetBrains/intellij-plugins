package tanvd.grazi.ide

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.vcs.commit.BaseCommitMessageInspection


class GraziCommitInspection : BaseCommitMessageInspection() {
    companion object {
        val graziInspection: LocalInspectionTool by lazy { GraziInspection() }
    }

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.find("TYPO") ?: HighlightDisplayLevel.WARNING

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return graziInspection.buildVisitor(holder, isOnTheFly)
    }

    override fun getDisplayName() = "Grazi proofreading inspection for VCS"
}
