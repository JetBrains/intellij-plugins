// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentsOfType
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hil.psi.impl.getHCLHost

object DynamicBlockVariableReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(id: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (id !is Identifier) return PsiReference.EMPTY_ARRAY
    val host = id.getHCLHost() ?: return PsiReference.EMPTY_ARRAY

    val parent = id.parent as? SelectExpression<*> ?: return PsiReference.EMPTY_ARRAY
    if (parent.from !== id) return PsiReference.EMPTY_ARRAY

    if (PsiTreeUtil.findFirstParent(host) { TfPsiPatterns.DynamicBlockContent.accepts(it) } == null) {
      // Either in 'content' or in 'labels'
      val labels = PsiTreeUtil.findFirstParent(host) { TfPsiPatterns.DynamicLabels.accepts(it) } as?HCLProperty
          ?: return PsiReference.EMPTY_ARRAY
      if (!PsiTreeUtil.isAncestor(labels.value, host, false)) return PsiReference.EMPTY_ARRAY
    }
    val name = id.name ?: return PsiReference.EMPTY_ARRAY
    getDynamicWithIteratorName(host, name) ?: return PsiReference.EMPTY_ARRAY
    return arrayOf(DynamicBlockNameVariableReference(id))
  }

  fun getDynamicWithIteratorName(host: HCLElement, name: String): HCLBlock? {
    val dynamics = host.parentsOfType(HCLBlock::class.java).filter { TfPsiPatterns.DynamicBlock.accepts(it) }
    for (dynamic in dynamics) {
      val iteratorPropertyValue = dynamic.`object`?.findProperty("iterator")?.value as? HCLIdentifier
      val iterator = iteratorPropertyValue?.id ?: dynamic.name
      if (iterator == name) {
        return dynamic
      }
    }
    return null
  }

  class DynamicBlockNameVariableReference(id: Identifier) : HCLElementLazyReferenceBase<Identifier>(id, false), SpeciallyHandledPsiReference {
    override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<PsiElement> {
      val parent = element.parent as? SelectExpression<*> ?: return emptyList()
      if (parent.from !== element) return emptyList()
      val host = element.getHCLHost() ?: return emptyList()
      val name = element.name ?: return emptyList()
      val dynamic = getDynamicWithIteratorName(host, name) ?: return emptyList()
      val iteratorPropertyValue = dynamic.`object`?.findProperty("iterator")?.value as? HCLIdentifier
      val iterator = iteratorPropertyValue?.id ?: dynamic.name
      if (iterator != element.name) {
        return emptyList()
      }
      if (iteratorPropertyValue != null) {
        return listOf(iteratorPropertyValue)
      }
      return listOf(FakeHCLProperty(iterator, dynamic))
    }

    override fun collectReferences(element: PsiElement, name: String, found: MutableList<PsiReference>) {
      if (name == "key") {
        // No refs, kinda 'fake' property
      } else if (name == "value") {
        if (element is Identifier) {
          found.add(DynamicValueReference(element, this.element.name!!))
        }
      } else {
        found.add(object : PsiReferenceBase.Immediate<PsiElement>(element, false, null), EmptyResolveMessageProvider {
          override fun getUnresolvedMessagePattern(): String {
            return "Unknown iterator field {0}"
          }

          override fun getVariants(): Array<LookupElement> {
            return arrayOf(LookupElementBuilder.create("key"), LookupElementBuilder.create("value"))
          }
        })
      }
    }
  }

  class DynamicValueReference(e: Identifier, private val dynamicIteratorName: String) : HCLElementLazyReferenceBase<Identifier>(e, false), SpeciallyHandledPsiReference {
    override fun collectReferences(element: PsiElement, name: String, found: MutableList<PsiReference>) {
      if (element !is Identifier) return
      found.add(HCLElementLazyReference(element, false) { incomplete, fake ->
        @Suppress("NAME_SHADOWING")
        val name = this.element.name ?: return@HCLElementLazyReference emptyList()
        val host = (element as? BaseExpression)?.getHCLHost() ?: return@HCLElementLazyReference emptyList()
        val dynamic = getDynamicWithIteratorName(host, dynamicIteratorName) ?: return@HCLElementLazyReference emptyList()
        val value = dynamic.`object`?.findProperty("for_each")?.value
        @Suppress("NAME_SHADOWING")
        val found = SmartList<HCLElement>()
        if (value == null) {
          if (fake) found.add(FakeHCLProperty(name, dynamic))
        } else if (value is HCLForArrayExpression) {
          (value.expression as? HCLObject)?.let { ILSelectFromSomethingReferenceProvider.collectReferences(it, name, found, fake) }
        } else if (value is HCLArray) {
          ILSelectFromSomethingReferenceProvider.collectReferences(value, name, found, fake)
        } else if (value is HCLMethodCallExpression) {
          if (fake) found.add(FakeHCLProperty(name, dynamic))
        } else {
          // e.g. 'var.something' reference
          HCLPsiUtil.getReferencesSelectAware(value).forEach { ref ->
            resolve(ref, incomplete, fake).forEach { resolved ->
              ILSelectFromSomethingReferenceProvider.collectReferences(resolved, name, found, fake)
            }
          }
        }
        found
      })
    }

    override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<PsiElement> {
      val host = (element as? BaseExpression)?.getHCLHost() ?: return emptyList()
      val dynamic = getDynamicWithIteratorName(host, dynamicIteratorName) ?: return emptyList()
      val value = dynamic.`object`?.findProperty("for_each")?.value
      return listOfNotNull(value)
    }
  }
}

