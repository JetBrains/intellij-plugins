package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.google.common.hash.HashFunction
import com.google.common.hash.Hasher
import com.jetbrains.qodana.sarif.model.CodeFlow
import com.jetbrains.qodana.sarif.model.Edge
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Node
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.ThreadFlow
import com.jetbrains.qodana.sarif.model.ThreadFlowLocation

private const val STEP_FQN_PROPERTY = "step_fqn"

class SafeHasher(private val hasher: Hasher) {
  fun put(value: Int?) {
    if (value != null) hasher.putInt(value)
  }

  fun put(value: String?) {
    if (value != null) hasher.putUnencodedChars(value)
  }
}

fun HashFunction.hash(f: (SafeHasher) -> Unit): String =
  newHasher()
    .apply { f(SafeHasher(this)) }
    .hash()
    .toString()

internal fun <T : Any> Collection<T?>?.forEachNotNull(f: (T) -> Unit) =
  this?.asSequence()?.filterNotNull()?.forEach { f(it) }

fun Location.hash(hasher: SafeHasher) {
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

internal fun CodeFlow.hash(hasher: SafeHasher, includeDescription: Boolean) {
  val threadFlows = threadFlows.orEmpty()
  if (threadFlows.isEmpty()) {
    if (includeDescription) {
      hasher.put(message?.text)
    }
    return
  }

  val useCodeFlowDescription = threadFlows.size == 1
  threadFlows.forEachNotNull { threadFlow ->
    threadFlow.hash(hasher)
    if (includeDescription) {
      hasher.put(threadFlow.message?.text ?: if (useCodeFlowDescription) message?.text else null)
    }
  }
}

internal fun ThreadFlow.hash(hasher: SafeHasher) {
  val orderedLocations = locations.orEmpty()
    .filterNotNull()
    .let { locations ->
      when {
        locations.all { it.executionOrder != null } -> locations.sortedBy { it.executionOrder }
        locations.all { it.index != null } -> locations.sortedBy { it.index }
        else -> locations
      }
    }

  val nodeIds = orderedLocations.mapIndexed { index, location -> location.nodeId(index) }
  orderedLocations.forEachIndexed { index, location ->
    hasher.put(nodeIds[index])
    location.location?.hash(hasher)
    location.properties.hashThreadFlowProperties(hasher)
  }

  nodeIds.zipWithNext().forEachIndexed { index, (sourceNodeId, targetNodeId) ->
    hasher.put((index + 1).toString())
    hasher.put(sourceNodeId)
    hasher.put(targetNodeId)
  }
}

private fun ThreadFlowLocation.nodeId(index: Int): String {
  return executionOrder?.toString()
         ?: this.index?.toString()
         ?: (index + 1).toString()
}

private fun PropertyBag?.hashThreadFlowProperties(hasher: SafeHasher) {
  this?.entries
    ?.filterNot { it.key == STEP_FQN_PROPERTY }
    ?.sortedBy { it.key }
    ?.forEachNotNull { (key, value) ->
      hasher.put(key)
      hasher.put(value?.hashCode())
    }
}
