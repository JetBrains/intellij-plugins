// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.


package org.intellij.terraform.hil.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parents
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.containers.addIfNotNull
import org.intellij.terraform.hcl.navigation.HCLQualifiedNameProvider
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.*
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.psi.TerraformReferenceContributor.Companion.Resource_Provider_Property
import org.intellij.terraform.hil.codeinsight.HILCompletionContributor
import org.intellij.terraform.hil.inspection.PsiFakeAwarePolyVariantReference
import org.intellij.terraform.hil.psi.impl.getHCLHost

object ILSelectFromSomethingReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is BaseExpression) return PsiReference.EMPTY_ARRAY
    val name = getSelectFieldText(element) ?: return PsiReference.EMPTY_ARRAY

    val host = element.getHCLHost() ?: return PsiReference.EMPTY_ARRAY

    val parent = element.parent as? SelectExpression<*> ?: return PsiReference.EMPTY_ARRAY

    if (parent.from === element && name in HILCompletionContributor.SCOPES) return PsiReference.EMPTY_ARRAY

    if (HCLPsiUtil.isPartOfPropertyKey(element)) return PsiReference.EMPTY_ARRAY

    // TODO: Properly support index expressions
    val indexed = parent.from is IndexSelectExpression<*>
    val expression = getGoodLeftElement(parent, element)
    @Suppress("IfNullToElvis")
    if (expression == null) {
      // v is leftmost, no idea what to do
      return PsiReference.EMPTY_ARRAY
    }

    val references = HCLPsiUtil.getReferencesSelectAware(expression)
    if (references.isNotEmpty()) {
      // If we select variable from resource or data provider
      // or some other element which references another property/block
      val refs = SmartList<PsiReference>()

      // Bypass references to the right of index/star
      if (isStarOrNumber(name)) {
        return references.mapNotNull { getBypassReference(element, it) }.toTypedArray()
      }

      for (reference in references) {
        if (reference is SpeciallyHandledPsiReference) {
          reference.collectReferences(element, name, refs)
        } else {
          refs.add(HCLElementLazyReference(element, false) { incompleteCode, fake ->
            val resolved = resolve(reference, incompleteCode, fake)
            val found = SmartList<HCLElement>()
            resolved.forEach {
              var from = it
              if (indexed && it is HCLProperty && it.value is HCLArray) {
                val arr = it.value as HCLArray
                val objects = arr.elements.filterIsInstance<HCLObject>()
                if (objects.isNotEmpty()) {
                  objects.forEach { o ->
                    collectReferences(o, name, found, fake)
                  }
                  return@HCLElementLazyReference found
                }
                from = arr.elements.firstOrNull() ?: it
              }
              collectReferences(from, name, found, fake)
            }
            found
          })
        }
      }
      return refs.toTypedArray()
    }

    if (Resource_Provider_Property.accepts(host.getParent(HCLProperty::class.java))) {
      // covered by ResourceProviderReferenceProvider
      return PsiReference.EMPTY_ARRAY
    }

    if (TerraformPatterns.PropertyUnderModuleProvidersPOB.accepts(host.getParent(HCLProperty::class.java))) {
      // covered by ModuleProvidersReferenceProvider
      return PsiReference.EMPTY_ARRAY
    }

    // Rest logic would try to find resource or data provider by element text
    val ev = getSelectFieldText(expression) ?: return PsiReference.EMPTY_ARRAY

    if (HILCompletionContributor.ILSE_DATA_SOURCE.accepts(parent)) {
      return arrayOf(HCLElementLazyReference(element, false) { _, _ ->
        val module = this.element.getHCLHost()?.getTerraformModule()
        val dataSources = module?.findDataSource(ev, getSelectFieldText(element)!!) ?: emptyList()
        dataSources
      })
    }

    // TODO: get suitable resource/provider/etc
    return arrayOf(HCLElementLazyReference(element, false) { _, _ ->
      val module = this.element.getHCLHost()?.getTerraformModule()
      val resources = module?.findResources(ev, getSelectFieldText(element)!!) ?: emptyList()
      resources
    })
  }

  fun collectReferences(r: PsiElement, name: String, found: MutableList<HCLElement>, fake: Boolean) {
    when (r) {
      is HCLIdentifier -> {
        val p = r.parent
        if (p is HCLForIntro) {
          // Resolve container we're iterating on
          return resolveForEachValueInner(p.container, name, found, fake, PsiTreeUtil.getParentOfType(p, HCLBlock::class.java))
        }
      }
      is HCLObject -> {
        val property = r.findProperty(name)
        val blocks = r.blockList.filter { it.nameElements.any { element -> element.name == name } }
        if (property != null) {
          found.add(property)
        } else if (blocks.isNotEmpty()) {
          found.addAll(blocks)
        }
      }
      is HCLBlock -> {
        val property = r.`object`?.findProperty(name)
        val blocks = r.`object`?.blockList?.filter { it.nameElements.any { it.name == name } }.orEmpty()
        val fqn = HCLQualifiedNameProvider.getQualifiedModelName(r)
        if (ApplicationManager.getApplication().getService(TypeModelProvider::class.java).ignored_references.contains(fqn)) {
          if (fake) found.add(FakeHCLProperty(name, r))
        } else if (TerraformPatterns.ModuleRootBlock.accepts(r)) {
          // TODO: Move this special TerraformPatters supports somewhere else
          val module = Module.getAsModuleBlock(r)
          if (module == null) {
            // Resolve everything
            if (fake) {
              found.add(FakeHCLProperty(name, r))
            }
          } else {
            val outputs = module.getDefinedOutputs().filter { it.name == name }
            if (outputs.isNotEmpty()) {
              found.addAll(outputs)
            } else if (fake) {
              //              found.add(FakeHCLProperty(name))
            }
          }
        } else if (TerraformPatterns.VariableRootBlock.accepts(r)) {
          val variable = Variable(r)
          val prev = found.size
          when (val default = variable.getDefault()) {
            is HCLObject -> collectReferences(default, name, found, fake)
            is HCLArray -> default.elements.filterIsInstance<HCLObject>().forEach {
              collectReferences(it, name, found, fake)
            }
          }
          if (prev == found.size) {
            found.addIfNotNull(resolveInType(variable.getType(), r, name) ?: if (fake) FakeHCLProperty(name, r, true) else null)
          }
        } else if (TerraformPatterns.DynamicBlock.accepts(r)) {
          // Moved to DynamicBlockVariableReferenceProvider.DynamicValueReference
          return
        } else if (TerraformPatterns.OutputRootBlock.accepts(r)) {
          // Probably reference to module output
          // If it's resource provide it's properties
          val value = r.`object`?.findProperty("value")?.value
          if (value == null) {
            if (fake) found.add(FakeHCLProperty(name, r))
          } else if (value is HCLObject) {
            collectReferences(value, name, found, fake)
          } else if (value is HCLArray) {
            value.elements.filterIsInstance<HCLObject>().forEach {
              collectReferences(it, name, found, fake)
            }
          } else {
            HCLPsiUtil.getReferencesSelectAware(value).forEach { ref ->
              resolve(ref, false, fake).forEach { resolved ->
                collectReferences(resolved, name, found, fake)
              }
            }
          }
        } else if (property != null) {
          found.add(property)
        } else if (blocks.isNotEmpty()) {
          found.addAll(blocks)
        } else {
          if (fqn != null) {
            val type = ModelHelper.getTypeModel(r.project).getByFQN(fqn)
            if (type is PropertyOrBlockType && type is BlockType && type.computed) {
              found.add(FakeHCLProperty(name, r))
              return
            }
          }
          if (fake) {
            val properties = ModelHelper.getBlockProperties(r)
            addBlockProperty(properties, name, r, found)
          }
        }
      }
      is FakeTypeProperty -> found.addIfNotNull(resolveInType(r.type, r, name) ?: if (fake) FakeHCLProperty(name, r, true) else null)
      is HCLProperty -> {
        if (r is FakeHCLProperty) {
          if (fake) {
            if (r.dynamic) {
              found.add(FakeHCLProperty(name, r, true))
              return
            }
            val fqn = HCLQualifiedNameProvider.getQualifiedModelName(r)
            if (fqn != null) {
              val type = ModelHelper.getTypeModel(r.project).getByFQN(fqn)
              if (type != null && type.computed) {
                if (type is BlockType) {
                  addBlockProperty(type.properties, name, r, found, true)
                  return
                }
                if (type is PropertyType) {
                  when (type.type) {
                    Types.Any -> found.add(FakeHCLProperty(name, r))
                    is MapType -> found.add(FakeHCLProperty(name, r))
                    // TODO add inner type to FakeHCLProperty
                    is ObjectType -> found.add(FakeHCLProperty(name, r))
                  }
                  return
                }
              }
            }
            if (ApplicationManager.getApplication().getService(TypeModelProvider::class.java).ignored_references.contains(fqn)) {
              found.add(FakeHCLProperty(name, r))
            }
          }
        } else if (r.parent is HCLObject && r.parent.parents(false).any { TerraformPatterns.LocalsRootBlock.accepts(it) }) {
          val value = r.value
          when {
            value is HCLObject -> collectReferences(value, name, found, fake)
            value is HCLArray -> collectReferences(value, name, found, fake)
            value is HCLForArrayExpression -> collectReferences(value.expression, name, found, fake)
            value != null && r.name == name -> found.add(r)
            value != null && isVariableReference(value) -> resolveForEachValueInner(value, name, found, fake, null)
          }
        } else {
          val value = r.value
          if (value is HCLObject) {
            val property = value.findProperty(name)
            if (property != null) {
              found.add(property)
              return
            }
            if (fake) {
              val fqn = HCLQualifiedNameProvider.getQualifiedModelName(r)
              val type = fqn?.let { ModelHelper.getTypeModel(r.project).getByFQN(it) }
              if (type is PropertyOrBlockType && type is BlockType) {
                // It's actually an incorrectly defined block, e.g. 'test = {}' instead of 'test {}'
                val properties = type.properties
                addBlockProperty(properties, name, r, found)
              }
            }
          }
        }
      }
      is HCLArray -> {
        for (it in r.elements) {
          if (it is HCLObject) {
            collectReferences(it, name, found, fake)
          } else if (it is HCLSelectExpression) {
            HCLPsiUtil.getReferencesSelectAware(it).forEach { ref ->
              resolve(ref, false, fake).forEach { resolved ->
                collectReferences(resolved, name, found, fake)
              }
            }
          }
        }
      }
    }
  }

  @Suppress("NAME_SHADOWING")
  private fun resolveInType(type: Type?, context: PsiElement, name: String): HCLElement? {
    // TODO: Use not FakeProperty but actual PSI element from Type declaration
    return when (type) {
      is ObjectType -> type.elements?.get(name)?.let { type -> FakeTypeProperty(name, context, type, true) }
      is TupleType -> type.elements.asSequence().filterIsInstance<ObjectType>().mapNotNull { it.elements?.get(name) }.firstOrNull()?.let { type ->
        FakeTypeProperty(name, context, type, true)
      }
      is ContainerType<*> -> if (type.elements is ObjectType) {
        type.elements.elements?.get(name)?.let { type ->
          FakeTypeProperty(name, context, type, true)
        }
      } else null
      else -> null
    }
  }

  fun resolveForEachValueInner(value: HCLExpression?, name: String, found: MutableList<HCLElement>, fake: Boolean, block: HCLBlock?) {
    when {
      value == null -> return
      value is HCLForArrayExpression -> (value.expression as? HCLObject)?.let { collectReferences(it, name, found, fake) }
      value is HCLForObjectExpression && value.value is HCLObject -> collectReferences(value.value, name, found, fake)
      value is HCLArray -> collectReferences(value, name, found, fake)
      value is HCLMethodCallExpression -> if (fake && block != null) found.add(FakeHCLProperty(name, block))
      isVariableReference(value) -> HCLPsiUtil.getReferencesSelectAware(value)
        .flatMap { resolve(it, false, fake) }
        .filterIsInstance<HCLBlock>()
        .filter { TerraformPatterns.VariableRootBlock.accepts(it) }
        .forEach {
          resolveVariableElementFromIterable(it, name, found, fake)
        }
      else -> 
        // e.g. 'local.name' reference or something else
        collectReferenceFromForEachValue(value, name, found, fake)
    }
  }

  /**
   * 'var.name' or 'var.name[0]'
   */
  private fun isVariableReference(value: HCLExpression): Boolean {
    var expr: PsiElement = value
    if (expr is IndexSelectExpression<*> && expr.parent is SelectExpression<*>) {
      expr = expr.parent
    }
    if (expr !is SelectExpression<*>) return false
    if (expr.field !is Identifier) return false
    val from = expr.from
    return from is Identifier && from.name == "var"
  }


  private fun resolveVariableElementFromIterable(block: HCLBlock, name: String, found: MutableList<HCLElement>, fake: Boolean) {
    val variable = Variable(block)
    val defaultMap = variable.getDefault()
    if (defaultMap is HCLObject) {
      defaultMap.propertyList.forEach {
        collectReferences(it, name, found, fake)
      }
      defaultMap.blockList.forEach {
        collectReferences(it, name, found, fake)
      }
    } else if (defaultMap is HCLArray) {
      collectReferences(defaultMap, name, found, fake)
    }
    found.addIfNotNull(resolveInType(variable.getType(), block, name))
    return
  }

  private fun collectReferenceFromForEachValue(p: PsiElement, name: String, found: MutableList<HCLElement>, fake: Boolean) {
    HCLPsiUtil.getReferencesSelectAware(p).forEach { ref ->
      resolve(ref, false, fake).forEach { resolved ->
        when (val value = getValueContainer(resolved, fake)) {
          is HCLObject -> {
            value.propertyList.forEach {
              collectReferences(it, name, found, fake)
            }
            value.blockList.forEach {
              collectReferences(it, name, found, fake)
            }
          }
          is HCLArray -> collectReferences(value, name, found, fake)
          else -> collectReferences(resolved, name, found, fake)
        }
      }
    }
  }

  private fun getValueContainer(r: PsiElement, fake: Boolean): HCLElement? {
    when (r) {
      is HCLIdentifier -> {
        val p = r.parent
        if (p is HCLForIntro) {
          // Resolve container we're iterating on
          return HCLPsiUtil.getReferencesSelectAware(p.container).flatMap { ref ->
            resolve(ref, false, fake).mapNotNull { resolved -> getValueContainer(resolved, fake) }
          }.firstOrNull()
        }
      }
      is HCLBlock -> {
        if (TerraformPatterns.VariableRootBlock.accepts(r)) {
          return r
        }
        return null
      }
      is HCLProperty -> {
        if (r is FakeHCLProperty) {
          return null
        }
        return r.value as? HCLContainer
      }
      is HCLObject -> {
        return r
      }
      is HCLArray -> {
        return r
      }
    }
    return null
  }

  private fun addBlockProperty(properties: Map<String, PropertyOrBlockType>, name: String, r: PsiElement, found: MutableCollection<HCLElement>, addFake: Boolean = false) {
    if (properties.containsKey(name)) {
      found.add(FakeHCLProperty(name, r, properties[name].asSafely<PropertyType>()?.type == Types.Any))
    } else if (properties.containsKey(Constants.HAS_DYNAMIC_ATTRIBUTES)) {
      found.add(FakeHCLProperty(name, r, true))
    } else if (addFake) {
      found.add(FakeHCLProperty(name, r))
    }
  }

}

fun getBypassReference(element: PsiElement, reference: PsiReference?): PsiReference? {
  return when (reference) {
    null -> null
    is HCLElementLazyReferenceBase<*> ->
      HCLElementLazyReference(element, reference.isSoft) { incomplete, fake -> reference.resolve(incomplete, fake) }
    is PsiPolyVariantReference ->
      object : PsiReferenceBase.Poly<PsiElement>(element, reference.isSoft) {
        override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
          return reference.multiResolve(incompleteCode)
        }
      }
    else ->
      object : PsiReferenceBase<PsiElement>(element, reference.isSoft) {
        override fun resolve(): PsiElement? {
          return reference.resolve()
        }
      }
  }
}

fun getSelectFieldText(expression: BaseExpression): String? {
  return when (expression) {
    is LiteralExpression -> expression.unquotedText
    is Identifier -> expression.name
    else -> null
  }
}

fun resolve(reference: PsiReference, incompleteCode: Boolean, fake: Boolean): SmartList<PsiElement> {
  val resolved = SmartList<PsiElement>()
  when (reference) {
    is PsiFakeAwarePolyVariantReference ->
      reference.multiResolve(incompleteCode, fake).mapNotNullTo(resolved) { it.element }
    is PsiPolyVariantReference ->
      reference.multiResolve(incompleteCode).mapNotNullTo(resolved) { it.element }
    else ->
      reference.resolve()?.let { resolved.add(it) }
  }
  return resolved
}

fun getGoodLeftElement(select: SelectExpression<*>, right: BaseExpression, skipStars: Boolean = true): BaseExpression? {
  if (select is IndexSelectExpression && select.field == right) return null
  // select = left.right
  var left = select.from
  if (left is IndexSelectExpression<*>) {
    left = left.from
  }
  if (left is SelectExpression<*>) {
    // left = from.middle
    val middle = left.field
    val from = left.from
    if (from is SelectExpression<*> && middle != null && skipStars) {
      val text = getSelectFieldText(middle)
      if (text != null && isStarOrNumber(text)) {
        // left == from.*
        // from == X.Y
        // select = X.Y.*.right
        // Y == from.field
        return from.field
      }
    }
    return middle
  }

  if (left !== right) return left
  // TODO: Investigate is that enough
  return null
}

fun isStarOrNumber(text: String) = text == "*" || text.toIntOrNull() != null


