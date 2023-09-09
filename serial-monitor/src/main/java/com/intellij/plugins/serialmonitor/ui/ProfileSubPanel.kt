package com.intellij.plugins.serialmonitor.ui

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.NlsContexts
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.SerialProfileService
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import org.jetbrains.annotations.Nls
import java.util.*
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent

class ProfileSubPanel(private val connectPanel: ConnectPanel) {

  val component: JComponent
  var selectedProfile: SerialPortProfile? = null

  init {
    component = profilesPanel()
  }

  private fun Row.profileButton(@NlsContexts.Button name: String,
                                icon: Icon,
                                enabled: ComponentPredicate,
                                doAction: (profileName: String) -> Unit): Cell<JButton> {
    val action = object : DumbAwareAction(icon) {
      override fun actionPerformed(e: AnActionEvent) {
        selectedProfile?.portName?.also { doAction(it) }
      }

      override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = selectedProfile != null
      }

      override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
    }
    return button(name, action).enabledIf(enabled)
  }

  private fun profilesPanel(): JComponent {

    return panel {
      row {
        //profileButton("Connect", AllIcons.Actions.Lightning, profiles.selectedConnectable, ::connectProfile)
        //profileButton("Modify", AllIcons.Actions.Edit, profiles.selectedNotInUse) { name ->
        //  with(service<SerialProfileService>()) {
        //    modifyProfile(getProfiles().getOrElse(name) { copyDefaultProfile(name) }, name, name)
        //  }
        //}
        //profileButton("Delete", AllIcons.General.Remove, selectedProfile.itemSelected, ::deleteProfile)
      }
    }
  }

  private fun connectProfile(name: @Nls String) {
    val profile = service<SerialProfileService>().getProfiles()[name]
    if (service<SerialPortService>().portUsable(profile?.portName)) {
      //connectPanel.connectProfile(profile!!, name)
    }
  }

  fun rescan(portsStatus: Map<String, PortStatus>) {
    val newConnectionsStatus = TreeMap<String, PortStatus>(String.CASE_INSENSITIVE_ORDER)
    for ((name, profile) in service<SerialProfileService>().getProfiles()) {
      newConnectionsStatus[name] = portsStatus[profile.portName] ?: PortStatus.MISSING
    }
    //if (connectionsStatus != newConnectionsStatus) {
    //  SwingUtilities.invokeLater {
    //    profiles.replaceList()
    //    connectionsStatus = newConnectionsStatus
    //  }
    //}
  }

  //fun getProfileNames(): Set<String> = connectionsStatus.keys
  //fun modifyProfile(profile: SerialPortProfile, name: @NlsSafe String, originalName: @NlsSafe String?) {
  //  val (newName, serialPortProfile) = EditProfileDialog(connectPanel, profile, name, originalName).ask()
  //  if (newName != null && serialPortProfile != null) with(service<SerialProfileService>()) {
  //    val profiles = getProfiles().toMutableMap()
  //    profiles[newName] = serialPortProfile
  //    setProfiles(profiles)
  //    rescan(service<SerialPortService>().getPortsStatus())
  //  }
  //}
}