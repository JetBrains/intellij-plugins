package com.intellij.protobuf.ide.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.protobuf.ide.PbIdeBundle
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
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
}