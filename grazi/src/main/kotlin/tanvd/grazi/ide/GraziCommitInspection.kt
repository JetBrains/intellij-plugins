package tanvd.grazi.ide

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.vcs.commit.message.BaseCommitMessageInspection


class GraziCommitInspection : BaseCommitMessageInspection() {
    companion object {
        val graziInspection: LocalInspectionTool by lazy { GraziInspection() }
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return graziInspection.buildVisitor(holder, isOnTheFly)
    }

    override fun getDisplayName(): String {
        return "Grazi Commit proofreading inspection"
    }
}
