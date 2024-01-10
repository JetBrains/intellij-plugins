// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css.refs

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.css.impl.util.table.CssDescriptorsUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo
import org.angular2.lang.html.parser.Angular2AttributeNameParser.parse
import org.angular2.lang.html.psi.PropertyBindingType

class Angular2CssClassInAttributeReferenceProvider : PsiReferenceProvider() {

  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (element !is XmlAttribute) {
      return PsiReference.EMPTY_ARRAY
    }
    val attributeName = element.name
    val info = parse(attributeName, element.parent)
    if (info !is PropertyBindingInfo || info.bindingType != PropertyBindingType.CLASS) {
      return PsiReference.EMPTY_ARRAY
    }
    val className = info.name
    val offset = attributeName.lastIndexOf(className)
    if (offset < 0) {
      return PsiReference.EMPTY_ARRAY
    }
    val descriptorProvider = CssDescriptorsUtil.findDescriptorProvider(element)!!
    return arrayOf(descriptorProvider.getStyleReference(element, offset, offset + className.length, true))
  }
}
