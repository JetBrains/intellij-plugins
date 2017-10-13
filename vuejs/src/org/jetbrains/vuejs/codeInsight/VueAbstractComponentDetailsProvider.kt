package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

/**
 * @author Irina.Chernushina on 10/12/2017.
 */
abstract class VueAbstractComponentDetailsProvider {
  abstract fun getIterable(descriptor: JSObjectLiteralExpression,
                           filter: ((String, PsiElement) -> Boolean)?,
                           onlyPublic: Boolean, onlyFirst: Boolean): Iterable<VueAttributeDescriptor>

  protected enum class CompMember(val propertyName: String,
                                  val isPublic: Boolean,
                                  val isFunctions: Boolean,
                                  private val canBeArray: Boolean) {
    Props("props", true, false, true),
    Computed("computed", false, true, false),
    Methods("methods", false, true, false),
    Data("data", false, false, false) {
      override fun getObjectLiteralFromResolved(resolved: PsiElement): JSObjectLiteralExpression? = findReturnedObjectLiteral(resolved)

      override fun getObjectLiteral(property: JSProperty): JSObjectLiteralExpression? {
        val function = property.tryGetFunctionInitializer() ?: return null
        return findReturnedObjectLiteral(function)
      }
    };

    companion object {
      private fun findReturnedObjectLiteral(resolved: PsiElement): JSObjectLiteralExpression? {
        resolved as? JSFunction ?: return null
        return JSStubBasedPsiTreeUtil.findDescendants<JSObjectLiteralExpression>(resolved, TokenSet.create(
          JSStubElementTypes.OBJECT_LITERAL_EXPRESSION))
          .find {
            it.context == resolved ||
            it.context is JSParenthesizedExpression && it.context?.context == resolved ||
            it.context is JSReturnStatement
          }
      }
    }

    protected open fun getObjectLiteral(property: JSProperty): JSObjectLiteralExpression? = null
    protected open fun getObjectLiteralFromResolved(resolved: PsiElement): JSObjectLiteralExpression? = null

    fun readMembers(property: JSProperty, filter: ((String, PsiElement) -> Boolean)?): List<VueAttributeDescriptor> {
      var propsObject = property.objectLiteralExpressionInitializer ?: getObjectLiteral(property)
      if (propsObject == null && property.initializerReference != null) {
        val resolved = JSStubBasedPsiTreeUtil.resolveLocally(property.initializerReference!!, property)
        if (resolved != null) {
          propsObject = JSStubBasedPsiTreeUtil.findDescendants(resolved, JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
                          .find { it.context == resolved } ?: getObjectLiteralFromResolved(resolved)
          if (propsObject == null && canBeArray) {
            return readPropsFromArray(resolved, filter)
          }
        }
      }
      if (propsObject != null) {
        return filteredObjectProperties(propsObject, filter)
      }
      return if (canBeArray) readPropsFromArray(property, filter) else return emptyList()
    }

    private fun filteredObjectProperties(propsObject: JSObjectLiteralExpression, filter: ((String, PsiElement) -> Boolean)?) =
      propsObject.properties.filter { filter == null || filter(it.name!!, it) }.map { VueAttributeDescriptor(it.name!!, it) }

    private fun readPropsFromArray(holder: PsiElement, filter: ((String, PsiElement) -> Boolean)?): List<VueAttributeDescriptor> =
      getStringLiteralsFromInitializerArray(holder, filter).map { VueAttributeDescriptor(StringUtil.unquoteString(it.text), it) }

    private fun getStringLiteralsFromInitializerArray(holder: PsiElement,
                                                      filter: ((String, PsiElement) -> Boolean)?): List<JSLiteralExpression> {
      return JSStubBasedPsiTreeUtil.findDescendants(holder, JSStubElementTypes.LITERAL_EXPRESSION)
        .filter({
                  val context = it.context
                  it.significantValue != null &&
                  (filter == null || filter(StringUtil.unquoteString(it.significantValue!!), it)) &&
                  ((context is JSArrayLiteralExpression) && (context.parent == holder) || context == holder)
                })
    }
  }
}