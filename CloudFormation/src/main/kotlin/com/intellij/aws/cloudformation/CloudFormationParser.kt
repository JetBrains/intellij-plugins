package com.intellij.aws.cloudformation

import com.intellij.json.psi.JsonFile
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLFile

object CloudFormationParser {
  fun parse(psiFile: PsiFile): CloudFormationParsedFile {
    assert(CloudFormationPsiUtils.isCloudFormationFile(psiFile)) { psiFile.name + " is not a CloudFormation file" }

    return when (psiFile) {
      is JsonFile -> JsonCloudFormationParser.parse(psiFile)
      is YAMLFile -> YamlCloudFormationParser.parse(psiFile)
      else -> error("Unsupported PSI file type: " + psiFile.javaClass.name)
    }
  }
}