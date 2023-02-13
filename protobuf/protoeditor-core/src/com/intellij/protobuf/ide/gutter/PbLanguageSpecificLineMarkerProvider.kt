package com.intellij.protobuf.ide.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.Language
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.protobuf.ide.PbIdeBundle
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType
import icons.ProtoeditorCoreIcons

abstract class PbLanguageSpecificLineMarkerProvider : RelatedItemLineMarkerProvider() {

  override fun getName(): String? {
    return PbIdeBundle.message("line.marker.navigate.to.declaration")
  }

  protected abstract fun isAcceptableElement(element: PsiElement): Boolean

  override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    if (element !is PsiNameIdentifierOwner) return
    val anchor = element.identifyingElement ?: return
    if (!isAcceptableElement(element) || !hasProtoDefinition(element)) return
    result.add(createImplementedElementMarker(anchor, element))
  }

  private fun createImplementedElementMarker(identifier: PsiElement,
                                             identifierOwner: PsiNameIdentifierOwner): RelatedItemLineMarkerInfo<PsiElement> {
    return NavigationGutterIconBuilder.create(ProtoeditorCoreIcons.GoToDeclaration)
      .setAlignment(GutterIconRenderer.Alignment.LEFT)
      .setTooltipText(PbIdeBundle.message("line.marker.navigate.to.declaration"))
      .setEmptyPopupText(PbIdeBundle.message("line.marker.no.declarations.found"))
      .setTargets(NotNullLazyValue.lazy { findProtoDefinitions(identifierOwner).toList() })
      .createLineMarkerInfo(identifier)
  }

  private fun hasProtoDefinition(psiElement: PsiElement): Boolean {
    return findProtoDefinitions(psiElement).any()
  }

  private fun findProtoDefinitions(psiElement: PsiElement): Sequence<PbElement> {
    val identifierOwner = psiElement.parentOfType<PsiNameIdentifierOwner>(true) ?: return emptySequence()
    val converters = collectRpcConvertersForLanguage(psiElement.language)
                       .takeIf(Collection<PbGeneratedCodeConverter>::isNotEmpty)
                     ?: return emptySequence()
    return IMPLEMENTATION_SEARCHER_EP_NAME.extensionList.asSequence()
      .flatMap { it.findDeclarationsForCodeElement(identifierOwner, converters) }
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
}