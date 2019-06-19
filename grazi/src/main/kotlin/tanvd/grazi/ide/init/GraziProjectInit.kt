package tanvd.grazi.ide.init

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.vcs.commit.CommitMessageInspectionProfile
import tanvd.grazi.ide.GraziCommitInspection

open class GraziProjectInit : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        val inspectionProfile = CommitMessageInspectionProfile.getInstance(project)
        inspectionProfile.addTool(project, LocalInspectionToolWrapper(GraziCommitInspection()), emptyMap())
        inspectionProfile.enableTool("GraziCommit", project)
    }
}
