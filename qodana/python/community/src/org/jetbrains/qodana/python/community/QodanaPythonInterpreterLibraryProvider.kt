// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.qodana.python.community

import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.OrderEntry
import com.intellij.python.sdk.backend.isInterpreterLibraryCarrier
import com.intellij.python.sdk.backend.getPythonInfo
import com.jetbrains.python.PyNames
import com.jetbrains.python.configuration.findModuleInterpreterIfReady
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaSdkLibraryProvider

/**
 * Reports the interpreter library of a Python module as its SDK. The interpreter library flow names the interpreter of a
 * module by a library entry, not by a Python SDK entry, and the version of the interpreter is the version of its SDK.
 */
internal class QodanaPythonInterpreterLibraryProvider : QodanaSdkLibraryProvider {
  override fun sdkOf(entry: OrderEntry): QodanaSdkLibraryProvider.SdkLibrary? {
    if (entry !is LibraryOrderEntry || !entry.isInterpreterLibraryCarrier()) return null
    val version = findModuleInterpreterIfReady(entry.ownerModule)?.getPythonInfo()?.successOrNull?.version
    return QodanaSdkLibraryProvider.SdkLibrary(type = PyNames.PYTHON_SDK_ID_NAME, version = version)
  }
}
