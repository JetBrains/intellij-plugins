package org.jetbrains.qodana.cloud.impl

import com.intellij.util.Url
import com.intellij.util.Urls
import org.jetbrains.qodana.cloud.QodanaCloudDefaultUrls
import org.jetbrains.qodana.cloud.StateManager
import org.jetbrains.qodana.cloud.UserState

internal class NotAuthorizedImpl(
  private val stateManager: StateManager<UserState>,
  initialSelfHostedFrontendUrl: Url?
) : UserState.NotAuthorized {
  private val defaultQodanaCloudFrontendUrl: Url
    get() = Urls.newFromEncoded(QodanaCloudDefaultUrls.websiteUrl)

  override val selfHostedFrontendUrl: Url? = if (defaultQodanaCloudFrontendUrl == initialSelfHostedFrontendUrl) null else initialSelfHostedFrontendUrl

  override fun authorize(selfHostedUrl: Url?): UserState.Authorizing? {
    val correctedSelfHostedFrontendUrl = if (selfHostedUrl == defaultQodanaCloudFrontendUrl) null else selfHostedUrl
    val authorizing = AuthorizingImpl(stateManager, correctedSelfHostedFrontendUrl)
    return stateManager.changeState(this, authorizing)
  }
}