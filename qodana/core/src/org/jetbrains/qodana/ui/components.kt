package org.jetbrains.qodana.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.bindEnabled
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.VideoPromoComponent
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.Url
import com.intellij.util.Urls
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.StartupUiUtil
import icons.QodanaIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.normalizeUrl
import java.net.MalformedURLException
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.event.DocumentEvent

internal val QODANA_PROMO_URL = QodanaBundle.message("qodana.website.url")
internal val QODANA_HELP_URL = QodanaBundle.message("qodana.documentation.ide.plugin.url")
internal val QODANA_PREVIEW_URL = QodanaBundle.message("qodana.preview.url")

fun buildSettingsLogInPanel(scope: CoroutineScope, server: String?, logInAction: (Url?) -> Unit) = panel {
  logInPanel(scope, server, logInAction)
  separator()
    .bottomGap(BottomGap.SMALL)
  qodanaPromo()
  qodanaPromoBanner()
}

fun Panel.logInPanel(scope: CoroutineScope, initialServer: String?, logInAction: (Url?) -> Unit) {
  val errorMessageFlow = MutableStateFlow<@Nls String?>(null)

  val useCustomServer = AtomicProperty(initialServer != null)
  val serverNameField = JBTextField(30).apply {
    emptyText.text = QodanaBundle.message("qodana.settings.panel.organization.url.template")
    if (initialServer != null) text = initialServer
  }.bindEnabled(useCustomServer)

  row {
    label(QodanaBundle.message("qodana.settings.panel.log.in.label"))
      .applyToComponent { font = JBFont.label().biggerOn(3.0f) }
  }
  row {
    button(QodanaBundle.message("qodana.settings.panel.log.in.button")) {
      val serverUrl = if (useCustomServer.get()) {
        if (serverNameField.text.isEmpty()) {
          errorMessageFlow.value = QodanaBundle.message("qodana.settings.panel.organization.url.empty.error")
          return@button
        }
        try {
          Urls.newFromEncoded(normalizeUrl(serverNameField.text))
        } catch (e: MalformedURLException) {
          e.message?.let { errorMessageFlow.value = QodanaBundle.message("qodana.settings.panel.organization.url.another.error") }
          return@button
        }
      } else null
      logInAction.invoke(serverUrl)
    }
      .apply { component.putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }
  }
  val selfHostedBlock = com.intellij.ui.dsl.builder.panel {
    row {
      checkBox(QodanaBundle.message("qodana.settings.panel.custom.server")).apply {
        onChanged {
          serverNameField.isEnabled = it.isSelected
        }
        bindSelected(useCustomServer)
      }
    }
    row(QodanaBundle.message("qodana.settings.panel.organization.url")) {
      cell(serverNameField)
    }.visibleIf(useCustomServer)
  }
  row {
    cell(selfHostedBlock)
      .apply { component.putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }
  }
  addServerFieldValidation(scope, serverNameField, errorMessageFlow)
}


private fun addServerFieldValidation(scope: CoroutineScope, serverNameField: JBTextField, errorMessageFlow: MutableStateFlow<@Nls String?>) {
  serverNameField.document.addDocumentListener( object : DocumentAdapter() {
    override fun textChanged(e: DocumentEvent) {
      errorMessageFlow.value = null
    }
  })
  scope.launch {
    val validatorDisposable = Disposer.newDisposable()
    val validator = ComponentValidator(validatorDisposable).installOn(serverNameField)
    try {
      errorMessageFlow.collect { errorMessage ->
        validator.updateInfo(errorMessage?.let { ValidationInfo(it, serverNameField) })
      }
    } finally {
      Disposer.dispose(validatorDisposable)
    }
  }
}

fun Panel.qodanaPromo(maxLineLength: Int = -1) {
  row { icon(QodanaIcons.Images.Qodana) }
  row { text(QodanaBundle.message("qodana.promo.text.full"), maxLineLength = maxLineLength) }
  row { browserLink(QodanaBundle.message("qodana.explore.promo.button"), QODANA_PROMO_URL) }
}

fun Panel.qodanaPromoBanner() {
  val qodanaPreview = VideoPromoComponent(JLabel(QodanaIcons.Images.QodanaVideoPreview),
                                          QodanaBundle.message("qodana.promo.banner.watch.overview"),
                                          alwaysDisplayLabel = true,
                                          darkLabel = StartupUiUtil.isUnderDarcula) {
    BrowserUtil.browse(QODANA_PREVIEW_URL)
  }
  row { cell(qodanaPreview) }
}

fun Wrapper.setContentAndRepaint(wrapped: JComponent?) {
  val oldWrapped = targetComponent
  setContent(wrapped)
  if (oldWrapped != wrapped) {
    repaint()
  }
}