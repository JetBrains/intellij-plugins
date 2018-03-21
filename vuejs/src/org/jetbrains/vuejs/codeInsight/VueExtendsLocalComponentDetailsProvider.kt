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

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.index.EXTENDS
import org.jetbrains.vuejs.index.LOCAL
import org.jetbrains.vuejs.index.VueExtendsBindingIndex
import org.jetbrains.vuejs.index.resolve

/**
 * @author Irina.Chernushina on 3/13/2018.
 */
class VueExtendsLocalComponentDetailsProvider: VueAdvancedComponentDetailsProvider {
  override fun getIndexedData(descriptor: JSObjectLiteralExpression?, project: Project): Collection<JSImplicitElement> {
    if (descriptor == null) return emptyList()
    val extendsProperty = findProperty(descriptor, EXTENDS) ?: return emptyList()
    val elements = resolve(LOCAL, GlobalSearchScope.fileScope(extendsProperty.containingFile), VueExtendsBindingIndex.KEY) ?: return emptyList()
    return elements.filter { PsiTreeUtil.isAncestor(extendsProperty, it.parent, false) }
  }

  override fun getDescriptorFinder(): (JSImplicitElement) -> JSObjectLiteralExpression? {
    return { VueComponents.vueMixinDescriptorFinder(it) }
  }
}