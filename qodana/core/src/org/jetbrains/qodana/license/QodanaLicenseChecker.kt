package org.jetbrains.qodana.license

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.diagnostic.logger
import com.intellij.ui.LicensingFacade
import org.jetbrains.qodana.license.QodanaLicenseType.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.Signature
import java.security.cert.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class QodanaLicense(val type: QodanaLicenseType, val trial: Boolean, val expirationDate: Date?)

enum class QodanaLicenseType(val presentableName: String) {
  COMMUNITY("Community"),
  ULTIMATE("Ultimate"),
  ULTIMATE_PLUS("Ultimate Plus"),
  PREMIUM("Premium"),
  NONE("None")
}

/**
 * Code is from
 * https://plugins.jetbrains.com/docs/marketplace/add-marketplace-license-verification-calls-to-the-plugin-code.html
 * we have to inline it to the plugin.
 */
@Suppress("NOTHING_TO_INLINE")
object QodanaLicenseChecker {
  ////// License check inlined magic
  @PublishedApi
  internal inline val PRODUCT_CODE get() = "QDL"
  @PublishedApi
  internal inline val KEY_PREFIX get() = "key:"
  @PublishedApi
  internal inline val EVAL_PREFIX get() = "eval:"
  /**
   * Public root certificates needed to verify JetBrains-signed licenses
   */
  @Suppress("SpellCheckingInspection")
  @PublishedApi
  internal inline val ROOT_CERTIFICATES
    get() = sequence {
      yield(sequence {
        yield("-----BEGIN CERTIFICATE-----")
        yield("MIIFOzCCAyOgAwIBAgIJANJssYOyg3nhMA0GCSqGSIb3DQEBCwUAMBgxFjAUBgNV")
        yield("BAMMDUpldFByb2ZpbGUgQ0EwHhcNMTUxMDAyMTEwMDU2WhcNNDUxMDI0MTEwMDU2")
        yield("WjAYMRYwFAYDVQQDDA1KZXRQcm9maWxlIENBMIICIjANBgkqhkiG9w0BAQEFAAOC")
        yield("Ag8AMIICCgKCAgEA0tQuEA8784NabB1+T2XBhpB+2P1qjewHiSajAV8dfIeWJOYG")
        yield("y+ShXiuedj8rL8VCdU+yH7Ux/6IvTcT3nwM/E/3rjJIgLnbZNerFm15Eez+XpWBl")
        yield("m5fDBJhEGhPc89Y31GpTzW0vCLmhJ44XwvYPntWxYISUrqeR3zoUQrCEp1C6mXNX")
        yield("EpqIGIVbJ6JVa/YI+pwbfuP51o0ZtF2rzvgfPzKtkpYQ7m7KgA8g8ktRXyNrz8bo")
        yield("iwg7RRPeqs4uL/RK8d2KLpgLqcAB9WDpcEQzPWegbDrFO1F3z4UVNH6hrMfOLGVA")
        yield("xoiQhNFhZj6RumBXlPS0rmCOCkUkWrDr3l6Z3spUVgoeea+QdX682j6t7JnakaOw")
        yield("jzwY777SrZoi9mFFpLVhfb4haq4IWyKSHR3/0BlWXgcgI6w6LXm+V+ZgLVDON52F")
        yield("LcxnfftaBJz2yclEwBohq38rYEpb+28+JBvHJYqcZRaldHYLjjmb8XXvf2MyFeXr")
        yield("SopYkdzCvzmiEJAewrEbPUaTllogUQmnv7Rv9sZ9jfdJ/cEn8e7GSGjHIbnjV2ZM")
        yield("Q9vTpWjvsT/cqatbxzdBo/iEg5i9yohOC9aBfpIHPXFw+fEj7VLvktxZY6qThYXR")
        yield("Rus1WErPgxDzVpNp+4gXovAYOxsZak5oTV74ynv1aQ93HSndGkKUE/qA/JECAwEA")
        yield("AaOBhzCBhDAdBgNVHQ4EFgQUo562SGdCEjZBvW3gubSgUouX8bMwSAYDVR0jBEEw")
        yield("P4AUo562SGdCEjZBvW3gubSgUouX8bOhHKQaMBgxFjAUBgNVBAMMDUpldFByb2Zp")
        yield("bGUgQ0GCCQDSbLGDsoN54TAMBgNVHRMEBTADAQH/MAsGA1UdDwQEAwIBBjANBgkq")
        yield("hkiG9w0BAQsFAAOCAgEAjrPAZ4xC7sNiSSqh69s3KJD3Ti4etaxcrSnD7r9rJYpK")
        yield("BMviCKZRKFbLv+iaF5JK5QWuWdlgA37ol7mLeoF7aIA9b60Ag2OpgRICRG79QY7o")
        yield("uLviF/yRMqm6yno7NYkGLd61e5Huu+BfT459MWG9RVkG/DY0sGfkyTHJS5xrjBV6")
        yield("hjLG0lf3orwqOlqSNRmhvn9sMzwAP3ILLM5VJC5jNF1zAk0jrqKz64vuA8PLJZlL")
        yield("S9TZJIYwdesCGfnN2AETvzf3qxLcGTF038zKOHUMnjZuFW1ba/12fDK5GJ4i5y+n")
        yield("fDWVZVUDYOPUixEZ1cwzmf9Tx3hR8tRjMWQmHixcNC8XEkVfztID5XeHtDeQ+uPk")
        yield("X+jTDXbRb+77BP6n41briXhm57AwUI3TqqJFvoiFyx5JvVWG3ZqlVaeU/U9e0gxn")
        yield("8qyR+ZA3BGbtUSDDs8LDnE67URzK+L+q0F2BC758lSPNB2qsJeQ63bYyzf0du3wB")
        yield("/gb2+xJijAvscU3KgNpkxfGklvJD/oDUIqZQAnNcHe7QEf8iG2WqaMJIyXZlW3me")
        yield("0rn+cgvxHPt6N4EBh5GgNZR4l0eaFEV+fxVsydOQYo1RIyFMXtafFBqQl6DDxujl")
        yield("FeU3FZ+Bcp12t7dlM4E0/sS1XdL47CfGVj4Bp+/VbF862HmkAbd7shs7sDQkHbU=")
        yield("-----END CERTIFICATE-----")
      })
      yield(sequence {
        yield("-----BEGIN CERTIFICATE-----")
        yield("MIIFTDCCAzSgAwIBAgIJAMCrW9HV+hjZMA0GCSqGSIb3DQEBCwUAMB0xGzAZBgNV")
        yield("BAMMEkxpY2Vuc2UgU2VydmVycyBDQTAgFw0xNjEwMTIxNDMwNTRaGA8yMTE2MTIy")
        yield("NzE0MzA1NFowHTEbMBkGA1UEAwwSTGljZW5zZSBTZXJ2ZXJzIENBMIICIjANBgkq")
        yield("hkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAoT7LvHj3JKK2pgc5f02z+xEiJDcvlBi6")
        yield("fIwrg/504UaMx3xWXAE5CEPelFty+QPRJnTNnSxqKQQmg2s/5tMJpL9lzGwXaV7a")
        yield("rrcsEDbzV4el5mIXUnk77Bm/QVv48s63iQqUjVmvjQt9SWG2J7+h6X3ICRvF1sQB")
        yield("yeat/cO7tkpz1aXXbvbAws7/3dXLTgAZTAmBXWNEZHVUTcwSg2IziYxL8HRFOH0+")
        yield("GMBhHqa0ySmF1UTnTV4atIXrvjpABsoUvGxw+qOO2qnwe6ENEFWFz1a7pryVOHXg")
        yield("P+4JyPkI1hdAhAqT2kOKbTHvlXDMUaxAPlriOVw+vaIjIVlNHpBGhqTj1aqfJpLj")
        yield("qfDFcuqQSI4O1W5tVPRNFrjr74nDwLDZnOF+oSy4E1/WhL85FfP3IeQAIHdswNMJ")
        yield("y+RdkPZCfXzSUhBKRtiM+yjpIn5RBY+8z+9yeGocoxPf7l0or3YF4GUpud202zgy")
        yield("Y3sJqEsZksB750M0hx+vMMC9GD5nkzm9BykJS25hZOSsRNhX9InPWYYIi6mFm8QA")
        yield("2Dnv8wxAwt2tDNgqa0v/N8OxHglPcK/VO9kXrUBtwCIfZigO//N3hqzfRNbTv/ZO")
        yield("k9lArqGtcu1hSa78U4fuu7lIHi+u5rgXbB6HMVT3g5GQ1L9xxT1xad76k2EGEi3F")
        yield("9B+tSrvru70CAwEAAaOBjDCBiTAdBgNVHQ4EFgQUpsRiEz+uvh6TsQqurtwXMd4J")
        yield("8VEwTQYDVR0jBEYwRIAUpsRiEz+uvh6TsQqurtwXMd4J8VGhIaQfMB0xGzAZBgNV")
        yield("BAMMEkxpY2Vuc2UgU2VydmVycyBDQYIJAMCrW9HV+hjZMAwGA1UdEwQFMAMBAf8w")
        yield("CwYDVR0PBAQDAgEGMA0GCSqGSIb3DQEBCwUAA4ICAQCJ9+GQWvBS3zsgPB+1PCVc")
        yield("oG6FY87N6nb3ZgNTHrUMNYdo7FDeol2DSB4wh/6rsP9Z4FqVlpGkckB+QHCvqU+d")
        yield("rYPe6QWHIb1kE8ftTnwapj/ZaBtF80NWUfYBER/9c6To5moW63O7q6cmKgaGk6zv")
        yield("St2IhwNdTX0Q5cib9ytE4XROeVwPUn6RdU/+AVqSOspSMc1WQxkPVGRF7HPCoGhd")
        yield("vqebbYhpahiMWfClEuv1I37gJaRtsoNpx3f/jleoC/vDvXjAznfO497YTf/GgSM2")
        yield("LCnVtpPQQ2vQbOfTjaBYO2MpibQlYpbkbjkd5ZcO5U5PGrQpPFrWcylz7eUC3c05")
        yield("UVeygGIthsA/0hMCioYz4UjWTgi9NQLbhVkfmVQ5lCVxTotyBzoubh3FBz+wq2Qt")
        yield("iElsBrCMR7UwmIu79UYzmLGt3/gBdHxaImrT9SQ8uqzP5eit54LlGbvGekVdAL5l")
        yield("DFwPcSB1IKauXZvi1DwFGPeemcSAndy+Uoqw5XGRqE6jBxS7XVI7/4BSMDDRBz1u")
        yield("a+JMGZXS8yyYT+7HdsybfsZLvkVmc9zVSDI7/MjVPdk6h0sLn+vuPC1bIi5edoNy")
        yield("PdiG2uPH5eDO6INcisyPpLS4yFKliaO4Jjap7yzLU9pbItoWgCAYa2NpxuxHJ0tB")
        yield("7tlDFnvaRnQukqSG+VqNWg==")
        yield("-----END CERTIFICATE-----")
      })
    }

  // Qodana license are passed as ENV variable or offline activation code.
  @PublishedApi
  internal inline fun getLicenseType(): QodanaLicense {
    val cstamp = LicensingFacade.getInstance()?.getConfirmationStamp(PRODUCT_CODE) ?: throw QodanaException("Incorrect license")
    return when {
      // the license is obtained via JetBrainsAccount or entered as an activation code
      cstamp.startsWith(KEY_PREFIX) -> getLicenseType(cstamp.substring(KEY_PREFIX.length))
      // Qodana doesn't support floating license server
      // cstamp.startsWith(STAMP_PREFIX) -> isLicenseServerStampValid(cstamp.substring(STAMP_PREFIX.length))
      cstamp.startsWith(EVAL_PREFIX) -> getEvaluationLicense(cstamp.substring(EVAL_PREFIX.length))
      else -> throw QodanaException("Incorrect license format")
    }
  }

  @PublishedApi
  internal inline fun getEvaluationLicense(expirationTime: String): QodanaLicense {
    val now = Date()
    val expiration = Date(expirationTime.toLong())
    now.before(expiration)
    return QodanaLicense(ULTIMATE_PLUS, true, expiration)
  }

  @PublishedApi
  internal inline fun getLicenseType(key: String): QodanaLicense {
    val licenseParts = key.split("-").toTypedArray()
    if (licenseParts.size != 4) {
      throw QodanaException("Incorrect license format")
    }

    val (_, licensePartBase64, signatureBase64, certBase64) = licenseParts
    val sig = Signature.getInstance("SHA" + 1 + "with" + "rsa".uppercase(Locale.getDefault()))
    // the last parameter of 'createCertificate()' set to 'false' switches off certificate expiration checks.
    // This might be the case if the key is at the same time a perpetual fallback license for older IDE versions.
    // Here it is only important that the key was signed with an authentic JetBrains certificate.
    sig.initVerify(createCertificate(
      Base64.getMimeDecoder().decode(certBase64.toByteArray()), emptySet(), false
    ))
    val licenseBytes = Base64.getMimeDecoder().decode(licensePartBase64.toByteArray())
    sig.update(licenseBytes)
    if (!sig.verify(Base64.getMimeDecoder().decode(signatureBase64.toByteArray()))) {
      throw QodanaException("Incorrect license format")
    }

    val licenseData = String(licenseBytes, StandardCharsets.UTF_8)
    val logger = logger<QodanaLicenseChecker>()
    logger.debug("License data:\n$licenseData")
    return parseLicense(licenseData)
  }

  @PublishedApi
  internal inline fun parseLicense(licenseData: String): QodanaLicense {
    try {
      val root: JsonNode = ObjectMapper().readTree(licenseData)
      val productRoot = root.path("products").elements().asSequence().firstOrNull { it.get("code")?.asText() == "QDL" }
                        ?: throw QodanaException("Incorrect license format")
      val licenseType = parseLicensePlan(productRoot.path("properties").path("plan").asText(null))
      val trial = root.path("trial").asText(null) == "true"

      val expirationDate = parseExpirationDateFormat(productRoot.path("paidUpTo").asText(null))

      return QodanaLicense(licenseType, trial, expirationDate)
    }
    catch (e: JsonParseException) {
      throw QodanaException("Incorrect license format", e)
    }
  }

  @PublishedApi
  internal inline fun parseLicensePlan(planText: String?): QodanaLicenseType {
    return when(planText) {
      null -> throw QodanaException("Incorrect license format. Qodana license should have a property 'plan'")
      "QDUE" -> ULTIMATE_PLUS
      "QDUP" -> ULTIMATE_PLUS
      "QDP" -> PREMIUM
      "QDU" -> ULTIMATE
      "QDC" -> COMMUNITY
      else -> throw QodanaException("Unknown license type '$planText'")
    }
  }

  @PublishedApi
  internal inline fun parseExpirationDateFormat(text: String?): Date? {
    text ?: return null
    try {
      return SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(text)
    }
    catch (e: ParseException) {
      logger<QodanaLicenseChecker>().error(e)
    }
    return null
  }

  @PublishedApi
  internal inline fun createCertificate(certBytes: ByteArray,
                                        intermediateCertsBytes: Collection<ByteArray>,
                                        checkValidityAtCurrentDate: Boolean): X509Certificate {
    runCatching {
      val x509factory = CertificateFactory.getInstance("X" + "." + 509)
      val cert = x509factory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
      val allCerts: MutableCollection<Certificate?> = HashSet()
      allCerts.add(cert)
      for (bytes in intermediateCertsBytes) {
        allCerts.add(x509factory.generateCertificate(ByteArrayInputStream(bytes)))
      }

      // Create the selector that specifies the starting certificate
      val selector = X509CertSelector()
      selector.certificate = cert
      // Configure the PKIX certificate builder algorithm parameters
      val trustAchors: List<TrustAnchor> = ROOT_CERTIFICATES
        .map { it.joinToString("\n") }
        .map { it.trim() }
        .map { TrustAnchor(x509factory.generateCertificate(ByteArrayInputStream(it.toByteArray())) as X509Certificate, null) }
        .toList()

      val pkixParams = PKIXBuilderParameters(trustAchors.toMutableSet(), selector)
      pkixParams.isRevocationEnabled = false
      if (!checkValidityAtCurrentDate) {
        // deliberately check validity on the start date of cert validity period, so that we do not depend on
        // the actual moment when the check is performed
        pkixParams.date = cert.notBefore
      }

      pkixParams.addCertStore(CertStore.getInstance("Collection", CollectionCertStoreParameters(allCerts)))
      // Build and verify the certification chain
      val path = CertPathBuilder.getInstance("PKIX").build(pkixParams).certPath
      if (path != null) {
        CertPathValidator.getInstance("PKIX").validate(path, pkixParams)
        return cert
      }
    }
    throw Exception("Certificate used to sign the license is not signed by JetBrains root certificate")
  }
}
