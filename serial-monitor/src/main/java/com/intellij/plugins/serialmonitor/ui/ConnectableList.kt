package com.intellij.plugins.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.SerialProfileService
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.ui.NamedColorUtil
import icons.SerialMonitorIcons
import org.jetbrains.annotations.Nls
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

internal class ConnectableList(val parent: ConnectPanel) : JBList<Any>() {
  abstract inner class Connectable(@NlsSafe val entityName: String, @Volatile var status: PortStatus) {

    abstract val selectionKey: Any

    abstract fun actions(): Array<AnAction>

    abstract fun connect()

    abstract fun portName(): String?

    abstract fun icon(): Icon?

    protected val disconnectAction = object : DumbAwareAction(SerialMonitorBundle.message("action.disconnect.text"), null,
                                                              AllIcons.Nodes.Pluginobsolete) {
      override fun actionPerformed(e: AnActionEvent) {
        parent.disconnectPort(portName())
      }
    }
    protected val openConsole = object : DumbAwareAction(SerialMonitorBundle.message("action.open.console.text"), null,
                                                         AllIcons.Actions.OpenNewTab) {
      override fun actionPerformed(e: AnActionEvent) {
        parent.openConsole(portName())
      }
    }

    val connectAction = object : DumbAwareAction(SerialMonitorBundle.message("action.connect.text"), null,
                                                 SerialMonitorIcons.ConnectActive) {
      override fun actionPerformed(e: AnActionEvent) {
        if (status == PortStatus.READY) connect()
      }

      override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
      override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = status == PortStatus.READY
      }
    }
  }

  inner class ConnectableProfile(@NlsSafe profileName: String, status: PortStatus) : Connectable(profileName, status) {

    override fun portName(): String? = application.service<SerialProfileService>().getProfiles()[entityName]?.portName

    override val selectionKey: Any = 'R' to profileName
    override fun icon(): Icon? = status.icon

    override fun connect() {
      application.service<SerialProfileService>().getProfiles()[entityName]?.also {
        parent.connectProfile(it, entityName)
      }
    }

    private val duplicateProfile = object : DumbAwareAction(SerialMonitorBundle.message("action.duplicate.profile.text"), null,
                                                            AllIcons.Actions.Copy) {
      override fun actionPerformed(e: AnActionEvent) = createNewProfile(entityName)
    }

    private val removeProfile = object : DumbAwareAction(SerialMonitorBundle.message("action.remove.profile.text"), null,
                                                         AllIcons.General.Remove) {
      override fun actionPerformed(e: AnActionEvent) {
        if (MessageDialogBuilder.yesNo(
            SerialMonitorBundle.message("dialog.title.delete.profile", entityName),
            SerialMonitorBundle.message("dialog.message.are.you.sure")).ask(this@ConnectableList)) {
          with(service<SerialProfileService>()) {
            val newProfiles = getProfiles().toMutableMap()
            newProfiles.remove(entityName)
            clearSelection()
            setProfiles(newProfiles)
            application.invokeLater { rescanProfiles() }
          }
        }
      }
    }

    override fun actions(): Array<AnAction> =
      when (status) {
        PortStatus.UNAVAILABLE ->
          arrayOf(duplicateProfile, Separator.getInstance(), removeProfile)
        PortStatus.CONNECTING,
        PortStatus.BUSY,
        PortStatus.UNAVAILABLE_DISCONNECTED ->
          arrayOf(openConsole, duplicateProfile, Separator.getInstance(), removeProfile)
        PortStatus.DISCONNECTED -> arrayOf(openConsole, connectAction, duplicateProfile, Separator.getInstance(), removeProfile)
        PortStatus.CONNECTED -> arrayOf(openConsole, disconnectAction, duplicateProfile, Separator.getInstance(), removeProfile)
        PortStatus.READY -> arrayOf(connectAction, duplicateProfile, Separator.getInstance(), removeProfile)
      }
  }

  inner class ConnectablePort(portName: String, status: PortStatus) : Connectable(portName, status) {
    override fun portName() = entityName

    override val selectionKey: Any = 'O' to entityName
    override fun icon(): Icon? = status.icon

    override fun connect() {
      parent.connectProfile(service<SerialProfileService>().copyDefaultProfile(entityName))
    }

    private val createProfile = object : DumbAwareAction(SerialMonitorBundle.message("action.create.profile.text"), null,
                                                         AllIcons.General.Add) {
      override fun actionPerformed(e: AnActionEvent) = createNewProfile(null, portName())
    }

    override fun actions(): Array<AnAction> =
      if (status == PortStatus.CONNECTED) arrayOf(openConsole, disconnectAction, createProfile)
      else arrayOf(connectAction, createProfile)
  }

  private val DEFAULT_ACTIONS = arrayOf(
    object : DumbAwareAction(SerialMonitorBundle.message("action.connect.text"), null, SerialMonitorIcons.ConnectPassive) {
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

  @RequiresEdt
  fun rescanProfiles(profileToSelect: String? = null) {
    var savedSelection = selectedValue.asSafely<Connectable>()?.selectionKey
    val portService = application.service<SerialPortService>()
    val newModel = DefaultListModel<Any>()
    @Nls val profilesSeparator = SerialMonitorBundle.message("connection.profiles")
    newModel.addElement(profilesSeparator)
    val profileService = application.service<SerialProfileService>()
    profileService.getProfiles().forEach {
      var status = portService.portStatus(it.value.portName)

      val profile = ConnectableProfile(it.key, status)
      newModel.addElement(profile)
      if (profileToSelect == it.key) {
        savedSelection = profile.selectionKey
      }
    }
    @Nls val portsSeparator = SerialMonitorBundle.message("available.ports")
    newModel.addElement(portsSeparator)
    portService.getPortsNames().forEach {
      val status = portService.portStatus(it)
      newModel.addElement(ConnectablePort(it, status))
    }
    model = newModel
    clearSelection()
    if (savedSelection != null) {
      for (i in 0..model.size - 1) {
        if (model.getElementAt(i).asSafely<Connectable>()?.selectionKey == savedSelection) {
          selectedIndex = i
          break
        }
      }
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
        var newIndex = index1 // according to documentation, index1 is used for the single selection mode
        val delta = if (newIndex < selectedIndex) -1 else 1

        // Skips over fake string labels and selects the first Connectable in the selected direction from the current element
        while (newIndex >= 0 && newIndex < model.size) {
          val element = model.getElementAt(newIndex)
          if (element is Connectable) {
            super.setSelectionInterval(newIndex, newIndex)
            break
          }
          newIndex += delta
        }
      }
    }
    installCellRenderer { value ->
      when (value) {
        is Connectable -> {
          JBLabel(value.entityName, value.icon() ?: AllIcons.Nodes.EmptyNode, JLabel.LEADING)
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
          model.getElementAt(locationToIndex(e.getPoint()))
            .asSafely<Connectable>()
            ?.also {
              if (it.status == PortStatus.READY) {
                it.connect()
              }
              else {
                parent.openConsole(it.portName())
              }
            }
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