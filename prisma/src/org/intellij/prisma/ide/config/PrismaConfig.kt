// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.config

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.serialization.Serializable
import java.nio.file.Path

internal const val CONFIG_DIR_NAME = ".config"

internal val EXTENSIONS = setOf("js", "ts", "cjs", "mjs", "cts", "mts")

/**
 * Files in regular directories are considered root configs.
 */
internal val ROOT_CONFIG_NAMES = EXTENSIONS.map { "prisma.config.$it" }

/**
 * Files in `.config` directories are considered nested configs.
 */
internal val NESTED_CONFIG_NAMES = EXTENSIONS.map { "prisma.$it" }

@Serializable
data class PrismaConfigData(val schema: String? = null)

class PrismaConfig(val file: VirtualFile, val data: PrismaConfigData?) {
  @RequiresReadLock
  fun resolveSchemaPath(): VirtualFile? {
    val schemaPath = data?.schema?.takeIf { it.isNotBlank() } ?: return null

    val nioPath = Path.of(schemaPath)
    if (nioPath.isAbsolute) {
      return VirtualFileManager.getInstance().findFileByNioPath(nioPath)
    }

    LOG.debug { "Resolving schema path for file ${file}: ${schemaPath}" }
    val schemaRoot = file.findFileByRelativePath(schemaPath)
    return schemaRoot?.takeIf { it.isValid }.also {
      LOG.debug { "Resolved schema path for file ${file}: $it" }
    }
  }

  companion object {
    private val LOG = logger<PrismaConfig>()

    fun isPrismaConfig(file: VirtualFile?): Boolean {
      if (file == null) return false
      if (file.extension !in EXTENSIONS) return false
      return file.name in ROOT_CONFIG_NAMES || file.parent?.name == CONFIG_DIR_NAME && file.name in NESTED_CONFIG_NAMES
    }
  }
}

