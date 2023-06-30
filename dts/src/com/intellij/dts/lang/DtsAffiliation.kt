package com.intellij.dts.lang

/**
 * Represents the affiliation of a DTS entry:
 * - ROOT: Indicates that the entry is valid at the root of a dts file.
 * - NODE: Indicates that the entry is valid inside a node.
 * - UNKNOWN: Indicates that the affiliation of the entry cannot be decided yet.
 */
enum class DtsAffiliation {
    ROOT,
    NODE,
    UNKNOWN;

    fun isRoot(): Boolean = this == ROOT

    fun isNode(): Boolean = this == NODE

    fun isUnknown(): Boolean = this == UNKNOWN
}