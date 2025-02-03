// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.cssModules

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.codeInsight.MODULE_ATTRIBUTE_NAME
import org.jetbrains.vuejs.index.findTopLevelVueTags
import org.jetbrains.vuejs.model.VueInstanceOwner
import org.jetbrains.vuejs.model.createImplicitPropertySignature
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider

class VueCssModulesInfoProvider : VueContainerInfoProvider {

  override fun getThisTypeProperties(instanceOwner: VueInstanceOwner,
                                     standardProperties: MutableMap<String, JSRecordType.PropertySignature>): Collection<JSRecordType.PropertySignature> {
    val context = instanceOwner.source as? PsiFile ?: instanceOwner.source?.context
    return context?.containingFile
             ?.asSafely<XmlFile>()
             ?.let { findTopLevelVueTags(it, HtmlUtil.STYLE_TAG_NAME) }
             ?.mapNotNull { tag ->
               PsiTreeUtil.getStubChildrenOfTypeAsList(tag, XmlAttribute::class.java)
                 .firstOrNull { it.name == MODULE_ATTRIBUTE_NAME }
             }
             ?.map { attr ->
               createImplicitPropertySignature(
                 attr.value?.takeIf { it.isNotEmpty() } ?: "\$style",
                 CssModuleType(attr.context!!, instanceOwner.source),
                 attr.context!!,
                 false,
                 true)
             }
             ?.toList()
           ?: emptyList()
  }

}