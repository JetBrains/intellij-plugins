package org.jetbrains.vuejs.language

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.JSInjectionBracesUtil
import com.intellij.lang.javascript.JSInjectionBracesUtil.injectInXmlTextByDelimiters
import com.intellij.lang.javascript.index.JavaScriptIndex
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.impl.source.xml.XmlElementImpl
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.NullableFunction
import org.jetbrains.vuejs.codeInsight.findProperty
import org.jetbrains.vuejs.index.VueOptionsIndex
import org.jetbrains.vuejs.index.resolve
import java.util.*

/**
 * @author Irina.Chernushina on 7/19/2017.
 */
class VueInjector : MultiHostInjector {
  companion object {
    val BRACES_FACTORY: NullableFunction<PsiElement, Pair<String, String>> = JSInjectionBracesUtil.delimitersFactory(
      VueJSLanguage.INSTANCE.displayName, { project, key ->
      if (project == null || key == null) return@delimitersFactory null
      calculateDelimitersFromIndex(project, key) ?: calculateDelimitersFromAssignment(project, key)
    })

    private fun calculateDelimitersFromIndex(project: Project, key: String): Pair<String, PsiElement>? {
      val element = resolve("", GlobalSearchScope.projectScope(project), VueOptionsIndex.KEY) ?: return null
      val obj = PsiTreeUtil.getParentOfType(element, JSObjectLiteralExpression::class.java) ?: return null
      val property = findProperty(obj, "delimiters") ?: return null
      val delimiter = getDelimiterValue(property, key) ?: return null
      return Pair.create(delimiter, element)
    }

    private fun calculateDelimitersFromAssignment(project: Project, key: String): Pair<String, PsiElement>? {
      val delimitersDefinitions = JavaScriptIndex.getInstance(project).getSymbolsByName("delimiters", false)
      return delimitersDefinitions.filter {
        it is JSDefinitionExpression &&
        (it as PsiElement).context != null &&
        it.qualifiedName == "Vue.config.delimiters"
      }.map {
        val delimiter = getDelimiterValue((it as PsiElement).context!!, key)
        if (delimiter != null) return Pair.create(delimiter, it)
        return null
      }.firstOrNull()
    }

    private fun getDelimiterValue(holder : PsiElement, key : String) : String? {
      val list = org.jetbrains.vuejs.codeInsight.getStringLiteralsFromInitializerArray(holder, null)
      if (list.size != 2) return null
      val literal = list[if (JSInjectionBracesUtil.START_SYMBOL_KEY == key) 0 else 1] as? JSLiteralExpression ?: return null
      return StringUtil.unquoteString(literal.significantValue!!)
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