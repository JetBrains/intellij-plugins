package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement

/**
 * @author Irina.Chernushina on 10/16/2017.
 */
interface VueAdvancedComponentDetailsProvider {
  fun getIndexedData(descriptor : JSObjectLiteralExpression) : Collection<JSImplicitElement>
  fun getDescriptorFinder() : (JSImplicitElement) -> JSObjectLiteralExpression?
}