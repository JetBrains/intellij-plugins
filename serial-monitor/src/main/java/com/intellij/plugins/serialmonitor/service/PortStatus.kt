package com.intellij.plugins.serialmonitor.service

import com.intellij.icons.AllIcons
import icons.SerialMonitorIcons
import javax.swing.Icon

enum class PortStatus(val icon: Icon) {
  UNAVAILABLE(SerialMonitorIcons.Invalid),
  UNAVAILABLE_DISCONNECTED(SerialMonitorIcons.Invalid),
  BUSY(SerialMonitorIcons.Invalid),
  CONNECTING(SerialMonitorIcons.Invalid),
  DISCONNECTED(SerialMonitorIcons.ConnectPassive),
  READY(AllIcons.Nodes.EmptyNode),
  CONNECTED(SerialMonitorIcons.ConnectActive)
}
