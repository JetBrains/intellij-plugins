package org.jetbrains.qodana.staticAnalysis.licenses

import org.jetbrains.qodana.license.QodanaLicense
import org.jetbrains.qodana.license.QodanaLicenseChecker
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat

class QodanaLicenseCheckerTest {
  @Test
  fun parseLicenseYes() {
    val license = """
      {
        "licenseId": "HG6V8JA1K3",
        "licenseeName": "Keith Wojciech",
        "assigneeName": "",
        "assigneeEmail": "",
        "licenseRestriction": "",
        "checkConcurrentUse": false,
        "products": [
          {
            "code": "QDL",
            "paidUpTo": "2023-10-20",
            "extended": false,
            "properties": {
              "plan": "QDUE"
            }
          },
          {
            "code": "QDUE",
            "paidUpTo": "2023-10-20",
            "extended": false
          }
        ],
        "metadata": "0120221020PSAA001009",
        "hash": "39107469/0:-1863690899",
        "gracePeriodDays": 7,
        "autoProlongated": false,
        "isAutoProlongated": false
      }
    """.trimIndent()
    Assert.assertEquals(
      QodanaLicense(QodanaLicenseType.ULTIMATE_PLUS, false, SimpleDateFormat("yyyy-MM-dd").parse("2023-10-20")),
      QodanaLicenseChecker.parseLicense(license))
  }

  @Test
  fun parseTrialLicenseYes() {
    val license = """
      {
         "licenseId":"A5CZ0CZKGB",
         "licenseeName":"Testing trial org",
         "licenseeType":"COMMERCIAL",
         "assigneeName":"",
         "assigneeEmail":"",
         "licenseRestriction":"Evaluation purpose only",
         "checkConcurrentUse":false,
         "products":[
            {
               "code":"QDL",
               "paidUpTo":"2024-01-22",
               "extended":false,
               "properties":{
                  "plan":"QDUP"
               }
            }
         ],
         "metadata":"0120231124CSAM000006",
         "hash":"TRIAL:1742514639",
         "gracePeriodDays":0,
         "autoProlongated":false,
         "isAutoProlongated":false,
         "trial":true,
         "aiAllowed":false
      }
    """.trimIndent()
    Assert.assertEquals(
      QodanaLicense(QodanaLicenseType.ULTIMATE_PLUS, true, SimpleDateFormat("yyyy-MM-dd").parse("2024-01-22")),
      QodanaLicenseChecker.parseLicense(license))
  }


  @Test
  fun parseLicenseWrongPlan() {
    val license = """
      {
        "licenseId": "HG6V8JA1K3",
        "licenseeName": "Keith Wojciech",
        "assigneeName": "",
        "assigneeEmail": "",
        "licenseRestriction": "",
        "checkConcurrentUse": false,
        "products": [
          {
            "code": "QDL",
            "paidUpTo": "2023-10-20",
            "extended": false,
            "properties": {
              "plan": "QDS"
            }
          },
          {
            "code": "QDS",
            "paidUpTo": "2023-10-20",
            "extended": false
          }
        ],
        "metadata": "0120221020PSAA001009",
        "hash": "39107469/0:-1863690899",
        "gracePeriodDays": 7,
        "autoProlongated": false,
        "isAutoProlongated": false
      }
    """.trimIndent()
    Assert.assertThrows(QodanaException::class.java) { QodanaLicenseChecker.parseLicense(license) }
  }

  @Test
  fun parseLicenseNo() {
    val license = """
      {
        "licenseId": "HG6V8JA1K3",
        "licenseeName": "Keith Wojciech",
        "assigneeName": "",
        "assigneeEmail": "",
        "licenseRestriction": "",
        "checkConcurrentUse": false,
        "products": [
          {
            "code": "QDL",
            "paidUpTo": "2023-10-20",
            "extended": false
          }
        ],
        "metadata": "0120221020PSAA001009",
        "hash": "39107469/0:-1863690899",
        "gracePeriodDays": 7,
        "autoProlongated": false,
        "isAutoProlongated": false
      }
    """.trimIndent()
    Assert.assertThrows(QodanaException::class.java) { QodanaLicenseChecker.parseLicense(license) }
  }

  @Test
  fun parseLicenseNoQdlProduct() {
    val license = """
      {
        "licenseId": "HG6V8JA1K3",
        "licenseeName": "Keith Wojciech",
        "assigneeName": "",
        "assigneeEmail": "",
        "licenseRestriction": "",
        "checkConcurrentUse": false,
        "products": [
          {
            "code": "QDS",
            "paidUpTo": "2023-10-20",
            "extended": false,
            "properties": {
              "plan": "QDU"
            }
          }
        ],
        "metadata": "0120221020PSAA001009",
        "hash": "39107469/0:-1863690899",
        "gracePeriodDays": 7,
        "autoProlongated": false,
        "isAutoProlongated": false
      }
    """.trimIndent()
    Assert.assertThrows(QodanaException::class.java) { QodanaLicenseChecker.parseLicense(license) }
  }

  @Test
  fun parseLicenseNoProducts() {
    val license = """
      {
        "licenseId": "HG6V8JA1K3",
        "licenseeName": "Keith Wojciech",
        "assigneeName": "",
        "assigneeEmail": "",
        "licenseRestriction": "",
        "checkConcurrentUse": false,
        "metadata": "0120221020PSAA001009",
        "hash": "39107469/0:-1863690899",
        "gracePeriodDays": 7,
        "autoProlongated": false,
        "isAutoProlongated": false
      }
    """.trimIndent()
    Assert.assertThrows(QodanaException::class.java) { QodanaLicenseChecker.parseLicense(license) }
  }

  @Test
  fun parseLicenseNotJson() {
    val license = """
      <xml></xml>
    """.trimIndent()
    Assert.assertThrows(QodanaException::class.java) { QodanaLicenseChecker.parseLicense(license) }
  }
}