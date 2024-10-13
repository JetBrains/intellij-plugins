package org.jetbrains.qodana.webUi

import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.util.application
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.run.QodanaConverterInput

class QodanaWebUiServiceTest : QodanaPluginLightTestBase() {
  private val testBrowserLauncher = TestBrowserLauncher()

  private val webUiService: QodanaWebUiService
    get() = QodanaWebUiService.getInstance(project)

  private val converterSarifInput: QodanaConverterInput.SarifFileOnly
    get() = QodanaConverterInput.SarifFileOnly(sarifTestReports.validForConverter)

  override fun runInDispatchThread(): Boolean = false

  override fun setUp() {
    super.setUp()
    invokeAndWaitIfNeeded {
      reinstansiateService(project, QodanaWebUiService::class.java, QodanaWebUiService(project, scope))
      reinstansiateService(application, BrowserLauncher::class.java, testBrowserLauncher)
    }
  }

  fun `test by default no active web uis`(): Unit = runBlocking {
    assertThat(webUiService.activeWebUis.value).isEmpty()
  }

  fun `test open active web ui`(): Unit = runBlocking {
    val id = "1"
    val activeWebUi = webUiService.requestOpenBrowserWebUi(id, converterSarifInput)
    assertThat(activeWebUi).isNotNull
    assertThat(activeWebUi!!.webUiId).isEqualTo(id)
    assertThat(testBrowserLauncher.timesBrowserOpened).isOne
    assertThat(webUiService.activeWebUis.value).contains(activeWebUi)
  }

  fun `test open active web ui has long enough token`(): Unit = runBlocking {
    val id = "1"
    val activeWebUi = webUiService.requestOpenBrowserWebUi(id, converterSarifInput)!!
    assertThat(activeWebUi.token.length).isGreaterThan(35)
  }

  fun `test open active web ui twice with same id`(): Unit = runBlocking {
    val id = "1"
    val activeWebUi = webUiService.requestOpenBrowserWebUi(id, converterSarifInput)
    assertThat(activeWebUi).isNotNull
    assertThat(activeWebUi!!.webUiId).isEqualTo(id)
    assertThat(testBrowserLauncher.timesBrowserOpened).isOne
    assertThat(webUiService.activeWebUis.value).contains(activeWebUi)

    val nextTryActiveWebUi = webUiService.requestOpenBrowserWebUi(id, converterSarifInput)
    assertThat(nextTryActiveWebUi).isSameAs(activeWebUi)
    assertThat(testBrowserLauncher.timesBrowserOpened).isEqualTo(2)
    assertThat(webUiService.activeWebUis.value).contains(activeWebUi)
  }

  fun `test open two active web uis`(): Unit = runBlocking {
    val id1 = "1"
    val activeWebUi1 = webUiService.requestOpenBrowserWebUi(id1, converterSarifInput)
    assertThat(activeWebUi1).isNotNull
    assertThat(activeWebUi1!!.webUiId).isEqualTo(id1)
    assertThat(testBrowserLauncher.timesBrowserOpened).isOne
    assertThat(webUiService.activeWebUis.value).contains(activeWebUi1)

    val id2 = "2"
    val activeWebUi2 = webUiService.requestOpenBrowserWebUi(id2, converterSarifInput)
    assertThat(activeWebUi2).isNotNull
    assertThat(activeWebUi2!!.webUiId).isEqualTo(id2)
    assertThat(testBrowserLauncher.timesBrowserOpened).isEqualTo(2)
    assertThat(webUiService.activeWebUis.value).contains(activeWebUi2)
    assertThat(webUiService.activeWebUis.value).contains(activeWebUi1)
    assertThat(activeWebUi2.token).isNotEqualTo(activeWebUi1.token)
  }

  fun `test open and close web ui`(): Unit = runBlocking {
    val id1 = "1"
    val activeWebUi1 = webUiService.requestOpenBrowserWebUi(id1, converterSarifInput)!!

    assertThat(activeWebUi1.close()).isTrue()
    assertThat(webUiService.activeWebUis.value).isEmpty()
  }

  fun `test open and close twice web ui`(): Unit = runBlocking {
    val id1 = "1"
    val activeWebUi1 = webUiService.requestOpenBrowserWebUi(id1, converterSarifInput)!!

    assertThat(activeWebUi1.close()).isTrue()
    assertThat(activeWebUi1.close()).isFalse()
    assertThat(webUiService.activeWebUis.value).isEmpty()
  }

  fun `test open two active web uis close first`(): Unit = runBlocking {
    val id1 = "1"
    val activeWebUi1 = webUiService.requestOpenBrowserWebUi(id1, converterSarifInput)!!

    val id2 = "2"
    val activeWebUi2 = webUiService.requestOpenBrowserWebUi(id2, converterSarifInput)!!

    assertThat(activeWebUi1.close()).isTrue()
    assertThat(webUiService.activeWebUis.value).isEqualTo(setOf(activeWebUi2))
  }
}