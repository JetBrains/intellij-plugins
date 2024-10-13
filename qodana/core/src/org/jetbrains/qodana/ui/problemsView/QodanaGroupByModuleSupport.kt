package org.jetbrains.qodana.ui.problemsView

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ExtensionPointListener
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.PluginDescriptor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface QodanaGroupByModuleSupport {
  companion object {
    val EP = ExtensionPointName<QodanaGroupByModuleSupport>("org.intellij.qodana.problemsViewModuleSupport")

    fun isSupported(): Boolean = EP.extensions.isNotEmpty()
  }
}

class QodanaGroupByModuleSupportImpl : QodanaGroupByModuleSupport

@Service(Service.Level.APP)
class QodanaGroupByModuleSupportService : Disposable {
  companion object {
    fun getInstance(): QodanaGroupByModuleSupportService = service()
  }

  private val _isSupported = MutableStateFlow(QodanaGroupByModuleSupport.isSupported())
  val isSupported: StateFlow<Boolean> = _isSupported.asStateFlow()

  private fun update() {
    _isSupported.value = QodanaGroupByModuleSupport.isSupported()
  }

  init {
    QodanaGroupByModuleSupport.EP.addExtensionPointListener(EPListener(), this)
  }

  private inner class EPListener : ExtensionPointListener<QodanaGroupByModuleSupport> {
    override fun extensionAdded(extension: QodanaGroupByModuleSupport, pluginDescriptor: PluginDescriptor) = update()

    override fun extensionRemoved(extension: QodanaGroupByModuleSupport, pluginDescriptor: PluginDescriptor) = update()
  }

  override fun dispose() = Unit
}