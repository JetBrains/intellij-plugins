package com.intellij.deno

import com.intellij.deno.icons.DenoIcons
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.IconUtil
import com.intellij.util.SystemProperties
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.Icon


object DenoUtil {
  const val HASH_FILE_NAME_LENGTH = 64

  fun getDenoTypings(): URL {
    val file = JSLanguageServiceUtil.getPluginDirectory(this::class.java,
                                                        "deno-service/node_modules/typescript-deno-plugin/lib/lib.deno.d.ts")
    if (file != null && file.isFile) {
      return file.toURI().toURL()
    }
    return this::class.java.classLoader.getResource("deno-service/node_modules/typescript-deno-plugin/lib/lib.deno.d.ts")
           ?: error("Cannot locate bundled 'lib.deno.d.ts'")
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
    val candidate = "$userHome/.deno/bin/$exec"
    val path = Path.of((candidate))
    return if (Files.exists(path)) listOf (candidate) else emptyList()
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

  fun isDenoCacheFile(file: VirtualFile): Boolean {
    val sequence = file.nameSequence
    val ext = FileUtilRt.getExtension(sequence)
    if (ext.isNotEmpty() || sequence.length != HASH_FILE_NAME_LENGTH) return false
    val path = file.path
    return path.contains("/deps/")
  }

  fun getDefaultDenoIcon(): Icon {
    return IconUtil.resizeSquared(DenoIcons.Deno, 16)
  }
}