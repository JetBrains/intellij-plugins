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

internal class PbCodeImplementationLineMarkerProvider : RelatedItemLineMarkerProvider() {
  override fun getId(): String {
    return "PbCodeImplementationLineMarkerProvider"
  }

  override fun getName(): String? {
    return PbIdeBundle.message("line.marker.overridden.element")
  }

  override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    val identifier = element.identifierChild() ?: return

    val marker =
      when (element) {
        is PbElement ->
          if (hasImplementation(element)) createOverriddenElementMarker(identifier) else null
        else ->
          if (hasProtoDefinition(element)) createImplementedElementMarker(identifier) else null
      } ?: return

    result.add(marker)
  }

  private fun createOverriddenElementMarker(psiElement: PsiElement): RelatedItemLineMarkerInfo<PsiElement> {
    return object : RelatedItemLineMarkerInfo<PsiElement>(
      psiElement,
      psiElement.textRange,
      AllIcons.General.OverridenMethod,
      { null },
      { event: MouseEvent, psiElement: PsiElement ->
        if (psiElement is PbElement) {
          val gotoRelatedItems = GotoRelatedItem.createItems(findImplementations(psiElement).toList()).toMutableList()
          NavigationUtil.getRelatedItemsPopup(gotoRelatedItems, PbIdeBundle.message("line.marker.navigate.to.implementation")).show(event.component)
        }
      },
      GutterIconRenderer.Alignment.LEFT,
      NotNullFactory {
        if (psiElement !is PbElement) return@NotNullFactory emptyList()
        GotoRelatedItem.createItems(findImplementations(psiElement).toList())
      }
    ) {}
  }

  private fun createImplementedElementMarker(psiElement: PsiElement): RelatedItemLineMarkerInfo<PsiElement> {
    return object : RelatedItemLineMarkerInfo<PsiElement>(
      psiElement,
      psiElement.textRange,
      AllIcons.General.ImplementingMethod,
      { PbIdeBundle.message("line.marker.navigate.to.declaration") },
      GutterIconNavigationHandler { event, element ->
        val project = element.project
        ReadAction.nonBlocking(Callable {
          val identifierOwner = element.parentIdentifierOwner() ?: return@Callable emptyList()
          findProtoDefinitions(identifierOwner).toList()
        })
          .expireWith(project.service<PbCompositeModificationTracker>())
          .withDocumentsCommitted(project)
          .coalesceBy(this@PbCodeImplementationLineMarkerProvider)
          .finishOnUiThread(ModalityState.NON_MODAL) { targets ->
            when (targets.size) {
              0 -> return@finishOnUiThread
              1 -> {
                val singleTarget = targets.singleOrNull() ?: return@finishOnUiThread
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
          .submit(AppExecutorUtil.getAppExecutorService())
      },
      GutterIconRenderer.Alignment.LEFT,
      NotNullFactory {
        GotoRelatedItem.createItems(findProtoDefinitions(psiElement).toList())
      }
    ) {}
  }

  private fun hasImplementation(pbElement: PbElement): Boolean {
    return findImplementations(pbElement).any()
  }

  private fun findImplementations(pbElement: PbElement): Sequence<PsiElement> {
    return EP_NAME.extensions().asSequence()
      .flatMap { it.findImplementationsForProtoElement(pbElement) }
  }

  private fun hasProtoDefinition(psiElement: PsiElement): Boolean {
    return findProtoDefinitions(psiElement).any()
  }

  private fun findProtoDefinitions(psiElement: PsiElement): Sequence<PbElement> {
    return EP_NAME.extensions().asSequence()
      .flatMap { it.findDeclarationsForCodeElement(psiElement) }
  }

  private fun PsiElement.identifierChild(): PsiElement? {
    return this.castSafelyTo<PsiNameIdentifierOwner>()?.identifyingElement
  }

  private fun PsiElement.parentIdentifierOwner(): PsiElement? {
    return this.parentOfType<PsiNameIdentifierOwner>(true)
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