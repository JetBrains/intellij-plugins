package com.intellij.aws.cloudformation

import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutor
import com.intellij.util.ArrayUtil
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.yaml.YAMLLanguage
import java.io.FileNotFoundException

class CloudFormationLanguageSubstitutor: LanguageSubstitutor() {
  override fun getLanguage(file: VirtualFile, project: Project): Language? {
    if (file == FileBasedIndex.getInstance().fileBeingCurrentlyIndexed ||
        file.extension != "template") return null
    val bytes = try {
      VfsUtilCore.loadNBytes(file, 10 * 1024)
    } catch (_: FileNotFoundException) {
      return null
    } catch (t: Throwable) {
      Logger.getInstance(javaClass).warn("Unable to read first bytes of file ${file.path}", t)
      return null
    }

    if (ArrayUtil.indexOf(bytes, bytes1,0) >= 0 || ArrayUtil.indexOf(bytes, bytes2,0) >= 0) {
      val string = String(bytes, Charsets.UTF_8)
      if (CloudFormationFileTypeDetector.isJson(string)) {
        return JsonLanguage.INSTANCE
      }

      if (CloudFormationFileTypeDetector.isYaml(string)) {
        return YAMLLanguage.INSTANCE
      }
    }

    return null
  }

  private val bytes1 = CloudFormationSection.FormatVersion.id.toByteArray(Charsets.US_ASCII)
  private val bytes2 = CloudFormationConstants.awsServerless20161031TransformName.toByteArray(Charsets.US_ASCII)
}
