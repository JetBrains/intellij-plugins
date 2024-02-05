package org.angular2.entities.source

import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.util.CachedValueProvider
import com.intellij.util.AstLoadingFilter
import com.intellij.util.SmartList
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2DirectiveSelectorImpl
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementWalkingVisitor
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes

object Angular2SourceUtil {

  fun getNgContentSelectors(template: PsiFile?): List<Angular2DirectiveSelector> =
    if (template is PsiFileImpl) {
      val result = SmartList<Angular2DirectiveSelector>()
      val root = template.greenStub
      if (root != null) {
        for (el in root.childrenStubs) {
          if (el.stubType === Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR) {
            result.add((el.psi as Angular2HtmlNgContentSelector).selector)
          }
        }
      }
      else {
        template.accept(object : Angular2HtmlRecursiveElementWalkingVisitor() {
          override fun visitNgContentSelector(ngContentSelector: Angular2HtmlNgContentSelector) {
            result.add(ngContentSelector.selector)
          }
        })
      }
      result
    }
    else
      emptyList()

  fun getComponentSelector(propertyOwner: PsiElement, property: JSProperty?): Angular2DirectiveSelector {
    var value: String? = null
    if (property != null) {
      val initializer: JSLiteralExpression?
      val stub = (property as JSPropertyImpl).stub
      if (stub != null) {
        initializer = stub.childrenStubs.firstNotNullOfOrNull { it.psi as? JSLiteralExpression }
        value = initializer?.significantValue
          ?.let { JSStringUtil.unquoteWithoutUnescapingStringLiteralValue(it) }
      }
      else {
        initializer = property.value as? JSLiteralExpression
        value = initializer?.stringValue
      }
      if (value != null && initializer != null) {
        return Angular2DirectiveSelectorImpl(initializer, StringUtil.unquoteString(value), 1)
      }
      value = AstLoadingFilter.forceAllowTreeLoading<String, RuntimeException>(property.containingFile) {
        Angular2DecoratorUtil.getExpressionStringValue(property.value)
      }
    }
    return Angular2DirectiveSelectorImpl(propertyOwner, value, null)
  }

}