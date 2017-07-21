package org.jetbrains.vuejs.language

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.impl.source.xml.XmlElementImpl
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.NullableFunction
import org.jetbrains.vuejs.codeInsight.DELIMITERS
import org.jetbrains.vuejs.index.VueOptionsIndex
import java.util.*

/**
 * @author Irina.Chernushina on 7/19/2017.
 */
class VueInjector : MultiHostInjector {
  companion object {
    val BRACES_FACTORY: NullableFunction<PsiElement, Pair<String, String>> = JSInjectionBracesUtil
      .delimitersFactory(VueJSLanguage.INSTANCE.displayName) { project, key ->
        val element = org.jetbrains.vuejs.index.resolve(DELIMITERS, GlobalSearchScope.projectScope(project), VueOptionsIndex.KEY)
                      ?: return@delimitersFactory null
        val property = PsiTreeUtil.getParentOfType(element, JSProperty::class.java) ?: return@delimitersFactory null
        val arr = property.value as JSArrayLiteralExpression
        if (arr.expressions.size != 2) return@delimitersFactory null
        val delimiter = parseStringLiteral(
          arr.expressions[if (JSInjectionBracesUtil.START_SYMBOL_KEY == key) 0 else 1]) ?: return@delimitersFactory null
        com.intellij.openapi.util.Pair.create(delimiter, element)
      }

    private fun parseStringLiteral(value : JSElement) : String? {
      val literal = value as JSLiteralExpression
      if (literal.isQuotedLiteral) return StringUtil.unquoteString(literal.text)
      return null
    }
  }


  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    val project = context.project
    if (!org.jetbrains.vuejs.index.hasVue(project)) return

    if (context is XmlTextImpl || context is XmlAttributeValueImpl) {
      val braces = BRACES_FACTORY.`fun`(context) ?: return
      injectInXmlTextByDelimiters(registrar, context, VueJSLanguage.INSTANCE, braces.getFirst(), braces.getSecond())
    }
  }

  override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
    return Arrays.asList<Class<out XmlElementImpl>>(XmlTextImpl::class.java, XmlAttributeValueImpl::class.java)
  }
}