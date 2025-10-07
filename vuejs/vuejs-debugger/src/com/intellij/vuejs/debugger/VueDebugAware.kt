// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vuejs.debugger

import com.intellij.javascript.debugger.JavaScriptDebugAwareBase
import org.jetbrains.vuejs.lang.html.VueFileType

internal class VueDebugAware : JavaScriptDebugAwareBase(VueFileType)