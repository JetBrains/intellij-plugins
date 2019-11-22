package com.intellij.aws.cloudformation

import com.google.common.primitives.Bytes
import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutor
import org.jetbrains.yaml.YAMLLanguage

class CloudFormationLanguageSubstitutor: LanguageSubstitutor() {
  override fun getLanguage(file: VirtualFile, project: Project): Language? {
    val bytes = FileUtil.loadFirstAndClose(file.inputStream, 10024)
    if (Bytes.indexOf(bytes, bytes1) >= 0 || Bytes.indexOf(bytes, bytes2) >= 0) {
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
