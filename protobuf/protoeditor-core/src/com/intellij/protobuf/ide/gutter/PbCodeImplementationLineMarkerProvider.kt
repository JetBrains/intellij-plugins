package com.intellij.protobuf.ide.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.Language
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.protobuf.ide.PbIdeBundle
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import icons.ProtoeditorCoreIcons

internal class PbCodeImplementationLineMarkerProvider : RelatedItemLineMarkerProvider() {
  override fun getId(): String {
    return "PbCodeImplementationLineMarkerProvider"
  }

  override fun getName(): String? {
    return PbIdeBundle.message("line.marker.overridden.element")
  }

  override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    if (element !is PsiNameIdentifierOwner) return
    val anchor = element.identifyingElement ?: return

    val marker =
      when {
        element is PbElement && hasImplementation(element) -> createOverriddenElementMarker(anchor, element)
        element !is PbElement && hasProtoDefinition(element) -> createImplementedElementMarker(anchor, element)
        else -> null
      } ?: return

    result.add(marker)
  }

  private fun createOverriddenElementMarker(identifier: PsiElement, identifierOwner: PbElement): RelatedItemLineMarkerInfo<PsiElement> {
    return NavigationGutterIconBuilder.create(ProtoeditorCoreIcons.GoToImplementation)
      .setAlignment(GutterIconRenderer.Alignment.LEFT)
      .setTooltipText(PbIdeBundle.message("line.marker.navigate.to.implementation"))
      .setEmptyPopupText(PbIdeBundle.message("line.marker.no.implementations.found"))
      .setTargets(NotNullLazyValue.lazy { findImplementations(identifierOwner).toList() })
      .createLineMarkerInfo(identifier)
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

  private fun hasImplementation(pbElement: PsiElement): Boolean {
    return findImplementations(pbElement).any()
  }

  private fun findImplementations(pbElement: PsiElement): Sequence<PsiElement> {
    val identifierOwner = pbElement.parentIdentifierOwner()?.asSafely<PbElement>() ?: return emptySequence()
    val converters = collectRpcConverters()
    return IMPLEMENTATION_SEARCHER_EP_NAME.extensionList.asSequence()
      .flatMap { it.findImplementationsForProtoElement(identifierOwner, converters) }
  }

  private fun hasProtoDefinition(psiElement: PsiElement): Boolean {
    return findProtoDefinitions(psiElement).any()
  }

  private fun findProtoDefinitions(psiElement: PsiElement): Sequence<PbElement> {
    val identifierOwner = psiElement.parentIdentifierOwner() ?: return emptySequence()
    val converters = collectRpcConvertersForLanguage(psiElement.language)
                       .takeIf(Collection<PbGeneratedCodeConverter>::isNotEmpty)
                     ?: return emptySequence()
    return IMPLEMENTATION_SEARCHER_EP_NAME.extensionList.asSequence()
      .flatMap { it.findDeclarationsForCodeElement(identifierOwner, converters) }
  }

  private fun PsiElement.parentIdentifierOwner(): PsiNameIdentifierOwner? {
    return this.parentOfType(true)
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
}

interface PbCodeImplementationSearcher {
  fun findImplementationsForProtoElement(pbElement: PbElement,
                                         converters: Collection<PbGeneratedCodeConverter>): Sequence<NavigatablePsiElement>

  fun findDeclarationsForCodeElement(psiElement: PsiElement, converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement>
}

private val IMPLEMENTATION_SEARCHER_EP_NAME =
  ExtensionPointName<PbCodeImplementationSearcher>("com.intellij.protobuf.codeImplementationSearcher")