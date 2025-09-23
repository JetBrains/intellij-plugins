package com.intellij.plugins.serialmonitor.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.UI
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.SerialProfileService
import com.intellij.plugins.serialmonitor.service.PortStatus
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.plugins.serialmonitor.service.SerialPortsListener
import com.intellij.plugins.serialmonitor.service.SerialPortsListener.Companion.SERIAL_PORTS_TOPIC
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.PopupHandler
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.ui.launchOnShow
import icons.SerialMonitorIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Nls
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

internal class ConnectableList(val parentPanel: ConnectPanel) : JBList<Any>() {
  abstract inner class Connectable(val entityName: @NlsSafe String, @Volatile var status: PortStatus) {

    abstract val selectionKey: Pair<Char, String>

    abstract fun actions(): Array<AnAction>

    abstract fun connect()

    abstract fun portName(): @NlsSafe String?

    abstract fun icon(): Icon

    abstract fun name(): @Nls String

    open fun description(): @Nls String? = null

    protected val disconnectAction = object : DumbAwareAction(SerialMonitorBundle.message("action.disconnect.text"), null,
                                                              AllIcons.Nodes.Pluginobsolete) {
      override fun actionPerformed(e: AnActionEvent) {
        parentPanel.disconnectPort(portName())
      }
    }
    protected val openConsole = object : DumbAwareAction(SerialMonitorBundle.message("action.open.console.text"), null,
                                                         AllIcons.Actions.OpenNewTab) {
      override fun actionPerformed(e: AnActionEvent) {
        parentPanel.openConsole(portName())
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

    override val selectionKey = createProfileSelectionKey(profileName)
    override fun icon(): Icon = status.icon
    override fun name(): @Nls String = entityName

    override fun connect() {
      application.service<SerialProfileService>().getProfiles()[entityName]?.also {
        parentPanel.connectProfile(it, entityName)
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

    override val selectionKey = createPortSelectionKey(entityName)
    override fun icon(): Icon = status.icon
    override fun name(): @Nls String = entityName
    override fun description(): @Nls String? = service<SerialPortService>().portDescriptiveName(entityName)

    override fun connect() {
      parentPanel.connectProfile(service<SerialProfileService>().copyDefaultProfile(entityName))
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

  private fun createProfileSelectionKey(profileName: String): Pair<Char, String> = 'R' to profileName
  private fun createPortSelectionKey(portName: String): Pair<Char, String> = 'O' to portName
  fun selectProfile(profileName: String) { select(createProfileSelectionKey(profileName)) }
  fun selectPort(portName: String) { select(createPortSelectionKey(portName)) }

  private fun select(selectionKey: Pair<Char, String>) {
    for (i in 0..<model.size) {
      if (model.getElementAt(i).asSafely<Connectable>()?.selectionKey == selectionKey) {
        selectedIndex = i
        break
      }
    }
  }

  private val updateFlow = MutableSharedFlow<Unit>(replay = 0)
  suspend fun awaitModelUpdate(): Unit = updateFlow.first()

  private suspend fun updateModel() {
    val savedSelection = withContext(Dispatchers.UI) {
      selectedValue.asSafely<Connectable>()?.selectionKey
    }

    val portService = application.serviceAsync<SerialPortService>()
    val newModel = DefaultListModel<Any>()
    @Nls val profilesSeparator = SerialMonitorBundle.message("connection.profiles")
    newModel.addElement(profilesSeparator)
    val profileService = application.serviceAsync<SerialProfileService>()
    profileService.getProfiles().forEach {
      val status = portService.portStatus(it.value.portName)

      val profile = ConnectableProfile(it.key, status)
      newModel.addElement(profile)
      checkCanceled()
    }
    @Nls val portsSeparator = SerialMonitorBundle.message("available.ports")
    newModel.addElement(portsSeparator)
    portService.getPortsNames().forEach {
      val status = portService.portStatus(it)
      newModel.addElement(ConnectablePort(it, status))
      checkCanceled()
    }

    withContext(Dispatchers.UI) {
      model = newModel
      clearSelection()
      if (savedSelection != null) {
        select(savedSelection)
      }
      invalidate()
      updateFlow.emit(Unit)
    }
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

    cellRenderer = object : ColoredListCellRenderer<Any>() {
      override fun customizeCellRenderer(list: JList<out Any>, value: Any, index: Int, selected: Boolean, hasFocus: Boolean) {
        when(value) {
          is Connectable -> {
            icon = value.icon()
            append(value.name())
          }
          else -> {
            @NlsSafe val label: String = value as? String ?: ""
            append(label, SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
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
                parentPanel.openConsole(it.portName())
              }
            }
        }
      }
    })
    ListSpeedSearch.installOn(this) { it.asSafely<Connectable>()?.entityName }
    PopupHandler.installPopupMenu(this, toolbarActions, ActionPlaces.POPUP)
    launchOnShow("Connectable List Model Updater") {
      val profilesFlow = serviceAsync<SerialProfileService>().profilesFlow
      val portNamesFlow = serviceAsync<SerialPortService>().portNamesFlow
      val portStatusFlow = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

      application.messageBus.connect(this).subscribe(SERIAL_PORTS_TOPIC, object : SerialPortsListener {
        override fun portsStatusChanged() {
          portStatusFlow.tryEmit(Unit)
        }
      })

      updateModel()
      merge(profilesFlow, portNamesFlow, portStatusFlow).collectLatest {
        updateModel()
      }
    }
  }

  @NlsSafe
  fun getSelectedPortName(): String? = selectedValue.asSafely<ConnectablePort>()?.entityName
  fun getSelectedProfile(): Pair<String, SerialPortProfile?>? {
    val profileName = selectedValue.asSafely<ConnectableProfile>()?.entityName
    return if (profileName != null)
      profileName to application.service<SerialProfileService>().getProfiles()[profileName]
    else null
  }

  private val removeProfile = object : DumbAwareAction(SerialMonitorBundle.message("action.remove.profile.text"), null,
                                                       AllIcons.General.Remove) {
    init {
      registerCustomShortcutSet(CommonShortcuts.getDelete(), this@ConnectableList)
    }
    override fun actionPerformed(e: AnActionEvent) {
      val selectedProfile = selectedValue.asSafely<ConnectableProfile>() ?: return
      val entityName = selectedProfile.entityName

      if (MessageDialogBuilder.yesNo(
          SerialMonitorBundle.message("dialog.title.delete.profile", entityName),
          SerialMonitorBundle.message("dialog.message.are.you.sure")).ask(this@ConnectableList)) {
        with(service<SerialProfileService>()) {
          val newProfiles = getProfiles().toMutableMap()
          newProfiles.remove(entityName)
          clearSelection()
          setProfiles(newProfiles)
        }
      }
    }
  }

  private val duplicateProfile = object : DumbAwareAction(SerialMonitorBundle.message("action.duplicate.profile.text"), null,
                                                          AllIcons.Actions.Copy) {
    init {
      registerCustomShortcutSet(CommonShortcuts.getDuplicate(), this@ConnectableList)
    }
    override fun actionPerformed(e: AnActionEvent) {
      val selectedProfile = selectedValue.asSafely<ConnectableProfile>() ?: return
      val entityName = selectedProfile.entityName
      currentThreadCoroutineScope().launch {
        createNewProfile(entityName)
      }
    }
  }

  private val createProfile = object : DumbAwareAction(SerialMonitorBundle.message("action.create.profile.text"), null,
                                                       AllIcons.General.Add) {
    init {
      registerCustomShortcutSet(CommonShortcuts.getNew(), this@ConnectableList)
    }
    override fun actionPerformed(e: AnActionEvent) {
      val portName = getSelectedPortName()
      currentThreadCoroutineScope().launch {
        createNewProfile(null, portName)
      }
    }
  }
}