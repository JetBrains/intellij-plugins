package tanvd.grazi.ide.init

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.vcs.commit.CommitMessageInspectionProfile
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.GraziCommitInspection
import tanvd.grazi.ide.msg.GraziStateLifecycle

open class GraziProjectInit : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        with(CommitMessageInspectionProfile.getInstance(project)) {
            addTool(project, LocalInspectionToolWrapper(GraziCommitInspection()), emptyMap())
            enableTool("GraziCommit", project)
        }

        GraziStateLifecycle.publisher.init(GraziConfig.get(), project)
    }
}
