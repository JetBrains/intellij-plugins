package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result

internal sealed interface AdditionalFingerprintData {
  fun stableKey(): String
}

internal data class InspectionFingerprintData(
  val entries: List<Entry>,
) : AdditionalFingerprintData {
  init {
    require(entries.isNotEmpty()) { "Inspection fingerprint data must not be empty" }
  }

  override fun stableKey(): String {
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
) : AdditionalFingerprintData {
  override fun stableKey(): String = "xml|$identity"
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
      return when (value) {
        null -> EMPTY
        is AdditionalFingerprintDataHolder -> value
        else -> SerializedFingerprintObject.from(value)?.toAdditionalFingerprintDataHolder() ?: EMPTY
      }
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

private class SerializedFingerprintObject private constructor(private val values: Map<*, *>) {
  fun nestedObject(name: String): SerializedFingerprintObject? = from(values[name])

  fun string(name: String): String? = values[name] as? String

  fun entries(name: String): SerializedFingerprintEntries? = SerializedFingerprintEntries.from(values[name])

  fun toAdditionalFingerprintDataHolder(): AdditionalFingerprintDataHolder {
    return AdditionalFingerprintDataHolder(
      inspectionFingerprintData = nestedObject("inspectionFingerprintData")?.toInspectionFingerprintData(),
      xmlSemanticIdentity = nestedObject("xmlSemanticIdentity")?.toXmlSemanticIdentity(),
    )
  }

  fun toInspectionFingerprintData(): InspectionFingerprintData? {
    val inspectionEntries = entries("entries")
      ?.objects()
      ?.mapNotNull { it.toInspectionFingerprintEntry() }
      ?.sortedBy(InspectionFingerprintData.Entry::name)
      .orEmpty()

    return inspectionEntries
      .takeIf { it.isNotEmpty() }
      ?.let(::InspectionFingerprintData)
  }

  fun toInspectionFingerprintEntry(): InspectionFingerprintData.Entry? {
    val name = string("name") ?: return null
    val value = string("value") ?: return null
    return InspectionFingerprintData.Entry(name, value)
  }

  fun toXmlSemanticIdentity(): XmlSemanticIdentity? {
    val identity = string("identity") ?: return null
    return XmlSemanticIdentity(identity)
  }

  companion object {
    fun from(value: Any?): SerializedFingerprintObject? {
      return (value as? Map<*, *>)?.let(::SerializedFingerprintObject)
    }
  }
}

private class SerializedFingerprintEntries private constructor(private val values: List<*>) {
  fun objects(): List<SerializedFingerprintObject> = values.mapNotNull(SerializedFingerprintObject::from)

  companion object {
    fun from(value: Any?): SerializedFingerprintEntries? {
      return (value as? List<*>)?.let(::SerializedFingerprintEntries)
    }
  }
}
