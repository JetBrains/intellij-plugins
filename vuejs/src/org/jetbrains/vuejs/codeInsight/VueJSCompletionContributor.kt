// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.completion.JSSmartCompletionContributor
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression

/**
 * @author Irina.Chernushina on 7/31/2017.
 */
class VueJSCompletionContributor : JSSmartCompletionContributor() {
  override fun getSmartCompletionVariants(location: JSReferenceExpression): List<LookupElement>? {
    val baseVariants = super.getSmartCompletionVariants(location)

    val scriptWithExport = findScriptWithExport(location.originalElement) ?: return baseVariants
    val defaultExport = scriptWithExport.second
    val obj = defaultExport.stubSafeElement as? JSObjectLiteralExpression
    val vueVariants = ArrayList<LookupElement>()
    VueComponentDetailsProvider.INSTANCE.getAttributes(obj, location.project, false, false)
      // do not suggest directives in injected javascript fragments
      .filter { !it.isDirective() }
      .forEach {
        val builder = if (it.declaration == null) LookupElementBuilder.create(it.name)
        else JSLookupUtilImpl.createLookupElement(it.declaration!!, it.name)
        vueVariants.add(JSCompletionUtil.withJSLookupPriority(builder, JSLookupPriority.MAX_PRIORITY))
      }
    return mergeVariants(baseVariants, vueVariants)
  }
}