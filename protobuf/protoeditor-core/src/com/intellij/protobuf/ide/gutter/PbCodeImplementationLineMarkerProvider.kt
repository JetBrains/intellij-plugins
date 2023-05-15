package com.intellij.protobuf.ide.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.protobuf.ide.PbIdeBundle
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icons.ProtoeditorCoreIcons

class PbCodeImplementationLineMarkerProvider : RelatedItemLineMarkerProvider() {
  override fun getId(): String {
    return "PbCodeImplementationLineMarkerProvider"
  }

  override fun getName(): String? {
    return PbIdeBundle.message("line.marker.overridden.element")
  }

  override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    if (element !is PsiNameIdentifierOwner) return
    val anchor = element.identifyingElement ?: return
    if (element !is PbElement || !hasImplementation(element)) return
    result.add(createOverriddenElementMarker(anchor, element))
  }

  private fun createOverriddenElementMarker(identifier: PsiElement, identifierOwner: PbElement): RelatedItemLineMarkerInfo<PsiElement> {
    return NavigationGutterIconBuilder.create(ProtoeditorCoreIcons.GoToImplementation)
      .setAlignment(GutterIconRenderer.Alignment.LEFT)
      .setTooltipText(PbIdeBundle.message("line.marker.navigate.to.implementation"))
      .setEmptyPopupText(PbIdeBundle.message("line.marker.no.implementations.found"))
      .setTargets(NotNullLazyValue.lazy { findImplementations(identifierOwner).toList() })
      .createLineMarkerInfo(identifier)
  }


  private fun hasImplementation(pbElement: PsiElement): Boolean {
    return findImplementations(pbElement).any()
  }
}