package com.intellij.deno.lang

import com.intellij.deno.DenoUtil
import com.intellij.deno.model.parseDenoUrl
import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.IOUtil
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput

private val INDEX_NAME: ID<String, DenoPackageInfo> = ID.create("deno.cache.json.index")

private const val jsonMetadataExtension = ".metadata.json"

fun isJsonMetadataHashName(fileName: @NlsSafe String): Boolean =
  fileName.endsWith(jsonMetadataExtension) &&
  fileName.length == DenoUtil.HASH_FILE_NAME_LENGTH + jsonMetadataExtension.length

fun getDenoCacheElementsByKey(project: Project, key: String): List<DenoPackageInfo> {
  return FileBasedIndex.getInstance().getValues(INDEX_NAME, key, GlobalSearchScope.allScope(project))
}

class DenoCacheContentIndexExtension : FileBasedIndexExtension<String, DenoPackageInfo>() {

  override fun getName(): ID<String, DenoPackageInfo> = INDEX_NAME

  override fun getVersion(): Int = 1

  override fun getInputFilter(): InputFilter {
    return object : DefaultFileTypeSpecificInputFilter(JsonFileType.INSTANCE) {
      override fun acceptInput(file: VirtualFile): Boolean {
        val fileName = file.name
        return isJsonMetadataHashName(fileName) && file.path.contains("/deno/")
      }
    }
  }

  override fun dependsOnFileContent(): Boolean = true


  override fun getIndexer(): DataIndexer<String, DenoPackageInfo, FileContent> {
    return DataIndexer { inputData ->
      val file = inputData.psiFile as? JsonFile
      val topLevel = file?.getTopLevelValue() as? JsonObject
      val url = topLevel?.findProperty("url")?.value as? JsonStringLiteral
      val urlValue = url?.value

      if (urlValue != null) {
        parseDenoUrl(urlValue)?.let {
          val path = it.fullPath()
          return@DataIndexer mapOf(path to DenoPackageInfo(urlValue, inputData.fileName.removeSuffix(jsonMetadataExtension)))
        }
      }

      return@DataIndexer emptyMap<String, DenoPackageInfo>()
    }
  }

  override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor()

  override fun getValueExternalizer(): DataExternalizer<DenoPackageInfo> {
    return object : DataExternalizer<DenoPackageInfo> {
      override fun save(out: DataOutput, value: DenoPackageInfo) {
        IOUtil.writeUTF(out, value.url)
        IOUtil.writeUTF(out, value.hash)
      }

      override fun read(input: DataInput): DenoPackageInfo {
        val url = IOUtil.readUTF(input)
        val hash = IOUtil.readUTF(input)
        return DenoPackageInfo(url, hash)
      }
    }
  }
}

data class DenoPackageInfo(val url: String, val hash: String)


