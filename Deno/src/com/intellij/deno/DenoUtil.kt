package com.intellij.deno

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SystemProperties
import java.io.File

object DenoUtil {
  
  fun getDenoTypings(): String {
    return JSLanguageServiceUtil.getPluginDirectory(this::class.java, "deno-service/node_modules/typescript-deno-plugin/lib").path
  }

  fun getDefaultDenoExecutable() = detectDenoPaths().firstOrNull()

  private fun detectDenoPaths(): List<String> {
    val list = (if (SystemInfo.isWindows) sequenceOf("deno.bat", "deno.cmd", "deno.exe") else sequenceOf("deno"))
      .mapNotNull(PathEnvironmentVariableUtil::findInPath)
      .map { it.absolutePath }
      .toList()
    if (list.isNotEmpty()) return list

    val userHome = FileUtil.toSystemIndependentName(SystemProperties.getUserHome())

    val exec = if (SystemInfoRt.isWindows) "deno.exe" else "deno"
    val path = "$userHome/.deno/bin/$exec"
    return if (File(path).exists()) listOf(path) else emptyList()
  }

  fun getDenoCache(): String { 
    return FileUtil.toSystemIndependentName(getDenoCacheInner())
  }
  
  private fun getDenoCacheInner(): String {
    val denoDir = System.getenv("DENO_DIR")
    if (denoDir != null) {
      return denoDir
    }
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