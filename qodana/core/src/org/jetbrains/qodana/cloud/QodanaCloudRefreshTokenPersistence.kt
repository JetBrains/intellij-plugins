package org.jetbrains.qodana.cloud

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaRefreshToken
import org.jetbrains.qodana.coroutines.QodanaDispatchers

private const val QODANA_CREDENTIALS_SUBSYSTEM = "Qodana"
private const val QODANA_CLOUD_REFRESH_KEY = "qodana.cloud.refresh.key"

internal class QodanaCloudRefreshTokenPersistence {
  private val storedCredentialAttributes: CredentialAttributes
    get() = CredentialAttributes(generateServiceName(QODANA_CREDENTIALS_SUBSYSTEM, QODANA_CLOUD_REFRESH_KEY))

  suspend fun getToken(): QodanaRefreshToken? {
    return withContext(QodanaDispatchers.IO) {
      PasswordSafe.instance.getPassword(storedCredentialAttributes)
    }
  }

  suspend fun saveToken(refreshToken: QodanaRefreshToken) {
    withContext(QodanaDispatchers.IO) {
      PasswordSafe.instance.setPassword(storedCredentialAttributes, refreshToken)
    }
  }

  suspend fun clear() {
    withContext(QodanaDispatchers.IO) {
      PasswordSafe.instance.setPassword(storedCredentialAttributes, null)
    }
  }
}