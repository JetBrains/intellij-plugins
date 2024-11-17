package org.jetbrains.qodana.cloud

import com.intellij.ide.BrowserUtil
import com.intellij.util.Url
import com.intellij.util.Urls
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.staticAnalysis.qodanaEnv


internal object QodanaCloudDefaultUrls {
  val websiteUrl: String
    get() = QodanaRegistry.Cloud.website

  val cloudApi: String
    get() = qodanaEnv().ENDPOINT.value ?: QodanaRegistry.Cloud.cloudApi

  val jbaOAuthUrl: String
    get() = QodanaRegistry.Cloud.jbaOAuthUrl

  val jbaAuthReferrer: String
    get() = QodanaRegistry.Cloud.jbaAuthReferrer

  val jbaClientId: String
    get() = QodanaRegistry.Cloud.jbaClientId
}

internal fun currentQodanaCloudFrontendUrl(): Url {
  return QodanaCloudStateService.getInstance().userState.value.frontendUrl
}

internal fun openBrowserWithCurrentQodanaCloudFrontend() {
  BrowserUtil.browse(currentQodanaCloudFrontendUrl().toString())
}

internal fun projectFrontendUrlForQodanaCloud(projectId: String, reportId: String?, cloudHost: String? = null): Url {
  val frontendUrl = cloudHost?.let { Urls.newFromEncoded(cloudHost) } ?: currentQodanaCloudFrontendUrl()
  val urlWithProject = frontendUrl.resolve("projects/$projectId")
  return reportId?.let { urlWithProject.resolve("reports/$reportId") } ?: urlWithProject
}

internal fun hostsEqual(url1: String?, url2: String?): Boolean {
  if (url1 == null || url2 == null) {
    return url1 == url2 || url1 == QodanaCloudDefaultUrls.websiteUrl || url2 == QodanaCloudDefaultUrls.websiteUrl
  }

  val host1 = Urls.newFromEncoded(url1)
  val host2 = Urls.newFromEncoded(url2)

  return host1 == host2
}

internal fun normalizeUrl(serverName: String): String {
  val result =
    if (serverName.startsWith("https://") || serverName.startsWith("http://"))
      serverName
    else
      "https://$serverName"
  return result.removeSuffix("/")
}