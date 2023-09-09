package com.intellij.plugins.serialmonitor.service

import com.intellij.icons.AllIcons
import com.intellij.ui.LayeredIcon
import icons.SerialMonitorIcons
import javax.swing.Icon

enum class PortStatus(val actionIcon: Icon, val statusIcon: Icon) { //todo better icons
  MISSING(AllIcons.Nodes.Pluginobsolete, LayeredIcon.layeredIcon(arrayOf(AllIcons.Nodes.Pluginobsolete, AllIcons.Nodes.ErrorMark))),
  RELEASED(AllIcons.Nodes.Plugin, SerialMonitorIcons.DisconnectedSerial),
  @Suppress("unused")
  BUSY(AllIcons.Plugins.Disabled, AllIcons.Plugins.Disabled), //Reserved for future use
  DISCONNECTED(AllIcons.Nodes.Plugin, SerialMonitorIcons.DisconnectedSerial),
  CONNECTING(AllIcons.Process.Step_passive, AllIcons.Process.Step_passive),
  CONNECTED(AllIcons.Nodes.Project, SerialMonitorIcons.ConnectedSerial)
}