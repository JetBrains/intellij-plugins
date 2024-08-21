// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.toolingDaemon

import com.google.gson.JsonObject

class DartToolingDaemonResponse(var result: JsonObject?, var error: JsonObject?)

fun interface DartToolingDaemonRequestHandler {
  fun handleRequest(request: JsonObject): DartToolingDaemonResponse
}
