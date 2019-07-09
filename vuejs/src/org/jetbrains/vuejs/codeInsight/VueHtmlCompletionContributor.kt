// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.XmlPatterns.xmlAttribute
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameCompletionProvider
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeValueCompletionProvider
import org.jetbrains.vuejs.codeInsight.tags.VueTagContentCompletionProvider

class VueHtmlCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_NAME).withParent(xmlAttribute()),
           VueAttributeNameCompletionProvider())
    extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_DATA_CHARACTERS),
           VueTagContentCompletionProvider())
    extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN),
           VueAttributeValueCompletionProvider())
  }
}

