package org.jetbrains.qodana.yaml

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.progress.runBlockingCancellable
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLScalar

abstract class QodanaYamlCompletionContributorBase : CompletionContributor() {
  protected abstract suspend fun variantsForKey(key: String, file: YAMLFile, prefix: String = ""): List<QodanaLookupElement>

  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    val element = parameters.position.parent
    if (!isQodanaYaml(parameters.originalFile) || element !is YAMLScalar) return
    val key = fullYamlKey(element)
    runBlockingCancellable {
      variantsForKey(key, element.containingFile as YAMLFile, element.textValue)
        .forEach(result::addElement)
    }
  }
}

open class QodanaLookupElement(val name: String, val description: String) : LookupElement() {
  override fun getLookupString(): String = name
  override fun renderElement(presentation: LookupElementPresentation) {
    presentation.typeText = description
    super.renderElement(presentation)
  }
}