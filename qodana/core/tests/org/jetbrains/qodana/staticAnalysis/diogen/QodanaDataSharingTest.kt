package org.jetbrains.qodana.staticAnalysis.diogen

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloudclient.QDCloudException
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.staticAnalysis.QodanaEnvEmpty
import org.jetbrains.qodana.staticAnalysis.addQodanaEnvMock

class QodanaDataSharingTest : QodanaPluginLightTestBase() {
  override fun setUp() {
    super.setUp()
    withToken("token")
  }

  override fun tearDown() {
    super.tearDown()
    System.clearProperty(ORGANIZATION_ALLOWS_ERROR_REPORTING_PROPERTY)
  }

  fun `test property wins and makes no request`() {
    System.setProperty(ORGANIZATION_ALLOWS_ERROR_REPORTING_PROPERTY, "true")

    assertThat(isAllowed()).isTrue()
    assertThat(mockQDCloudHttpClient.requestsCount).isZero()
  }

  fun `test explicit false property makes no request`() {
    System.setProperty(ORGANIZATION_ALLOWS_ERROR_REPORTING_PROPERTY, "false")

    assertThat(isAllowed()).isFalse()
    assertThat(mockQDCloudHttpClient.requestsCount).isZero()
  }

  fun `test no token gives false`() {
    // The test env returns the latest non-null value, so an empty token overrides the default one.
    withToken("")
    assertThat(isAllowed()).isFalse()
    assertThat(mockQDCloudHttpClient.requestsCount).isZero()
  }

  fun `test api true`() {
    respondLicense("""{"licenseKey": "k", "organizationAllowsErrorReporting": true}""")

    assertThat(isAllowed()).isTrue()
  }

  fun `test old field gives false`() {
    respondLicense("""{"licenseKey": "k", "dataSharingAllowed": true}""")

    assertThat(isAllowed()).isFalse()
  }

  fun `test missing field gives false`() {
    respondLicense("""{"licenseKey": "k"}""")

    assertThat(isAllowed()).isFalse()
  }

  fun `test malformed json gives false`() {
    respondLicense("not json")

    assertThat(isAllowed()).isFalse()
  }

  fun `test http error gives false`() {
    mockQDCloudHttpClient.respond("linters/license-key") {
      QDCloudResponse.Error.ResponseFailure(QDCloudException.Error("boom", 500))
    }

    assertThat(isAllowed()).isFalse()
  }

  private fun isAllowed(): Boolean = runBlocking { isOrganizationAllowingErrorReporting() }

  private fun withToken(token: String) {
    addQodanaEnvMock(testRootDisposable, object : QodanaEnvEmpty() {
      override val QODANA_TOKEN by value(token)
    })
  }

  private fun respondLicense(body: String) {
    mockQDCloudHttpClient.respond("linters/license-key") { qodanaCloudResponse { body } }
  }
}
