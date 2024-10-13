package org.jetbrains.qodana.help

import com.intellij.openapi.help.WebHelpProvider
import org.jetbrains.qodana.ui.QODANA_PROMO_URL

class QodanaWebHelpProvider : WebHelpProvider() {
  companion object {
    private const val PLUGIN_HELP_ID = "org.intellij.qodana."
    const val WEBSITE_ID = "${PLUGIN_HELP_ID}website"
  }

  override fun getHelpPageUrl(helpTopicId: String): String {
    return when (helpTopicId) {
      WEBSITE_ID -> QODANA_PROMO_URL
      else -> "https://www.jetbrains.com/help/qodana/welcome.html"
    }
  }
}