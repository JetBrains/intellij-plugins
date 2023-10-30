package com.intellij.dts.api

import com.intellij.dts.util.DtsUtil

/**
 * Represents a path to a node in the devicetree. If the path is absolut it
 * starts from a root node. If the path is no absolut it is considered relative
 * to a specific node.
 */
data class DtsPath(val absolut: Boolean, val segments: List<String>) {
    companion object {
        val root = DtsPath(true,  emptyList())

        fun from(path: String): DtsPath {
            return DtsPath(
                path.startsWith('/'),
                path.trim('/').split('/').filter { it.isNotEmpty() },
            )
        }
    }

    fun relativize(other: DtsPath): DtsPath? {
        if (other.segments.size < segments.size) return null

        for (i in segments.indices) {
            if (segments[i] != other.segments[i]) return null
        }

        return DtsPath(false, other.segments.slice(segments.size until other.segments.size))
    }

    fun parent(): DtsPath {
        return DtsPath(absolut, segments.dropLast(1))
    }

    fun name(): String? {
        return segments.lastOrNull()
    }

    fun nameWithoutUnit(): String? {
        val name = name() ?: return null
        return DtsUtil.splitName(name).first
    }

    override fun toString(): String {
        return segments.joinToString(separator = "/", prefix = if (absolut) "/" else "")
    }
}