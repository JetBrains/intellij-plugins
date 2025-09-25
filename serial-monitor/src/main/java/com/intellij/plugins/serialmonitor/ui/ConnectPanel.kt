package com.intellij.plugins.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindow
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortsListener
import com.intellij.plugins.serialmonitor.service.SerialPortsListener.Companion.SERIAL_PORTS_TOPIC
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.Align.Companion.FILL
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.Nls

internal val SERIAL_MONITOR = Key<SerialMonitor>(SerialMonitor::javaClass.name)

internal class ConnectPanel(private val toolWindow: ToolWindow) : OnePixelSplitter(false, 0.4f, 0.1f, 0.9f) {

  private val ports = ConnectableList(this)
  private var disposable: Disposable = Disposer.newDisposable()
  private val listToolbar = ActionManager.getInstance()
    .createActionToolbar(ActionPlaces.TOOLBAR, ports.toolbarActions, true)
    .apply {
      targetComponent = ports
    }

  private fun selectionChanged() {
    secondComponent.removeAll()
    Disposer.dispose(disposable)
    disposable = Disposer.newDisposable("Serial Profile Parameters")
    Disposer.register(toolWindow.disposable, disposable)
    val portName = ports.getSelectedPortName()
    val panel: DialogPanel? =
      if (portName != null)
        portSettings(ports, portName, disposable)
      else
        profileSettings(ports, disposable)

    if (panel != null) {
      secondComponent = JBScrollPane(panel).apply { border = JBUI.Borders.empty() }
      invalidate()
    }
  }

  init {
    setAndLoadSplitterProportionKey("ConnectPanel.splitterProportionKey")
    firstComponent = panel {
      row {
        cell(listToolbar.component)
      }
      row {
        cell(JBScrollPane(ports))
          .align(FILL)
          .applyToComponent {
            border = JBUI.Borders.empty()
          }
      }.resizableRow()
    }
    ports.addListSelectionListener { selectionChanged() }
    secondComponent = JBPanel<JBPanel<*>>()
  }

  private fun contentByPortName(portName: String?): Content? {
    if (portName == null) return null
    return toolWindow.contentManager.contents
      .firstOrNull {
        val serialMonitor = it.getUserData(SERIAL_MONITOR)
        serialMonitor?.portProfile?.portName == portName
      }
  }

  private fun monitorByProfile(profile: SerialPortProfile): SerialMonitor? {
    return toolWindow.contentManager.contents
      .mapNotNull { it.getUserData(SERIAL_MONITOR) }
      .firstOrNull {
        it.portProfile == profile
      }
  }

  /**
   * Return the profile with which the port is currently configured, if any.
   */
  fun getOpenedProfile(portName: String): SerialPortProfile? {
    return contentByPortName(portName)?.getUserData(SERIAL_MONITOR)?.portProfile
  }

  fun openConsole(portName: String?) {
    val content = contentByPortName(portName)
    toolWindow.getContentManager().setSelectedContent(content ?: return)
  }

  fun disconnectPort(portName: String?) {
    contentByPortName(portName)?.getUserData(SERIAL_MONITOR)?.disconnect()
  }

  fun notifyProfileChanged(profile: SerialPortProfile) {
    monitorByProfile(profile)?.notifyProfileChanged()
  }

  fun reconnectProfile(profile: SerialPortProfile,
                       name: @Nls String = profile.defaultName()) {
    val openedTab = contentByPortName(profile.portName)
    if (openedTab != null) {
      toolWindow.getContentManager().removeContent(openedTab, true)
    }
    connectProfile(profile, name)
  }

  fun connectProfile(profile: SerialPortProfile,
                     name: @Nls String = profile.defaultName()) {
    val contentManager = toolWindow.getContentManager()
    val panel = SimpleToolWindowPanel(false, true)
    val content = ContentFactory.getInstance().createContent(panel, name, true)
    content.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
    val serialMonitor = SerialMonitor(toolWindow.getProject(), name, profile)
    content.putUserData(SERIAL_MONITOR, serialMonitor)
    panel.setContent(serialMonitor.component)
    content.setDisposer(serialMonitor)
    content.setCloseable(true)
    contentManager.addContent(content)
    val handler = object : SerialPortsListener {
      override fun portsStatusChanged() {
        // TODO: do not invoke later, use coroutines?
        application.invokeLater(
          {
            val status = serialMonitor.getStatus()
            content.icon = if (status == PortStatus.DISCONNECTED) AllIcons.Nodes.EmptyNode else status.icon
          },
          { toolWindow.isDisposed })
      }
    }
    application.messageBus.connect(content).subscribe(SERIAL_PORTS_TOPIC, handler)

    serialMonitor.connect()
    contentManager.setSelectedContent(content, true)
    toolWindow.setAvailable(true)
    toolWindow.show()
    toolWindow.activate(null, true)
  }

}