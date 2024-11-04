package com.intellij.plugins.serialmonitor.ui

import com.intellij.ide.ActivityTracker
import com.intellij.ide.impl.ProjectUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.SerialMonitorException
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService.SerialConnection
import com.intellij.plugins.serialmonitor.service.SerialPortsListener
import com.intellij.plugins.serialmonitor.ui.console.JeditermSerialMonitorDuplexConsoleView
import com.intellij.ui.TextFieldWithStoredHistory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLoadingPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.*
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty1

private const val HISTORY_KEY = "serialMonitor.commands"

class SerialMonitor(private val project: Project,
                    name: @NlsSafe String,
                    val portProfile: SerialPortProfile) : Disposable, SerialPortsListener {
  private val myPanel: JBLoadingPanel = JBLoadingPanel(GridLayoutManager(2, 5, JBUI.insets(5), -1, -1), this, 300)
  private val mySend: JButton
  private val myCommand: TextFieldWithStoredHistory
  private val myLineEnd: JBCheckBox
  private val myHardwareControlFlow: DialogPanel
  private val duplexConsoleView: JeditermSerialMonitorDuplexConsoleView

  fun getStatus(): PortStatus = duplexConsoleView.status

  override fun portsStatusChanged() {
    mySend.isEnabled = duplexConsoleView.status == PortStatus.CONNECTED
    ActivityTracker.getInstance().inc()
  }

  private fun send(txt: String) {
    var s = txt
    if (myLineEnd.isSelected) {
      s += portProfile.newLine.value
    }

    if (s.isNotEmpty()) {
      duplexConsoleView.apply {
        val bytes = s.toByteArray(this.charset)
        ApplicationManager.getApplication().executeOnPooledThread {
          try {
            this.connection.write(bytes)
          }
          catch (sme: SerialMonitorException) {
            errorNotification((sme.message)!!, project)
          }
        }
      }
    }
  }

  val component: JComponent
    get() = myPanel


  fun disconnect() {
    duplexConsoleView.connect(false)
  }

  override fun dispose() {}

  fun connect() {
    duplexConsoleView.connect(true)
  }

  internal fun isTimestamped(): Boolean = duplexConsoleView.isTimestamped

  internal fun isHex(): Boolean = !duplexConsoleView.isPrimaryConsoleEnabled

  init {
    myPanel.setLoadingText(SerialMonitorBundle.message("connecting"))
    duplexConsoleView = JeditermSerialMonitorDuplexConsoleView.create(project, name, portProfile, myPanel)
    Disposer.register(this, duplexConsoleView)
    val consoleComponent = duplexConsoleView.component
    duplexConsoleView.component.border = BorderFactory.createEtchedBorder()
    val toolbarActions = DefaultActionGroup()
    val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, toolbarActions, false)
    toolbarActions.addAll(*duplexConsoleView.createConsoleActions())
    toolbar.targetComponent = consoleComponent
    myCommand = TextFieldWithStoredHistory(HISTORY_KEY)
    myLineEnd = JBCheckBox(SerialMonitorBundle.message("checkbox.send.eol"), true)
    mySend = JButton(SerialMonitorBundle.message("send.title"))
    mySend.isEnabled = false
    myCommand.setHistorySize(10)
    myCommand.addKeyboardListener(object : KeyAdapter() {
      override fun keyPressed(e: KeyEvent) {
        // send on CTRL + ENTER
        if (e.isControlDown && e.keyChar.code == KeyEvent.VK_ENTER) {
          myCommand.hidePopup()
          mySend.doClick()
        }
      }
    })
    mySend.addActionListener(ActionListener {
      send(myCommand.text)
      myCommand.addCurrentTextToHistory()
      myCommand.text = ""
    })

    myHardwareControlFlow = panel {
      align(AlignX.RIGHT + AlignY.CENTER)
      row {
        val rtsCheckbox = checkBox("RTS")
        val dtrCheckbox = checkBox("DTR")

        fun Cell<JBCheckBox>.changesBind(prop: KMutableProperty1<SerialConnection, Boolean>, connection: SerialConnection) {
          this.whenStateChangedFromUi(this@SerialMonitor) {
            try {
              val value = prop.get(connection)
              if (value != it) {
                prop.set(connection, it)
              }
            }
            catch(e: SerialMonitorException) {
              errorNotification(e.message!!, project)
            }
          }
        }

        val connection = duplexConsoleView.connection
        rtsCheckbox.component.isSelected = connection.rts
        rtsCheckbox.changesBind(SerialConnection::rts, connection)
        dtrCheckbox.component.isSelected = connection.dtr
        dtrCheckbox.changesBind(SerialConnection::dtr, connection)
      }
    }

    ApplicationManager.getApplication().messageBus.connect().subscribe(SerialPortsListener.SERIAL_PORTS_TOPIC, this)
    myPanel.add(toolbar.component,
                GridConstraints(0, 0, 2, 1, ANCHOR_NORTH, FILL_VERTICAL, SIZEPOLICY_FIXED, SIZE_POLICY_RESIZEABLE, null, null, null))
    myPanel.add(myCommand,
                GridConstraints(0, 1, 1, 1, ANCHOR_NORTHWEST, FILL_HORIZONTAL, SIZE_POLICY_RESIZEABLE, SIZEPOLICY_FIXED, null, null, null))
    myPanel.add(myLineEnd, GridConstraints(0, 2, 1, 1, ANCHOR_CENTER, FILL_NONE, SIZEPOLICY_FIXED,
                                           SIZEPOLICY_FIXED, null, null, null))
    myPanel.add(mySend,
                GridConstraints(0, 3, 1, 1, ANCHOR_NORTHEAST, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED, null, null, null))

    myPanel.add(myHardwareControlFlow,
                GridConstraints(0, 4, 1, 1, ANCHOR_NORTHEAST, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED, null, null, null))

    myPanel.add(consoleComponent,
                GridConstraints(1, 1, 1, 4, ANCHOR_NORTHWEST, FILL_BOTH, SIZE_POLICY_RESIZEABLE, SIZE_POLICY_RESIZEABLE, null, null, null))
    duplexConsoleView.addSwitchListener(this::hideSendControls, this)
    hideSendControls(duplexConsoleView.isPrimaryConsoleEnabled)
  }

  private fun hideSendControls(q: Boolean) {
    mySend.isVisible = !q
    myCommand.isVisible = !q
    myLineEnd.isVisible = !q
  }

  companion object {
    private const val SERIAL_NOTIFICATION_GROUP_NAME = "Serial Monitor Notification"

    private const val SIZE_POLICY_RESIZEABLE = SIZEPOLICY_CAN_GROW + SIZEPOLICY_CAN_SHRINK + SIZEPOLICY_WANT_GROW

    fun errorNotification(content: @NlsContexts.NotificationContent String, project: Project?) {
      return service<NotificationGroupManager>()
        .getNotificationGroup(SERIAL_NOTIFICATION_GROUP_NAME)
        .createNotification(content, NotificationType.ERROR)
        .notify(project)
    }

    fun errorNotification(content: @NlsContexts.NotificationContent String, component: Component) {
      errorNotification(content, ProjectUtil.getProjectForComponent(component))
    }
  }
}