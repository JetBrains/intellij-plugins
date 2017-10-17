package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

/**
 * @author Irina.Chernushina on 10/13/2017.
 */
class VueComponentDetailsProvider {
  fun getAttributes(descriptor: JSObjectLiteralExpression, onlyPublic: Boolean): List<VueAttributeDescriptor> {
    val result: MutableList<VueAttributeDescriptor> = mutableListOf()
    val details: List<VueAttributeDescriptor> = VueComponentOwnDetailsProvider.getDetails(descriptor, null, onlyPublic, false)
    result.addAll(details)
    iterateProviders(descriptor, {
      result.addAll(VueComponentOwnDetailsProvider.getDetails(it, null, onlyPublic, false))
      true
    })

    return result.map {
      @Suppress("UnnecessaryVariable")
      val attrDescriptor = it
      getNameVariants(it.name, true).map { attrDescriptor.createNameVariant(it) }
    }.flatten()
  }

  fun resolveAttribute(descriptor: JSObjectLiteralExpression,
                       attrName: String,
                       onlyPublic: Boolean): VueAttributeDescriptor? {
    val filter = nameVariantsFilter(attrName)
    val direct = VueComponentOwnDetailsProvider.getDetails(descriptor, filter, onlyPublic, true).firstOrNull()
    if (direct != null) return direct
    val holder : Ref<VueAttributeDescriptor> = Ref()
    iterateProviders(descriptor, {
      holder.set(VueComponentOwnDetailsProvider.getDetails(it, filter, onlyPublic, true).firstOrNull())
      holder.isNull
    })
    return holder.get()
  }

  companion object {
    val INSTANCE = VueComponentDetailsProvider()
    private val BIND_VARIANTS = setOf(":", "v-bind:")
    fun nameVariantsFilter(attributeName : String) : (String, PsiElement) -> Boolean {
      val prefix = BIND_VARIANTS.find { attributeName.startsWith(it) }
      val normalizedName = if (prefix != null) attributeName.substring(prefix.length) else attributeName
      val nameVariants = getNameVariants(normalizedName, true)
      return { name, _ -> name in nameVariants }
    }
  }

  private fun iterateProviders(descriptor: JSObjectLiteralExpression, processor : (JSObjectLiteralExpression) -> Boolean) {
    listOf(VueMixinLocalComponentDetailsProvider(), VueGlobalMixinComponentDetailsProvider())
      .firstOrNull {
        val finder = it.getDescriptorFinder()
        val indexedData = it.getIndexedData(descriptor)
        val selected = indexedData.firstOrNull {
          val obj = finder(it)
          obj != null && !processor(obj)
        }
        selected != null
      }
  }
}