package org.jetbrains.qodana.yaml

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.findFileOrDirectory
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLScalar

internal class QodanaYamlPathReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(
      PlatformPatterns.psiElement(YAMLScalar::class.java)
        .with(QODANA_YAML_PATTERN)
        .with(yamlKeysPattern(QODANA_INSPECTION_INCLUDE_PATHS, QODANA_INSPECTION_EXCLUDE_PATHS)),
      QodanaYamlPathReferenceProvider()
    )
  }

  class QodanaYamlPathReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(baseElement: PsiElement, context: ProcessingContext): Array<PsiReference> {
      if (baseElement !is YAMLScalar) return emptyArray()
      val project = baseElement.project
      val root = project.guessProjectDir() ?: return emptyArray()
      val fileOrDIr = root.findFileOrDirectory(baseElement.textValue) ?: return emptyArray()
      return arrayOf(PsiReferenceBase.Immediate(
        baseElement,
        true,
        if (fileOrDIr.isDirectory) PsiManager.getInstance(project).findDirectory(fileOrDIr)
        else PsiManager.getInstance(project).findFile(fileOrDIr)
      ))
    }
  }
}