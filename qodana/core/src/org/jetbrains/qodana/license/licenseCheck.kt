package org.jetbrains.qodana.license

import com.intellij.ide.nls.NlsMessages
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.ui.LicensingFacade
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.runner.COMMUNITY_PRODUCT_CODES
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.util.*
import kotlin.system.exitProcess

private const val EAP_LICENSE_DURATION = 60

/**
 * On failure to obtain license will exit app with exit code = 7.
 */
fun checkLicense(): QodanaLicense {
  val isCommunityProductCode = ApplicationInfo.getInstance().build.productCode in COMMUNITY_PRODUCT_CODES
  val licensingFacade = LicensingFacade.getInstance()
  if (licensingFacade == null && !isCommunityProductCode) {
      println("""Non-community product requires license""")
      exitProcess(7)
  }

  if (isCommunityProductCode) {
    val expirationDate = checkCommunityEapLicense()
    return QodanaLicense(QodanaLicenseType.COMMUNITY, false, expirationDate)
  }

  return checkUltimateLicense()
}

private fun checkUltimateLicense(): QodanaLicense {
  try {
    return QodanaLicenseChecker.getLicenseType()

  } catch (e: QodanaException) {
    e.printStackTrace()
    println("""There is no valid license or license has expired.""")
    exitProcess(7)
  }
}

fun printLicenseInfo(license: QodanaLicense) {
  if (isEapLicensing()) {
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
    } else {
      println(QodanaBundle.message("release.community.license.message"))
    }
  }
  println("Qodana license plan: ${license.type.presentableName}")
}

private fun isEapLicensing(): Boolean {
  // eap.require.license is only for test purposes. It changes behaviour of LicenseManager and qodana output.
  return ApplicationInfo.getInstance().isEAP && System.getProperty("eap.require.license") != "release"
}

// LicenseManager can't set expiration date on community builds.
// Release builds should have perpetual license and EAP's 30-days limit.
// So method duplicates logic of HeadlessLicense for checking community EAP's.
private fun checkCommunityEapLicense(): Date? {
  if (!isEapLicensing()) return null
  val calendar = ApplicationInfo.getInstance().buildDate.clone() as Calendar
  val buildDate = calendar.time

  calendar.add(Calendar.DAY_OF_MONTH, EAP_LICENSE_DURATION)
  val expirationDate = calendar.time


  val tomorrowCalendar = Calendar.getInstance()
  tomorrowCalendar.setTime(Date())
  tomorrowCalendar.add(Calendar.DAY_OF_MONTH, 1)
  val tomorrow = tomorrowCalendar.time

  if (tomorrow.before(buildDate) || expirationDate.before(Date())) {
    println("""EAP license of this Qodana image is expired. Please use "docker pull" to update image.""")
    exitProcess(7)
  }

  return expirationDate
}