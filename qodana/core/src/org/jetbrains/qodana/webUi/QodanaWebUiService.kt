package org.jetbrains.qodana.webUi

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.ui.JBColor
import com.intellij.util.Urls
import com.intellij.util.io.DigestUtil
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.run.QodanaConverterInput
import org.jetbrains.qodana.run.QodanaConverterResults
import org.jetbrains.qodana.run.runQodanaConverter
import org.jetbrains.qodana.webUi.handlers.QODANA_WEB_UI_SOURCES_HANDLER_NAME
import org.jetbrains.qodana.webUi.handlers.QODANA_WEB_UI_TOKEN_PARAM
import java.util.*

@Service(Service.Level.PROJECT)
class QodanaWebUiService(@Suppress("unused") private val project: Project, private val scope: CoroutineScope) {
  companion object {
    fun getInstance(project: Project): QodanaWebUiService = project.service()
  }

  private val changeWebUiStateRequest = MutableSharedFlow<ChangeWebUiStateRequest>(replay = 1)

  val activeWebUis: StateFlow<Set<ActiveWebUi>> = createActiveWebUisFlow()

  private sealed interface ChangeWebUiStateRequest {
    class Open(val id: String, val converterInput: QodanaConverterInput) : ChangeWebUiStateRequest {
      val activeWebUiDeferred = CompletableDeferred<ActiveWebUi?>()
    }

    class Close(val webUiToClose: ActiveWebUi) : ChangeWebUiStateRequest {
      val hasClosedDeferred = CompletableDeferred<Boolean>()
    }
  }

  private fun createActiveWebUisFlow(): StateFlow<Set<ActiveWebUi>> {
    val activeWebUisStateFlow = MutableStateFlow<Set<ActiveWebUiImpl>>(emptySet())
    scope.launch(QodanaDispatchers.Default) {
      changeWebUiStateRequest.collect { changeWebUiStateRequest ->
        val newActiveWebUis: MutableMap<String, ActiveWebUiImpl> = activeWebUisStateFlow.value.associateBy { it.webUiId }.toMutableMap()
        when(changeWebUiStateRequest) {
          is ChangeWebUiStateRequest.Open -> {
            var resultWebUi: ActiveWebUiImpl? = null
            supervisorScope {
              launch {
                val converterInput = changeWebUiStateRequest.converterInput
                val id = changeWebUiStateRequest.id
                val alreadyPresentWebUi = newActiveWebUis[id]

                val webUi = if (alreadyPresentWebUi == null) {
                  val converterOutput = runQodanaConverterWithProgress(converterInput)
                  ActiveWebUiImpl(converterOutput, id)
                }
                else {
                  alreadyPresentWebUi
                }
                newActiveWebUis[id] = webUi
                resultWebUi = webUi
              }
            }
            activeWebUisStateFlow.value = newActiveWebUis.values.toSet()
            resultWebUi?.openUiInBrowser()
            changeWebUiStateRequest.activeWebUiDeferred.complete(resultWebUi)
          }
          is ChangeWebUiStateRequest.Close -> {
            val hasClosed = newActiveWebUis.remove(changeWebUiStateRequest.webUiToClose.webUiId) != null
            activeWebUisStateFlow.value = newActiveWebUis.values.toSet()
            changeWebUiStateRequest.hasClosedDeferred.complete(hasClosed)
          }
        }
      }
    }
    return activeWebUisStateFlow.asStateFlow()
  }

  private suspend fun runQodanaConverterWithProgress(converterInput: QodanaConverterInput): QodanaConverterResults {
    return withBackgroundProgress(project, QodanaBundle.message("qodana.web.ui.loading")) {
      runQodanaConverter(converterInput)
    }
  }

  suspend fun requestOpenBrowserWebUi(id: String, converterInput: QodanaConverterInput): ActiveWebUi? {
    val openWebUiRequest = ChangeWebUiStateRequest.Open(id, converterInput)
    val isRequestSubmitted = changeWebUiStateRequest.tryEmit(openWebUiRequest)
    if (!isRequestSubmitted) return null

    return openWebUiRequest.activeWebUiDeferred.await()
  }

  private inner class ActiveWebUiImpl(
    override val qodanaConverterResults: QodanaConverterResults,
    override val webUiId: String
  ) : ActiveWebUi {
    override val token: String = UUID.randomUUID().toString()

    fun openUiInBrowser() {
      val port = BuiltInServerManager.getInstance().port
      val sourcesHandler = QODANA_WEB_UI_SOURCES_HANDLER_NAME
      val theme = if (JBColor.isBright()) "light" else "dark"

      val md5 = DigestUtil.md5()
      md5.update(project.locationHash.toByteArray())
      val projectHash = DigestUtil.digestToHash(md5)

      val url = Urls.newFromEncoded("http://localhost:$port/$sourcesHandler/idea.html")
        .addParameters(mapOf(
          "projectKey" to projectHash, // projectKey is not sensitive, put it first to not show off sensitive _qdt param in browser
          QODANA_WEB_UI_TOKEN_PARAM to token,
          "theme" to theme,
        ))

      BrowserUtil.browse(url.toExternalForm())
    }

    override suspend fun close(): Boolean {
      val closeWebUiRequest = ChangeWebUiStateRequest.Close(this)
      changeWebUiStateRequest.emit(closeWebUiRequest)
      return closeWebUiRequest.hasClosedDeferred.await()
    }

    override fun hashCode(): Int = webUiId.hashCode()

    override fun equals(other: Any?): Boolean {
      if (other !is ActiveWebUi) return false
      return webUiId == other.webUiId
    }
  }
}