package com.intellij.plugins.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.SerialProfileService
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.ui.NamedColorUtil
import icons.SerialMonitorIcons
import org.jetbrains.annotations.Nls
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ConnectableList(val parent: ConnectPanel) : JBList<Any>() {


  abstract inner class Connectable(@NlsSafe val entityName: String, @Volatile var status: PortStatus) {
    abstract fun actions(): Array<AnAction>

    abstract fun connect()

    protected abstract fun portName(): String?

    protected val disconnectAction = object : DumbAwareAction("Disconnect", null, AllIcons.Nodes.Pluginobsolete) {
      override fun actionPerformed(e: AnActionEvent) {
        parent.disconnectPort(portName())
      }
    }
    protected val openConsole = object : DumbAwareAction("Open Console", null, AllIcons.Actions.OpenNewTab) {
      override fun actionPerformed(e: AnActionEvent) {
        parent.openConsole(portName())
      }
    }

    val connectAction = object : DumbAwareAction("Connect", null, SerialMonitorIcons.ConnectActive) {
      override fun actionPerformed(e: AnActionEvent) {
        if (status == PortStatus.DISCONNECTED) connect()
      }

      override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
      override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = status == PortStatus.DISCONNECTED
      }
    }
  }

  inner class ConnectableProfile(@NlsSafe profileName: String, status: PortStatus) : Connectable(profileName, status) {

    override fun portName(): String? = application.service<SerialProfileService>().getProfiles()[entityName]?.portName

    override fun connect() {
      application.service<SerialProfileService>().getProfiles()[entityName]?.also {
        parent.connectProfile(it, entityName)
      }
    }

    private val duplicateProfile = object : DumbAwareAction("Duplicate Profile", null, AllIcons.Actions.Copy) {
      override fun actionPerformed(e: AnActionEvent) = createNewProfile(entityName)
    }

    private val removeProfile = object : DumbAwareAction("Remove Profile", null, AllIcons.General.Remove) {
      override fun actionPerformed(e: AnActionEvent) {
        if (MessageDialogBuilder.yesNo("Delete profile $entityName", "Are you sure").ask(this@ConnectableList)) {
          with(service<SerialProfileService>()) {
            val newProfiles = getProfiles().toMutableMap()
            newProfiles.remove(entityName)
            clearSelection()
            setProfiles(newProfiles)
            rescanPorts()
          }
        }
      }
    }

    override fun actions(): Array<AnAction> =
      if (status == PortStatus.CONNECTED) arrayOf(openConsole, disconnectAction, duplicateProfile, Separator.getInstance(), removeProfile)
      else arrayOf(connectAction, duplicateProfile, Separator.getInstance(), removeProfile)
  }

  inner class ConnectablePort(entityName: String, status: PortStatus) : Connectable(entityName, status) {
    override fun portName() = entityName

    override fun connect() {
      parent.connectProfile(service<SerialProfileService>().copyDefaultProfile(entityName))
    }

    private val createProfile = object : DumbAwareAction("Create Profile", null, AllIcons.General.Add) {
      override fun actionPerformed(e: AnActionEvent) = createNewProfile(null, portName())
    }

    override fun actions(): Array<AnAction> =
      if (status == PortStatus.CONNECTED) arrayOf(openConsole, disconnectAction, createProfile)
      else arrayOf(connectAction, createProfile)
  }

  private val DEFAULT_ACTIONS = arrayOf(
    object : DumbAwareAction("Connect", null, SerialMonitorIcons.ConnectPassive) {
      override fun actionPerformed(e: AnActionEvent) {}
      override fun getActionUpdateThread() = ActionUpdateThread.EDT
      override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = false
      }
    })

  val toolbarActions: ActionGroup = object : ActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<out AnAction> {
      val actions = selectedValue.asSafely<Connectable>()?.actions()
      if (actions != null) return actions
      return if (e?.place == ActionPlaces.POPUP) emptyArray() else DEFAULT_ACTIONS
    }
  }

  fun rescanPorts(profileToSelect: String? = null) {
    var savedSelection = selectedValue
    val portService = application.service<SerialPortService>()
    val newModel = DefaultListModel<Any>()
    @Nls val profilesSeparator = "Connection Profiles"
    newModel.addElement(profilesSeparator)
    val profileService = application.service<SerialProfileService>()
    profileService.getProfiles().forEach {
      val profile = ConnectableProfile(it.key, PortStatus.DISCONNECTED /*todo*/)
      newModel.addElement(profile)
      if (profileToSelect == it.key) {
        savedSelection = profile
      }
    }
    @Nls val portsSeparator = "Available Ports"
    newModel.addElement(portsSeparator)
    portService.getPortsNames().forEach { newModel.addElement(ConnectablePort(it, portService.portStatus(it))) }
    model = newModel
    clearSelection()
    if (savedSelection != null) {
      setSelectedValue(savedSelection, true)
    }
    PopupHandler.installPopupMenu(this, toolbarActions, ActionPlaces.POPUP)
    invalidate()
  }

  init {
    selectionModel = object : DefaultListSelectionModel() {
      init {
        selectionMode = SINGLE_SELECTION
      }

      override fun setSelectionInterval(index0: Int, index1: Int) {
        if (index0 >= 0 && model.getElementAt(index0) is Connectable ) {
          super.setSelectionInterval(index0, index0)
        }
        else {
          clearSelection()
        }
      }
    }
    installCellRenderer { value ->
      when (value) {
        is Connectable -> {
          JBLabel(value.entityName, value.status.icon, JLabel.LEADING)
        }
        else -> {
          @NlsSafe val label: String = value.asSafely<String>() ?: ""
          JBLabel(label).apply {
            font = font.deriveFont(font.size * 0.7f)
            foreground = NamedColorUtil.getInactiveTextColor()
          }
        }
      }

    }
    addMouseListener(object : MouseAdapter() {
      override fun mousePressed(e: MouseEvent) {
        val selectedValue = model.getElementAt(locationToIndex(e.getPoint())).asSafely<Connectable>()
        if (selectedValue != null) {
          setSelectedValue(selectedValue, true)
        }
        else {
          clearSelection()
        }
      }

      override fun mouseClicked(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e) && e.clickCount >= 2) {
          model.getElementAt(locationToIndex(e.getPoint())).asSafely<Connectable>()?.connect()
        }

      }
    })
    ListSpeedSearch.installOn(this) { it.asSafely<Connectable>()?.entityName }
  }

  @NlsSafe
  fun getSelectedPortName(): String? = selectedValue.asSafely<ConnectablePort>()?.entityName
  fun getSelectedProfile(): Pair<String, SerialPortProfile?>? {
    val profileName = selectedValue.asSafely<ConnectableProfile>()?.entityName
    return if (profileName != null)
      profileName to application.service<SerialProfileService>().getProfiles()[profileName]
    else null
  }

}