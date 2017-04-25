package org.jetbrains.vuejs.run

import com.intellij.javascript.debugger.JavaScriptDebugAwareBase
import org.jetbrains.vuejs.VueFileType

internal class VueDebugAware : JavaScriptDebugAwareBase(VueFileType.INSTANCE)