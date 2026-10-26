package org.jetbrains.qodana.webUi

import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.util.application
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.run.QodanaConverterInput
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class QodanaWebUiServiceTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private val testBrowserLauncher = TestBrowserLauncher()

  private val webUiService: QodanaWebUiService
    get() = QodanaWebUiService.getInstance(qodanaFixture.project)

  private val converterSarifInput: QodanaConverterInput.SarifFileOnly
    get() = QodanaConverterInput.SarifFileOnly(qodanaFixture.sarifTestReports.validForConverter)

  @BeforeEach
  fun setUp() {
    qodanaFixture.reinstansiateService(qodanaFixture.project, QodanaWebUiService::class.java, QodanaWebUiService(qodanaFixture.project, qodanaFixture.scope))
    qodanaFixture.reinstansiateService(application, BrowserLauncher::class.java, testBrowserLauncher)
  }

  @Test
  fun `test by default no active web uis`(): Unit = timeoutRunBlocking {
    assertThat(webUiService.activeWebUis.value).isEmpty()
  }

  @Test
  fun `test open active web ui`(): Unit = timeoutRunBlocking {
    val id = "1"
    val activeWebUi = webUiService.requestOpenBrowserWebUi(id, converterSarifInput)
    assertThat(activeWebUi).isNotNull
    assertThat(activeWebUi!!.webUiId).isEqualTo(id)
    assertThat(testBrowserLauncher.timesBrowserOpened).isOne
    assertThat(webUiService.activeWebUis.value).contains(activeWebUi)
  }

  @Test
  fun `test open active web ui has long enough token`(): Unit = timeoutRunBlocking {
    val id = "1"
    val activeWebUi = webUiService.requestOpenBrowserWebUi(id, converterSarifInput)!!
    assertThat(activeWebUi.token.length).isGreaterThan(35)
  }

  @Test
  fun `test open active web ui twice with same id`(): Unit = timeoutRunBlocking {
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

  @Test
  fun `test open two active web uis`(): Unit = timeoutRunBlocking {
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

  @Test
  fun `test open and close web ui`(): Unit = timeoutRunBlocking {
    val id1 = "1"
    val activeWebUi1 = webUiService.requestOpenBrowserWebUi(id1, converterSarifInput)!!

    assertThat(activeWebUi1.close()).isTrue()
    assertThat(webUiService.activeWebUis.value).isEmpty()
  }

  @Test
  fun `test open and close twice web ui`(): Unit = timeoutRunBlocking {
    val id1 = "1"
    val activeWebUi1 = webUiService.requestOpenBrowserWebUi(id1, converterSarifInput)!!

    assertThat(activeWebUi1.close()).isTrue()
    assertThat(activeWebUi1.close()).isFalse()
    assertThat(webUiService.activeWebUis.value).isEmpty()
  }

  @Test
  fun `test open two active web uis close first`(): Unit = timeoutRunBlocking {
    val id1 = "1"
    val activeWebUi1 = webUiService.requestOpenBrowserWebUi(id1, converterSarifInput)!!

    val id2 = "2"
    val activeWebUi2 = webUiService.requestOpenBrowserWebUi(id2, converterSarifInput)!!

    assertThat(activeWebUi1.close()).isTrue()
    assertThat(webUiService.activeWebUis.value).isEqualTo(setOf(activeWebUi2))
  }
}