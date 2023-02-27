/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hil.psi

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hil.psi.impl.getHCLHost

object ResourceEachVariableReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(id: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (id !is Identifier) return PsiReference.EMPTY_ARRAY
    val name = id.name ?: return PsiReference.EMPTY_ARRAY
    if (name != "each") return PsiReference.EMPTY_ARRAY

    val host = id.getHCLHost() ?: return PsiReference.EMPTY_ARRAY

    val parent = id.parent as? SelectExpression<*> ?: return PsiReference.EMPTY_ARRAY
    if (parent.from !== id) return PsiReference.EMPTY_ARRAY

    val block = getContainingResourceOrDataSourceOrModule(host) ?: return PsiReference.EMPTY_ARRAY

    // Don't report reference if inside dynamic block with iterator with name 'each'
    if (DynamicBlockVariableReferenceProvider.getDynamicWithIteratorName(host, name) != null) return PsiReference.EMPTY_ARRAY

    val for_each = block.`object`?.findProperty("for_each")
    if (for_each == null) {
      return arrayOf(object : PsiReferenceBase.Immediate<PsiElement>(id, false, null), EmptyResolveMessageProvider {
        override fun getUnresolvedMessagePattern(): String {
          return "The \"each\" object can be used only in \"resource\"/\"data\"/\"module\" blocks, and only when the \"for_each\" argument is set"
        }
      })
    }

    return arrayOf(ResourceEachVariableReference(id))
  }

  class ResourceEachVariableReference(id: Identifier) : HCLElementLazyReferenceBase<Identifier>(id, false), SpeciallyHandledPsiReference {
    override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<PsiElement> {
      val parent = element.parent as? SelectExpression<*> ?: return emptyList()
      if (parent.from !== element) return emptyList()
      val host = element.getHCLHost() ?: return emptyList()
      val name = element.name ?: return emptyList()

      val block = getContainingResourceOrDataSourceOrModule(host) ?: return emptyList()
      block.`object`?.findProperty("for_each") ?: return emptyList()

      return listOf(FakeHCLProperty(name, block))
    }

    override fun collectReferences(element: PsiElement, name: String, found: MutableList<PsiReference>) {
      if (name == "key") {
        // No refs, kinda 'fake' property
        found.add(HCLElementLazyReference(element, true) { _, _ ->
          val host = (element as? BaseExpression)?.getHCLHost() ?: return@HCLElementLazyReference emptyList()
          val block = getContainingResourceOrDataSourceOrModule(host) ?: return@HCLElementLazyReference emptyList()
          val value = block.`object`?.findProperty("for_each")?.value
          listOfNotNull(value)
        })
      } else if (name == "value") {
        if (element is Identifier) {
          found.add(DynamicValueReference(element))
        }
      } else {
        found.add(object : PsiReferenceBase.Immediate<PsiElement>(element, true, null), EmptyResolveMessageProvider {
          override fun getUnresolvedMessagePattern(): String {
            return "Unknown iterator field {0}"
          }

          override fun getVariants(): Array<Any> {
            return arrayOf(LookupElementBuilder.create("key"), LookupElementBuilder.create("value"))
          }
        })
      }
    }
  }

  class DynamicValueReference(e: Identifier) : HCLElementLazyReferenceBase<Identifier>(e, false), SpeciallyHandledPsiReference {
    override fun collectReferences(element: PsiElement, name: String, found: MutableList<PsiReference>) {
      if (element !is Identifier) return
      found.add(HCLElementLazyReference(element, false) { _, fake ->
        @Suppress("NAME_SHADOWING")
        val name = this.element.name ?: return@HCLElementLazyReference emptyList()
        val host = (element as? BaseExpression)?.getHCLHost() ?: return@HCLElementLazyReference emptyList()
        val block = getContainingResourceOrDataSourceOrModule(host) ?: return@HCLElementLazyReference emptyList()
        val value = block.`object`?.findProperty("for_each")?.value
        @Suppress("NAME_SHADOWING")
        val found = SmartList<HCLElement>()
        if (value == null) {
          if (fake) found.add(FakeHCLProperty(name, block))
        } else {
          ILSelectFromSomethingReferenceProvider.resolveForEachValueInner(value, name, found, fake, block)
        }
        found
      })
    }

    override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<PsiElement> {
      val host = (element as? BaseExpression)?.getHCLHost() ?: return emptyList()
      val block = getContainingResourceOrDataSourceOrModule(host) ?: return emptyList()
      val value = block.`object`?.findProperty("for_each")?.value
      return listOfNotNull(value)
    }
  }
}

