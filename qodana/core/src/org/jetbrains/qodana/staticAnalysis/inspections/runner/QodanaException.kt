// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import kotlinx.coroutines.CancellationException

class QodanaException : RuntimeException {
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(message: String) : super(message)
}

class QodanaCancellationException(message: String) : CancellationException(message)