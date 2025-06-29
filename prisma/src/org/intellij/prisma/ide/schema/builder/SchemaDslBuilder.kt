// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

@DslMarker
annotation class SchemaDslBuilderMarker

@SchemaDslBuilderMarker
interface SchemaDslBuilder<out T> {

  fun build(): T

  fun String.list(): String {
    return "$this[]"
  }

  fun String.optional(): String {
    return "$this?"
  }
}
