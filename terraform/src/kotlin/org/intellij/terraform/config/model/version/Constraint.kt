// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.version

import java.util.*
import java.util.function.BiFunction
import java.util.regex.Pattern

class VersionConstraint private constructor(val constraints: List<Constraint>) {
  data class Constraint(val operation: ConstraintFunction, val check: Version, val original: String) {
    fun check(version: Version): Boolean {
      return operation.apply(version, this.check)
    }

    override fun toString(): String {
      return original
    }
  }

  companion object {
    val AnyVersion by lazy { return@lazy parse(">=0.0.0") }

    @Throws(MalformedConstraintException::class)
    fun parse(source: String): VersionConstraint {
      return VersionConstraint(source.split(',').map {
        parseSingle(it)
      })
    }

    fun intersect(a: VersionConstraint, b: VersionConstraint): VersionConstraint {
      if (a == AnyVersion) return b
      if (b == AnyVersion) return a

      return VersionConstraint(a.constraints + b.constraints)
    }

    private val ops = mapOf(
        "" to ConstraintFunction.Equal,
        "=" to ConstraintFunction.Equal,
        "!=" to ConstraintFunction.NotEqual,
        ">" to ConstraintFunction.GreaterThan,
        "<" to ConstraintFunction.LessThan,
        ">=" to ConstraintFunction.GreaterThanOrEqual,
        "<=" to ConstraintFunction.LessThanOrEqual,
        "~>" to ConstraintFunction.Pessimistic
    )

    private val constraintRegexp = String.format("^\\s*(%s)\\s*(%s)\\s*\$", ops.keys.joinToString("|") { Pattern.quote(it) }, VersionRegexpRaw).toRegex()


    private fun parseSingle(s: String): Constraint {
      val match = constraintRegexp.matchEntire(s)?.groupValues ?: throw MalformedConstraintException("Malformed constraint: $s")
      val version = try {
        Version.parse(match[2])
      } catch (e: MalformedVersionException) {
        throw MalformedConstraintException(e.message!!)
      }
      val operation = ops[match[1]] ?: throw MalformedConstraintException("Unsupported operation: ${match[1]}")
      return Constraint(operation, version, s)
    }
  }

  fun check(version: Version): Boolean {
    return constraints.all { it.check(version) }
  }

  // Intersects all constrains an checks whether it's empty
  fun isEmpty(): Boolean {
    val map = constraints.groupBy(Constraint::operation, Constraint::check)
    val equal: Set<Version> = map[ConstraintFunction.Equal]?.toSortedSet() ?: emptySet()
    val notEqual: Set<Version> = map[ConstraintFunction.NotEqual]?.toSortedSet() ?: emptySet()

    val greaterThan = map[ConstraintFunction.GreaterThan]?.toSortedSet()?.maxOrNull() ?: Version.ZERO
    val greaterThanOrEqual = map[ConstraintFunction.GreaterThanOrEqual]?.toSortedSet()?.maxOrNull() ?: Version.ZERO

    val lessThan = map[ConstraintFunction.LessThan]?.toSortedSet()?.minOrNull() ?: Version.MAX
    val lessThanOrEqual = map[ConstraintFunction.LessThanOrEqual]?.toSortedSet()?.minOrNull() ?: Version.MAX

    val pessimistic = map[ConstraintFunction.Pessimistic]?.toSortedSet()?.maxOrNull() ?: Version.ZERO

    if (equal.size > 1) {
      return true
    }

    var unique: Version? = equal.firstOrNull()

    if (lessThan <= greaterThan) return true
    // OK: LT > GT
    if (lessThan <= greaterThanOrEqual) return true
    // OK: LT > GTE
    if (lessThan <= pessimistic) return true
    // OK: LT > P

    if (lessThanOrEqual <= greaterThan) return true
    // OK: LTE > GT
    if (lessThanOrEqual < greaterThanOrEqual) return true
    // OK: LTE >= GTE
    if (lessThanOrEqual < pessimistic) return true
    // OK: LTE >= P

    if (lessThanOrEqual == greaterThanOrEqual || lessThanOrEqual == pessimistic) {
      if (unique != null && unique != lessThanOrEqual) return true
      unique = lessThanOrEqual
    }
    if (lessThan == Version.ZERO) return true

    if (pessimistic != Version.ZERO) {
      if (greaterThan >= pessimistic && !ConstraintFunction.Pessimistic.apply(greaterThan, pessimistic)) return true
      if (greaterThanOrEqual >= pessimistic && !ConstraintFunction.Pessimistic.apply(greaterThanOrEqual, pessimistic)) return true
    }

    if (unique != null) {
      if (notEqual.contains(unique)) return true
      if (unique >= lessThan) return true
      if (unique > lessThanOrEqual) return true
      if (unique < greaterThanOrEqual) return true
      if (greaterThan != Version.ZERO && unique <= greaterThan) return true
    }
    return false
  }

  override fun toString(): String {
    return constraints.joinToString(",")
  }
}

class MalformedConstraintException(message: String) : Exception(message)

@Suppress("KotlinConstantConditions", "ControlFlowWithEmptyBody")
fun prereleaseCheck(v: Version, c: Version): Boolean {
  val vPre = v.pre.isNotEmpty()
  val cPre = c.pre.isNotEmpty()
  if (cPre && vPre) {
    // A constraint with a pre-release can only match a pre-release version
    // with the same base segments.
    return Arrays.equals(c.segments, v.segments)
  }
  if (!cPre && vPre) {
    // A constraint without a pre-release can only match a version without a
    // pre-release.
    return false
  }
  if (cPre && !vPre) {
    // OK, except with the pessimistic operator
  }
  if (!cPre && !vPre) {
    // OK
  }
  return true
}

sealed class ConstraintFunction(val name: String, private val f: (Version, Version) -> Boolean) : BiFunction<Version, Version, Boolean> {
  override fun apply(t: Version, u: Version): Boolean = f(t, u)

  object Equal : ConstraintFunction("=", { v, c -> v == c })
  object NotEqual : ConstraintFunction("!=", { v, c -> v != c })
  object GreaterThan : ConstraintFunction(">", { v, c -> prereleaseCheck(v, c) && v > c })
  object LessThan : ConstraintFunction("<", { v, c -> prereleaseCheck(v, c) && v < c })
  object GreaterThanOrEqual : ConstraintFunction(">=", { v, c -> prereleaseCheck(v, c) && v >= c })
  object LessThanOrEqual : ConstraintFunction("<=", { v, c -> prereleaseCheck(v, c) && v <= c })

  object Pessimistic : ConstraintFunction("~>", fun(v: Version, c: Version): Boolean {
    if (!prereleaseCheck(v, c) || (c.pre.isNotEmpty() && v.pre.isEmpty())) {
      // Using a pessimistic constraint with a pre-release, restricts versions to pre-releases
      return false
    }

    // If the version being checked is naturally less than the constraint, then there
    // is no way for the version to be valid against the constraint
    if (v < c) {
      return false
    }

    // If the version being checked has less specificity than the constraint, then there
    // is no way for the version to be valid against the constraint
    if (c.segments.size > v.segments.size) {
      return false
    }

    // Check the segments in the constraint against those in the version. If the version
    // being checked, at any point, does not have the same values in each index of the
    // constraints segments, then it cannot be valid against the constraint.
    for (i in 0..(c.si - 2)) {
      if (v.segments[i] != c.segments[i]) {
        return false
      }
    }

    // Check the last part of the segment in the constraint. If the version segment at
    // this index is less than the constraints segment at this index, then it cannot
    // be valid against the constraint
    if (c.segments[c.si - 1] > v.segments[c.si - 1]) {
      return false
    }

    // If nothing has rejected the version by now, it's valid
    return true
  })
}


