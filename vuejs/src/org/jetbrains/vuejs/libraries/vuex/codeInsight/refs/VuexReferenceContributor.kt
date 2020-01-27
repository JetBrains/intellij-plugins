// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight.refs

import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.COMMIT
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.DISPATCH
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_ACTIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_MUTATIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ROOT_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_CALL_ARGUMENT_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_INDEXED_ACCESS_REF_PROVIDER

class VuexReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(VUEX_CALL_ARGUMENT,
                                        VUEX_CALL_ARGUMENT_REF_PROVIDER)
    registrar.registerReferenceProvider(VUEX_INDEXED_ACCESS,
                                        VUEX_INDEXED_ACCESS_REF_PROVIDER)
    registrar.registerReferenceProvider(VUEX_REFERENCE,
                                        VuexJSRefExprReferenceProvider())
  }

  companion object {
    private val VUEX_CALL_ARGUMENT = JSPatterns.jsLiteralExpression()
      .withAncestor(3, PlatformPatterns.psiElement(JSCallExpression::class.java).withFirstChild(
        JSPatterns.jsReferenceExpression().withReferenceNames(MAP_GETTERS, MAP_STATE, MAP_ACTIONS, MAP_MUTATIONS, COMMIT, DISPATCH)))

    private val VUEX_INDEXED_ACCESS = JSPatterns.jsLiteralExpression()
      .withParent(PlatformPatterns.psiElement(JSIndexedPropertyAccessExpression::class.java).withFirstChild(
        JSPatterns.jsReferenceExpression().withReferenceNames(GETTERS, ROOT_GETTERS, STATE, ROOT_STATE)))

    private val VUEX_REFERENCE = JSPatterns.jsReferenceExpression().withFirstChild(
      JSPatterns.jsReferenceExpression().withReferenceNames(GETTERS, ROOT_GETTERS, STATE, ROOT_STATE)
    )
  }


}
