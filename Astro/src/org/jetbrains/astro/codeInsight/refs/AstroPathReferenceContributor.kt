// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.refs

import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor.createPathContainedTagFilter
import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor.createPathReferenceProvider
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.filters.position.FilterPattern

class AstroPathReferenceContributor: PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(creatPathAttributeValuePattern(), PATH_REFERENCE_PROVIDER)
  }
}

private val PATH_REFERENCE_PROVIDER = createPathReferenceProvider()

private fun creatPathAttributeValuePattern(): XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue("href", "to")
  .withSuperParent(2, XmlPatterns.xmlTag().and(FilterPattern(createPathContainedTagFilter(false))))