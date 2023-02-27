// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.version

import org.intellij.lang.annotations.Language
import java.util.*
import kotlin.math.sign


// Based on github.com/hashicorp/go-version

@Language("RegExp")
internal val VersionRegexpRaw = "v?([0-9]+(\\.[0-9]+)*?)" +
    "(-([0-9]+[0-9A-Za-z\\-~]*(\\.[0-9A-Za-z\\-~]+)*)|(-?([A-Za-z\\-~]+[0-9A-Za-z\\-~]*(\\.[0-9A-Za-z\\-~]+)*)))?" +
    "(\\+([0-9A-Za-z\\-~]+(\\.[0-9A-Za-z\\-~]+)*))?"

internal val VersionRegexp = "^$VersionRegexpRaw\$".toRegex()

/*
type Version struct {
	metadata string
	pre      string
	segments []int64
	si       int
	original string
}
 */
data class Version(val metadata: String, val pre: String, val segments: LongArray, val si: Int, val original: String) : Comparable<Version> {
  init {
    assert(segments.size >= 3)
  }

  companion object {
    val MAX = parse("${Int.MAX_VALUE}.${Int.MAX_VALUE}.${Int.MAX_VALUE}")
    val ZERO = parse("0.0")

    fun parseOrNull(v: String): Version? {
      return try {
        parse(v)
      } catch (e: MalformedVersionException) {
        null
      }
    }

    @Throws(MalformedVersionException::class)
    fun parse(v: String): Version {
      val matches = VersionRegexp.matchEntire(v)?.groupValues ?: throw MalformedVersionException("Malformed version: $v")
      val segmentsStr = matches[1].split(".")
      val segments = segmentsStr.mapTo(ArrayList<Long>()) {
        it.toLongOrNull() ?: throw MalformedVersionException("Malformed version part: $it, full version: $v")
      }

      // Even though we could support more than three segments, if we
      // got less than three, pad it with 0s. This is to cover the basic
      // default use case of semver, which is MAJOR.MINOR.PATCH at the minimum
      while (segments.size < 3) {
        segments.add(0L)
      }

      var pre = matches[7]
      if (pre.isEmpty()) pre = matches[4]


      return Version(matches[10], pre, segments.toLongArray(), segmentsStr.size, v)
    }
  }

  override fun compareTo(other: Version): Int {
    if (other.toString() == this.toString()) return 0
    val segmentsSelf = segments
    val segmentsOther = other.segments

    // If the segments are the same, we must compare on prerelease info
    if (Arrays.equals(segmentsSelf, segmentsOther)) {
      val preSelf = pre
      val preOther = other.pre

      if (preSelf == preOther) return 0
      if (preSelf.isEmpty()) return 1
      if (preOther.isEmpty()) return -1
      return comparePrereleases(preSelf, preOther)
    }

    // Compare the segments
    // Because a constraint could have more/less specificity than the version it's
    // checking, we need to account for a lopsided or jagged comparison
    for (i in 0..Math.max(segmentsSelf.lastIndex, segmentsOther.lastIndex)) {
      if (i > segmentsSelf.lastIndex) {
        // This means Self had the lower specificity
        // Check to see if the remaining segments in Other are all zeros
        if (!isAllZero(segmentsOther, i)) {
          // if not, it means that Other has to be greater than Self
          return -1
        }
        break
      }
      if (i > segmentsOther.lastIndex) {
        // this means Other had the lower specificity
        // Check to see if the remaining segments in Self are all zeros -
        if (!isAllZero(segmentsSelf, i)) {
          //if not, it means that Self has to be greater than Other
          return 1
        }
        break
      }

      val lhs = segmentsSelf[i]
      val rhs = segmentsOther[i]
      if (lhs == rhs) continue
      else return lhs.compareTo(rhs).sign
    }

    return 0
  }

  private fun isAllZero(longs: LongArray, startIndex: Int): Boolean {
    for (i in startIndex..longs.lastIndex) {
      if (longs[i] != 0L) return false
    }
    return true
  }

  private fun comparePrereleases(self: String, other: String): Int {
    if (self == other) return 0

    // split both pre releases for analyse their parts
    val selfPreReleaseMeta = self.split('.')
    val otherPreReleaseMeta = other.split('.')


    for (i in 0..Math.max(selfPreReleaseMeta.lastIndex, otherPreReleaseMeta.lastIndex)) {
      val partSelfPre = selfPreReleaseMeta.getOrNull(i) ?: ""
      val partOtherPre = otherPreReleaseMeta.getOrNull(i) ?: ""

      val compare = comparePart(partSelfPre, partOtherPre).sign
      if (compare != 0) return compare
    }

    return 0
  }

  private fun comparePart(self: String, other: String): Int {
    if (self == other) return 0

    val selfInt = self.toLongOrNull()

    val otherInt = other.toLongOrNull()

    // if a part is empty, we use the other to decide
    if (self.isEmpty()) {
      if (otherInt != null) return -1
      return 1
    }
    if (other.isEmpty()) {
      if (selfInt != null) return 1
      return -1
    }

    if (selfInt != null) {
      if (otherInt != null) return selfInt.compareTo(otherInt)
      return -1
    } else {
      if (otherInt == null) return self.compareTo(other)
      return 1
    }
  }

  override fun toString(): String {
    val sb = StringBuilder()
    segments.joinTo(sb, ".")
    if (pre.isNotEmpty()) sb.append('-').append(pre)
    if (metadata.isNotEmpty()) sb.append('+').append(metadata)
    return sb.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Version) return false

    return this.compareTo(other) == 0
  }

  override fun hashCode(): Int {
    var result = metadata.hashCode()
    result = 31 * result + pre.hashCode()
    result = 31 * result + segments.sliceArray(0..2).contentHashCode()
    return result
  }
}

class MalformedVersionException(message: String) : Exception(message)
