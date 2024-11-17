package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.google.common.hash.HashFunction
import com.google.common.hash.Hasher
import com.jetbrains.qodana.sarif.model.Edge
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Node
import com.jetbrains.qodana.sarif.model.Region

internal class SafeHasher(private val hasher: Hasher) {
  fun put(value: Int?) {
    if (value != null) hasher.putInt(value)
  }

  fun put(value: String?) {
    if (value != null) hasher.putUnencodedChars(value)
  }
}

internal fun HashFunction.hash(f: (SafeHasher) -> Unit): String =
  newHasher()
    .apply { f(SafeHasher(this)) }
    .hash()
    .toString()

internal fun <T : Any> Collection<T?>?.forEachNotNull(f: (T) -> Unit) =
  this?.asSequence()?.filterNotNull()?.forEach { f(it) }

internal fun Location.hash(hasher: SafeHasher) {
  hasher.put(physicalLocation?.artifactLocation?.uri)
  hasher.put(physicalLocation?.artifactLocation?.uriBaseId)

  physicalLocation?.region?.hash(hasher)
  physicalLocation?.contextRegion?.hash(hasher)

  logicalLocations.forEachNotNull {
    hasher.put(it.name)
    hasher.put(it.fullyQualifiedName)
    hasher.put(it.kind)
  }

  relationships.forEachNotNull {
    it.kinds.forEachNotNull(hasher::put)
    hasher.put(it.target)
  }
}

internal fun Region.hash(hasher: SafeHasher) {
  hasher.put(sourceLanguage)
  hasher.put(startLine)
  hasher.put(startColumn)
  hasher.put(charLength)
  hasher.put(charOffset)
  hasher.put(snippet?.text)
}

internal fun Edge.hash(hasher: SafeHasher) {
  hasher.put(id)
  hasher.put(sourceNodeId)
  hasher.put(targetNodeId)
}

internal fun Node.hash(hasher: SafeHasher) {
  hasher.put(id)
  location?.hash(hasher)

  properties?.entries?.sortedBy { it.key }.forEachNotNull { (key, value) ->
    hasher.put(key)
    hasher.put(value?.hashCode())
  }
}
