package com.intellij.protobuf.ide.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.NotNullFactory
import com.intellij.protobuf.ide.PbIdeBundle
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import kotlin.streams.asSequence

internal class PbCodeImplementationLineMarkerProvider : RelatedItemLineMarkerProvider() {
  override fun getId(): String {
    return "PbCodeImplementationLineMarkerProvider"
  }

  override fun getName(): String? {
    return PbIdeBundle.message("line.marker.overridden.element")
  }

  override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    val marker =
      when (element) {
        is PbElement ->
          if (hasImplementations(element)) createOverriddenElementMarker(element) else null
        else ->
          if (hasProtoDefinition(element)) createImplementedElementMarker(element) else null
      } ?: return

    result.add(marker)
  }

  private fun createOverriddenElementMarker(protoElement: PbElement): RelatedItemLineMarkerInfo<PsiElement> {
    return object : RelatedItemLineMarkerInfo<PsiElement>(
      protoElement,
      protoElement.textRange,
      AllIcons.General.OverridenMethod,
      { null },
      null,
      GutterIconRenderer.Alignment.LEFT,
      NotNullFactory {
        mutableListOf()
      }
    ) {}
  }

  private fun createImplementedElementMarker(psiElement: PsiElement): RelatedItemLineMarkerInfo<PsiElement> {
    return object : RelatedItemLineMarkerInfo<PsiElement>(
      psiElement,
      psiElement.textRange,
      AllIcons.General.ImplementingMethod,
      { null },
      null,
      GutterIconRenderer.Alignment.LEFT,
      NotNullFactory {
        mutableListOf()
      }
    ) {}
  }

  private fun hasImplementations(pbElement: PbElement): Boolean {
    return EP_NAME.extensions().asSequence()
      .flatMap { it.findImplementationsForProtoElement(pbElement) }
      .any()
  }

  private fun hasProtoDefinition(psiElement: PsiElement): Boolean {
    return EP_NAME.extensions().asSequence()
      .flatMap { it.findDeclarationsForCodeElement(psiElement) }
      .any()
  }

  companion object {
    private val EP_NAME =
      ExtensionPointName.create<PbCodeImplementationSearcher>("com.intellij.protobuf.codeImplementationSearcher")
  }
}

interface PbCodeImplementationSearcher {
  fun findImplementationsForProtoElement(pbElement: PbElement): Sequence<NavigatablePsiElement>
  fun findDeclarationsForCodeElement(psiElement: PsiElement): Sequence<PbElement>
}