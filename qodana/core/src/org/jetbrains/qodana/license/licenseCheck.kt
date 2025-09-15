package org.jetbrains.qodana.license

import com.intellij.ide.nls.NlsMessages
import com.intellij.ui.LicensingFacade
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import kotlin.system.exitProcess

/**
 * On failure to obtain license will exit app with exit code = 7.
 */
fun checkLicense(): QodanaLicense {
  try {
    return QodanaLicenseChecker.getLicenseType()
  }
  catch (e: QodanaException) {
    e.printStackTrace()
    println("""There is no valid license or license has expired.""")
    exitProcess(7)
  }
}

fun printLicenseInfo(license: QodanaLicense) {
  if (QodanaLicenseChecker.isEapLicensing()) {
    if (license.expirationDate != null) {
      val formattedExpirationDate = NlsMessages.formatDateLong(license.expirationDate)
      println(QodanaBundle.message("eap.license.message.with.expiration.date", formattedExpirationDate))
    }
    else {
      println(QodanaBundle.message("eap.license.message", EAP_LICENSE_DURATION))
    }
  }
  else {
    if (license.type != QodanaLicenseType.COMMUNITY) {
      println(QodanaBundle.message("release.license.message"))
      val licensingFacade = LicensingFacade.getInstance()
      if (licensingFacade != null) {
        val licensedTo = licensingFacade.licensedToMessage
        if (licensedTo != null) {
          println(licensedTo)
        }
        licensingFacade.licenseRestrictionsMessages.forEach {
          println(it)
        }
      }
    }
    else {
      println(QodanaBundle.message("release.community.license.message"))
    }
  }
  println("Qodana license plan: ${license.type.presentableName}")
}
