package com.jetbrains.cidr.cpp.diagnostics.model

/**
 * A serializable section that can reproduce its historical text representation.
 */
interface Reportable {
  fun toText(): String
}
