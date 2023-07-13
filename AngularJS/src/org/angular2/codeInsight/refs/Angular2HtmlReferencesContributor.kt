// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs

import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.IMG_TAG
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.NG_SRC_ATTR

internal class Angular2HtmlReferencesContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(NG_SRC_VALUE_PATTERN, Angular2NgSrcReferencesProvider())
  }

  private val NG_SRC_VALUE_PATTERN = XmlPatterns.xmlAttributeValue(NG_SRC_ATTR)
    .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(IMG_TAG))

}
