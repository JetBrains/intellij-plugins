package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionProperty
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement

/**
 * @author Irina.Chernushina on 10/13/2017.
 */
class VueComponentOwnDetailsProvider : VueAbstractComponentDetailsProvider() {
  companion object {
    private val FUNCTION_FILTER = { element: PsiElement ->
      element is JSFunctionProperty || element is JSProperty && element.value is JSFunction
    }
  }

  override fun getIterable(descriptor: JSObjectLiteralExpression,
                           filter: ((String, PsiElement) -> Boolean)?,
                           onlyPublic: Boolean, onlyFirst: Boolean): Iterable<VueAttributeDescriptor> {
    val detailsList = CompMember.values().filter { !onlyPublic || it.isPublic }
    val result = mutableListOf<VueAttributeDescriptor>()
    // since Kotlin "streams" seems to be not lazy
    for (compMember in detailsList) {
      val details = readMembers(compMember, descriptor, filter)
      if (onlyFirst && !details.isEmpty()) return details
      result.addAll((details))
    }
    return result
  }

  private fun readMembers(compMember: CompMember,
                          descriptor: JSObjectLiteralExpression,
                          filter: ((String, PsiElement) -> Boolean)?): List<VueAttributeDescriptor> {
    val detailsFilter = if (compMember.isFunctions) { name, element -> FUNCTION_FILTER(element) &&
                                                               (filter == null || filter(name, element))} else filter
    val property = descriptor.findProperty(compMember.propertyName) ?: return emptyList()
    return compMember.readMembers(property, detailsFilter)
  }
}