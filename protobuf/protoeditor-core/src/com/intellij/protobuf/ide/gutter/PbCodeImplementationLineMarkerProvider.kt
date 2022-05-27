package com.intellij.protobuf.ide.gutter

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationUtil
import com.intellij.icons.AllIcons
import com.intellij.ide.util.EditSourceUtil
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.NotNullFactory
import com.intellij.pom.Navigatable
import com.intellij.protobuf.ide.PbCompositeModificationTracker
import com.intellij.protobuf.ide.PbIdeBundle
import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.parentOfType
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.castSafelyTo
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.event.MouseEvent
import java.util.concurrent.Callable
import kotlin.streams.asSequence
import kotlin.streams.toList

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
      when (element) {
        is PbElement ->
          if (hasImplementation(element)) createOverriddenElementMarker(anchor, element) else null
        else ->
          if (hasProtoDefinition(element)) createImplementedElementMarker(anchor, element) else null
      } ?: return

    result.add(marker)
  }

  private fun createOverriddenElementMarker(identifier: PsiElement, identifierOwner: PbElement): RelatedItemLineMarkerInfo<PsiElement> {
    return object : RelatedItemLineMarkerInfo<PsiElement>(
      identifier,
      identifier.textRange,
      AllIcons.General.ImplementingMethod, //todo
      { PbIdeBundle.message("line.marker.navigate.to.declaration") },
      navigationHandler { element ->
        if (element !is PbElement) return@navigationHandler emptyList()
        findImplementations(element).toList()
      },
      GutterIconRenderer.Alignment.LEFT,
      NotNullFactory { GotoRelatedItem.createItems(findImplementations(identifierOwner).toList()) }
    ) {}
  }

  private fun createImplementedElementMarker(identifier: PsiElement,
                                             identifierOwner: PsiNameIdentifierOwner): RelatedItemLineMarkerInfo<PsiElement> {
    return object : RelatedItemLineMarkerInfo<PsiElement>(
      identifier,
      identifier.textRange,
      AllIcons.General.ImplementingMethod, //todo
      { PbIdeBundle.message("line.marker.navigate.to.declaration") },
      navigationHandler { element -> findProtoDefinitions(element).toList() },
      GutterIconRenderer.Alignment.LEFT,
      NotNullFactory { GotoRelatedItem.createItems(findProtoDefinitions(identifierOwner).toList()) }
    ) {}
  }

  private fun navigationHandler(lazyTargets: (PsiElement) -> Collection<PsiElement>): GutterIconNavigationHandler<PsiElement> {
    return GutterIconNavigationHandler<PsiElement> { event, element ->
      ReadAction.nonBlocking(Callable { lazyTargets(element) })
        .expireWith(element.project.service<PbCompositeModificationTracker>())
        .withDocumentsCommitted(element.project)
        .coalesceBy(this@PbCodeImplementationLineMarkerProvider)
        .finishOnUiThread(ModalityState.NON_MODAL) { targets -> navigateOrShowPopup(event, targets) }
        .submit(AppExecutorUtil.getAppExecutorService())
    }
  }

  private fun navigateOrShowPopup(event: MouseEvent, targets: Collection<PsiElement>) {
    when (targets.size) {
      0 -> return
      1 -> {
        val singleTarget = targets.singleOrNull() ?: return
        EditSourceUtil.getDescriptor(singleTarget)?.takeIf(Navigatable::canNavigate)?.navigate(true)
      }
      else -> {
        NavigationUtil.getPsiElementPopup(
          targets.toTypedArray(),
          PbIdeBundle.message("line.marker.navigate.to.declaration")
        ).show(RelativePoint(event))
      }
    }
  }

  private fun hasImplementation(pbElement: PbElement): Boolean {
    return findImplementations(pbElement).any()
  }

  private fun findImplementations(pbElement: PbElement): Sequence<PsiElement> {
    val identifierOwner = pbElement.parentIdentifierOwner()?.castSafelyTo<PbElement>() ?: return emptySequence()
    val converters = collectRpcConverters()
    return IMPLEMENTATION_SEARCHER_EP_NAME.extensions().asSequence()
      .flatMap { it.findImplementationsForProtoElement(identifierOwner, converters) }
  }

  private fun hasProtoDefinition(psiElement: PsiElement): Boolean {
    return findProtoDefinitions(psiElement).any()
  }

  private fun findProtoDefinitions(psiElement: PsiElement): Sequence<PbElement> {
    val identifierOwner = psiElement.parentIdentifierOwner() ?: return emptySequence()
    val converters = collectRpcConverters()
    return IMPLEMENTATION_SEARCHER_EP_NAME.extensions().asSequence()
      .flatMap { it.findDeclarationsForCodeElement(identifierOwner, converters) }
  }

  private fun PsiElement.parentIdentifierOwner(): PsiNameIdentifierOwner? {
    return this.parentOfType<PsiNameIdentifierOwner>(true)
  }

  private fun collectRpcConverters(): Collection<PbGeneratedCodeConverter> {
    return CONVERTER_EP_NAME.extensions().map(PbGeneratedCodeConverterProvider::getProtoConverter).toList()
  }
}

interface PbCodeImplementationSearcher {
  fun findImplementationsForProtoElement(pbElement: PbElement,
                                         converters: Collection<PbGeneratedCodeConverter>): Sequence<NavigatablePsiElement>

  fun findDeclarationsForCodeElement(psiElement: PsiElement, converters: Collection<PbGeneratedCodeConverter>): Sequence<PbElement>
}

private val IMPLEMENTATION_SEARCHER_EP_NAME =
  ExtensionPointName.create<PbCodeImplementationSearcher>("com.intellij.protobuf.codeImplementationSearcher")