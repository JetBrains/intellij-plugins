package org.jetbrains.qodana.staticAnalysis.diogen

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.QDCloudException
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.staticAnalysis.QodanaEnvEmpty
import org.jetbrains.qodana.staticAnalysis.addQodanaEnvMock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class QodanaDataSharingTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  @BeforeEach
  fun setUp() {
    withToken("token")
  }

  @AfterEach
  fun tearDown() {
    System.clearProperty(ORGANIZATION_ALLOWS_ERROR_REPORTING_PROPERTY)
  }

  @Test
  fun `test property wins and makes no request`() {
    System.setProperty(ORGANIZATION_ALLOWS_ERROR_REPORTING_PROPERTY, "true")

    assertThat(isAllowed()).isTrue()
    assertThat(mockQDCloudHttpClient.requestsCount).isZero()
  }

  @Test
  fun `test explicit false property makes no request`() {
    System.setProperty(ORGANIZATION_ALLOWS_ERROR_REPORTING_PROPERTY, "false")

    assertThat(isAllowed()).isFalse()
    assertThat(mockQDCloudHttpClient.requestsCount).isZero()
  }

  @Test
  fun `test no token gives false`() {
    // The test env returns the latest non-null value, so an empty token overrides the default one.
    withToken("")
    assertThat(isAllowed()).isFalse()
    assertThat(mockQDCloudHttpClient.requestsCount).isZero()
  }

  @Test
  fun `test api true`() {
    respondLicense("""{"licenseKey": "k", "organizationAllowsErrorReporting": true}""")

    assertThat(isAllowed()).isTrue()
  }

  @Test
  fun `test old field gives false`() {
    respondLicense("""{"licenseKey": "k", "dataSharingAllowed": true}""")

    assertThat(isAllowed()).isFalse()
  }

  @Test
  fun `test missing field gives false`() {
    respondLicense("""{"licenseKey": "k"}""")

    assertThat(isAllowed()).isFalse()
  }

  @Test
  fun `test malformed json gives false`() {
    respondLicense("not json")

    assertThat(isAllowed()).isFalse()
  }

  @Test
  fun `test http error gives false`() {
    mockQDCloudHttpClient.respond("linters/license-key") {
      QDCloudResponse.Error.ResponseFailure(QDCloudException.Error("boom", 500))
    }

    assertThat(isAllowed()).isFalse()
  }

  private fun isAllowed(): Boolean = runBlocking { isOrganizationAllowingErrorReporting() }

  private fun withToken(token: String) {
    addQodanaEnvMock(qodanaFixture.testRootDisposable, object : QodanaEnvEmpty() {
      override val QODANA_TOKEN by value(token)
    })
  }

  private fun respondLicense(body: String) {
    mockQDCloudHttpClient.respond("linters/license-key") { qodanaCloudResponse { body } }
  }
}
