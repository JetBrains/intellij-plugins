package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.psi.PsiElement

/**
 * @author Irina.Chernushina on 10/13/2017.
 */
class VueComponentDetailsProvider: VueAbstractComponentDetailsProvider() {
  fun getAttributes(descriptor: JSObjectLiteralExpression, onlyPublic: Boolean): List<VueAttributeDescriptor> =
    getIterable(descriptor, { _, _ -> true }, onlyPublic, false)
      .map {
        @Suppress("UnnecessaryVariable")
        val attrDescriptor = it
        getNameVariants(it.name, true).map { attrDescriptor.createNameVariant(it) }
      }.flatten()

  fun resolveAttribute(descriptor: JSObjectLiteralExpression,
                       attrName: String,
                       onlyPublic: Boolean): VueAttributeDescriptor? {
    return getIterable(descriptor, nameVariantsFilter(attrName), onlyPublic, true).firstOrNull()
  }

  companion object {
    private val providers = listOf(VueComponentOwnDetailsProvider(), VueMixinComponentDetailsProvider())
    val INSTANCE = VueComponentDetailsProvider()
    private val BIND_VARIANTS = setOf(":", "v-bind:")
    fun nameVariantsFilter(attributeName : String) : (String, PsiElement) -> Boolean {
      val prefix = BIND_VARIANTS.find { attributeName.startsWith(it) }
      val normalizedName = if (prefix != null) attributeName.substring(prefix.length) else attributeName
      val nameVariants = getNameVariants(normalizedName, true)
      return { name, _ -> name in nameVariants }
    }
  }

  override fun getIterable(descriptor: JSObjectLiteralExpression,
                           filter: ((String, PsiElement) -> Boolean)?,
                           onlyPublic: Boolean, onlyFirst: Boolean): Iterable<VueAttributeDescriptor> {
    // must be lazy
    return object: Iterable<VueAttributeDescriptor> {
      override fun iterator(): Iterator<VueAttributeDescriptor> {
        return object: Iterator<VueAttributeDescriptor> {
          private var currentIterator = initIterator(0)
          private var currentIdx = 0
          private fun initIterator(idx: Int) = providers[idx].getIterable(descriptor, filter, onlyPublic, onlyFirst).iterator()

          override fun next(): VueAttributeDescriptor {
            if (!hasNext()) throw IllegalStateException()
            return currentIterator.next()
          }

          override fun hasNext(): Boolean {
            while ((currentIdx + 1) < providers.size && !currentIterator.hasNext()) {
              ++currentIdx
              currentIterator = initIterator(currentIdx)
            }
            return currentIterator.hasNext()
          }
        }
      }
    }
  }
}