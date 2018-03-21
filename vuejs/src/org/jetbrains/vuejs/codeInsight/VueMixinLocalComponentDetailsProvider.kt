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

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.vueMixinDescriptorFinder
import org.jetbrains.vuejs.index.LOCAL
import org.jetbrains.vuejs.index.MIXINS
import org.jetbrains.vuejs.index.VueMixinBindingIndex
import org.jetbrains.vuejs.index.resolve

/**
 * @author Irina.Chernushina on 10/12/2017.
 */
class VueMixinLocalComponentDetailsProvider : VueAdvancedComponentDetailsProvider {
  override fun getIndexedData(descriptor: JSObjectLiteralExpression?, project: Project): Collection<JSImplicitElement> {
    if (descriptor == null) return emptyList()
    val mixinsProperty = findProperty(descriptor, MIXINS) ?: return emptyList()
    val elements = resolve(LOCAL, GlobalSearchScope.fileScope(mixinsProperty.containingFile.originalFile), VueMixinBindingIndex.KEY)
                   ?: return emptyList()
    val original = CompletionUtil.getOriginalOrSelf<PsiElement>(mixinsProperty)
    return elements.filter { PsiTreeUtil.isAncestor(original, it.parent, false) }
  }

  override fun getDescriptorFinder(): (JSImplicitElement) -> JSObjectLiteralExpression? {
    return { vueMixinDescriptorFinder(it) }
  }
}