package com.intellij.deno

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.util.SystemProperties
import java.io.File

object DenoUtil {
  private val urlKey = Key.create<String?>("deno.file.url")
  
  fun getDenoTypings(): String {
    return JSLanguageServiceUtil.getPluginDirectory(this::class.java,
                                                    "deno-service/node_modules/typescript-deno-plugin/lib/lib.deno.d.ts").path
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

  @NlsSafe
  fun getOwnUrlForFile(place: PsiElement, virtualFile: VirtualFile): String? {
    return getOwnUrlForFile(place.manager, virtualFile)
  }
  
  @NlsSafe
  fun getOwnUrlForFile(psiManager: PsiManager, virtualFile: VirtualFile): String? {
    val userData = virtualFile.getUserData(urlKey)
    if (userData != null) return userData

    val metadata = virtualFile.parent.findChild(virtualFile.name + ".metadata.json")
    if (metadata == null) return null
    val metaDataPsi = psiManager.findFile(metadata)
    if (metaDataPsi !is JsonFile) return null
    val values = metaDataPsi.allTopLevelValues
    for (topLevelValue in values) {
      val property = (topLevelValue as? JsonObject)?.findProperty("url") ?: continue
      val url = (property.value as? JsonStringLiteral)?.value
      if (url != null) {
        virtualFile.putUserData(urlKey, url)
        return url
      }
    }

    return null
  }

  fun isDenoCacheFile(file: VirtualFile): Boolean {
    val sequence = file.nameSequence
    val ext = FileUtilRt.getExtension(sequence)
    if (ext.isNotEmpty() || sequence.length != 64) return false
    val path = file.path
    return path.contains("/deps/")
  }
}