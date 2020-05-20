package com.intellij.deno

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SystemProperties
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.addIfNotNull
import java.io.File

object DenoUtil {

  fun getDenoPackagesPath(): String {
    return FileUtil.toSystemIndependentName(getDenoDirPath()) + "/deps"
  }

  fun getDenoTypings(): String {
    return JSLanguageServiceUtil.getPluginDirectory(this::class.java, "deno-service/node_modules/typescript-deno-plugin/lib").path
  }

  fun getDenoExecutablePath() = detectDenoPaths().firstOrNull()

  private fun detectDenoPaths(): List<String> =
    (if (SystemInfo.isWindows) sequenceOf("deno.bat", "deno.cmd", "deno.exe") else sequenceOf("deno"))
      .mapNotNull(PathEnvironmentVariableUtil::findInPath)
      .map { it.absolutePath }
      .toList()

  private fun getDenoDirPath(): String {
    val userHome = SystemProperties.getUserHome()
    if (SystemInfoRt.isMac) {
      return "$userHome/Library/Caches/deno"
    }

    if (SystemInfoRt.isWindows) {
      var localAppData = System.getenv("LOCALAPPDATA")
      if (localAppData.isNullOrEmpty()) localAppData = "$userHome\\AppData"

      return "$localAppData\\deno"
    }
    if (SystemInfoRt.isUnix) {
      var dir = System.getenv("XDG_CACHE_HOME")
      if (dir == null || dir.isEmpty()) dir = "$userHome/.cache"
      return "$dir/deno"
    }
    return "$userHome/.deno"
  }
}