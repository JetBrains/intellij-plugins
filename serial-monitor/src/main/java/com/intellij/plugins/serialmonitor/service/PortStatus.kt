package com.intellij.plugins.serialmonitor.service

import com.intellij.icons.AllIcons
import com.intellij.ui.LayeredIcon
import icons.SerialMonitorIcons
import javax.swing.Icon

enum class PortStatus(val icon: Icon) {
  MISSING(SerialMonitorIcons.Invalid),
  RELEASED(AllIcons.Nodes.EmptyNode),
  @Suppress("unused")
  BUSY(SerialMonitorIcons.Invalid), //Reserved for future use
  DISCONNECTED(AllIcons.Nodes.EmptyNode),
  CONNECTING(AllIcons.Process.Step_passive),
  CONNECTED(SerialMonitorIcons.ConnectActive)
}