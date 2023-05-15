package com.intellij.plugins.serialmonitor.ui

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.openapi.actionSystem.ActionUpdateThread.EDT
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.MasterDetailsComponent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.SerialProfileService
import com.intellij.plugins.serialmonitor.service.JsscSerialService
import com.intellij.plugins.serialmonitor.service.SerialSettingsChangeListener
import com.intellij.ui.UIBundle
import com.intellij.util.IconUtil
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.tree.TreeUtil


internal class SerialSettingsConfigurable : SearchableConfigurable, MasterDetailsComponent() {

  init {
    reset()
    initTree()
  }

  override fun reset() {
    super<MasterDetailsComponent>.reset()
    clearChildren()

    val serialProfileService = SerialProfileService.getInstance()
    createNode("", serialProfileService.copyDefaultProfile(), true, false)
    serialProfileService.getProfiles().forEach { createNode(it.key, it.value, false, false) }
    myTree.updateUI()
  }


  private fun createNode(name: @NlsSafe String, profile: SerialPortProfile, isDefault: Boolean, isNew: Boolean): MyNode {
    val serialProfileConfigurable = SerialProfileConfigurable(name, profile, isDefault, isNew)
    val node = MyNode(serialProfileConfigurable)
    addNode(node, myRoot)
    return node
  }

  override fun getNodeComparator() = object : Comparator<MyNode> {
    override fun compare(node1: MyNode, node2: MyNode): Int {
      val default1 = (node1.userObject as SerialProfileConfigurable).isDefaultProfile
      val default2 = (node2.userObject as SerialProfileConfigurable).isDefaultProfile
      if (default1) {
        return if (default2) 0 else -1
      }
      if (default2) {
        return 1
      }
      return StringUtil.naturalCompare(node1.displayName, node2.displayName)
    }
  }

  override fun createActions(fromPopup: Boolean): MutableList<AnAction> {
    val addAction = object : DumbAwareAction(IdeBundle.messagePointer("action.NamedItemsListEditor.AddAction.text.add"),
                                             IdeBundle.messagePointer("action.NamedItemsListEditor.AddAction.description.add"),
                                             IconUtil.addIcon) {
      override fun actionPerformed(e: AnActionEvent) {
        val portNames = JsscSerialService.getPortNames().toTypedArray()
        var portName = if (portNames.isNotEmpty()) portNames[0]
        else
          SerialProfileConfigurable.systemDefaultPortName()
        portName =
          Messages.showEditableChooseDialog(SerialMonitorBundle.message("dialog.message.port"),
                                            SerialMonitorBundle.message("dialog.title.create.serial.connection.profile"),
                                            AllIcons.Nodes.Plugin,
                                            portNames, portName, null)
        if (portName.isNullOrBlank()) return
        var profileName = portName

        if (findNodeByName(myRoot, profileName) != null) {
          for (index in 1..999) {
            profileName = "$portName ($index)"
            if (findNodeByName(myRoot, profileName) == null) break
          }
        }
        val defaultConfigurable =
          findNodeByCondition(myRoot) { (it as? SerialProfileConfigurable)?.isDefaultProfile ?: false }
            .userObject as SerialProfileConfigurable
        defaultConfigurable.apply()
        val newNode = createNode(name = profileName,
                                 profile = defaultConfigurable.editableObject.copy(portName = portName),
                                 isDefault = false,
                                 isNew = true)
        ensureInitialized(newNode.userObject as SerialProfileConfigurable)
        selectNodeInTree(newNode, true, true)
      }
    }
    addAction.registerCustomShortcutSet(CommonShortcuts.INSERT, myTree)

    val deleteAction = object : DumbAwareAction(CommonBundle.messagePointer("button.delete"), CommonBundle.messagePointer("button.delete"),
                                                PlatformIcons.DELETE_ICON) {
      override fun actionPerformed(e: AnActionEvent) {
        removePaths(myTree.selectionPath)
      }

      override fun getActionUpdateThread() = EDT

      override fun update(e: AnActionEvent) {
        val selectedProfile = (myTree.selectionPath?.lastPathComponent as? MyNode)?.userObject as SerialProfileConfigurable?
        e.presentation.isEnabled = if (selectedProfile != null) !selectedProfile.isDefaultProfile else false
      }
    }
    deleteAction.registerCustomShortcutSet(CommonShortcuts.getDelete(), myTree)

    val copyAction = object : DumbAwareAction(IdeBundle.messagePointer("action.NamedItemsListEditor.CopyAction.text.copy"),
                                              IdeBundle.messagePointer("action.NamedItemsListEditor.CopyAction.description.copy"),
                                              COPY_ICON) {
      override fun update(e: AnActionEvent) {
        val selectedProfile = (myTree.selectionPath?.lastPathComponent as? MyNode)?.userObject as SerialProfileConfigurable?
        e.presentation.isEnabled = if (selectedProfile != null) !selectedProfile.isDefaultProfile else false
      }

      override fun getActionUpdateThread() = EDT

      override fun actionPerformed(e: AnActionEvent) {
        val selectedProfileConfigurable = selectedNode?.configurable as SerialProfileConfigurable?
        if (selectedProfileConfigurable == null) return
        var defaultName = ""
        for (i in 1..999) {
          defaultName = "${selectedProfileConfigurable.name} ($i)"
          val duplicate = TreeUtil.treeNodeTraverser(myRoot).firstOrNull { (it as MyNode).configurable.displayName == defaultName }
          if (duplicate == null) break
        }
        val name: String? = Messages.showInputDialog(
          SerialMonitorBundle.message("label.new.profile.name"),
          SerialMonitorBundle.message("dialog.title.create.serial.connection.profile"),
          Messages.getQuestionIcon(), defaultName, object : InputValidator {
          override fun checkInput(s: String): Boolean {
            return s.isNotEmpty() && findNodeByName(myRoot, s) == null
          }

          override fun canClose(s: String): Boolean {
            return checkInput(s)
          }
        })
        if (name == null) return
        selectedProfileConfigurable.apply()
        val newNode = createNode(name, selectedProfileConfigurable.editableObject.copy(), false, true)
        ensureInitialized(newNode.userObject as SerialProfileConfigurable)
        selectNodeInTree(newNode, true, true)
      }
    }
    copyAction.registerCustomShortcutSet(CommonShortcuts.getDuplicate(), myTree)
    return mutableListOf(addAction, deleteAction, copyAction)
  }

  override fun wasObjectStored(editableObject: Any?): Boolean {
    return true
  }

  override fun getId(): String = "serialmonitor.settings"
  override fun getHelpTopic(): String = id
  override fun getDisplayName(): String = SerialMonitorBundle.message("configurable.name.serial.settings")

  override fun apply() {
    super.apply()
    lateinit var defaultProfile: SerialPortProfile
    val profiles = mutableMapOf<String, SerialPortProfile>()
    TreeUtil.treeNodeTraverser(myRoot).forEach {
      val configurable = (it as MyNode).configurable
      if (configurable is SerialProfileConfigurable) {
        val portProfile = configurable.editableObject
        if (configurable.isDefaultProfile) {
          defaultProfile = portProfile
        }
        else {
          val name = configurable.name
          if (name.isBlank()) {
            throw ConfigurationException(UIBundle.message("master.detail.err.empty.name"))
          }
          if (profiles.put(name, portProfile) != null) {
            throw ConfigurationException(SerialMonitorBundle.message("dialog.message.duplicated.profile.name", name)); }
        }

      }
    }
    SerialProfileService.getInstance().apply {
      setDefaultProfile(defaultProfile)
      setProfiles(profiles)
    }
    ApplicationManager.getApplication().messageBus.syncPublisher(SerialSettingsChangeListener.TOPIC).settingsChanged()
  }

}

class SerialSettingsConfigurableProvider : ConfigurableProvider() {
  override fun createConfigurable(): Configurable = SerialSettingsConfigurable()
}