// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.types

val PRISMA_BINARY_TARGETS = setOf(
  "native",

  "darwin",
  "windows",
  "linux-musl",

  "debian-openssl-1.0.x",
  "debian-openssl-1.1.x",
  "debian-openssl-3.0.x",

  "rhel-openssl-1.0.x",
  "rhel-openssl-1.1.x",
  "rhel-openssl-3.0.x",

  "linux-arm64-openssl-1.0.x",
  "linux-arm64-openssl-1.1.x",
  "linux-arm64-openssl-3.0.x"
)