package com.intellij.protobuf.ide.gutter

import com.intellij.lang.Language
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely

fun findProtoDefinitions(psiElement: PsiElement): Sequence<PbElement> {
  val identifierOwner = psiElement.parentOfType<PsiNameIdentifierOwner>(true) ?: return emptySequence()
  val converters = collectRpcConvertersForLanguage(psiElement.language)
                     .takeIf(Collection<PbGeneratedCodeConverter>::isNotEmpty)
                   ?: return emptySequence()
  return IMPLEMENTATION_SEARCHER_EP_NAME.extensionList.asSequence()
    .flatMap { it.findDeclarationsForCodeElement(identifierOwner, converters) }
}

fun findImplementations(pbElement: PsiElement): Sequence<PsiElement> {
  val identifierOwner = pbElement.parentOfType<PsiNameIdentifierOwner>(true)
                          ?.asSafely<PbElement>()
                        ?: return emptySequence()
  val converters = collectRpcConverters()
  return IMPLEMENTATION_SEARCHER_EP_NAME.extensionList.asSequence()
    .flatMap { it.findImplementationsForProtoElement(identifierOwner, converters) }
}


private fun collectRpcConverters(): Collection<PbGeneratedCodeConverter> {
  return fetchConverters()
    .map(PbGeneratedCodeConverterProvider::getProtoConverter)
    .toList()
}

private fun collectRpcConvertersForLanguage(language: Language): Collection<PbGeneratedCodeConverter> {
  return fetchConverters()
    .filter { it.acceptsLanguage(language) }
    .map(PbGeneratedCodeConverterProvider::getProtoConverter)
    .toList()
}

private fun fetchConverters(): Sequence<PbGeneratedCodeConverterProvider> {
  return CONVERTER_EP_NAME.extensionList.asSequence()
}