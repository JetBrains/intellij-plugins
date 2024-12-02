package org.jetbrains.qodana.jvm.java.jps

import com.intellij.collaboration.auth.isSpacePrivatePackageUrl
import com.intellij.jarRepository.JarRepositoryAuthenticationDataProvider
import com.intellij.jarRepository.RemoteRepositoryDescription
import com.intellij.openapi.progress.runBlockingCancellable

internal class QodanaSpaceJarRepositoryAuthenticationDataProvider : JarRepositoryAuthenticationDataProvider {
  override fun provideAuthenticationData(description: RemoteRepositoryDescription): JarRepositoryAuthenticationDataProvider.AuthenticationData? {
    val authFromEnv = provideAuthenticationFromEnv() ?: return null
    return runBlockingCancellable {
      if (isSpacePrivatePackageUrl(description.url)) authFromEnv else null
    }
  }

  private fun provideAuthenticationFromEnv(): JarRepositoryAuthenticationDataProvider.AuthenticationData? {
    val spaceUsername = System.getenv("JPS_SPACE_AUTH_USERNAME") ?: return null
    val spacePassword = System.getenv("JPS_SPACE_AUTH_PASSWORD") ?: return null
    return JarRepositoryAuthenticationDataProvider.AuthenticationData(spaceUsername, spacePassword)
  }
}