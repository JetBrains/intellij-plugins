// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package io.flutter.run.devtools

class DevToolsInstance(val host: String, val port: Int) {
  fun getUrl(): String {
    return "$host:$port"
  }
}