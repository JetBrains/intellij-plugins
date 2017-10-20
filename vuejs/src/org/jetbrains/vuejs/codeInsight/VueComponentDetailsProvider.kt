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
    val details: List<VueAttributeDescriptor> = VueComponentOwnDetailsProvider.getDetails(descriptor, EMPTY_FILTER, onlyPublic, false)
    result.addAll(details)
    iterateProviders(descriptor, {
      result.addAll(VueComponentOwnDetailsProvider.getDetails(it, EMPTY_FILTER, onlyPublic, false))
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
    private val BIND_VARIANTS = setOf(".prop", ".camel", ".sync")
    private val ON_VARIANTS = setOf("*")
    private val PREFIX_VARIANTS = mapOf(Pair(":", BIND_VARIANTS),
                                        Pair("v-bind:", BIND_VARIANTS),
                                        Pair("@", ON_VARIANTS), Pair("v-on:", ON_VARIANTS))
    private val EVENT_MODIFIERS = setOf(".stop", ".prevent", ".capture", ".self", ".once")
    private val NO_VALUE = mapOf(Pair("@", EVENT_MODIFIERS), Pair("v-on:", EVENT_MODIFIERS))

    fun attributeAllowsNoValue(attributeName : String) : Boolean {
      return NO_VALUE.any {
        val cutPrefix = attributeName.substringAfter(it.key, "")
        !cutPrefix.isEmpty() && it.value.any { cutPrefix.endsWith(it) }
      }
    }

    fun getBoundName(attributeName : String): String? {
      return PREFIX_VARIANTS.map {
        val after = attributeName.substringAfter(it.key, "")
        if (!after.isEmpty()) {
          if (it.value.contains("*")) {
            return after.substringBefore(".", after)
          }
          return@map it.value.map { after.substringBefore(it, "") }.firstOrNull { !it.isEmpty() } ?: after
        }
        return@map ""
      }.firstOrNull { !it.isEmpty() }
    }

    fun nameVariantsFilter(attributeName : String) : (String, PsiElement) -> Boolean {
      val prefix = PREFIX_VARIANTS.keys.find { attributeName.startsWith(it) }
      val normalizedName = if (prefix != null) attributeName.substring(prefix.length) else attributeName
      val nameVariants = getNameVariants(normalizedName, true)
      return { name, _ -> name in nameVariants }
    }
  }

  private fun iterateProviders(descriptor: JSObjectLiteralExpression, processor : (JSObjectLiteralExpression) -> Boolean) {
    listOf(VueMixinLocalComponentDetailsProvider(), VueGlobalMixinComponentDetailsProvider())
      .any {
        val finder = it.getDescriptorFinder()
        it.getIndexedData(descriptor).any {
          val obj = finder(it)
          obj != null && !processor(obj)
        }
      }
  }
}