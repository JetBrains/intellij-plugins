package tanvd.grazi.ide.init

import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitMessageInspectionProfile
import tanvd.grazi.ide.GraziCommitInspection

open class GraziProjectInit(private val project: Project) : ProjectComponent {
    override fun getComponentName(): String {
        return "GraziProjectInit"
    }

    override fun projectOpened() {
        val inspectionProfile = CommitMessageInspectionProfile.getInstance(project)
        inspectionProfile.addTool(project, LocalInspectionToolWrapper(GraziCommitInspection()), emptyMap())
        inspectionProfile.enableTool("GraziCommit", project)
    }
}
