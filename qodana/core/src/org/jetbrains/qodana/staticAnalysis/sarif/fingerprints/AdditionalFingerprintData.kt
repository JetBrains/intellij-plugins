package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result

internal data class InspectionFingerprintData(
  val entries: List<Entry>,
) {
  init {
    require(entries.isNotEmpty()) { "Inspection fingerprint data must not be empty" }
  }

  fun stableKey(): String {
    return buildString {
      append("inspection")
      entries.forEach { entry ->
        append('|')
        append(entry.name)
        append('=')
        append(entry.value)
      }
    }
  }

  data class Entry(
    val name: String,
    val value: String,
  )
}

internal data class XmlSemanticIdentity(
  val identity: String,
) {
  fun stableKey(): String = "xml|$identity"
}

internal data class AdditionalFingerprintDataHolder(
  val inspectionFingerprintData: InspectionFingerprintData? = null,
  val xmlSemanticIdentity: XmlSemanticIdentity? = null,
) {
  fun isEmpty(): Boolean = inspectionFingerprintData == null && xmlSemanticIdentity == null

  fun stableHashCode(): Int? {
    if (isEmpty()) return null
    return listOfNotNull(
      inspectionFingerprintData?.stableKey(),
      xmlSemanticIdentity?.stableKey(),
    ).hashCode()
  }

  fun merge(other: AdditionalFingerprintDataHolder): AdditionalFingerprintDataHolder {
    return AdditionalFingerprintDataHolder(
      inspectionFingerprintData = other.inspectionFingerprintData ?: inspectionFingerprintData,
      xmlSemanticIdentity = other.xmlSemanticIdentity ?: xmlSemanticIdentity,
    )
  }

  companion object {
    val EMPTY = AdditionalFingerprintDataHolder()

    fun fromInspectionFingerprintData(entries: Map<String, String>): AdditionalFingerprintDataHolder {
      val normalizedEntries = entries.entries
        .map { (name, value) -> InspectionFingerprintData.Entry(name, value) }
        .sortedBy(InspectionFingerprintData.Entry::name)

      val inspectionFingerprintData = normalizedEntries
        .takeIf { it.isNotEmpty() }
        ?.let(::InspectionFingerprintData)

      return AdditionalFingerprintDataHolder(inspectionFingerprintData = inspectionFingerprintData)
    }

    fun fromXmlSemanticIdentity(identity: String): AdditionalFingerprintDataHolder {
      return AdditionalFingerprintDataHolder(xmlSemanticIdentity = XmlSemanticIdentity(identity))
    }

    fun fromSerialized(value: Any?): AdditionalFingerprintDataHolder {
      return value as? AdditionalFingerprintDataHolder ?: EMPTY
    }
  }
}

internal fun PropertyBag.additionalFingerprintData(): AdditionalFingerprintDataHolder {
  return AdditionalFingerprintDataHolder.fromSerialized(get(BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA))
}

internal fun PropertyBag.mergeAdditionalFingerprintData(additionalFingerprintData: AdditionalFingerprintDataHolder) {
  if (additionalFingerprintData.isEmpty()) return

  put(BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA, additionalFingerprintData().merge(additionalFingerprintData))
}

internal fun Result.additionalFingerprintData(): AdditionalFingerprintDataHolder {
  return properties?.additionalFingerprintData() ?: AdditionalFingerprintDataHolder.EMPTY
}
