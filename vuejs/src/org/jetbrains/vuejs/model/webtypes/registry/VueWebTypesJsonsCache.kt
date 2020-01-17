// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.registry

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.util.io.HttpRequests
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.jetbrains.vuejs.model.webtypes.registry.VueWebTypesRegistry.Companion.WEB_TYPES_FILE_SUFFIX
import java.io.*
import java.net.URI

class VueWebTypesJsonsCache {

  companion object {

    fun getWebTypesJson(url: String): InputStream {
      if (url.endsWith("json") && url.startsWith(StandardFileSystems.FILE_PROTOCOL_PREFIX)) {
        return FileInputStream(File(URI(url)))
      }
      val downloadedJson = File(PathManager.getSystemPath(),
                                "web-types/" + File(url).nameWithoutExtension + WEB_TYPES_FILE_SUFFIX)
      if (!downloadedJson.exists()) {
        downloadedJson.parentFile.mkdirs()
        val content = downloadWebTypesJson(url)
        val mapper = ObjectMapper()
        val webTypesJson = mapper.readTree(content)
        mapper.writer().writeValue(downloadedJson, webTypesJson)
      }
      return FileInputStream(downloadedJson)
    }

    private fun downloadWebTypesJson(tarball: String): String? {
      val contents = HttpRequests.request(tarball).readBytes(null)
      val bi = BufferedInputStream(ByteArrayInputStream(contents))
      val gzi = GzipCompressorInputStream(bi)
      val input = TarArchiveInputStream(gzi)
      var e: ArchiveEntry? = input.nextEntry
      while (e != null) {
        if (e.name.endsWith(WEB_TYPES_FILE_SUFFIX)) {
          if (input.canReadEntryData(e)) {
            return FileUtil.loadTextAndClose(input)
          }
        }
        e = input.nextEntry
      }
      return null
    }

  }

}
