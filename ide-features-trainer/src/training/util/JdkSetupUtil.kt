/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.util

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.JdkBundle
import com.intellij.util.JdkBundleList
import com.intellij.util.lang.JavaVersion
import org.jetbrains.annotations.NonNls
import java.io.File

object JdkSetupUtil {
  @NonNls
  private val bundledJdkFile = bundledJDKFile

  private val bundledJDKFile: File
    get() {
      val bundledJDKPath = StringBuilder("jre")
      if (SystemInfo.isMac) {
        bundledJDKPath.append(File.separator).append("jdk")
      }
      return File(bundledJDKPath.toString())
    }

  private const val STANDARD_JDK_LOCATION_ON_MAC_OS_X = "/Library/Java/JavaVirtualMachines/"
  private val STANDARD_JVM_LOCATIONS_ON_LINUX = arrayOf(
          "/usr/lib/jvm/",  // Ubuntu
          "/usr/java/"     // Fedora
  )
  private const val STANDARD_JVM_X64_LOCATIONS_ON_WINDOWS = "Program Files/Java"
  private const val STANDARD_JVM_X86_LOCATIONS_ON_WINDOWS = "Program Files (x86)/Java"
  private val JDK8_VERSION = JavaVersion.compose(8)
  fun findJdkPaths(): JdkBundleList {
    val bootJdk = JdkBundle.createBoot()
    val jdkBundleList = JdkBundleList()
    jdkBundleList.addBundle(bootJdk)
    if (File(PathManager.getHomePath() + File.separator + bundledJdkFile).exists()) {
      val bundledJdk: JdkBundle? = JdkBundle.createBundle(bundledJdkFile)
      if (bundledJdk != null) {
        jdkBundleList.addBundle(bundledJdk)
      }
    }
    when {
      SystemInfo.isMac -> {
        jdkBundleList.addBundlesFromLocation(STANDARD_JDK_LOCATION_ON_MAC_OS_X, JDK8_VERSION, null)
      }
      SystemInfo.isLinux -> {
        for (location in STANDARD_JVM_LOCATIONS_ON_LINUX) {
          jdkBundleList.addBundlesFromLocation(location, JDK8_VERSION, null)
        }
      }
      SystemInfo.isWindows -> {
        for (root in File.listRoots()) {
          if (SystemInfo.is32Bit) {
            jdkBundleList.addBundlesFromLocation(File(root, STANDARD_JVM_X86_LOCATIONS_ON_WINDOWS).absolutePath, JDK8_VERSION, null)
          } else {
            jdkBundleList.addBundlesFromLocation(File(root, STANDARD_JVM_X64_LOCATIONS_ON_WINDOWS).absolutePath, JDK8_VERSION, null)
          }
        }
      }
    }
    return jdkBundleList
  }

  fun getJavaHomePath(jdkBundle: JdkBundle): String {
    val homeSubPath = if (SystemInfo.isMac) "/Contents/Home" else ""
    return jdkBundle.location.absolutePath + homeSubPath
  }
}