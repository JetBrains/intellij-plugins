package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.JdkUtil
import com.intellij.openapi.roots.ui.configuration.SdkLookup
import java.nio.file.InvalidPathException
import java.nio.file.Path

internal fun buildJavaSdkLookup(jdkName: String) = SdkLookup
  .newLookupBuilder()
  .withSdkName(jdkName)
  .withSdkType(JavaSdk.getInstance())
  .withSdkHomeFilter { homePath ->
    try {
      val path = Path.of(homePath)
      JdkUtil.checkForJdk(path) && JdkUtil.checkForJre(path)
    }
    catch (_: InvalidPathException) { false }
  }