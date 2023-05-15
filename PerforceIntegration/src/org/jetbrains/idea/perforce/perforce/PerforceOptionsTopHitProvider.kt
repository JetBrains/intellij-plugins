package org.jetbrains.idea.perforce.perforce

import com.intellij.application.options.editor.CheckboxDescriptor
import com.intellij.ide.ui.search.OptionDescription
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vcs.configurable.VcsOptionsTopHitProviderBase
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.PerforceVcs

private val p4OptionGroupName get() = PerforceBundle.message("settings.p4.option.group")
private fun configuration(project: Project) = PerforceSettings.getSettings(project)

// @formatter:off
private fun cdEnableOffline(project: Project): CheckboxDescriptor =         CheckboxDescriptor(PerforceBundle.message("checkbox.switch.offline"), configuration(project)::myCanGoOffline, groupName = p4OptionGroupName)
private fun cdDumpCommandsToLog(project: Project): CheckboxDescriptor =     CheckboxDescriptor(PerforceBundle.message("checkbox.toggle.perforce.log.commands"), configuration(project)::showCmds, groupName = p4OptionGroupName)
private fun cdUseLoginAuth(project: Project): CheckboxDescriptor =          CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.use.login.authentication"), configuration(project)::USE_LOGIN, groupName = p4OptionGroupName)
private fun cdShowBranchingHistory(project: Project): CheckboxDescriptor =  CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.show.branching.history"), configuration(project)::SHOW_BRANCHES_HISTORY, groupName = p4OptionGroupName)
private fun cdShowIntegrated(project: Project): CheckboxDescriptor =        CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.show.integrated.changelists"), configuration(project)::SHOW_INTEGRATED_IN_COMMITTED_CHANGES, groupName = p4OptionGroupName)
private fun cdUseP4Jobs(project: Project): CheckboxDescriptor =             CheckboxDescriptor(PerforceBundle.message("perforce.use.perforce.jobs"), configuration(project)::USE_PERFORCE_JOBS, groupName = p4OptionGroupName)
// @formatter:on

private fun cdEnable(project: Project): CheckboxDescriptor =
  CheckboxDescriptor(PerforceBundle.message("checkbox.configure.perforce.is.enabled"),
                     { configuration(project).ENABLED },
                     { enabled ->
                       val configuration = configuration(project)
                       configuration.ENABLED = enabled
                       if (enabled) {
                         configuration.enable()
                       }
                       else {
                         configuration.disable(true)
                         VcsDirtyScopeManager.getInstance(project).markEverythingDirty()
                       }
                     },
                     groupName = p4OptionGroupName)

internal class PerforceOptionsTopHitProvider : VcsOptionsTopHitProviderBase() {
  override fun getId(): String {
    return "vcs"
  }

  override fun getOptions(project: Project): Collection<OptionDescription> {
    if (isEnabled(project, PerforceVcs.getKey())) {
      return listOf(
        cdEnable(project),
        cdEnableOffline(project),
        cdDumpCommandsToLog(project),
        cdUseLoginAuth(project),
        cdShowBranchingHistory(project),
        cdShowIntegrated(project),
        cdUseP4Jobs(project)
      ).map(CheckboxDescriptor::asOptionDescriptor)
    }
    return emptyList()
  }
}